package sk.virtualvoid.nyxdroid.v2;

import sk.virtualvoid.nyxdroid.v2.internal.NavigationType;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

/**
 * 
 * @author Juraj
 * 
 */
public class InformationActivity extends BaseActivity {

	@Override
	protected int getContentViewId() {
		return R.layout.information_activity;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		String appNameAndVersion = getResources().getString(R.string.app_name_and_version);

		TextView textView = (TextView)findViewById(R.id.information_appname_and_version);
		textView.setText(Html.fromHtml(String.format("click for changelog: <u>%s</u>", appNameAndVersion)));
		textView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ChangeLogDialog dialog = new ChangeLogDialog(InformationActivity.this);
				dialog.show();
			}
		});
	}

	@Override
	public boolean onNavigationRequested(NavigationType navigationType, String url, Long discussionId, Long writeupId) {
		/* Not needed here */
		return false;
	}
}
