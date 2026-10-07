package sk.virtualvoid.nyxdroid.v2;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipData.Item;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Build;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Toast;

import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import sk.virtualvoid.core.CoreUtility;
import sk.virtualvoid.core.ITaskQuery;
import sk.virtualvoid.core.Task;
import sk.virtualvoid.core.TaskListener;
import sk.virtualvoid.core.TaskManager;
import sk.virtualvoid.core.widgets.CustomViewPager;
import sk.virtualvoid.nyxdroid.library.Constants;
import sk.virtualvoid.nyxdroid.v2.internal.NavigationType;
import sk.virtualvoid.nyxdroid.v2.internal.GalleryImageSaver;
import uk.co.senab.photoview.PhotoView;


/**
 * 
 * @author Juraj
 * 
 */
public class GalleryActivity extends BaseActivity implements View.OnLongClickListener {
	private static final int REQUEST_SAVE_IMAGE_PERMISSION = 65;
	private static final String STATE_PENDING_SAVE_URL = "pending_save_url";
	private Bundle firstItem;
	private Bundle currentItem;
	private boolean displayVotingThumbs;
	private boolean savingImage;
	private String pendingSaveUrl;

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
		if (savedInstanceState != null) {
			pendingSaveUrl = savedInstanceState.getString(STATE_PENDING_SAVE_URL);
		}

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		displayVotingThumbs = prefs.getBoolean("display_voting_thumbs", true);

		Intent launchIntent = getIntent();
		Bundle launchExtras = launchIntent.getExtras();

		Parcelable[] parcelableArray = launchExtras.getParcelableArray(Constants.KEY_BUNDLE_ARRAY);

		final Bundle[] infoArray = new Bundle[parcelableArray.length];
		for (int i = 0; i < parcelableArray.length; i++) {
			infoArray[i] = (Bundle) parcelableArray[i];
		}

		int position = 0;
		if (launchExtras.containsKey(Constants.KEY_WU_ID)) {
			long writeupId = launchExtras.getLong(Constants.KEY_WU_ID);
			for (int index = 0; index < infoArray.length; index++) {
				Bundle test = infoArray[index];
				if (test.containsKey(Constants.KEY_WU_ID) && test.getLong(Constants.KEY_WU_ID) == writeupId) {
					position += index;
					break;
				}
			}
		}

		if (launchExtras.containsKey(Constants.KEY_URL)) {
			String url = launchExtras.getString(Constants.KEY_URL);

			for (int index = position, counter = 0; index < infoArray.length; index++, counter++) {
				Bundle test = infoArray[index];
				if (test.containsKey(Constants.KEY_URL) && test.getString(Constants.KEY_URL).equalsIgnoreCase(url)) {
					if (index != position) {
						position += counter;
					}
					break;
				}
			}
		}

		CustomViewPager viewPager = (CustomViewPager) findViewById(R.id.gallery_vp);
		viewPager.setAdapter(new ImagePagerAdapter(infoArray, this));
		viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageSelected(int page) {
				buildTitle(infoArray[page]);
			}

			@Override
			public void onPageScrolled(int page, float positionOffset, int positionOffsetPixels) {
			}

