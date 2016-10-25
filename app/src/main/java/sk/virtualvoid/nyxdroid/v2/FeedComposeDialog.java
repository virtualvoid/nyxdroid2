package sk.virtualvoid.nyxdroid.v2;

import sk.virtualvoid.core.DialogDecorator;
import android.app.Activity;
import android.app.Dialog;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * 
 * @author juraj
 * 
 */
public class FeedComposeDialog extends Dialog {
	public FeedComposeDialog(Activity context, final OnPostListener onPostListener) {
		super(context, R.style.full_screen_dialog);

		DialogDecorator.decorate(this);

		setContentView(R.layout.feed_compose);		

		final EditText txtMessage = (EditText)findViewById(R.id.feed_post_message);
		txtMessage.requestFocus();
		
		Button btnPost = (Button) findViewById(R.id.feed_button_post);
		btnPost.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onPostListener.onPost(txtMessage.getText().toString());
				dismiss();
			}
		});

		Button btnCancel = (Button) findViewById(R.id.feed_button_cancel);
		btnCancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				cancel();
			}
		});
	}
	
	public interface OnPostListener {
		void onPost(String value);
	}
}
