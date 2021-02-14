package sk.virtualvoid.nyxdroid.v2.data.adapters;

import java.util.ArrayList;
import java.util.Date;

import sk.virtualvoid.core.CustomHtml;
import sk.virtualvoid.core.ImageDownloader;
import sk.virtualvoid.core.ImageGetterAsync;
import sk.virtualvoid.core.ResponsibleBaseAdapter;
import sk.virtualvoid.nyxdroid.v2.R;
import sk.virtualvoid.nyxdroid.v2.data.BasePoco;
import sk.virtualvoid.nyxdroid.v2.data.Writeup;
import sk.virtualvoid.nyxdroid.v2.internal.IWriteupSelectionHandler;
import sk.virtualvoid.nyxdroid.v2.internal.IVotingHandler;
import sk.virtualvoid.nyxdroid.v2.internal.VotingType;
import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 
 * @author Juraj
 * 
 */
public class WriteupAdapter extends BasePocoAdapter<Writeup> implements ResponsibleBaseAdapter {
	private ImageDownloader imageDownloader;
	private ImageGetterAsync imageGetterAsync;
	private boolean displayVotingThumbs;
	private boolean selectionVisible;
	private Date now;
	private String spoilerAlert;

	public WriteupAdapter(Activity context, ArrayList<Writeup> model) {
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
	
	public boolean getSelectionVisible() {
		return selectionVisible;
	}
	
	public void setSelectionVisible(boolean selectionVisible) {
		this.selectionVisible = selectionVisible;
	}
	
	public void setIsSelectedForAll(boolean isSelected) {
		if (model.size() > 0) {
			for (int i = model.size() - 1; i >= 0; i--) {
				Writeup wu = model.get(i);
				wu.IsSelected = isSelected;
			}
		}
	}

	public ImageGetterAsync getImageGetterAsync() {
		return imageGetterAsync;
	}

	public int getLastUnreadIndex() {
		int lastIndex = 0;

		if (model.size() > 0) {
			for (int i = model.size() - 1; i >= 0; i--) {
				Writeup wu = model.get(i);
				if (wu.Unread) {
					lastIndex = i;
					break;
				}
			}
		}

		return lastIndex;
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

			holder.VoteUp = (ImageButton) row.findViewById(R.id.writeup_row_voteup);
			holder.VoteUp.setFocusable(false);
			holder.VoteUp.setFocusableInTouchMode(false);

			holder.VoteDown = (ImageButton) row.findViewById(R.id.writeup_row_votedown);
			holder.VoteDown.setFocusable(false);
			holder.VoteDown.setFocusableInTouchMode(false);

			holder.UnreadMarker = (ImageView) row.findViewById(R.id.writeup_row_unread_mark);

			holder.Selected = (CheckBox) row.findViewById(R.id.writeup_row_selected);

			if (!displayVotingThumbs) {
				holder.Rating.setVisibility(View.GONE);
				holder.VoteUp.setVisibility(View.GONE);
				holder.VoteDown.setVisibility(View.GONE);
			}

			appearance.setFontSize(holder.Nick, holder.Content);

			row.setTag(holder);
		} else {
			holder = (ViewHolder) row.getTag();
		}

		final Writeup wu = (Writeup) getItem(position);

		if (appearance.isEntryUnreadColorStandard()) {
			if (wu.Unread) {
				holder.UnreadMarker.setVisibility(View.VISIBLE);
			} else {
				holder.UnreadMarker.setVisibility(View.INVISIBLE);
			}
		} else {
			holder.UnreadMarker.setVisibility(View.INVISIBLE);

			if (wu.Unread) {
				holder.Nick.setTextColor(appearance.getEntryUnreadColor());
				holder.Time.setTextColor(appearance.getEntryUnreadColor());
			} else {
				holder.Nick.setTextColor(appearance.getUseDarkTheme() ? Color.WHITE : Color.BLACK);
				holder.Time.setTextColor(appearance.getUseDarkTheme() ? Color.WHITE : Color.BLACK);
			}
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

		if (wu.Location != null && wu.Location.valid()) {
			holder.Location.setVisibility(View.VISIBLE);
			holder.Location.setText(wu.Location.toString(now));
		} else {
			holder.Location.setVisibility(View.INVISIBLE);
		}

		if (displayVotingThumbs) {
			if (wu.Rating != 0) {
				holder.Rating.setText(CustomHtml.fromHtml(String.format("<font color='%s'>%d</font>", wu.Rating < 0 ? appearance.getVoteNegativeColor() : appearance.getVotePositiveColor(), wu.Rating)));
			} else {
				holder.Rating.setText("");
			}

			if (wu.IsMine) {
				holder.VoteUp.setVisibility(View.INVISIBLE);
				holder.VoteDown.setVisibility(View.INVISIBLE);
			} else {
				holder.VoteUp.setVisibility(View.VISIBLE);
				holder.VoteDown.setVisibility(View.VISIBLE);
			}
		}
		
		if (context instanceof IVotingHandler) {
			holder.VoteUp.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					((IVotingHandler) context).onVote(position, VotingType.POSITIVE);
				}
			});

			holder.VoteDown.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					((IVotingHandler) context).onVote(position, VotingType.NEGATIVE);
				}
			});
		}

		if (selectionVisible) {
			holder.Selected.setVisibility(View.VISIBLE);
		} else {
			holder.Selected.setVisibility(View.GONE);
		}

		if (context instanceof IWriteupSelectionHandler) {
			holder.Selected.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					((IWriteupSelectionHandler)context).onSelectionChanged(wu, isChecked);
				}
			});
		}

		holder.Selected.setChecked(wu.IsSelected);
		
		holder.setId(wu.Id);

		return row;
	}

	/**
	 * 
	 * @author Juraj
	 * 
	 */
	static class ViewHolder implements InformedViewHolder {
		public ImageView Thumbnail;
		public TextView Nick;
		public TextView Content;
		public TextView Time;
		public TextView Rating;
		public TextView Location;
		public ImageButton VoteUp;
		public ImageButton VoteDown;
		public ImageView UnreadMarker;
		public CheckBox Selected;

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