			@Override
			public void onPageScrollStateChanged(int state) {
			}
		});

		buildTitle(firstItem = infoArray[0]);
		viewPager.setCurrentItem(position);
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
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(R.id.ge_download).setEnabled(!savingImage && pendingSaveUrl == null);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.ge_download:
				return downloadImage();
			case R.id.ge_openbrowser:
				return viewInBrowser();
			case R.id.ge_begin:
				return toBegin();
			case R.id.ge_current:
				return toCurrent();
		}
		return super.onOptionsItemSelected(item);
	}

	private boolean downloadImage() {
		if (savingImage || pendingSaveUrl != null || currentItem == null) {
			return true;
		}
		String url = currentItem.getString(Constants.KEY_URL);
		if (url == null || url.isEmpty()) {
			Toast.makeText(this, R.string.gallery_download_failed, Toast.LENGTH_LONG).show();
			return true;
		}
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q
				&& ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
			pendingSaveUrl = url;
			invalidateOptionsMenu();
			ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE }, REQUEST_SAVE_IMAGE_PERMISSION);
		} else {
			startImageDownload(url);
		}
		return true;
	}

	private void startImageDownload(String url) {
		savingImage = true;
		invalidateOptionsMenu();
		Task<ITaskQuery, String> task = new Task<>(this, new GalleryImageSaver(getApplicationContext(), url), new TaskListener<String>() {
			@Override
			public void done(String path) {
				savingImage = false;
				invalidateOptionsMenu();
				Toast.makeText(GalleryActivity.this, getString(R.string.gallery_download_saved, path), Toast.LENGTH_LONG).show();
			}

			@Override
			public void handleError(Throwable error) {
				savingImage = false;
				invalidateOptionsMenu();
				Log.e(Constants.TAG, "Unable to save gallery image", error);
				Toast.makeText(GalleryActivity.this, R.string.gallery_download_failed, Toast.LENGTH_LONG).show();
			}
		});
		TaskManager.startTask(task, null);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == REQUEST_SAVE_IMAGE_PERMISSION) {
			String url = pendingSaveUrl;
			pendingSaveUrl = null;
			if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && url != null) {
				startImageDownload(url);
			} else {
				invalidateOptionsMenu();
				Toast.makeText(this, R.string.gallery_download_permission_denied, Toast.LENGTH_LONG).show();
			}
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(STATE_PENDING_SAVE_URL, pendingSaveUrl);
		savingImage = false;
		invalidateOptionsMenu();
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
		private Bundle[] model;
		private Activity context;
		//private Pattern pattern;
		//private Matcher matcher;
		private Drawable placeholder;
		//private ImageLoader imageLoader;

		public ImagePagerAdapter(Bundle[] model, Activity context) {
			this.model = model;
			this.context = context;

			//this.pattern = Pattern.compile("([^\\s]+(\\.(?i)(gif))$)", Pattern.CASE_INSENSITIVE);
			this.placeholder = context.getResources().getDrawable(R.drawable.placeholder);
			//this.imageLoader = ImageLoader.getInstance();
		}

		@Override
		public int getCount() {
			return model.length;
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			View view = null;
			final String url = model[position].getString(Constants.KEY_URL);

//			matcher = pattern.matcher(url);
//			if (matcher.matches()) {
//				view = createMovieViewer(url);
//			} else {
				view = createPhotoViewer(url);
//			}

			view.setOnLongClickListener(GalleryActivity.this);

			container.addView(view);

			return view;
		}

		private PhotoView createPhotoViewer(final String url) {
			final PhotoView photoView = new PhotoView(context);
			//photoView.setImageDrawable(placeholder);

			Glide.with(context)
					.load(url)
					.diskCacheStrategy(DiskCacheStrategy.SOURCE)
					.fitCenter()
					.dontAnimate()
					.placeholder(placeholder)
					.into(photoView);

//			imageLoader.displayImage(url, photoView, new ImageLoadingListener() {
//				@Override
//				public void onLoadingStarted(String url, View view) {
//					setProgressBarIndeterminateVisibility(true);
//				}
//
//				@Override
//				public void onLoadingFailed(String url, View view, FailReason failReason) {
//					setProgressBarIndeterminateVisibility(false);
//				}
//
//				@Override
//				public void onLoadingComplete(String url, View view, Bitmap bitmap) {
//					setProgressBarIndeterminateVisibility(false);
//				}
//
//				@Override
//				public void onLoadingCancelled(String url, View view) {
//					setProgressBarIndeterminateVisibility(false);
//				}
//			});

			return photoView;
		}

//		@SuppressLint("SetJavaScriptEnabled")
//		private WebView createMovieViewer(final String url) {
//			final WebView view = new WebView(context);
//
//			view.setBackgroundColor(0x00000000);
//			view.setLayerType(WebView.LAYER_TYPE_HARDWARE, null);
//			view.getSettings().setJavaScriptEnabled(true);
//			view.getSettings().setBuiltInZoomControls(true);
//
//			view.setWebChromeClient(new WebChromeClient() {
//				@Override
//				public void onProgressChanged(WebView view, int newProgress) {
//					super.onProgressChanged(view, newProgress);
//					view.invalidate();
//					setProgressBarIndeterminateVisibility(false);
//				}
//			});
//
//			view.loadUrl(url);
//
//			setProgressBarIndeterminateVisibility(true);
//
//			return view;
//		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			View view = (View) object;

//			if (view instanceof WebView) {
//				((WebView)view).freeMemory();
//			}

			if (view instanceof PhotoView) {
				// what now ?
			}

			container.removeView(view);
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == object;
		}
	}
}
