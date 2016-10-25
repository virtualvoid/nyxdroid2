package sk.virtualvoid.nyxdroid.v2;

import java.util.ArrayList;

import sk.virtualvoid.core.CustomHtml;
import sk.virtualvoid.core.DialogDecorator;
import sk.virtualvoid.core.ImageDownloader;
import sk.virtualvoid.core.Task;
import sk.virtualvoid.core.TaskListener;
import sk.virtualvoid.core.TaskManager;
import sk.virtualvoid.nyxdroid.v2.data.BasePoco;
import sk.virtualvoid.nyxdroid.v2.data.FeedComment;
import sk.virtualvoid.nyxdroid.v2.data.NullResponse;
import sk.virtualvoid.nyxdroid.v2.data.adapters.BasePocoAdapter;
import sk.virtualvoid.nyxdroid.v2.data.dac.FeedDataAccess;
import sk.virtualvoid.nyxdroid.v2.data.query.FeedQuery;
import android.app.Activity;
import android.app.Dialog;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

/**
 * 
 * @author juraj
 * 
 */
public class FeedCommentDialog extends Dialog {
	private ListView listView;
	private EditText txtMessage;
	private Button btnPost;

	private FeedQuery query;
	private FeedCommentsAdapter adapter;

	public FeedCommentDialog(final Activity context, FeedQuery query, ArrayList<FeedComment> output) {
		super(context, R.style.full_screen_dialog);

		DialogDecorator.decorate(this);

		setContentView(R.layout.feed_comment);

		this.query = query;

		listView = (ListView) findViewById(R.id.feed_comment_list);
		listView.setDivider(null);
		listView.setDividerHeight(0);
		listView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
		listView.setStackFromBottom(true);
		listView.setAdapter(adapter = new FeedCommentsAdapter(context, output));

		txtMessage = (EditText) findViewById(R.id.feed_enter_your_comment);

		btnPost = (Button) findViewById(R.id.feed_button_comment_post);
		btnPost.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onPostClick(context);
			}
		});
	}

	private void onPostClick(final Activity context) {
		String message = txtMessage.getText().toString();
		if (message.length() == 0) {
			return;
		}
		txtMessage.setText("");

		query.Message = message;

		Task<FeedQuery, NullResponse> task = FeedDataAccess.postComment(context, new TaskListener<NullResponse>() {
			@Override
			public void done(NullResponse output) {
				FeedComment comment = new FeedComment();

				comment.Id = null;
				comment.Nick = query.Nick;
				comment.Text = query.Message;
				comment.Time = BasePoco.timeNow();

				adapter.addItem(comment);
				adapter.notifyDataSetChanged();
			}
		});
		TaskManager.startTask(task, query);
	}

	/**
	 * 
	 * @author juraj
	 * 
	 */
	static class FeedCommentsAdapter extends BasePocoAdapter<FeedComment> {
		private ImageDownloader imageDownloader;

		public FeedCommentsAdapter(Activity context, ArrayList<FeedComment> model) {
			super(context, model);

			Drawable emptyAvatar = context.getResources().getDrawable(R.drawable.empty_avatar);

			imageDownloader = new ImageDownloader(context, emptyAvatar);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			View row = convertView;

			if (row == null) {
				row = context.getLayoutInflater().inflate(R.layout.feed_comment_row, parent, false);

				holder = new ViewHolder();
				holder.Thumbnail = (ImageView) row.findViewById(R.id.feed_comment_row_thumbnail);
				holder.Header = (TextView) row.findViewById(R.id.feed_comment_row_header);
				holder.Text = (TextView) row.findViewById(R.id.feed_comment_row_text);

				row.setTag(holder);
			} else {
				holder = (ViewHolder) row.getTag();
			}

			FeedComment comment = (FeedComment) getItem(position);

			imageDownloader.download(BasePoco.nickToUrl(comment.Nick), holder.Thumbnail);
			holder.Header.setText(CustomHtml.fromHtml(String.format("<b>%s</b> <small>%s</small>", comment.Nick, BasePoco.timeToString(context, comment.Time))));
			holder.Text.setText(CustomHtml.fromHtml(comment.Text));

			return row;
		}

		/**
		 * 
		 * @author juraj
		 * 
		 */
		static class ViewHolder {
			public ImageView Thumbnail;
			public TextView Header;
			public TextView Text;
		}
	}
}
