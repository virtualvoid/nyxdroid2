package sk.virtualvoid.nyxdroid.v2.data.adapters;

import java.util.ArrayList;

import sk.virtualvoid.core.CustomHtml;
import sk.virtualvoid.core.ImageDownloader;
import sk.virtualvoid.core.ImageGetterAsync;
import sk.virtualvoid.nyxdroid.v2.R;
import sk.virtualvoid.nyxdroid.v2.data.BasePoco;
import sk.virtualvoid.nyxdroid.v2.data.Search;
import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * 
 * @author Juraj
 * 
 */
public class SearchAdapter extends BasePocoAdapter<Search> {
	private ImageDownloader imageDownloader;
	private ImageGetterAsync imageGetterAsync;

	public SearchAdapter(AppCompatActivity context, ArrayList<Search> model) {
		super(context, model);

		Drawable emptyAvatar = context.getResources().getDrawable(R.drawable.empty_avatar);

		imageDownloader = new ImageDownloader(context, emptyAvatar);
		//imageDownloader.setMode(Mode.CORRECT);

		imageGetterAsync = new ImageGetterAsync(context);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		ViewHolder holder = null;

		Search search = (Search) getItem(position);

		if (row == null) {
			holder = new ViewHolder();

			row = context.getLayoutInflater().inflate(R.layout.search_row, parent, false);
			holder.Discussion = (TextView) row.findViewById(R.id.search_row_discussion);
			holder.Time = (TextView) row.findViewById(R.id.search_row_time);

			holder.Content = (TextView) row.findViewById(R.id.search_row_text);
			holder.Content.setLinkTextColor(appearance.getLinkColor());

			holder.Nick = (TextView) row.findViewById(R.id.search_row_nick);
			holder.Thumbnail = (ImageView) row.findViewById(R.id.search_row_thumbnail);
			holder.Rating = (TextView) row.findViewById(R.id.search_row_rating);

			appearance.setFontSize(holder.Nick, holder.Content);

			row.setTag(holder);
		} else {
			holder = (ViewHolder) row.getTag();
		}

		imageDownloader.download(BasePoco.nickToUrl(search.Nick, context), holder.Thumbnail);

		holder.Discussion.setText(search.DiscussionName);
		holder.Nick.setText(search.Nick);

		holder.Content.setTag(position);
		holder.Content.setText(CustomHtml.fromHtml(search.Content, imageGetterAsync.spawn(position, search.Content, holder.Content)));

		holder.Time.setText(BasePoco.timeToString(context, search.Time));

		if (search.Rating != 0) {
			holder.Rating.setText(CustomHtml.fromHtml(String.format("<font color='%s'>%d</font>", search.Rating < 0 ? "red" : "green", search.Rating)));
		} else {
			holder.Rating.setText("");
		}

		return row;
	}

	static class ViewHolder {
		public TextView Discussion;
		public TextView Time;
		public TextView Content;
		public TextView Nick;
		public ImageView Thumbnail;
		public TextView Rating;
	}
}
