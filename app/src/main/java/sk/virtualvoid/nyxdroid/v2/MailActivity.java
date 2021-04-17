package sk.virtualvoid.nyxdroid.v2;

import java.util.ArrayList;

import sk.virtualvoid.core.CoreUtility;
import sk.virtualvoid.core.ITaskQuery;
import sk.virtualvoid.core.Task;
import sk.virtualvoid.core.TaskListener;
import sk.virtualvoid.core.TaskManager;
import sk.virtualvoid.core.Tuple;
import sk.virtualvoid.core.widgets.ISecondBaseMenu;
import sk.virtualvoid.nyxdroid.library.Constants;
import sk.virtualvoid.nyxdroid.v2.am.MailActionMode;
import sk.virtualvoid.nyxdroid.v2.data.Conversation;
import sk.virtualvoid.nyxdroid.v2.data.Mail;
import sk.virtualvoid.nyxdroid.v2.data.NullResponse;
import sk.virtualvoid.nyxdroid.v2.data.adapters.ConversationAdapter;
import sk.virtualvoid.nyxdroid.v2.data.adapters.MailAdapter;
import sk.virtualvoid.nyxdroid.v2.data.dac.MailDataAccess;
import sk.virtualvoid.nyxdroid.v2.data.query.MailQuery;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipData.Item;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import androidx.appcompat.widget.SearchView;


/**
 * 
 * @author juraj
 * 
 */
public class MailActivity extends BaseActivity implements ISecondBaseMenu {
	private MailsTaskListener mailsTaskListener = new MailsTaskListener();
	private ConversationTaskListener conversationTaskListener = new ConversationTaskListener();
	private NoopTaskListener noopTaskListener = new NoopTaskListener();
	private MailAdapter adapter;
	private ActionMode mailActionModeHandle;
	private SearchView searchView;

	private ProgressBar conversationProgress;
	private ConversationAdapter conversationAdapter;

	private String lastFilterText;
	private String lastFilterUser;
	
	private boolean refreshReceiverEnabled;
	
	@Override
	protected int getContentViewId() {
		return R.layout.generic_listview;
	}

