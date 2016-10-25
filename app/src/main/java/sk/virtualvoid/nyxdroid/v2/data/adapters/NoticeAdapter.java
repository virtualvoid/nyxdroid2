package sk.virtualvoid.nyxdroid.v2.data.adapters;

import java.util.ArrayList;

import sk.virtualvoid.core.CustomHtml;
import sk.virtualvoid.core.ImageDownloader;
import sk.virtualvoid.core.ImageGetterAsync;
import sk.virtualvoid.nyxdroid.v2.R;
import sk.virtualvoid.nyxdroid.v2.data.BasePoco;
import sk.virtualvoid.nyxdroid.v2.data.Notice;
import sk.virtualvoid.nyxdroid.v2.data.NoticeType;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 
 * @author Juraj
 * 
 */
public class NoticeAdapter extends BasePocoAdapter<Notice> {
	private ImageDownloader imageDownloader;
	private ImageGetterAsync imageGetterAsync;

	public NoticeAdapter(Activity context, ArrayList<Notice> model) {
		super(context, model);

		Drawable emptyAvatar = context.getResources().getDrawable(R.drawable.empty_avatar);

		imageDownloader = new ImageDownloader(context, emptyAvatar);
		//imageDownloader.setMode(Mode.CORRECT);

		imageGetterAsync = new ImageGetterAsync(context);
	}

	@Override
	public int getViewTypeCount() {
		return 3;
	}

	@Override
	public int getItemViewType(int position) {
		Notice notice = (Notice) getItem(position);

		switch (notice.Type) {
			case NOTICE:
				return 0;
			case REPLY:
				return 1;
			case THUMBS:
				return 2;
		}

		return -1;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		Notice notice = (Notice) getItem(position);

		if (notice.Type == NoticeType.NOTICE) {
			NoticeViewHolder holder = null;

			if (row == null) {
				row = context.getLayoutInflater().inflate(R.layout.notice_row, parent, false);

				holder = new NoticeViewHolder();
				holder.Thumbnail = (ImageView) row.findViewById(R.id.notice_row_thumbnail);
				holder.Nick = (TextView) row.findViewById(R.id.notice_row_nick);

				holder.Content = (TextView) row.findViewById(R.id.notice_row_text);
				holder.Content.setLinkTextColor(appearance.getLinkColor());

				holder.Time = (TextView) row.findViewById(R.id.notice_row_time);

				appearance.setFontSize(holder.Nick, holder.Content);

				row.setTag(holder);
			} else {
				holder = (NoticeViewHolder) row.getTag();
			}

			imageDownloader.download(BasePoco.nickToUrl(notice.Nick), holder.Thumbnail);

			holder.Nick.setText(notice.Nick);
			holder.Time.setText(BasePoco.timeToString(context, notice.Time));

			holder.Content.setTag(position);
			holder.Content.setText(CustomHtml.fromHtml(notice.Content, imageGetterAsync.spawn(position, notice.Content, holder.Content)));
		}

		if (notice.Type == NoticeType.REPLY) {
			ReplyViewHolder holder = null;

			if (row == null) {
				row = context.getLayoutInflater().inflate(R.layout.notice_reply_row, parent, false);

				holder = new ReplyViewHolder();
				holder.Indent = row.findViewById(R.id.notice_row_reply_indent);
				holder.Thumbnail = (ImageView) row.findViewById(R.id.notice_row_reply_entry_image);
				holder.Content = (TextView) row.findViewById(R.id.notice_row_reply_entry_content);
				holder.Content.setLinkTextColor(appearance.getLinkColor());

				appearance.setFontSize(holder.Content);

				row.setTag(holder);
			} else {
				holder = (ReplyViewHolder) row.getTag();
			}

			imageDownloader.download(BasePoco.nickToUrl(notice.Nick), holder.Thumbnail);

			if (notice.IsNew) {
				holder.Indent.setBackgroundColor(Color.parseColor("#cacaca"));
				holder.Indent.setVisibility(View.VISIBLE);
			} else {
				holder.Indent.setVisibility(View.INVISIBLE);
			}

			holder.Content.setTag(position);
			holder.Content.setText(CustomHtml.fromHtml(notice.Content, imageGetterAsync.spawn(position, notice.Content, holder.Content)));
		}

		if (notice.Type == NoticeType.THUMBS) {
			ThumbsViewHolder holder = null;

			if (row == null) {
				row = context.getLayoutInflater().inflate(R.layout.notice_thumbs_row, parent, false);

				holder = new ThumbsViewHolder();
				holder.Count = (TextView) row.findViewById(R.id.notice_row_thumb_count);

				row.setTag(holder);
			} else {
				holder = (ThumbsViewHolder) row.getTag();
			}

			holder.Count.setText(String.format("+%d", notice.Thumbs));
		}

		return row;
	}

	/**
	 * 
	 * @author Juraj
	 * 
	 */
	static class NoticeViewHolder {
		public ImageView Thumbnail;
		public TextView Nick;
		public TextView Content;
		public TextView Time;
	}

	/**
	 * 
	 * @author Juraj
	 * 
	 */
	static class ThumbsViewHolder {
		public TextView Count;
	}

	/**
	 * 
	 * @author Juraj
	 * 
	 */
	static class ReplyViewHolder {
		public View Indent;
		public ImageView Thumbnail;
		public TextView Content;
	}
}
