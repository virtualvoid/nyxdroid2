package sk.virtualvoid.nyxdroid.v2;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipData.Item;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.widget.TextView;

import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;

import sk.virtualvoid.core.CoreUtility;
import sk.virtualvoid.core.Task;
import sk.virtualvoid.core.TaskListener;
import sk.virtualvoid.core.TaskManager;
import sk.virtualvoid.core.widgets.CustomViewPager;
import sk.virtualvoid.nyxdroid.library.Constants;
import sk.virtualvoid.nyxdroid.library.Constants.WriteupDirection;
import sk.virtualvoid.nyxdroid.v2.data.SuccessResponse;
import sk.virtualvoid.nyxdroid.v2.data.WriteupResponse;
import sk.virtualvoid.nyxdroid.v2.data.dac.WriteupDataAccess;
import sk.virtualvoid.nyxdroid.v2.data.query.WriteupQuery;
import sk.virtualvoid.nyxdroid.v2.internal.GalleryState;
import sk.virtualvoid.nyxdroid.v2.internal.NavigationType;
import uk.co.senab.photoview.PhotoView;


/**
 * 
 * @author Juraj
 * 
 */
public class GalleryActivity extends BaseActivity implements View.OnLongClickListener {
	private Bundle firstItem;
	private Bundle currentItem;
	private boolean displayVotingThumbs;
	private GalleryState galleryState;
	private CustomViewPager viewPager;
	private ImagePagerAdapter imageAdapter;
	private Task<WriteupQuery, SuccessResponse<WriteupResponse>> pageTask;
	private boolean galleryActive;

	@Override
	protected boolean useSlidingMenu() {
		return false;
	}

