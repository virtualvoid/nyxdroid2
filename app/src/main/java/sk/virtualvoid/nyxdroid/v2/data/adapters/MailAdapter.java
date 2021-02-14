package sk.virtualvoid.nyxdroid.v2.data.adapters;

import java.util.ArrayList;
import java.util.Date;

import sk.virtualvoid.core.CustomHtml;
import sk.virtualvoid.core.ImageDownloader;
import sk.virtualvoid.core.ImageGetterAsync;
import sk.virtualvoid.nyxdroid.library.Constants;
import sk.virtualvoid.nyxdroid.v2.R;
import sk.virtualvoid.nyxdroid.v2.data.BasePoco;
import sk.virtualvoid.nyxdroid.v2.data.Mail;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 
 * @author juraj
 * 
 */
public class MailAdapter extends BasePocoAdapter<Mail> {
	private ImageDownloader imageDownloader;
	private ImageGetterAsync imageGetterAsync;
	private Date now;

	public MailAdapter(Activity context, ArrayList<Mail> model) {
		super(context, model);

		now = new Date();

		Drawable emptyAvatar = context.getResources().getDrawable(R.drawable.empty_avatar);

		imageDownloader = new ImageDownloader(context, emptyAvatar);
		//imageDownloader.setMode(Mode.CORRECT);

		imageGetterAsync = new ImageGetterAsync(context);
	}

	@Override
	public int getItemViewType(int position) {
		return ((Mail) getItem(position)).Direction.equals(Constants.FROM) ? Constants.FROM_ID : Constants.TO_ID;
	}

	@Override
	public int getViewTypeCount() {
		return 2;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		ViewHolder holder = null;

		if (row == null) {
			if (getItemViewType(position) == Constants.FROM_ID) {
				row = context.getLayoutInflater().inflate(R.layout.mail_row_from, parent, false);
			} else {
				row = context.getLayoutInflater().inflate(R.layout.mail_row_to, parent, false);
			}

			holder = new ViewHolder();
			holder.Thumbnail = (ImageView) row.findViewById(R.id.mail_row_thumbnail);
			holder.Nick = (TextView) row.findViewById(R.id.mail_row_nick);

			holder.Content = (TextView) row.findViewById(R.id.mail_row_text);
			holder.Content.setLinkTextColor(appearance.getLinkColor());
			holder.Content.setMovementMethod(LinkMovementMethod.getInstance());
			holder.Content.setFocusable(false);

			holder.Time = (TextView) row.findViewById(R.id.mail_row_time);
			holder.Location = (TextView) row.findViewById(R.id.mail_row_location);
			holder.UnreadMarker = (ImageView) row.findViewById(R.id.mail_row_unread_mark);

			appearance.setFontSize(holder.Nick, holder.Content);

			row.setTag(holder);
		} else {
			holder = (ViewHolder) row.getTag();
		}

		Mail mail = (Mail) getItem(position);

		if (appearance.isEntryUnreadColorStandard()) {
			if (mail.IsUnread || !mail.OtherSawIt) {
				holder.UnreadMarker.setVisibility(View.VISIBLE);
			} else {
				holder.UnreadMarker.setVisibility(View.INVISIBLE);
			}
		} else {
			holder.UnreadMarker.setVisibility(View.INVISIBLE);
			
			if (mail.IsUnread || !mail.OtherSawIt) {
				holder.Nick.setTextColor(appearance.getEntryUnreadColor());
				holder.Time.setTextColor(appearance.getEntryUnreadColor());
			} else {
				holder.Nick.setTextColor(appearance.getUseDarkTheme() ? Color.WHITE : Color.BLACK);
				holder.Time.setTextColor(appearance.getUseDarkTheme() ? Color.WHITE : Color.BLACK);
			}
		}

		imageDownloader.download(BasePoco.nickToUrl(mail.Nick, context), holder.Thumbnail);

		holder.Nick.setText(mail.Nick);
		holder.Time.setText(BasePoco.timeToString(context, mail.Time));

		holder.Content.setTag(position);
		holder.Content.setText(CustomHtml.fromHtml(mail.Content, imageGetterAsync.spawn(position, mail.Content, holder.Content)));

		if (mail.Location != null && mail.Location.valid()) {
			holder.Location.setVisibility(View.VISIBLE);
			holder.Location.setText(mail.Location.toString(now));
		} else {
			holder.Location.setVisibility(View.INVISIBLE);
		}

		return row;
	}

	/**
	 * 
	 * @author juraj
	 * 
	 */
	static class ViewHolder {
		public ImageView Thumbnail;
		public TextView Nick;
		public TextView Content;
		public TextView Time;
		public TextView Location;
		public ImageView UnreadMarker;
	}
}
