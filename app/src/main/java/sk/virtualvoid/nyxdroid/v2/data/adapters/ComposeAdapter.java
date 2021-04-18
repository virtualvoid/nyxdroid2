package sk.virtualvoid.nyxdroid.v2.data.adapters;

import java.util.ArrayList;

import android.app.Activity;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import sk.virtualvoid.core.CustomHtml;
import sk.virtualvoid.core.ImageDownloader;
import sk.virtualvoid.core.ImageGetterAsync;
import sk.virtualvoid.nyxdroid.library.Constants;
import sk.virtualvoid.nyxdroid.v2.R;
import sk.virtualvoid.nyxdroid.v2.data.BaseComposePoco;
import sk.virtualvoid.nyxdroid.v2.data.BasePoco;
import sk.virtualvoid.nyxdroid.v2.data.TypedPoco;
import sk.virtualvoid.nyxdroid.v2.data.Writeup;
import sk.virtualvoid.nyxdroid.v2.data.TypedPoco.Type;
import sk.virtualvoid.nyxdroid.v2.data.Attachment;

/**
 * 
 * @author Juraj
 * 
 */
@SuppressWarnings("unchecked")
public class ComposeAdapter<TComposePoco extends BaseComposePoco> extends BasePocoAdapter<TypedPoco<?>> {
	private ImageDownloader imageDownloader;
	private ImageGetterAsync imageGetterAsync;
	
	public ComposeAdapter(AppCompatActivity context, ArrayList<TypedPoco<?>> model, ImageDownloader imageDownloader, ImageGetterAsync imageGetterAsync) {
		super(context, model);
		
		this.imageDownloader = imageDownloader;
		this.imageGetterAsync = imageGetterAsync;
	}

	@Override
	public int getViewTypeCount() {
		return 2;
	}

	@Override
	public int getItemViewType(int position) {
		TypedPoco<Writeup> item = (TypedPoco<Writeup>) getItem(position);
		
		if (item.Type == Type.REPLY) {
			return 0;
		}
		
		if (item.Type == Type.ATTACHMENT) {
			return 1;
		}
		
		Log.e(Constants.TAG, String.format("ComposeAdapter<T>: unknown itemViewType at position = %d", position));
		
		return -1;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		TypedPoco<?> item = (TypedPoco<?>) getItem(position);

		if (item.Type == Type.REPLY) {
			ReplyViewHolder holder = null;

			if (row == null) {
				row = context.getLayoutInflater().inflate(R.layout.response_row, parent, false);

				holder = new ReplyViewHolder();
				holder.Thumbnail = (ImageView) row.findViewById(R.id.response_row_thumbnail);
				holder.Header = (TextView) row.findViewById(R.id.response_row_header);
				holder.Content = (TextView) row.findViewById(R.id.response_row_text);
				holder.Content.setLinkTextColor(appearance.getLinkColor());
				holder.Content.setMovementMethod(LinkMovementMethod.getInstance());
				holder.Content.setFocusable(false);			

				row.setTag(holder);
			} else {
				holder = (ReplyViewHolder) row.getTag();
			}
			
			BaseComposePoco writeup = (BaseComposePoco) item.ChildPoco;
			
			imageDownloader.download(BasePoco.nickToUrl(writeup.Nick, context), holder.Thumbnail);

			holder.Header.setText(CustomHtml.fromHtml(String.format("<b>%s</b>&nbsp;<small>%s</small>", writeup.Nick, BasePoco.timeToString(context, writeup.Time))));

			holder.Content.setTag(position);
			holder.Content.setText(CustomHtml.fromHtml(writeup.Content, imageGetterAsync != null ? imageGetterAsync.spawn(position, writeup.Content, holder.Content) : null));
		}

		if (item.Type == Type.ATTACHMENT) {
			AttachmentViewHolder holder = null;
			
			if (row == null) {
				row = context.getLayoutInflater().inflate(R.layout.response_attachment_row, parent, false);
				
				holder = new AttachmentViewHolder();
				holder.AttachmentComment = (TextView)row.findViewById(R.id.response_attachment_row_comment);
				holder.Thumbnail = (ImageView)row.findViewById(R.id.response_attachment_row_thumbnail);
				
				row.setTag(holder);
			} else {
				holder = (AttachmentViewHolder) row.getTag();
			}
			
			Attachment attachment = (Attachment)item.ChildPoco;
			
			imageDownloader.download(Constants.ImageLoader.FILE_PROTOCOL + attachment.AttachmentSource, holder.Thumbnail);
		}

		return row;
	}

	static class ReplyViewHolder {
		public ImageView Thumbnail;
		public TextView Header;
		public TextView Content;
	}
	
	static class AttachmentViewHolder {
		public TextView AttachmentComment;
		public ImageView Thumbnail;
	}
}