	@Override
	protected int getContentViewId() {
		return R.layout.gallery;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		displayVotingThumbs = prefs.getBoolean("display_voting_thumbs", true);

		galleryState = new ViewModelProvider(this).get(GalleryState.class);
		if (!galleryState.initialized) {
			Bundle launchExtras = getIntent().getExtras();
			Parcelable[] parcelableArray = launchExtras.getParcelableArray(Constants.KEY_BUNDLE_ARRAY);
			if (parcelableArray != null) {
				for (Parcelable item : parcelableArray) {
					galleryState.images.add((Bundle) item);
				}
			}
			galleryState.discussionId = launchExtras.getLong(Constants.KEY_ID);
			galleryState.lastWriteupId = launchExtras.getLong(Constants.KEY_GALLERY_LAST_POST_ID);
			galleryState.filterUser = launchExtras.getString(Constants.KEY_GALLERY_FILTER_USER);
			galleryState.filterContents = launchExtras.getString(Constants.KEY_GALLERY_FILTER_CONTENTS);
			galleryState.hasMore = galleryState.discussionId > 0 && galleryState.lastWriteupId > 0;
			galleryState.position = findStartPosition(launchExtras);
			galleryState.initialized = true;
		}
		if (galleryState.images.isEmpty()) {
			finish();
			return;
		}
		viewPager = (CustomViewPager) findViewById(R.id.gallery_vp);
		imageAdapter = new ImagePagerAdapter(galleryState.images, this);
		viewPager.setAdapter(imageAdapter);
		viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageSelected(int page) {
				galleryState.position = page;
				buildTitle(galleryState.images.get(page));
				loadMoreIfNeeded();
			}

			@Override
			public void onPageScrolled(int page, float positionOffset, int positionOffsetPixels) {
			}

			@Override
			public void onPageScrollStateChanged(int state) {
			}
		});

		firstItem = galleryState.images.get(0);
		buildTitle(galleryState.images.get(galleryState.position));
		viewPager.setCurrentItem(galleryState.position);
		findViewById(R.id.gallery_page_retry).setOnClickListener(view -> {
			galleryState.loadFailed = false;
			loadMoreIfNeeded();
		});
		updatePageStatus();
	}

	private int findStartPosition(Bundle extras) {
		int position = 0;
		if (extras.containsKey(Constants.KEY_WU_ID)) {
			for (int index = 0; index < galleryState.images.size(); index++) {
				Bundle image = galleryState.images.get(index);
				if (image.containsKey(Constants.KEY_WU_ID) && image.getLong(Constants.KEY_WU_ID) == extras.getLong(Constants.KEY_WU_ID)) {
					position = index;
					break;
				}
			}
		}
		String url = extras.getString(Constants.KEY_URL);
		if (url != null) {
			for (int index = position; index < galleryState.images.size(); index++) {
				if (url.equalsIgnoreCase(galleryState.images.get(index).getString(Constants.KEY_URL))) {
					return index;
				}
			}
		}
		return position;
	}

	@Override
	protected void onResume() {
		super.onResume();
		galleryActive = true;
		if (viewPager != null) {
			loadMoreIfNeeded();
		}
	}

	@Override
	protected void onPause() {
		galleryActive = false;
		TaskManager.killIfNeeded(pageTask);
		pageTask = null;
		super.onPause();
	}

	private void loadMoreIfNeeded() {
		if (!galleryActive || pageTask != null || !galleryState.shouldLoadMore()) {
			updatePageStatus();
			return;
		}
		WriteupQuery query = new WriteupQuery();
		query.Id = galleryState.discussionId;
		query.LastId = galleryState.lastWriteupId;
		query.Direction = WriteupDirection.WRITEUP_DIRECTION_OLDER;
		query.FilterUser = galleryState.filterUser;
		query.FilterContents = galleryState.filterContents;
		pageTask = WriteupDataAccess.getWriteups(this, new TaskListener<SuccessResponse<WriteupResponse>>() {
			@Override
			public void done(SuccessResponse<WriteupResponse> response) {
				try {
					galleryState.appendOlderPosts(response.getData().Writeups);
				} catch (RuntimeException error) {
					handleError(error);
					return;
				}
				pageTask = null;
				imageAdapter.notifyDataSetChanged();
				updatePageStatus();
				// Keep searching through text-only pages while the user is still near the end.
				viewPager.post(() -> loadMoreIfNeeded());
			}

			@Override
			public void handleError(Throwable error) {
				pageTask = null;
				galleryState.loadFailed = true;
				Log.e(Constants.TAG, "Unable to load older gallery posts", error);
				updatePageStatus();
			}
		});
		updatePageStatus();
		TaskManager.startTask(pageTask, query);
	}

	private void updatePageStatus() {
		boolean loadingPage = pageTask != null;
		boolean atEnd = galleryState.position == galleryState.images.size() - 1;
		boolean visible = atEnd && galleryState.discussionId > 0
				&& (loadingPage || galleryState.loadFailed || !galleryState.hasMore);
		findViewById(R.id.gallery_page_status).setVisibility(visible ? View.VISIBLE : View.GONE);
		findViewById(R.id.gallery_page_progress).setVisibility(loadingPage ? View.VISIBLE : View.GONE);
		findViewById(R.id.gallery_page_retry).setVisibility(galleryState.loadFailed ? View.VISIBLE : View.GONE);
		TextView label = (TextView) findViewById(R.id.gallery_page_message);
		label.setText(loadingPage ? R.string.gallery_loading_more
				: galleryState.loadFailed ? R.string.gallery_load_more_failed : R.string.gallery_no_more_images);
	}

	@Override
	public boolean onLongClick(View v) {
		if (currentItem != null) {
			String url = currentItem.getString(Constants.KEY_URL);

			Item item = new Item(url);
			ClipData data = new ClipData(new ClipDescription("Link Copied", new String[] { ClipDescription.MIMETYPE_TEXT_PLAIN }), item);
			ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
			clipboardManager.setPrimaryClip(data);

			Toast.makeText(GalleryActivity.this, R.string.link_copied, Toast.LENGTH_SHORT).show();

			Log.d(Constants.TAG, url);
		}
		return true;
	}

	private void buildTitle(Bundle item) {
		currentItem = item;

		String nick = currentItem.getString(Constants.KEY_NICK);
		int rating = currentItem.getInt(Constants.KEY_RATING);
		boolean unread = currentItem.getBoolean(Constants.KEY_UNREAD);

		String title = String.format("%s%s%s", (unread ? "N " : ""), (rating != 0 && displayVotingThumbs) ? String.format("(%d) ", rating) : "", nick);
		setTitle(title);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.gallery_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.ge_openbrowser:
				return viewInBrowser();
			case R.id.ge_begin:
				return toBegin();
			case R.id.ge_current:
				return toCurrent();
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		pageTask = null;
	}

	@Override
	protected void onDestroy() {
		TaskManager.cancelTasks(this);
		super.onDestroy();
	}

	private boolean viewInBrowser() {
		if (!CoreUtility.launchBrowser(this, currentItem.getString(Constants.KEY_URL))) {
			Toast.makeText(this, R.string.cant_open_it, Toast.LENGTH_SHORT).show();
		}
		return true;
	}

	private boolean toBegin() {
		Intent data = new Intent();
		data.putExtra(Constants.KEY_WU_ID, firstItem.getLong(Constants.KEY_WU_ID));

		setResult(Constants.REQUEST_RESPONSE_OK, data);
		finish();

		return true;
	}

	private boolean toCurrent() {
		Intent data = new Intent();
		data.putExtra(Constants.KEY_WU_ID, currentItem.getLong(Constants.KEY_WU_ID));

		setResult(Constants.REQUEST_RESPONSE_OK, data);
		finish();

		return true;
	}

	@Override
	public boolean onNavigationRequested(NavigationType navigationType, String url, Long discussionId, Long writeupId, Long mailId) {
		/* Not needed here */
		return false;
	}

	private class ImagePagerAdapter extends PagerAdapter {
		private final ArrayList<Bundle> model;
		private final Activity context;
		private final Drawable placeholder;

		public ImagePagerAdapter(ArrayList<Bundle> model, Activity context) {
			this.model = model;
			this.context = context;

			this.placeholder = context.getResources().getDrawable(R.drawable.placeholder);
		}

		@Override
		public int getCount() {
			return model.size();
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			final String url = model.get(position).getString(Constants.KEY_URL);
			PhotoView view = createPhotoViewer(url);
			view.setOnLongClickListener(GalleryActivity.this);

			container.addView(view);

			return view;
		}

		private PhotoView createPhotoViewer(final String url) {
			final PhotoView photoView = new PhotoView(context);

			Glide.with(context)
					.load(url)
					.diskCacheStrategy(DiskCacheStrategy.SOURCE)
					.fitCenter()
					.dontAnimate()
					.placeholder(placeholder)
					.into(photoView);

			return photoView;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			View view = (View) object;

			if (view instanceof PhotoView) {
				Glide.clear((PhotoView) view);
			}

			container.removeView(view);
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == object;
		}
	}
}
