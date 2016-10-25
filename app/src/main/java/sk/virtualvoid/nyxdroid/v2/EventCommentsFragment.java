package sk.virtualvoid.nyxdroid.v2;

import java.util.ArrayList;

import sk.virtualvoid.core.CustomHtml;
import sk.virtualvoid.core.ImageDownloader;
import sk.virtualvoid.nyxdroid.v2.EventActivity.EventFragmentHandler;
import sk.virtualvoid.nyxdroid.v2.data.BasePoco;
import sk.virtualvoid.nyxdroid.v2.data.Event;
import sk.virtualvoid.nyxdroid.v2.data.EventComment;
import sk.virtualvoid.nyxdroid.v2.data.adapters.BasePocoAdapter;
import android.app.Activity;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

/**
 * 
 * @author Juraj
 * 
 */
public class EventCommentsFragment extends BaseFragment implements EventFragmentHandler {
	public static final String TAG = "evcomments";

	private ImageDownloader imageDownloader;
	private int linkColor;
	private ListView listView;

	public EventCommentsFragment() {
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		EventActivity parent = (EventActivity) activity;

		imageDownloader = parent.getImageDownloader();
		linkColor = parent.getLinkColor();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.generic_listview, container, false);
		return view;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		listView = (ListView) view.findViewById(R.id.list);
		listView.setEmptyView(view.findViewById(R.id.list_empty));
	}

	@Override
	public void setData(Event data, Object tag) {
		if (listView == null) {
			return;
		}

		listView.setAdapter(new CommentsAdapter(getActivity(), data.Comments));
	}

	private class CommentsAdapter extends BasePocoAdapter<EventComment> {
		public CommentsAdapter(Activity context, ArrayList<EventComment> model) {
			super(context, model);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View row = convertView;
			ViewHolder holder = null;

			if (row == null) {
				holder = new ViewHolder();

				row = context.getLayoutInflater().inflate(R.layout.event_comment_row, parent, false);
				holder.Thumbnail = (ImageView) row.findViewById(R.id.event_comment_row_thumbnail);
				holder.Header = (TextView) row.findViewById(R.id.event_comment_row_header);
				holder.Contents = (TextView) row.findViewById(R.id.event_comment_row_text);
				holder.Contents.setLinkTextColor(linkColor);
				
				//TODO: moznost pohybu po komentaroch v evente, treba v navigationHandleri citat aj wu ak tam je
				//holder.Contents.setMovementMethod(LinkMovementMethod.getInstance());
				//holder.Contents.setFocusable(false);

				row.setTag(holder);
			} else {
				holder = (ViewHolder) row.getTag();
			}

			EventComment comment = (EventComment) getItem(position);

			imageDownloader.download(BasePoco.nickToUrl(comment.Nick), holder.Thumbnail);

			holder.Header.setText(CustomHtml.fromHtml(String.format("<b>%s</b> <small>%s</small>", comment.Nick, BasePoco.timeToString(context, comment.Time))));
			holder.Contents.setText(CustomHtml.fromHtml(comment.Text));

			return row;
		}
	}

	private static class ViewHolder {
		public ImageView Thumbnail;
		public TextView Header;
		public TextView Contents;
	}
}
