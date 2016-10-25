package sk.virtualvoid.nyxdroid.v2.am;

import sk.virtualvoid.nyxdroid.v2.R;
import sk.virtualvoid.nyxdroid.v2.R.id;
import sk.virtualvoid.nyxdroid.v2.R.menu;
import android.app.Activity;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

/**
 * 
 * @author Juraj
 *
 */
public class FriendsActionMode implements ActionMode.Callback {

	private Listener listener;
	
	public FriendsActionMode(Activity context, Listener listener) {
		this.listener = listener;
	}
	
	@Override
	public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
		switch(item.getItemId()) {
			case R.id.sendmail:
				listener.onSendMail();
				return true;
			case R.id.gotodiscussion:
				listener.onGotoDiscussion();
				return true;
		}
		return false;
	}

	@Override
	public boolean onCreateActionMode(ActionMode mode, Menu menu) {
		MenuInflater mi = mode.getMenuInflater();
		mi.inflate(R.menu.friends_contextual, menu);
		return true;
	}

	@Override
	public void onDestroyActionMode(ActionMode mode) {
	}

	@Override
	public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
		return false;
	}

	public interface Listener {
		void onSendMail();
		void onGotoDiscussion();
	}
}
