package sk.virtualvoid.nyxdroid.v2.data.adapters;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Date;

import sk.virtualvoid.core.CustomHtml;
import sk.virtualvoid.core.ImageDownloader;
import sk.virtualvoid.core.ImageGetterAsync;
import sk.virtualvoid.core.ResponsibleBaseAdapter;
import sk.virtualvoid.nyxdroid.v2.R;
import sk.virtualvoid.nyxdroid.v2.data.BasePoco;
import sk.virtualvoid.nyxdroid.v2.data.Last;
import sk.virtualvoid.nyxdroid.v2.data.Writeup;
import sk.virtualvoid.nyxdroid.v2.internal.IVotingHandler;
import sk.virtualvoid.nyxdroid.v2.internal.IWriteupSelectionHandler;
import sk.virtualvoid.nyxdroid.v2.internal.VotingType;

/**
 * @author Juraj
 */
public class LastAdapter extends BasePocoAdapter<Last> implements ResponsibleBaseAdapter {
    private ImageDownloader imageDownloader;
    private ImageGetterAsync imageGetterAsync;
    private boolean displayVotingThumbs;
    private boolean selectionVisible;
    private Date now;
    private String spoilerAlert;

    public LastAdapter(AppCompatActivity context, ArrayList<Last> model) {
        super(context, model);

        now = new Date();

        Drawable emptyAvatar = context.getResources().getDrawable(R.drawable.empty_avatar);

        spoilerAlert = context.getResources().getString(R.string.spoiler_alert);

        imageDownloader = new ImageDownloader(context, emptyAvatar);
        imageGetterAsync = new ImageGetterAsync(context);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        displayVotingThumbs = prefs.getBoolean("display_voting_thumbs", true);

        selectionVisible = false;
    }

    public ImageGetterAsync getImageGetterAsync() {
        return imageGetterAsync;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ViewHolder holder = null;

        if (row == null) {
            row = context.getLayoutInflater().inflate(R.layout.writeup_row, parent, false);

            holder = new ViewHolder();
            holder.Thumbnail = (ImageView) row.findViewById(R.id.writeup_row_thumbnail);
            holder.Nick = (TextView) row.findViewById(R.id.writeup_row_nick);

            holder.Content = (TextView) row.findViewById(R.id.writeup_row_text);
            holder.Content.setLinkTextColor(appearance.getLinkColor());
            holder.Content.setMovementMethod(LinkMovementMethod.getInstance());
            holder.Content.setFocusable(false);

            holder.Time = (TextView) row.findViewById(R.id.writeup_row_time);
            holder.Rating = (TextView) row.findViewById(R.id.writeup_row_rating);
            holder.Location = (TextView) row.findViewById(R.id.writeup_row_location);

            ((ImageButton) row.findViewById(R.id.writeup_row_voteup)).setVisibility(View.INVISIBLE);
            ((ImageButton) row.findViewById(R.id.writeup_row_votedown)).setVisibility(View.INVISIBLE);
            ((ImageView) row.findViewById(R.id.writeup_row_unread_mark)).setVisibility(View.INVISIBLE);
            ((CheckBox) row.findViewById(R.id.writeup_row_selected)).setVisibility(View.INVISIBLE);

            //appearance.setFontSize(holder.Nick, holder.Content);

            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }

        final Writeup wu = (Writeup) getItem(position);

        if (!appearance.isEntryUnreadColorStandard()) {
            holder.Nick.setTextColor(appearance.getUseDarkTheme() ? Color.WHITE : Color.BLACK);
            holder.Time.setTextColor(appearance.getUseDarkTheme() ? Color.WHITE : Color.BLACK);
        }

        imageDownloader.download(BasePoco.nickToUrl(wu.Nick, context), holder.Thumbnail);

        holder.Nick.setText(wu.Nick);

        holder.Content.setTag(position);

        if (wu.spoilerPresent()) {
            holder.Content.setText(CustomHtml.fromHtml(String.format("<font color='#666666'>%s</font>", spoilerAlert)));
        } else {
            holder.Content.setText(CustomHtml.fromHtml(wu.Content, imageGetterAsync.spawn(position, wu.Content, holder.Content)));
        }

        holder.Time.setText(BasePoco.timeToString(context, wu.Time));

        if (wu instanceof Last) {
            holder.Location.setVisibility(View.VISIBLE);
            holder.Location.setText(((Last)wu).DiscussionName);
        } else {
            if (wu.Location != null && wu.Location.valid()) {
                holder.Location.setVisibility(View.VISIBLE);
                holder.Location.setText(wu.Location.toString(now));
            } else {
                holder.Location.setVisibility(View.INVISIBLE);
            }
        }

        if (wu.Rating != 0) {
            holder.Rating.setText(CustomHtml.fromHtml(String.format("<font color='%s'>%d</font>", wu.Rating < 0 ? appearance.getVoteNegativeColor() : appearance.getVotePositiveColor(), wu.Rating)));
        } else {
            holder.Rating.setText("");
        }

        holder.setId(wu.Id);

        return row;
    }

    /**
     * @author Juraj
     */
    static class ViewHolder implements InformedViewHolder {
        public ImageView Thumbnail;
        public TextView Nick;
        public TextView Content;
        public TextView Time;
        public TextView Rating;
        public TextView Location;

        private long id;

        public void setId(long id) {
            this.id = id;
        }

        @Override
        public long getId() {
            return this.id;
        }
    }
}