	@Override
	public View getSecondBaseMenu() {
		View view = getLayoutInflater().inflate(R.layout.mail_menu, null, false);

		conversationProgress = (ProgressBar) view.findViewById(R.id.mail_menu_progress);
		conversationProgress.setIndeterminate(true);

		ListView listView = (ListView) view.findViewById(R.id.mail_secondmenu);
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View row, int position, long id) {
				Conversation conversation = (Conversation) parent.getItemAtPosition(position);
				if (conversation != null) {
					toggleMenu();
					conversationProgress.setVisibility(View.VISIBLE);
					load(true, null, conversation.Nick, null);
				}
			}
		});

		listView.setAdapter(conversationAdapter);

		return view;
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		outState.putString("lastFilterText", lastFilterText);
		outState.putString("lastFilterUser", lastFilterUser);
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		
		lastFilterText = savedInstanceState.getString("lastFilterText", "");
		lastFilterUser = savedInstanceState.getString("lastFilterUser", "");
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		conversationAdapter = new ConversationAdapter(this);
		initializeSecondMenu();

		ListView lv = getListView();
		lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View row, int position, long id) {
				Mail mail = (Mail) adapter.getItem(position);
				compose(mail);
			}
		});

		lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View row, int position, long id) {
				final Mail mail = (Mail) adapter.getItem(position);

				MailActionMode mode = new MailActionMode(MailActivity.this, new MailActionMode.Listener() {
					@Override
					public void onReply() {
						mailActionModeHandle.finish();

						compose(mail);
					}

					@Override
					public void onReminder() {
						mailActionModeHandle.finish();

						MailQuery query = new MailQuery();
						query.Id = mail.Id;
						query.NewState = !mail.IsReminded;

						Task<MailQuery, NullResponse> task = MailDataAccess.reminderMail(MailActivity.this, noopTaskListener);
						TaskManager.startTask(task, query);
					}

					@Override
					public void onDelete() {
						mailActionModeHandle.finish();

						MailQuery query = new MailQuery();
						query.Id = mail.Id;

						adapter.removeItem(mail);
						adapter.notifyDataSetChanged();

						Task<MailQuery, NullResponse> task = MailDataAccess.deleteMail(MailActivity.this, noopTaskListener);
						TaskManager.startTask(task, query);
					}

					@Override
					public void onCopy() {
						mailActionModeHandle.finish();

						Item item = new Item(mail.Content.replaceAll("\\<.*?>", ""));
						ClipData data = new ClipData(new ClipDescription("Mail Text Copied", new String[] { ClipDescription.MIMETYPE_TEXT_PLAIN }), item);
						ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
						clipboardManager.setPrimaryClip(data);
					}
				});

				mailActionModeHandle = startActionMode(mode);

				return true;
			}
		});

		getPullToRefreshAttacher().setRefreshableView(lv, new PullToRefreshAttacher.OnRefreshListener() {
			@Override
			public void onRefreshStarted(View view) {
				load(true, null, null, null);
			}
		});
				
		refreshReceiverEnabled = true;
		registerReceiver(refreshReceiver, new IntentFilter(Constants.REFRESH_MAIL_INTENT_FILTER));

		load(true, null, null, null);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(refreshReceiver);
	}

	@Override
	protected void onPause() {
		super.onPause();
		refreshReceiverEnabled = false;
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		refreshReceiverEnabled = true;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.mail_menu, menu);

		MenuItem searchMenuItem = menu.findItem(R.id.search);
		searchMenuItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
			@Override
			public boolean onMenuItemActionExpand(MenuItem item) {
				if (searchView != null) {
					searchView.requestFocus();
				}
				Log.d(Constants.TAG, "SearchView on SearchActivity expanded.");
				return true;
			}

			@Override
			public boolean onMenuItemActionCollapse(MenuItem item) {
				Log.d(Constants.TAG, "SearchView on SearchActivity closed.");
				return true;
			}
		});

		searchView = (SearchView) searchMenuItem.getActionView();

		searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
			@Override
			public boolean onQueryTextSubmit(String query) {
				search(query);
				return true;
			}

			@Override
			public boolean onQueryTextChange(String newText) {
				return false;
			}
		});

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.compose) {
			return compose(null);
		}
		if (item.getItemId() == R.id.refresh) {
			return load(true, null, null, null);
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onListViewDataRequested() {
		Mail lastMail = null;
		if (adapter != null && (lastMail = adapter.getLastItem()) != null) {
			load(false, lastMail.Id, lastFilterUser, lastFilterText);
		}
	}

	@Override
	public void onSecondBaseMenuOpened() {
		// TODO: toto neviem ci je dobry napad volat tu
		loadConversation();
	}

	private void loadConversation() {
		Task<ITaskQuery, ArrayList<Conversation>> task = MailDataAccess.getConversations(MailActivity.this, conversationTaskListener);
		TaskManager.startTask(task, ITaskQuery.empty);
	}

	private boolean load(boolean refresh, Long id, String filterUser, String filterText) {
		MailQuery query = new MailQuery();
		query.Refresh = refresh;
		query.LastId = id;
		query.FilterUser = filterUser;
		query.FilterText = filterText;

		Task<MailQuery, ArrayList<Mail>> task = MailDataAccess.getMail(MailActivity.this, mailsTaskListener);
		TaskManager.startTask(task, query);

		Log.d(Constants.TAG, "MailActivity load requested, last id = " + id);

		return true;
	}

	private void search(String text) {
		Tuple<String, String> tuple = CoreUtility.splitSearch(text);
		if (tuple == null) {
			Log.w(Constants.TAG, "Empty search term.");
			return;
		}

		lastFilterUser = tuple.First;
		lastFilterText = tuple.Second;
		
		load(true, null, tuple.First, tuple.Second);
	}

	private boolean compose(Mail mail) {
		final Intent intent = new Intent(MailActivity.this, MailComposeActivity.class);
		if (mail == null) {
			intent.putExtra(Constants.KEY_NICK, "");
			startActivityForResult(intent, Constants.REQUEST);
			overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
		} else {
			intent.putExtra(Constants.REQUEST_MAIL, mail);
			startActivityForResult(intent, Constants.REQUEST);
			overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
		}
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Constants.REQUEST_RESPONSE_OK) {
			load(true, null, null, null);
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private class MailsTaskListener extends TaskListener<ArrayList<Mail>> {
		@Override
		public void done(ArrayList<Mail> output) {
			MailQuery query = (MailQuery) getTag();
			MailActivity context = (MailActivity) getContext();

			if (query.Refresh) {
				adapter = new MailAdapter(context, output);
				context.setListAdapter(adapter);
			} else {
				adapter.addItems(adapter.filter(output));
				adapter.notifyDataSetChanged();
			}

			context.getPullToRefreshAttacher().setRefreshComplete();
		}
	}

	private class NoopTaskListener extends TaskListener<NullResponse> {
		@Override
		public void done(NullResponse output) {
		}
	}

	private class ConversationTaskListener extends TaskListener<ArrayList<Conversation>> {
		@Override
		public void done(ArrayList<Conversation> output) {
			if (conversationAdapter != null) {
				conversationAdapter.clearItems();
				conversationAdapter.addItems(output);
				conversationAdapter.notifyDataSetChanged();
			}

			if (conversationProgress != null) {
				conversationProgress.setVisibility(View.INVISIBLE);
			}
		}
	}

	private BroadcastReceiver refreshReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (!refreshReceiverEnabled) {
				Log.w(Constants.TAG, "refreshReceiver not enabled.");
				return;
			}
			
			load(true, null, null, null);
		}
	};

}
