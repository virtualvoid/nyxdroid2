package sk.virtualvoid.nyxdroid.v2;

import java.io.File;
import java.util.ArrayList;
import sk.virtualvoid.core.CoreUtility;
import sk.virtualvoid.core.ImageDownloader;
import sk.virtualvoid.core.ImageGetterAsync;
import sk.virtualvoid.core.Task;
import sk.virtualvoid.core.TaskListener;
import sk.virtualvoid.core.TaskManager;
import sk.virtualvoid.nyxdroid.library.Constants;
import sk.virtualvoid.nyxdroid.v2.data.NullResponse;
import sk.virtualvoid.nyxdroid.v2.data.TypedPoco;
import sk.virtualvoid.nyxdroid.v2.data.TypedPoco.Type;
import sk.virtualvoid.nyxdroid.v2.data.Writeup;
import sk.virtualvoid.nyxdroid.v2.data.Attachment;
import sk.virtualvoid.nyxdroid.v2.data.adapters.ComposeAdapter;
import sk.virtualvoid.nyxdroid.v2.data.dac.WriteupDataAccess;
import sk.virtualvoid.nyxdroid.v2.data.query.WriteupQuery;
import sk.virtualvoid.nyxdroid.v2.internal.VotingType;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;

/**
 * 
 * @author Juraj
 * 
 */
public class WriteupComposeActivity extends BaseActivity {

	private SendWriteupTaskListener sendWriteupTaskListener = new SendWriteupTaskListener();

	private Long discussionId;
	private String discussionName;
	private ArrayList<Writeup> replyingWriteupList;
	private String attachmentSource;

	private ImageDownloader imageDownloader;
	private ImageGetterAsync imageGetterAsync;
	private ArrayList<TypedPoco<?>> adapterModel = new ArrayList<TypedPoco<?>>();
	private ComposeAdapter adapter;

	private EditText txtMessage;

	@Override
	protected int getContentViewId() {
		return R.layout.generic_compose;
	}
	
