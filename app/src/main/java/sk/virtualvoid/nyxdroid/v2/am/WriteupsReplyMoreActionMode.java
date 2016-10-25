package sk.virtualvoid.nyxdroid.v2.am;

import sk.virtualvoid.nyxdroid.v2.BaseActivity;
import sk.virtualvoid.nyxdroid.v2.R;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;

/**
 * 
 * @author Juraj
 * 
 */
public class WriteupsReplyMoreActionMode implements ActionMode.Callback {
	private BaseActivity context;
	private Listener listener;
	private boolean isActive;
	private int choiceMode;

	public WriteupsReplyMoreActionMode(BaseActivity context, Listener listener) {
		this.context = context;
		this.listener = listener;
		this.isActive = false;

		initialState();
	}

	private void initialState() {
		if (context != null && context.getListAdapter() != null) {
			choiceMode = context.getListView().getChoiceMode();

			context.getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		}
	}

	private void restoreState() {
		if (context != null && context.getListAdapter() != null) {
			context.getListView().setChoiceMode(choiceMode);
		}
	}

	@Override
	public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
		switch (item.getItemId()) {
			case R.id.reply:
				listener.onReply();
				return true;
		}
		return false;
	}

	@Override
	public boolean onCreateActionMode(ActionMode mode, Menu menu) {
		MenuInflater mi = mode.getMenuInflater();
		mi.inflate(R.menu.writeup_replytomore, menu);
		return true;
	}

	@Override
	public void onDestroyActionMode(ActionMode mode) {
		listener.onDestroy();
		
		restoreState();
		isActive = false;
	}

	@Override
	public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
		isActive = true;
		return false;
	}

	public boolean isActive() {
		return this.isActive;
	}

	public interface Listener {
		void onReply();
		void onDestroy();
	}
}
