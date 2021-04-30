package sk.virtualvoid.nyxdroid.v2.data.adapters;

import java.util.ArrayList;

import sk.virtualvoid.core.CustomHtml;
import sk.virtualvoid.nyxdroid.v2.R;
import sk.virtualvoid.nyxdroid.v2.data.BasePoco;
import sk.virtualvoid.nyxdroid.v2.data.Bookmark;
import sk.virtualvoid.nyxdroid.v2.data.BookmarkCategory;
import sk.virtualvoid.nyxdroid.v2.data.BookmarkReminder;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.LayoutInflaterCompat;

/**
 * @author Juraj
 */
public class BookmarkAdapter extends BasePocoAdapter<Bookmark> {

    public BookmarkAdapter(AppCompatActivity context, ArrayList<Bookmark> model) {
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
        return 3;
    }

    @Override
    public int getItemViewType(int position) {
        if (getItem(position) instanceof BookmarkCategory) {
            return 0;
        }
        if (getItem(position) instanceof BookmarkReminder) {
            return 2;
        }
        // bookmark reminder
        return 1;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;

        Object item = getItem(position);
        int itemType = getItemViewType(position);

        if (itemType == 0) {
            row = getCategoryView(parent, row, (BookmarkCategory) item);
        } else if (itemType == 1) {
            row = getBookmarkView(parent, row, (Bookmark) item);
        } else if (itemType == 2) {
            row = getReminderView(parent, row, (BookmarkReminder) item);
        }
        return row;
    }

    private View getBookmarkView(ViewGroup parent, View row, Bookmark item) {
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

        if (item.Unread > 0) {
            holder.UnreadCount.setText(Integer.toString(item.Unread));
            holder.Title.setTextColor(appearance.getBookmarkUnreadColor());
        } else {
            holder.UnreadCount.setText("");
            holder.Title.setTextColor(appearance.getBookmarkReadColor());
        }

        if (item.Replies > 0) {
            holder.Title.setText(CustomHtml.fromHtml(String.format("%s <small><b>%d repl%s</b></small>", item.Name, item.Replies, item.Replies == 1 ? "y" : "ies")));
        } else {
            holder.Title.setText(item.Name);
        }
        return row;
    }

    private View getReminderView(ViewGroup parent, View row, BookmarkReminder item) {
        ViewHolderReminder holder = null;

        if (row == null) {
            row = context.getLayoutInflater().inflate(R.layout.bookmark_reminder_row, parent, false);
            row.setPadding(appearance.getBookmarksPadding(), appearance.getBookmarksPadding(), appearance.getBookmarksPadding(), appearance.getBookmarksPadding());

            holder = new ViewHolderReminder();
            holder.Title = (TextView) row.findViewById(R.id.bookmark_reminder_row_title);
            holder.RepliesCount = (TextView) row.findViewById(R.id.bookmark_reminder_row_reply_count);

            appearance.setFontSize(holder.Title);

            row.setTag(holder);
        } else {
            holder = (ViewHolderReminder) row.getTag();
        }

        holder.Title.setText(CustomHtml.fromHtml(String.format("<b>%s</b> <small>%s</small>", item.Nick, BasePoco.timeToString(context, item.Time))));
//        if (item.Replies > 0) {
//            holder.RepliesCount.setText(Integer.toString(item.Replies));
//        } else {
            holder.RepliesCount.setText("");
//        }

        holder.Title.setTextColor(appearance.getBookmarkReadColor());
        return row;
    }

    private View getCategoryView(ViewGroup parent, View row, BookmarkCategory item) {
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

        holder.Title.setText(item.Name);
        return row;
    }

    static class ViewHolder {
        public TextView UnreadCount;
        public TextView Title;
    }

    static class ViewHolderCategory {
        public TextView Title;
    }

    static class ViewHolderReminder {
        public TextView Title;
        public TextView RepliesCount;
    }
}
