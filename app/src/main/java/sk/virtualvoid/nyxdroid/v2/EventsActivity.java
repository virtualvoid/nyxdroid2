package sk.virtualvoid.nyxdroid.v2;

import java.util.ArrayList;

import sk.virtualvoid.core.Task;
import sk.virtualvoid.core.TaskListener;
import sk.virtualvoid.core.TaskManager;
import sk.virtualvoid.nyxdroid.library.Constants;
import sk.virtualvoid.nyxdroid.v2.data.Event;
import sk.virtualvoid.nyxdroid.v2.data.adapters.EventAdapter;
import sk.virtualvoid.nyxdroid.v2.data.dac.EventDataAccess;
import sk.virtualvoid.nyxdroid.v2.data.query.EventQuery;
import sk.virtualvoid.nyxdroid.v2.internal.NavigationType;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

/**
 * 
 * @author Juraj
 *
 */
public class EventsActivity extends BaseActivity {
	private String currentEventsType = Constants.EVENT_TYPE_WATCHED;
	private GetEventsTaskListener getEventsTaskListener = new GetEventsTaskListener();
	
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
				detail(id);
			}
		});
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.events_menu, menu);
		
		final String[] eventTypesValues = new String[] { Constants.EVENT_TYPE_WATCHED };
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, 	R.array.event_types_names, android.R.layout.simple_list_item_1);
		
		MenuItem eventsTypeMenu = menu.findItem(R.id.events_type);

		final Spinner eventsType = (Spinner) eventsTypeMenu.getActionView();
		eventsType.setAdapter(adapter);
		eventsType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				refresh(currentEventsType = eventTypesValues[position]);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
		
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.refresh) {
			refresh(currentEventsType);
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void refresh(String type) {
		EventQuery query = new EventQuery();
		query.Type = type;
		
		Task<EventQuery, ArrayList<Event>> task = EventDataAccess.getEvents(EventsActivity.this, getEventsTaskListener);
		TaskManager.startTask(task, query);
	}
	
	private void detail(long id) {
		Intent intent = new Intent(EventsActivity.this, EventActivity.class);
		intent.putExtra(Constants.KEY_ID, id);
		startActivity(intent);
		overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
	}
	
	@Override
	public boolean onNavigationRequested(NavigationType navigationType, String url, Long discussionId, Long writeupId) {
		/* Not needed here */
		return false;
	}
	
	/**
	 * 
	 * @author Juraj
	 *
	 */
	private class GetEventsTaskListener extends TaskListener<ArrayList<Event>> {
		@Override
		public void done(ArrayList<Event> output) {
			setListAdapter(new EventAdapter((Activity) getContext(), output));
		}
	}
}
