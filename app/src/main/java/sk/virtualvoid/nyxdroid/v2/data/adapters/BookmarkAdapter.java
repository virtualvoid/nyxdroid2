package sk.virtualvoid.nyxdroid.v2.data.adapters;

import java.util.ArrayList;

import sk.virtualvoid.core.CustomHtml;
import sk.virtualvoid.nyxdroid.v2.R;
import sk.virtualvoid.nyxdroid.v2.data.Bookmark;
import sk.virtualvoid.nyxdroid.v2.data.BookmarkCategory;
import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * 
 * @author Juraj
 * 
 */
public class BookmarkAdapter extends BasePocoAdapter<Bookmark> {

	public BookmarkAdapter(Activity context, ArrayList<Bookmark> model) {
		super(context, model);
	}

	public void clearCategory(long categoryId) {
		ArrayList<Bookmark> temp = new ArrayList<Bookmark>(model);
		for (Bookmark bookmark : temp) {
			if (!(bookmark instanceof BookmarkCategory) && bookmark.CategoryId == categoryId) {
				model.remove(bookmark);
			}
		}
	}

	public void replaceCategory(long categoryId, ArrayList<Bookmark> output) {
		clearCategory(categoryId);

		for (int i = 0; i < model.size(); i++) {
			Bookmark bookmark = model.get(i);

			if (bookmark instanceof BookmarkCategory && bookmark.Id == categoryId) {
				for (int j = 0; j < output.size(); j++) {
					Bookmark newBookmark = output.get(j);

					model.add(i + 1 + j, newBookmark);
				}
			}
		}
	}

	@Override
	public int getViewTypeCount() {
		return 2;
	}

	@Override
	public int getItemViewType(int position) {
		if (getItem(position) instanceof BookmarkCategory) {
			return 0;
		}
		return 1;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;

		if (getItem(position) instanceof BookmarkCategory) {
			ViewHolderCategory holder = null;

			if (row == null) {
				row = context.getLayoutInflater().inflate(R.layout.bookmark_category_row, parent, false);
				row.setPadding(appearance.getBookmarksPadding(), appearance.getBookmarksPadding(), appearance.getBookmarksPadding(), appearance.getBookmarksPadding());
				row.setBackgroundColor(appearance.getCategoryBackgroundColor());

				holder = new ViewHolderCategory();
				holder.Title = (TextView) row.findViewById(R.id.bookmark_category_row_title);
				
				appearance.setFontSize(holder.Title);
				
				row.setTag(holder);
			} else {
				holder = (ViewHolderCategory) row.getTag();
			}

			BookmarkCategory obj = (BookmarkCategory) getItem(position);

			holder.Title.setText(obj.Name);
		} else {
			ViewHolder holder = null;

			if (row == null) {
				row = context.getLayoutInflater().inflate(R.layout.bookmark_row, parent, false);
				row.setPadding(appearance.getBookmarksPadding(), appearance.getBookmarksPadding(), appearance.getBookmarksPadding(), appearance.getBookmarksPadding());
				
				holder = new ViewHolder();
				holder.UnreadCount = (TextView) row.findViewById(R.id.bookmark_row_unread_count);
				holder.Title = (TextView) row.findViewById(R.id.bookmark_row_title);

				appearance.setFontSize(holder.UnreadCount, holder.Title);
				
				row.setTag(holder);
			} else {
				holder = (ViewHolder) row.getTag();
			}

			Bookmark obj = (Bookmark) getItem(position);

			if (obj.Unread > 0) {
				holder.UnreadCount.setText(Integer.toString(obj.Unread));
				holder.Title.setTextColor(appearance.getBookmarkUnreadColor());
			} else {
				holder.UnreadCount.setText("");
				holder.Title.setTextColor(appearance.getBookmarkReadColor());
			}

			if (obj.Replies > 0) {
				holder.Title.setText(CustomHtml.fromHtml(String.format("%s <small><b>%d repl%s</b></small>", obj.Name, obj.Replies, obj.Replies == 1 ? "y" : "ies")));
			} else {
				holder.Title.setText(obj.Name);
			}
		}

		return row;
	}

	/**
	 * 
	 * @author Juraj
	 * 
	 */
	static class ViewHolder {
		public TextView UnreadCount;
		public TextView Title;
	}

	/**
	 * 
	 * @author Juraj
	 * 
	 */
	static class ViewHolderCategory {
		public TextView Title;
	}
}
