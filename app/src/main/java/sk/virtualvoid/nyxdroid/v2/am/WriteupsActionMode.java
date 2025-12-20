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
public class WriteupsActionMode implements ActionMode.Callback {
	private Listener listener;

	public WriteupsActionMode(Activity context, Listener listener) {
		this.listener = listener;
	}

	@Override
	public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
		int id= item.getItemId();
        if (id == R.id.reply){
				listener.onReply();
				return true;}
        if (id == R.id.sendmail) {
				listener.onSendMail();
				return true;}
        if (id == R.id.viewreplies){
				listener.onViewReplies();
				return true;}
        if (id == R.id.viewrating){
				listener.onViewRating();
				return true;}
        if (id == R.id.copy){
				listener.onCopy();
				return true;}
        if (id == R.id.view_as_gallery_from_here){
				listener.onViewGallery();
				return true;}
        if (id == R.id.reminder){
				listener.onReminder();
				return true;}
        if (id == R.id.delete){
				listener.onDelete();
				return true;}
        if (id == R.id.copylink){
				listener.onCopyLink();
				return true;
		}
		return false;
	}

	@Override
	public boolean onCreateActionMode(ActionMode mode, Menu menu) {
		MenuInflater mi = mode.getMenuInflater();
		mi.inflate(R.menu.writeup_contextual, menu);
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

		void onSendMail();

		void onViewReplies();

		void onViewRating();

		void onCopy();

		void onViewGallery();

		void onReminder();

		void onDelete();
		
		void onCopyLink();
	}
}
