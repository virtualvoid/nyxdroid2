package sk.virtualvoid.nyxdroid.v2;

import java.util.ArrayList;

import sk.virtualvoid.core.Task;
import sk.virtualvoid.core.TaskListener;
import sk.virtualvoid.core.TaskManager;
import sk.virtualvoid.core.widgets.CustomAutocompleteTextView;
import sk.virtualvoid.nyxdroid.v2.data.UserSearch;
import sk.virtualvoid.nyxdroid.v2.data.adapters.UserSearchAdapter;
import sk.virtualvoid.nyxdroid.v2.data.dac.SearchDataAccess;
import sk.virtualvoid.nyxdroid.v2.data.query.UserSearchQuery;
import sk.virtualvoid.nyxdroid.v2.internal.DelayedTextWatcher;
import android.app.Activity;
import android.app.Dialog;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

/**
 * 
 * @author juraj
 * 
 */
public class EnterNickDialog extends Dialog {
	private UserSearchAdapter adapter;
	private Task<UserSearchQuery, ArrayList<UserSearch>> task;
	private UserSearchTaskListener listener  = new UserSearchTaskListener();
	
	public EnterNickDialog(final Activity context, final OnNickEnteredListener onNickEnteredListener) {
		super(context);

		setContentView(R.layout.enter_nick);
		setTitle(R.string.new_mail_to_);

		final CustomAutocompleteTextView txtNick = (CustomAutocompleteTextView) findViewById(R.id.enter_nick);
		txtNick.addTextChangedListener(new DelayedTextWatcher(500) {
			@Override
			public void afterTextChangedDelayed(String str) {
				if (str.length() < 2) {
					return;
				}
				
				TaskManager.killIfNeeded(task);
				
				task = SearchDataAccess.searchUsers(context, listener);
				TaskManager.startTask(task, new UserSearchQuery(str));
			}
		});

		adapter = new UserSearchAdapter(context);
		txtNick.setAdapter(adapter);
		txtNick.setFocusable(true);
		txtNick.requestFocusFromTouch();
		
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		
		final Button btnOk = (Button) findViewById(R.id.enter_nick_accept);
		btnOk.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String nick = txtNick.getText().toString();
				if (nick.length() > 0) {
					onNickEnteredListener.onNickEntered(nick);
					dismiss();
				}
			}
		});
	}

	public interface OnNickEnteredListener {
		void onNickEntered(String nick);
	}
	
	private class UserSearchTaskListener extends TaskListener<ArrayList<UserSearch>> {
		@Override
		public void done(ArrayList<UserSearch> output) {
			adapter.clearAll();
			adapter.addItems(output);
			adapter.notifyDataSetChanged();
		}
	}
}
