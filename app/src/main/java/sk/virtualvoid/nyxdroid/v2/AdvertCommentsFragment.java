package sk.virtualvoid.nyxdroid.v2;

import java.util.ArrayList;

import sk.virtualvoid.core.CustomHtml;
import sk.virtualvoid.core.ImageDownloader;
import sk.virtualvoid.nyxdroid.v2.AdvertActivity.AdvertFragmentHandler;
import sk.virtualvoid.nyxdroid.v2.data.Advert;
import sk.virtualvoid.nyxdroid.v2.data.AdvertComment;
import sk.virtualvoid.nyxdroid.v2.data.BasePoco;
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
public class AdvertCommentsFragment extends BaseFragment implements AdvertFragmentHandler {
	public static final String TAG = "adcomments";
	
	private ImageDownloader imageDownloader;
	private int linkColor; 
	private ListView listView;
	
	public AdvertCommentsFragment() {
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		AdvertActivity parent = (AdvertActivity)activity;
		
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
	public void setData(Advert data) {
		if (listView == null) {
			return;
		}
		
		listView.setAdapter(new CommentsAdapter(getActivity(), data.getCommentsOrDefault()));
	}
	
	private class CommentsAdapter extends BasePocoAdapter<AdvertComment> {
		public CommentsAdapter(Activity context, ArrayList<AdvertComment> model) {
			super(context, model);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View row = convertView;
			
			ViewHolder holder = null;

			if (row == null) {
				holder = new ViewHolder();

				row = context.getLayoutInflater().inflate(R.layout.advert_comment_row, parent, false);
				holder.Thumbnail = (ImageView) row.findViewById(R.id.advert_comment_row_thumbnail);
				holder.Header = (TextView) row.findViewById(R.id.advert_comment_row_header);
				holder.Contents = (TextView) row.findViewById(R.id.advert_comment_row_text);
				holder.Contents.setLinkTextColor(linkColor);
				holder.Contents.setMovementMethod(LinkMovementMethod.getInstance());
				holder.Contents.setFocusable(false);

				row.setTag(holder);
			} else {
				holder = (ViewHolder) row.getTag();
			}

			AdvertComment comment = (AdvertComment) getItem(position);

			imageDownloader.download(BasePoco.nickToUrl(comment.Nick), holder.Thumbnail);

			holder.Header.setText(CustomHtml.fromHtml(String.format("<b>%s</b> <small>%s</small>", comment.Nick, BasePoco.timeToString(context, comment.Time))));
			holder.Contents.setText(CustomHtml.fromHtml(comment.Content));
			
			return row;
		}
	}
	
	private static class ViewHolder {
		public ImageView Thumbnail;
		public TextView Header;
		public TextView Contents;
	}
}
