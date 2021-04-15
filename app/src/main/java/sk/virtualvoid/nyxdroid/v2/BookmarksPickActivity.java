package sk.virtualvoid.nyxdroid.v2;

import java.io.File;
import java.util.ArrayList;

import sk.virtualvoid.core.CoreUtility;
import sk.virtualvoid.core.Task;
import sk.virtualvoid.core.TaskListener;
import sk.virtualvoid.core.TaskManager;
import sk.virtualvoid.nyxdroid.library.Constants;
import sk.virtualvoid.nyxdroid.v2.data.Bookmark;
import sk.virtualvoid.nyxdroid.v2.data.BookmarkCategory;
import sk.virtualvoid.nyxdroid.v2.data.NullResponse;
import sk.virtualvoid.nyxdroid.v2.data.SuccessResponse;
import sk.virtualvoid.nyxdroid.v2.data.adapters.BookmarkAdapter;
import sk.virtualvoid.nyxdroid.v2.data.dac.BookmarkDataAccess;
import sk.virtualvoid.nyxdroid.v2.data.dac.WriteupDataAccess;
import sk.virtualvoid.nyxdroid.v2.data.query.BookmarkQuery;
import sk.virtualvoid.nyxdroid.v2.data.query.WriteupQuery;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

/**
 * 
 * @author Juraj
 * 
 */
public class BookmarksPickActivity extends BaseActivity {
	private BookmarksTaskListener bookmarksTaskListener = new BookmarksTaskListener();
	private SendWriteupTaskListener sendWriteupTaskListener = new SendWriteupTaskListener();

	private EditText txtMessage;
	
	@Override
	protected int getContentViewId() {
		return R.layout.share_activity;
	}

	@Override
	protected boolean useSlidingMenu() {
		return false;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getActionBar().setHomeButtonEnabled(false);
		getActionBar().setDisplayHomeAsUpEnabled(false);

		final Intent intent = getIntent();
		final String action = intent.getAction();
		final String type = intent.getType();

		ListView lv = getListView();
		lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Bookmark bookmark = (Bookmark) parent.getItemAtPosition(position);
				if (bookmark instanceof BookmarkCategory) {
					return;
				}

				if (Intent.ACTION_SEND.equals(action) && type != null) {
					if (type.startsWith("text/")) {
						handleShareText(bookmark.Id, bookmark.Name, intent);
					}
					if (type.startsWith("image/")) {
						handleShareImage(bookmark.Id, bookmark.Name, intent);
					}
				}
			}
		});

		txtMessage = (EditText)findViewById(R.id.share_message);
		
		load();
	}

	private void load() {
		BookmarkQuery query = new BookmarkQuery();
		query.IncludeUnread = true;

		Task<BookmarkQuery, SuccessResponse<ArrayList<Bookmark>>> task = BookmarkDataAccess.getBookmarks(BookmarksPickActivity.this, bookmarksTaskListener);
		TaskManager.startTask(task, query);
	}

	private AlertDialog.Builder getDialogBuilder(String name) {
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(BookmarksPickActivity.this);
		dialogBuilder.setTitle(R.string.really_wanna_share);

		String shareQuestion = getResources().getString(R.string.are_you_sure_you_wanna_share_this);
		dialogBuilder.setMessage(String.format(shareQuestion, name));
		
		dialogBuilder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				BookmarksPickActivity.this.finish();
			}
		});
		
		return dialogBuilder; 
	}
	
	private void handleShareText(final long id, final String name, Intent intent) {
		String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
		if (sharedText == null) {
			return;
		}

		String additionalText = txtMessage.getText().toString();

		final String contents = String.format("%s %s", additionalText, sharedText);
		
		AlertDialog.Builder dialogBuilder = getDialogBuilder(name);

		dialogBuilder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				WriteupQuery query = new WriteupQuery();
				query.Id = id;
				query.Contents = contents;

				Task<WriteupQuery, NullResponse> task = WriteupDataAccess.sendWriteup(BookmarksPickActivity.this, sendWriteupTaskListener);
				TaskManager.startTask(task, query);
			}
		});

		AlertDialog dialog = dialogBuilder.create();
		dialog.show();
	}

	private void handleShareImage(final long id, final String name, Intent intent) {
		final Uri imageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
		final String imageUrl = CoreUtility.getRealPathFromURI(BookmarksPickActivity.this, imageUri);
		if (imageUrl == null) {
			Toast.makeText(this, R.string.file_doesnt_exists_or_cant_read, Toast.LENGTH_LONG).show();
			return;
		}
		
		final File attachmentFile = new File(imageUrl);
		if (!attachmentFile.exists() || !attachmentFile.canRead()) {
			Toast.makeText(this, R.string.file_doesnt_exists_or_cant_read, Toast.LENGTH_LONG).show();
			return;
		}
		
		final String additionalText = txtMessage.getText().toString();
		
		AlertDialog.Builder dialogBuilder = getDialogBuilder(name);
		dialogBuilder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				WriteupQuery query = new WriteupQuery();
				query.Id = id;
				query.Contents = additionalText;
				query.AttachmentSource = attachmentFile;
				
				Task<WriteupQuery, NullResponse> task = WriteupDataAccess.sendWriteup(BookmarksPickActivity.this, sendWriteupTaskListener);
				TaskManager.startTask(task, query);
			}
		});

		AlertDialog dialog = dialogBuilder.create();
		dialog.show();
		
		Log.w(Constants.TAG, "URI: " + imageUri + ", URL: " + imageUrl);
	}

	private static class BookmarksTaskListener extends TaskListener<SuccessResponse<ArrayList<Bookmark>>> {
		@Override
		public void done(SuccessResponse<ArrayList<Bookmark>> output) {
			BookmarksPickActivity context = (BookmarksPickActivity) getContext();
			BookmarkAdapter adapter = new BookmarkAdapter(context, output.getData());
			context.setListAdapter(adapter);
		}
	}

	private static class SendWriteupTaskListener extends TaskListener<NullResponse> {
		@Override
		public void done(NullResponse output) {
			BookmarksPickActivity context = (BookmarksPickActivity) getContext();
			context.finish();
		}
	}
}
