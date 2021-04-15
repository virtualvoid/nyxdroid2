package sk.virtualvoid.nyxdroid.v2;

import java.io.File;
import java.util.ArrayList;

import sk.virtualvoid.core.CoreUtility;
import sk.virtualvoid.core.CustomHtml;
import sk.virtualvoid.core.ImageDownloader;
import sk.virtualvoid.core.Task;
import sk.virtualvoid.core.TaskListener;
import sk.virtualvoid.core.TaskManager;
import sk.virtualvoid.core.widgets.CustomAutocompleteTextView;
import sk.virtualvoid.nyxdroid.library.Constants;
import sk.virtualvoid.nyxdroid.v2.data.BasePoco;
import sk.virtualvoid.nyxdroid.v2.data.Mail;
import sk.virtualvoid.nyxdroid.v2.data.Attachment;
import sk.virtualvoid.nyxdroid.v2.data.NullResponse;
import sk.virtualvoid.nyxdroid.v2.data.TypedPoco;
import sk.virtualvoid.nyxdroid.v2.data.UserSearch;
import sk.virtualvoid.nyxdroid.v2.data.TypedPoco.Type;
import sk.virtualvoid.nyxdroid.v2.data.adapters.ComposeAdapter;
import sk.virtualvoid.nyxdroid.v2.data.adapters.UserSearchAdapter;
import sk.virtualvoid.nyxdroid.v2.data.dac.MailDataAccess;
import sk.virtualvoid.nyxdroid.v2.data.dac.SearchDataAccess;
import sk.virtualvoid.nyxdroid.v2.data.query.MailQuery;
import sk.virtualvoid.nyxdroid.v2.data.query.UserSearchQuery;
import sk.virtualvoid.nyxdroid.v2.internal.DelayedTextWatcher;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;

/**
 * 
 * @author juraj
 * 
 */
public class MailComposeActivity extends BaseActivity {

	private SendMailTaskListener sendMailTaskListener = new SendMailTaskListener();

	private Task<UserSearchQuery, ArrayList<UserSearch>> userSearchTask;

	private ImageDownloader imageDownloader;

	private String replyingNick;
	private Mail replyingMail;
	private String attachmentSource;

	private ArrayList<TypedPoco<?>> adapterModel = new ArrayList<TypedPoco<?>>();
	private ComposeAdapter adapter;

	private CustomAutocompleteTextView txtRecipient;
	private EditText txtMessage;

	@Override
	protected int getContentViewId() {
		return R.layout.generic_compose_mail;
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

		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

		txtRecipient = (CustomAutocompleteTextView) findViewById(R.id.generic_compose_recipient);
		txtMessage = (EditText) findViewById(R.id.generic_compose_message);

		setTitle(getResources().getString(R.string.app_name_mail));

		Intent spawnIntent = getIntent();
		Bundle spawnBundle = spawnIntent.getExtras();
		if (spawnBundle.containsKey(Constants.REQUEST_MAIL)) {
			replyingMail = (Mail) spawnBundle.get(Constants.REQUEST_MAIL);
			replyingNick = replyingMail.Nick;

			adapterModel.add(new TypedPoco<Mail>(Type.REPLY, replyingMail));

			txtRecipient.setVisibility(View.GONE);
			txtMessage.requestFocus();
		} else if (spawnBundle.containsKey(Constants.KEY_NICK)) {
			final UserSearchAdapter userSearchAdapter = new UserSearchAdapter(MailComposeActivity.this);

			final TaskListener<ArrayList<UserSearch>> userSearchListener = new TaskListener<ArrayList<UserSearch>>() {
				@Override
				public void done(ArrayList<UserSearch> output) {
					userSearchAdapter.clearAll();
					userSearchAdapter.addItems(output);
					userSearchAdapter.notifyDataSetChanged();
				}
			};

			txtRecipient.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					UserSearch userSearch = (UserSearch) userSearchAdapter.getItem(position);
					replyingNick = userSearch.Nick;

					txtMessage.requestFocus();
				}
			});

			txtRecipient.addTextChangedListener(new DelayedTextWatcher(400) {
				@Override
				public void afterTextChangedDelayed(String str) {
					if (str.length() < 2) {
						return;
					}

					TaskManager.killIfNeeded(userSearchTask);

					userSearchTask = SearchDataAccess.searchUsers(MailComposeActivity.this, userSearchListener);
					TaskManager.startTask(userSearchTask, new UserSearchQuery(str));
				}
			});

			txtRecipient.setAdapter(userSearchAdapter);
			txtRecipient.requestFocus();

			replyingNick = spawnBundle.getString(Constants.KEY_NICK);
			if (replyingNick != null && replyingNick.length() > 0) {
				txtRecipient.setText(replyingNick);
				txtMessage.requestFocus();
			}

			getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		}

		ListView lv = getListView();
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

		adapter = new ComposeAdapter<Mail>(this, adapterModel, imageDownloader, null);
		setListAdapter(adapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.generic_compose_menu, menu);
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
				send();
				return true;
		}
		return false;
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

	private void send() {
		if (replyingNick == null || replyingNick.length() == 0) {
			Toast.makeText(this, R.string.and_what_about_nick, Toast.LENGTH_SHORT).show();
			return;
		}

		String message = txtMessage.getText().toString();
		if (message.length() == 0 && attachmentSource == null) {
			Toast.makeText(this, R.string.and_what_about_message, Toast.LENGTH_SHORT).show();
			return;
		}

		MailQuery query = new MailQuery();
		query.To = replyingNick;
		query.Message = message;

		if (attachmentSource != null) {
			query.AttachmentSource = new File(attachmentSource);
		}

		Task<MailQuery, NullResponse> task = MailDataAccess.sendMail(MailComposeActivity.this, sendMailTaskListener);
		TaskManager.startTask(task, query);
	}

	/**
	 * 
	 * @author juraj
	 * 
	 */
	public class SendMailTaskListener extends TaskListener<NullResponse> {
		@Override
		public void done(NullResponse output) {
			if (!output.Success) {
				Toast.makeText(getContext(), R.string.something_terrible_happened_to_nyx, Toast.LENGTH_SHORT).show();
			}

			setResult(Constants.REQUEST_RESPONSE_OK);
			finish();
		}
	}
}
