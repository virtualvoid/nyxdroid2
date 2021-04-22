package sk.virtualvoid.nyxdroid.v2;

import java.util.ArrayList;

import sk.virtualvoid.core.Task;
import sk.virtualvoid.core.TaskListener;
import sk.virtualvoid.core.TaskManager;
import sk.virtualvoid.nyxdroid.library.Constants;
import sk.virtualvoid.nyxdroid.library.Constants.WriteupDirection;
import sk.virtualvoid.nyxdroid.v2.data.SuccessResponse;
import sk.virtualvoid.nyxdroid.v2.data.Writeup;
import sk.virtualvoid.nyxdroid.v2.data.WriteupList;
import sk.virtualvoid.nyxdroid.v2.data.WriteupResponse;
import sk.virtualvoid.nyxdroid.v2.data.adapters.GalleryGridAdapter;
import sk.virtualvoid.nyxdroid.v2.data.dac.WriteupDataAccess;
import sk.virtualvoid.nyxdroid.v2.data.query.WriteupQuery;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;

/**
 * 
 * @author Juraj
 * 
 */
public class GalleryGridActivity extends BaseActivity {
	private WriteupTaskListener writeupTaskListener = new WriteupTaskListener();
	private Task<WriteupQuery, SuccessResponse<WriteupResponse>> tempDataTask = null;

	private long discussionId;
	private String discussionName;
	private Long lastWriteupId;

	private int scrollFirstVisibleItem;
	private int scrollVisibleItemCount;
	private int scrollTotalItemCount;

	private GridView gridView;
	private GalleryGridAdapter adapter;
	private WriteupList writeupList;

	@Override
	protected boolean useSlidingMenu() {
		return false;
	}

	@Override
	protected int getContentViewId() {
		return R.layout.gallerygrid;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent launchIntent = getIntent();
		Bundle launchExtras = launchIntent.getExtras();
		discussionId = launchExtras.getLong(Constants.KEY_ID);

		adapter = new GalleryGridAdapter(this);
		writeupList = new WriteupList();

		gridView = (GridView) findViewById(R.id.gallerygrid_view);
		
		gridView.setOnScrollListener(new AbsListView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if (scrollState == SCROLL_STATE_IDLE && scrollFirstVisibleItem + scrollVisibleItemCount >= scrollTotalItemCount - 1) {
					load();
				}
			}
			
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				scrollFirstVisibleItem = firstVisibleItem;
				scrollVisibleItemCount = visibleItemCount;
				scrollTotalItemCount = totalItemCount;
			}
		});
		
		gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View row, int position, long id) {
				Bundle currentBundle = (Bundle) adapter.getItem(position);
				long currentWriteupId = currentBundle.getLong(Constants.KEY_WU_ID);
				String currentUrl = currentBundle.getString(Constants.KEY_URL);
				
				ArrayList<Bundle> bundles = adapter.getItems();
				Bundle[] bundlesArray = bundles.toArray(new Bundle[bundles.size()]);
				
				Intent intent = new Intent(GalleryGridActivity.this, GalleryActivity.class);
				intent.putExtra(Constants.KEY_WU_ID, currentWriteupId);
				intent.putExtra(Constants.KEY_URL, currentUrl);
				intent.putExtra(Constants.KEY_BUNDLE_ARRAY, bundlesArray);

				startActivityForResult(intent, Constants.REQUEST_GALLERY);
				overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
			}
		});
		
		gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View row, int position, long id) {
				Bundle currentBundle = (Bundle) adapter.getItem(position);
				long currentWriteupId = currentBundle.getLong(Constants.KEY_WU_ID);
				Writeup currentWriteup = writeupList.getItem(currentWriteupId);
				if (currentWriteup == null) {
					return false;
				}

				ArrayList<Writeup> wuList = new ArrayList<Writeup>();
				wuList.add(currentWriteup);
				
				Intent intent = new Intent(GalleryGridActivity.this, WriteupComposeActivity.class);
				intent.putExtra(Constants.REQUEST_WRITEUP_DISCUSSION_ID, discussionId);
				intent.putExtra(Constants.REQUEST_WRITEUP_DISCUSSION_NAME, discussionName);
				intent.putExtra(Constants.REQUEST_WRITEUP, wuList);
				
				startActivityForResult(intent, Constants.REQUEST);
				overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
				
				return true;
			}
		});
		
		gridView.setAdapter(adapter);

		load();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == Constants.REQUEST_GALLERY && resultCode == Constants.REQUEST_RESPONSE_OK) {
			galleryResultHandler(data.getLongExtra(Constants.KEY_WU_ID, 0));
		}

		super.onActivityResult(requestCode, resultCode, data);
	}
	
	private void galleryResultHandler(long wuId) {
		if (wuId == 0 || adapter == null) {
			return;
		}

		int position = adapter.getItemPosition(wuId);
		gridView.setSelection(position);
	}

	private void load() {
		WriteupQuery query = new WriteupQuery();

		query.NavigatingOutside = false;
		query.Id = discussionId;
		query.LastId = lastWriteupId;
		query.Direction = lastWriteupId == null ? WriteupDirection.WRITEUP_DIRECTION_NEWEST : WriteupDirection.WRITEUP_DIRECTION_OLDER;
		query.FilterContents = "img src";

		TaskManager.killIfNeeded(tempDataTask);

		tempDataTask = WriteupDataAccess.getWriteups(this, writeupTaskListener);
		TaskManager.startTask(tempDataTask, query);

		getPullToRefreshAttacher().setRefreshing(true);
	}

	private class WriteupTaskListener extends TaskListener<SuccessResponse<WriteupResponse>> {
		@Override
		public void done(SuccessResponse<WriteupResponse> response) {
			WriteupResponse output = response.getData();

			WriteupQuery query = (WriteupQuery) getTag();
			if (query.Direction == WriteupDirection.WRITEUP_DIRECTION_NEWEST) {
				adapter.clearItems();
				writeupList.clear();
			}

			setTitle(discussionName = output.Name);
			
			ArrayList<Writeup> writeups = (ArrayList<Writeup>) writeupList.filter(output.Writeups);
			if (writeups.size() > 0) {
				Writeup lastWriteup = writeups.get(writeups.size() - 1);
				lastWriteupId = lastWriteup.Id;
			}
			
			ArrayList<Bundle> bundles = new ArrayList<Bundle>();
			for (Writeup writeup : writeups) {
				bundles.addAll(writeup.allImages());
			}

			if (bundles.size() > 0) {
				adapter.addItems(bundles);
				adapter.notifyDataSetChanged();
			}

			getPullToRefreshAttacher().setRefreshComplete();
		}
	}
}
