package sk.virtualvoid.nyxdroid.v2.data.adapters;

import java.util.ArrayList;

import sk.virtualvoid.core.CustomHtml;
import sk.virtualvoid.core.ImageDownloader;
import sk.virtualvoid.nyxdroid.v2.R;
import sk.virtualvoid.nyxdroid.v2.data.BasePoco;
import sk.virtualvoid.nyxdroid.v2.data.Feed;
import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 
 * @author juraj
 * 
 */
public class FeedAdapter extends BasePocoAdapter<Feed> {
	private ImageDownloader imageDownloader;

	public FeedAdapter(Activity context, ArrayList<Feed> model) {
		super(context, model);

		Drawable emptyAvatar = context.getResources().getDrawable(R.drawable.empty_avatar);

		imageDownloader = new ImageDownloader(context, emptyAvatar);
		//imageDownloader.setMode(Mode.CORRECT);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		View row = convertView;

		if (row == null) {
			row = context.getLayoutInflater().inflate(R.layout.feed_row, parent, false);

			holder = new ViewHolder();
			holder.Thumbnail = (ImageView) row.findViewById(R.id.feed_row_thumbnail);
			holder.Nick = (TextView) row.findViewById(R.id.feed_row_nick);

			holder.Text = (TextView) row.findViewById(R.id.feed_row_text);
			holder.Text.setLinkTextColor(appearance.getLinkColor());
			holder.Text.setMovementMethod(LinkMovementMethod.getInstance());
			holder.Text.setFocusable(false);

			holder.Time = (TextView) row.findViewById(R.id.feed_row_time);
			holder.CommentsCount = (TextView) row.findViewById(R.id.feed_row_comments_count);

			appearance.setFontSize(holder.Nick, holder.Text);

			row.setTag(holder);
		} else {
			holder = (ViewHolder) row.getTag();
		}

		Feed feed = (Feed) getItem(position);

		imageDownloader.download(BasePoco.nickToUrl(feed.Nick, context), holder.Thumbnail);

		holder.Nick.setText(feed.Nick);
		holder.Text.setText(CustomHtml.fromHtml(feed.Text));
		holder.Time.setText(BasePoco.timeToString(context, feed.Time));
		holder.CommentsCount.setText(context.getResources().getString(R.string.feed_comments_count).replace("$1", Integer.toString(feed.CommentsCount)));

		return row;
	}

	/**
	 * 
	 * @author juraj
	 * 
	 */
	static class ViewHolder {
		public ImageView Thumbnail;
		public TextView Nick;
		public TextView Text;
		public TextView Time;
		public TextView CommentsCount;
	}
}
