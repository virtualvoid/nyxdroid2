package sk.virtualvoid.nyxdroid.v2;

import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;

import sk.virtualvoid.nyxdroid.v2.internal.NavigationType;

/**
 * @author Juraj
 */
public class InformationActivity extends BaseActivity {

    @Override
    protected int getContentViewId() {
        return R.layout.information_activity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String appName = getResources().getString(R.string.app_name_and_version);
        String appVersion = "";

        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            appVersion = pInfo.versionName;
        } catch (Throwable e) {
            e.printStackTrace();
        }

        TextView textView = (TextView) findViewById(R.id.information_appname_and_version);
        textView.setText(Html.fromHtml(String.format("<u>%s %s</u>", appName, appVersion)));
    }

    @Override
    public boolean onNavigationRequested(NavigationType navigationType, String url, Long discussionId, Long writeupId) {
        return false;
    }
}