	@Override
	protected boolean useSlidingMenu() {
		return false;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Drawable emptyAvatar = getResources().getDrawable(R.drawable.empty_avatar);

		imageDownloader = new ImageDownloader(this, emptyAvatar);
		imageGetterAsync = new ImageGetterAsync(this);

		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

		txtMessage = (EditText) findViewById(R.id.generic_compose_message);

		Intent spawnIntent = getIntent();
		Bundle spawnBundle = savedInstanceState == null ? spawnIntent.getExtras() : savedInstanceState;

		discussionId = spawnBundle.getLong(Constants.REQUEST_WRITEUP_DISCUSSION_ID);
		setTitle(discussionName = spawnBundle.getString(Constants.REQUEST_WRITEUP_DISCUSSION_NAME));

		if (spawnBundle.containsKey(Constants.REQUEST_WRITEUP)) {
			replyingWriteupList = (ArrayList<Writeup>) spawnBundle.getSerializable(Constants.REQUEST_WRITEUP);
			if (replyingWriteupList != null) {
				// pridame vecae na odpovedanie
				for (Writeup wu : replyingWriteupList) {
					adapterModel.add(new TypedPoco<Writeup>(Type.REPLY, wu));
				}
				// ale ak mam len jeden wu, tak nastavime rovno aj reply message
				if (replyingWriteupList.size() == 1) {
					Writeup replyingWriteup = replyingWriteupList.get(0);
					txtMessage.append(getReplyingMessage(replyingWriteup));
					txtMessage.requestFocus();
				}
			}
		}

		ListView lv = getListView();

		lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				TypedPoco<?> item = (TypedPoco<?>) adapterModel.get(position);
				if (item.Type == Type.REPLY) {
					Writeup replyingWriteup = (Writeup) item.ChildPoco;
					txtMessage.append(getReplyingMessage(replyingWriteup));
					txtMessage.requestFocus();
				}
			}
		});

		lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				TypedPoco<?> item = (TypedPoco<?>) adapterModel.get(position);
				if (item.Type == Type.ATTACHMENT) {
					attachmentSource = null;
					adapterModel.remove(position);
					adapter.notifyDataSetChanged();
					return true;
				}

				return false;
			}
		});

		adapter = new ComposeAdapter<Writeup>(this, adapterModel, imageDownloader, imageGetterAsync);
		setListAdapter(adapter);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putLong(Constants.REQUEST_WRITEUP_DISCUSSION_ID, discussionId);
		outState.putString(Constants.REQUEST_WRITEUP_DISCUSSION_NAME, discussionName);

		if (replyingWriteupList != null) {
			outState.putSerializable(Constants.REQUEST_WRITEUP, replyingWriteupList);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// ak mame viacero prispevkov, tak vzdy genericke menu (bez vote)
		if (replyingWriteupList != null && replyingWriteupList.size() > 1) {
			getMenuInflater().inflate(R.menu.generic_compose_menu, menu);
		}
		// ak mame jeden prispevok a je nahodou nas, tak vzdy genericke menu
		// (bez vote)
		else if (replyingWriteupList != null && replyingWriteupList.size() == 1 && replyingWriteupList.get(0).IsMine) {
			getMenuInflater().inflate(R.menu.generic_compose_menu, menu);
		}
		// ak mame jeden prispevok a nieje nas, tak ide wu menu aj s vote
		else if (replyingWriteupList != null && replyingWriteupList.size() == 1 && !replyingWriteupList.get(0).IsMine) {
			getMenuInflater().inflate(R.menu.writeup_compose_menu, menu);
		}
		// alebo ak proste nemame nic, tak ide generic
		else if (replyingWriteupList == null) {
			getMenuInflater().inflate(R.menu.generic_compose_menu, menu);
		}
		// if ((replyingWriteup != null && replyingWriteup.IsMine) ||
		// replyingWriteup == null) {
		// getMenuInflater().inflate(R.menu.generic_compose_menu, menu);
		// } else {
		// getMenuInflater().inflate(R.menu.writeup_compose_menu, menu);
		// }
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				setResult(Constants.REQUEST_RESPONSE_CANCEL);
				finish();
				return true;
			case R.id.attachment:
				return attachment();
			case R.id.send:
				return send();
			case R.id.voteup:
				return vote(VotingType.POSITIVE);
			case R.id.votedown:
				return vote(VotingType.NEGATIVE);
		}
		return false;
	}

	private boolean vote(VotingType votingType) {
		Writeup replyingWriteup = replyingWriteupList.get(0);

		Intent data = new Intent();
		data.putExtra(Constants.KEY_ID, replyingWriteup.Id);
		data.putExtra(Constants.KEY_VOTING_RESULT, votingType);

		setResult(Constants.REQUEST_RESPONSE_VOTING, data);
		finish();

		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == Constants.REQUEST_ATTACHMENT && resultCode == Activity.RESULT_OK) {
			attachmentSource = CoreUtility.getRealPathFromURI(this, data.getData());
			if (attachmentSource == null) {
				Toast.makeText(this, R.string.file_doesnt_exists_or_cant_read, Toast.LENGTH_LONG).show();
				return;
			}

			File attachmentFile = new File(attachmentSource);
			if (!attachmentFile.exists() || !attachmentFile.canRead()) {
				Toast.makeText(this, R.string.file_doesnt_exists_or_cant_read, Toast.LENGTH_LONG).show();
				attachmentSource = null;
			} else {
				adapterModel.add(new TypedPoco<Attachment>(Type.ATTACHMENT, new Attachment(attachmentSource)));
				adapter.notifyDataSetChanged();
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private boolean attachment() {
		Intent intent = new Intent(Intent.ACTION_PICK);
		intent.setType("image/*");
		intent.putExtra("return-data", false);
		startActivityForResult(intent, Constants.REQUEST_ATTACHMENT);
		return true;
	}

	private boolean send() {
		String message = txtMessage.getText().toString();
		if (message.length() == 0 && attachmentSource == null) {
			Toast.makeText(this, R.string.and_what_about_message, Toast.LENGTH_SHORT).show();
			return false;
		}

		WriteupQuery query = new WriteupQuery();
		query.Id = discussionId;
		query.Contents = message;

		if (attachmentSource != null) {
			query.AttachmentSource = new File(attachmentSource);
		}

		Task<WriteupQuery, NullResponse> task = WriteupDataAccess.sendWriteup(WriteupComposeActivity.this, sendWriteupTaskListener);
		TaskManager.startTask(task, query);

		return true;
	}
	
	private static String getReplyingMessage(Writeup replyingWriteup) {
		String replyingMessage = String.format("{reply %s|%s}: ", replyingWriteup.Nick, Long.toString(replyingWriteup.Id));
		return replyingMessage;
	}

	private static class SendWriteupTaskListener extends TaskListener<NullResponse> {
		@Override
		public void done(NullResponse output) {
			BaseActivity context = (BaseActivity) getContext();

			if (!output.Success) {
				Toast.makeText(getContext(), R.string.something_terrible_happened_to_nyx, Toast.LENGTH_SHORT).show();
				context.setResult(Constants.REQUEST_RESPONSE_FAIL);
			} else {
				Toast.makeText(getContext(), R.string.sent, Toast.LENGTH_SHORT).show();
				context.setResult(Constants.REQUEST_RESPONSE_OK);
			}

			context.finish();
		}
	}
}
