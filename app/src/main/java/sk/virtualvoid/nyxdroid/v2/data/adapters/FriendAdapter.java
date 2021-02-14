package sk.virtualvoid.nyxdroid.v2.data.adapters;

import java.util.ArrayList;
import java.util.Date;

import sk.virtualvoid.core.ImageDownloader;
import sk.virtualvoid.nyxdroid.v2.R;
import sk.virtualvoid.nyxdroid.v2.data.BasePoco;
import sk.virtualvoid.nyxdroid.v2.data.Friend;
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
public class FriendAdapter extends BasePocoAdapter<Friend> {
	private ImageDownloader imageDownloader;
	private Date now;

	public FriendAdapter(Activity context, ArrayList<Friend> model) {
		super(context, model);

		now = new Date();
		
		Drawable emptyAvatar = context.getResources().getDrawable(R.drawable.empty_avatar);
		
		imageDownloader = new ImageDownloader(context, emptyAvatar);
		//imageDownloader.setMode(Mode.CORRECT);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		ViewHolder holder = null;
		
		if (row == null) {
			holder = new ViewHolder();			
			row = context.getLayoutInflater().inflate(R.layout.friend_row, parent, false);
			
			holder.Thumbnail = (ImageView)row.findViewById(R.id.friend_row_thumbnail);
			holder.Nick = (TextView)row.findViewById(R.id.friend_row_nick);
			holder.Time = (TextView)row.findViewById(R.id.friend_row_time);
			holder.Location = (TextView)row.findViewById(R.id.friend_row_location);
			
			appearance.setFontSize(holder.Nick, holder.Location);
			
			row.setTag(holder);
		} else {
			holder = (ViewHolder) row.getTag();
		}
		
		Friend friend = (Friend) getItem(position);
		
		imageDownloader.download(BasePoco.nickToUrl(friend.Nick, context), holder.Thumbnail);
		
		holder.Nick.setText(friend.Nick);
		holder.Time.setText(friend.Location.toRelativeTimeSpanString(now));
		holder.Location.setText(friend.Location.Location);
		
		return row;
	}
	
	static class ViewHolder {
		public ImageView Thumbnail;
		public TextView Nick;
		public TextView Time;
		public TextView Location;
	}
}
