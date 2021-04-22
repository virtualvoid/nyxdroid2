package sk.virtualvoid.nyxdroid.v2;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

import sk.virtualvoid.core.ITaskQuery;
import sk.virtualvoid.core.Task;
import sk.virtualvoid.core.TaskListener;
import sk.virtualvoid.core.TaskManager;
import sk.virtualvoid.nyxdroid.library.Constants;
import sk.virtualvoid.nyxdroid.v2.data.Last;
import sk.virtualvoid.nyxdroid.v2.data.SuccessResponse;
import sk.virtualvoid.nyxdroid.v2.data.adapters.LastAdapter;
import sk.virtualvoid.nyxdroid.v2.data.dac.WriteupDataAccess;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;

public class LastActivity extends BaseActivity {
    private LastTaskListener listener = new LastTaskListener();
    private Task<ITaskQuery, SuccessResponse<ArrayList<Last>>> tempTask = null;

    private LastAdapter adapter = null;

    @Override
    protected int getContentViewId() {
        return R.layout.generic_listview;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ListView lv = getListView();
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Last writeup = (Last) parent.getItemAtPosition(position);

                Intent intent = new Intent(LastActivity.this, WriteupsActivity.class);
                intent.putExtra(Constants.KEY_ID, writeup.DiscussionId);
                intent.putExtra(Constants.KEY_WU_ID, writeup.Id + 1);
                startActivity(intent);
                overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
            }
        });

        getPullToRefreshAttacher().setRefreshableView(lv, new PullToRefreshAttacher.OnRefreshListener() {
            @Override
            public void onRefreshStarted(View view) {
                refresh();
            }
        });

        refresh();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.last_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.refresh) {
            return refresh();
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean refresh() {
        TaskManager.killIfNeeded(tempTask);

        tempTask = WriteupDataAccess.getLastWriteups(this, listener);
        TaskManager.startTask(tempTask, ITaskQuery.empty);

        getPullToRefreshAttacher().setRefreshing(true);

        return true;
    }

    private class LastTaskListener extends TaskListener<SuccessResponse<ArrayList<Last>>> {

        @Override
        public void done(SuccessResponse<ArrayList<Last>> response) {
            LastActivity activity = (LastActivity)getContext();
            activity.setListAdapter(new LastAdapter(activity, response.getData()));

            getPullToRefreshAttacher().setRefreshComplete();

            displayMailNotificationOnToolbar(response.getContext());
            displayReplyNotificationOnToolbar(response.getContext());
        }
    }
}
