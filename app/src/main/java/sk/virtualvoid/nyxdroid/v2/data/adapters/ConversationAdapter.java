package sk.virtualvoid.nyxdroid.v2.data.adapters;

import java.util.ArrayList;
import java.util.Collection;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import sk.virtualvoid.core.ImageDownloader;
import sk.virtualvoid.nyxdroid.v2.R;
import sk.virtualvoid.nyxdroid.v2.data.BasePoco;
import sk.virtualvoid.nyxdroid.v2.data.Conversation;

/**
 * 
 * @author suchan_j
 * 
 */
public class ConversationAdapter extends BaseAdapter {
	private AppCompatActivity context;
	private LayoutInflater layoutInflater;
	private ArrayList<Conversation> model;
	private ImageDownloader imageDownloader;

	public ConversationAdapter(AppCompatActivity context) {
		this.context = context;
		
		model = new ArrayList<Conversation>();

		layoutInflater = LayoutInflater.from(context);
		
		Drawable emptyAvatar = context.getResources().getDrawable(R.drawable.empty_avatar);
		imageDownloader = new ImageDownloader(context, emptyAvatar);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		ViewHolder holder = null;

		if (row == null) {
			row = layoutInflater.inflate(R.layout.conversation_row, parent, false);

			holder = new ViewHolder();
			holder.Thumnbail = (ImageView) row.findViewById(R.id.conversation_row_thumbnail);
			holder.Nick = (TextView) row.findViewById(R.id.conversation_row_nick);
			holder.Time = (TextView) row.findViewById(R.id.conversation_row_time);
			holder.Direction = (TextView) row.findViewById(R.id.conversation_row_direction);
			
			row.setTag(holder);
		} else {
			holder = (ViewHolder) row.getTag();
		}
		
		Conversation item = (Conversation) getItem(position);
		
		imageDownloader.download(BasePoco.nickToUrl(item.Nick, context), holder.Thumnbail);
		
		holder.Time.setText(BasePoco.timeToString(context, item.Time));
		holder.Nick.setText(item.Nick);

		if (item.Direction.equalsIgnoreCase("from")) {
			holder.Direction.setText(" <<<");
		} else {
			holder.Direction.setText(" >>>");
		}
		
		return row;
	}

	@Override
	public int getCount() {
		return model.size();
	}

	@Override
	public Object getItem(int position) {
		return model.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public void addItem(Conversation conversation) {
		model.add(conversation);
	}

	public void addItems(Collection<Conversation> conversations) {
		model.addAll(conversations);
	}

	public void clearItems() {
		model.clear();
	}

	static class ViewHolder {
		ImageView Thumnbail;
		TextView Nick;
		TextView Time;
		TextView Direction;
	}
}
