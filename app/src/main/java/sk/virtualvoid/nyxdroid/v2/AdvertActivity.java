package sk.virtualvoid.nyxdroid.v2;

import sk.virtualvoid.core.ImageDownloader;
import sk.virtualvoid.core.ImageGetterAsync;
import sk.virtualvoid.core.Task;
import sk.virtualvoid.core.TaskListener;
import sk.virtualvoid.core.TaskManager;
import sk.virtualvoid.nyxdroid.library.Constants;
import sk.virtualvoid.nyxdroid.v2.data.Advert;
import sk.virtualvoid.nyxdroid.v2.data.dac.AdvertDataAccess;
import sk.virtualvoid.nyxdroid.v2.data.query.AdvertQuery;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

/**
 * 
 * @author Juraj
 * 
 */
public class AdvertActivity extends BaseActivity implements ActionBar.TabListener {

	public static final int TAB_DETAILS = 0x1;
	public static final int TAB_COMMENTS = 0x2;
	public static final int TAB_PHOTOS = 0x3;

	private Task<AdvertQuery, Advert> tempTask;
	private GetAdvertDetailsTaskListener listener = new GetAdvertDetailsTaskListener();
	private long id;

	private ImageDownloader imageDownloader;
	private ImageGetterAsync imageGetterAsync;
	private int linkColor;

	private Fragment currentFragment;
	private AdvertDetailFragment detailFragment;
	private AdvertCommentsFragment commentsFragment;
	private AdvertPhotosFragment photosFragment;

	@Override
	protected boolean useSlidingMenu() {
		return false;
	}

	@Override
	protected int getContentViewId() {
		return R.layout.empty_view;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		linkColor = appearance.getLinkColor();

		Drawable emptyAvatar = getResources().getDrawable(R.drawable.empty_avatar);
		imageDownloader = new ImageDownloader(this, emptyAvatar);

		imageGetterAsync = new ImageGetterAsync(this);

		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		FragmentManager fm = getFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();

		commentsFragment = (AdvertCommentsFragment) fm.findFragmentByTag(AdvertCommentsFragment.TAG);
		if (commentsFragment == null) {
			commentsFragment = new AdvertCommentsFragment();
		}
		ft.add(R.id.empty_view_ll, commentsFragment, AdvertCommentsFragment.TAG);
		ft.hide(commentsFragment);

		photosFragment = (AdvertPhotosFragment)fm.findFragmentByTag(AdvertPhotosFragment.TAG);
		if (photosFragment == null) {
			photosFragment = new AdvertPhotosFragment();
		}
		ft.add(R.id.empty_view_ll, photosFragment, AdvertPhotosFragment.TAG);
		ft.hide(photosFragment);
		
		currentFragment = detailFragment = (AdvertDetailFragment) fm.findFragmentByTag(AdvertDetailFragment.TAG);
		if (currentFragment == null) {
			currentFragment = detailFragment = new AdvertDetailFragment();
		}
		ft.add(R.id.empty_view_ll, detailFragment, AdvertDetailFragment.TAG);
		ft.show(detailFragment);

		ft.commit();

		actionBar.addTab(actionBar.newTab().setText(R.string.advert_details).setTag(TAB_DETAILS).setTabListener(this));
		actionBar.addTab(actionBar.newTab().setText(R.string.advert_photos).setTag(TAB_PHOTOS).setTabListener(this));
		actionBar.addTab(actionBar.newTab().setText(R.string.advert_comments).setTag(TAB_COMMENTS).setTabListener(this));

		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		id = extras.getLong(Constants.KEY_ID);

		refresh();
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		if (currentFragment != null) {
			ft.hide(currentFragment);
		}

		if (TAB_DETAILS == (Integer) tab.getTag()) {
			currentFragment = detailFragment;
		}

		if (TAB_PHOTOS == (Integer) tab.getTag()) {
			currentFragment = photosFragment;
		}

		if (TAB_COMMENTS == (Integer) tab.getTag()) {
			currentFragment = commentsFragment;
		}

		ft.show(currentFragment);
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.advert_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			finish();
		}

		if (item.getItemId() == R.id.refresh) {
			refresh();
		}

		return true;
	}

	@Override
	public void onBackPressed() {
		finish();
	}

	private void refresh() {
		AdvertQuery query = new AdvertQuery();
		query.Id = id;

		TaskManager.killIfNeeded(tempTask);

		tempTask = AdvertDataAccess.getAdvertDetails(AdvertActivity.this, listener);
		TaskManager.startTask(tempTask, query);
	}

	private void setData(Advert advert) {
		setTitle(advert.Title);
	}

	public ImageDownloader getImageDownloader() {
		return imageDownloader;
	}

	public int getLinkColor() {
		return linkColor;
	}

	public ImageGetterAsync getImageGetter() {
		return imageGetterAsync;
	}

	/**
	 * 
	 * @author Juraj
	 * 
	 */
	public interface AdvertFragmentHandler {
		void setData(Advert data);
	}

	/**
	 * 
	 * @author Juraj
	 * 
	 */
	private class GetAdvertDetailsTaskListener extends TaskListener<Advert> {
		@Override
		public void done(Advert advert) {
			if (advert != null) {
				setData(advert);

				detailFragment.setData(advert);
				photosFragment.setData(advert);
				commentsFragment.setData(advert);
			}
		}
	}
}
