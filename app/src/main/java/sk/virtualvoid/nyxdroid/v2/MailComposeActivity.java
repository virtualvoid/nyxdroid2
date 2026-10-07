package sk.virtualvoid.nyxdroid.v2;

import java.util.ArrayList;

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
import android.content.ActivityNotFoundException;
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
	private boolean sending;

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

			if(replyingMail.ReplyTo) {
				txtMessage.setText(String.format(Constants.REPLY_TO_MAIL, replyingMail.Id));
			}

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

		if (savedInstanceState != null) {
			replyingNick = savedInstanceState.getString("compose_recipient", replyingNick);
			ArrayList<Attachment> attachments = (ArrayList<Attachment>) savedInstanceState.getSerializable(Attachment.STATE_ATTACHMENTS);
			if (attachments != null) {
				for (Attachment attachment : attachments) adapterModel.add(new TypedPoco<>(Type.ATTACHMENT, attachment));
			}
		}

		ListView lv = getListView();
		lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				TypedPoco<?> item = (TypedPoco<?>) adapterModel.get(position);
				if (item.Type == Type.ATTACHMENT) {
					if (sending) return true;
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
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable(Attachment.STATE_ATTACHMENTS, Attachment.getSelected(adapterModel));
		outState.putString("compose_recipient", replyingNick);
		setSending(false);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(R.id.send).setEnabled(!sending);
		menu.findItem(R.id.attachment).setEnabled(!sending);
		return super.onPrepareOptionsMenu(menu);
	}

	private void setSending(boolean value) {
		sending = value;
		txtMessage.setEnabled(!value);
		txtRecipient.setEnabled(!value);
		adapter.setAttachmentsEditable(!value);
		invalidateOptionsMenu();
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
	protected void onDestroy() {
		TaskManager.cancelTasks(this);
		super.onDestroy();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == Constants.REQUEST_ATTACHMENT && resultCode == Activity.RESULT_OK) {
			if (!Attachment.addSelected(this, data, adapterModel)) {
				Toast.makeText(this, R.string.file_doesnt_exists_or_cant_read, Toast.LENGTH_LONG).show();
			}
			adapter.notifyDataSetChanged();
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private boolean attachment() {
		if (sending) return true;
		Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		intent.setType("*/*");
		intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
		intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
		try {
			startActivityForResult(intent, Constants.REQUEST_ATTACHMENT);
		} catch (ActivityNotFoundException e) {
			Toast.makeText(this, R.string.cant_open_it, Toast.LENGTH_SHORT).show();
		}
		return true;
	}

	private void send() {
		if (sending) return;
		if (replyingNick == null || replyingNick.length() == 0) {
			Toast.makeText(this, R.string.and_what_about_nick, Toast.LENGTH_SHORT).show();
			return;
		}

		String message = txtMessage.getText().toString();
		ArrayList<Attachment> attachments = Attachment.getSelected(adapterModel);
		if (message.length() == 0 && attachments.isEmpty()) {
			Toast.makeText(this, R.string.and_what_about_message, Toast.LENGTH_SHORT).show();
			return;
		}

		MailQuery query = new MailQuery();
		query.To = replyingNick;
		query.Message = message;

		query.Attachments = attachments;

		Task<MailQuery, NullResponse> task = MailDataAccess.sendMail(MailComposeActivity.this, sendMailTaskListener);
		setSending(true);
		TaskManager.startTask(task, query);
	}

	/**
	 * 
	 * @author juraj
	 * 
	 */
	public class SendMailTaskListener extends TaskListener<NullResponse> {
		@Override
		public void handleError(Throwable error) {
			setSending(false);
			super.handleError(error);
		}

		@Override
		public void done(NullResponse output) {
			setSending(false);
			if (!output.Success) {
				Toast.makeText(getContext(), R.string.something_terrible_happened_to_nyx, Toast.LENGTH_SHORT).show();
				return;
			}

			setResult(Constants.REQUEST_RESPONSE_OK);
			finish();
		}
	}
}
