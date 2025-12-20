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
 * @author juraj
 * 
 */
public class MailActionMode implements ActionMode.Callback {

	private Listener listener;

	public MailActionMode(Activity context, Listener listener) {
		this.listener = listener;
	}

	@Override
	public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
		int id =item.getItemId();
        if (id == R.id.reply){
				listener.onReply();
				return true;}
        if (id == R.id.copy){
				listener.onCopy();
				return true;}
        if (id == R.id.reminder){
				listener.onReminder();
				return true;}
        if (id == R.id.delete){
				listener.onDelete();
				return true;
		}
		return false;
	}

	@Override
	public boolean onCreateActionMode(ActionMode mode, Menu menu) {
		MenuInflater mi = mode.getMenuInflater();
		mi.inflate(R.menu.mail_contextual, menu);
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
		void onReply();

		void onCopy();

		void onReminder();

		void onDelete();
	}
}
