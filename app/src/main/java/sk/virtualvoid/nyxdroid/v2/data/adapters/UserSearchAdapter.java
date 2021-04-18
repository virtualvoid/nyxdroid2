package sk.virtualvoid.nyxdroid.v2.data.adapters;

import sk.virtualvoid.core.ImageDownloader;
import sk.virtualvoid.nyxdroid.v2.R;
import sk.virtualvoid.nyxdroid.v2.data.BasePoco;
import sk.virtualvoid.nyxdroid.v2.data.UserSearch;
import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * 
 * @author suchan_j
 * 
 */
public class UserSearchAdapter extends BasePocoAdapter<UserSearch> implements Filterable {
	private ImageDownloader imageDownloader;

	public UserSearchAdapter(AppCompatActivity context) {
		super(context);

		Drawable emptyAvatar = context.getResources().getDrawable(R.drawable.empty_avatar);

		imageDownloader = new ImageDownloader(context, emptyAvatar);
	}

	@Override
	public Filter getFilter() {
		return new Filter() {
			@Override
			protected FilterResults performFiltering(CharSequence constraint) {
				FilterResults filterResults = new FilterResults();

				filterResults.count = model.size();
				filterResults.values = model;

				return filterResults;
			}

			@Override
			protected void publishResults(CharSequence constraint, FilterResults results) {
				if (results != null && results.count > 0) {
					notifyDataSetChanged();
				} else {
					notifyDataSetInvalidated();
				}
			}
		};
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		View row = convertView;

		if (row == null) {
			row = context.getLayoutInflater().inflate(R.layout.enter_nick_row, parent, false);

			holder = new ViewHolder();
			holder.Thumbnail = (ImageView) row.findViewById(R.id.enter_nick_row_image);
			holder.Nick = (TextView) row.findViewById(R.id.enter_nick_row_nick);

			appearance.setFontSize(holder.Nick);

			row.setTag(holder);
		} else {
			holder = (ViewHolder) row.getTag();
		}

		UserSearch userSearch = (UserSearch) getItem(position);

		imageDownloader.download(BasePoco.nickToUrl(userSearch.Nick, context), holder.Thumbnail);
		holder.Nick.setText(userSearch.Nick);

		return row;
	}

	static class ViewHolder {
		public ImageView Thumbnail;
		public TextView Nick;
	}
}
