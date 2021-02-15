package sk.virtualvoid.nyxdroid.v2.data.adapters;

import java.util.ArrayList;

import sk.virtualvoid.core.ImageDownloader;
import sk.virtualvoid.nyxdroid.v2.R;
import sk.virtualvoid.nyxdroid.v2.data.BasePoco;
import sk.virtualvoid.nyxdroid.v2.data.Event;
import android.app.Activity;
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
public class EventAdapter extends BasePocoAdapter<Event> {
	private ImageDownloader imageDownloader;

	public EventAdapter(Activity context, ArrayList<Event> model) {
		super(context, model);

		Drawable emptyAvatar = context.getResources().getDrawable(R.drawable.empty_avatar);

		imageDownloader = new ImageDownloader(context, emptyAvatar);
		//imageDownloader.setMode(Mode.CORRECT);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		ViewHolder holder = null;

		if (row == null) {
			row = context.getLayoutInflater().inflate(R.layout.event_row, parent, false);

			holder = new ViewHolder();
			holder.Thumbnail = (ImageView) row.findViewById(R.id.event_row_thumbnail);
			holder.Nick = (TextView) row.findViewById(R.id.event_row_nick_owner);
			holder.Title = (TextView) row.findViewById(R.id.event_row_title);

			holder.Summary = (TextView) row.findViewById(R.id.event_row_summary);
			holder.Summary.setLinkTextColor(appearance.getLinkColor());

			holder.TimeStart = (TextView) row.findViewById(R.id.event_row_time_start);
			holder.TimeEnd = (TextView) row.findViewById(R.id.event_row_time_end);
			holder.UnreadMark = (ImageView) row.findViewById(R.id.event_row_unread_mark);

			appearance.setFontSize(holder.Nick, holder.Title, holder.Summary);

			row.setTag(holder);
		} else {
			holder = (ViewHolder) row.getTag();
		}

		Event data = (Event) getItem(position);

		imageDownloader.download(BasePoco.nickToUrl(data.Nick, context), holder.Thumbnail);

		holder.Nick.setText(data.Nick);
		holder.Title.setText(data.Title);
		holder.Summary.setText(data.Summary);
		holder.TimeStart.setText(BasePoco.timeToString(context, data.Time));
		holder.TimeEnd.setText(BasePoco.timeToString(context, data.TimeEnd));
		holder.UnreadMark.setVisibility(data.NewComments ? View.VISIBLE : View.INVISIBLE);

		return row;
	}

	static class ViewHolder {
		public ImageView Thumbnail;
		public TextView Nick;
		public TextView Title;
		public TextView Summary;
		public TextView TimeStart;
		public TextView TimeEnd;
		public ImageView UnreadMark;
	}
}
