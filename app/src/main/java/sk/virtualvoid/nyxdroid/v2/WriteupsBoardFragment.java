package sk.virtualvoid.nyxdroid.v2;

import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

import sk.virtualvoid.core.CoreUtility;
import sk.virtualvoid.core.Task;
import sk.virtualvoid.core.TaskListener;
import sk.virtualvoid.core.TaskManager;
import sk.virtualvoid.nyxdroid.v2.data.WriteupHomeResponse;
import sk.virtualvoid.nyxdroid.v2.data.dac.WriteupDataAccess;
import sk.virtualvoid.nyxdroid.v2.data.query.WriteupQuery;

public class WriteupsBoardFragment extends BaseFragment {
    public static final String TAG = "wuboard";

    private WebView contents;
    private ProgressBar progress;
    private TextView message;
    private Task<WriteupQuery, WriteupHomeResponse> task;
    private long discussionId;
    private String html;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.writeup_board, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        contents = (WebView) view.findViewById(R.id.writeup_board_contents);
        progress = (ProgressBar) view.findViewById(R.id.writeup_board_progress);
        message = (TextView) view.findViewById(R.id.writeup_board_message);
        BaseActivity context = (BaseActivity) getActivity();
        contents.setBackgroundColor(Color.parseColor(context.appearance.getUseDarkTheme() ? "#121212" : "#FAFAFA"));
        contents.getSettings().setDefaultTextEncodingName("UTF-8");
        contents.getSettings().setAllowFileAccess(false);
        contents.getSettings().setAllowContentAccess(false);
        contents.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return openLink(url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return request.isForMainFrame() && openLink(request.getUrl().toString());
            }
        });
        if (html != null) {
            showContents();
        } else if (discussionId > 0) {
            load(discussionId);
        }
    }

    public void load(long id) {
        if (discussionId != id) {
            TaskManager.killIfNeeded(task);
            task = null;
            html = null;
            discussionId = id;
        }
        // A tab can be selected before its fragment view is created.
        if (contents == null || !isAdded() || html != null || task != null) {
            return;
        }

        contents.setVisibility(View.GONE);
        message.setVisibility(View.GONE);
        progress.setVisibility(View.VISIBLE);
        WriteupQuery query = new WriteupQuery();
        query.Id = id;
        task = WriteupDataAccess.getBoard((BaseActivity) getActivity(), new TaskListener<WriteupHomeResponse>() {
            @Override
            public void done(WriteupHomeResponse output) {
                task = null;
                if (contents != null && isAdded()) {
                    html = output.Home;
                    showContents();
                }
            }

            @Override
            public void handleError(Throwable error) {
                task = null;
                if (message != null && isAdded()) {
                    progress.setVisibility(View.GONE);
                    message.setText(R.string.unspecified_data_error);
                    message.setVisibility(View.VISIBLE);
                }
            }
        });
        TaskManager.startTask(task, query);
    }

    public void reload(long id) {
        TaskManager.killIfNeeded(task);
        task = null;
        html = null;
        load(id);
    }

    private void showContents() {
        progress.setVisibility(View.GONE);
        if (html.isEmpty()) {
            contents.setVisibility(View.GONE);
            message.setText(R.string.board_empty);
            message.setVisibility(View.VISIBLE);
            return;
        }

        BaseActivity context = (BaseActivity) getActivity();
        boolean dark = context.appearance.getUseDarkTheme();
        String linkColor = String.format(Locale.US, "#%06X", context.appearance.getLinkColor() & 0xFFFFFF);
        String page = "<!doctype html><html><head><meta charset=\"utf-8\">"
                + "<meta name=\"viewport\" content=\"width=device-width,initial-scale=1\">"
                + "<style>body{margin:8px;font-family:sans-serif;font-size:" + context.appearance.getFontSize() + "px;"
                + "background:" + (dark ? "#121212" : "#FAFAFA") + ";color:" + (dark ? "#FFFFFF" : "#000000") + ";}"
                + "a{color:" + linkColor + ";}img,video{max-width:100%;height:auto;}"
                + "section+section{margin-top:16px;}pre{white-space:pre-wrap;}"
                + "body{overflow-wrap:break-word;}</style></head><body>" + html + "</body></html>";
        contents.loadDataWithBaseURL(getPageUrl(), page, "text/html", "UTF-8", null);
        message.setVisibility(View.GONE);
        contents.setVisibility(View.VISIBLE);
    }

    private String getPageUrl() {
        return "https://nyx.cz/discussion/" + discussionId + "/content/home";
    }

    private boolean openLink(String url) {
        Uri uri = Uri.parse(url);
        if (uri.getFragment() != null && uri.buildUpon().fragment(null).build().toString().equals(getPageUrl())) {
            return false;
        }
        if (isAdded() && !CoreUtility.launchBrowser(getActivity(), url)) {
            Toast.makeText(getActivity(), R.string.cant_open_it, Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    @Override
    public void onDestroyView() {
        TaskManager.killIfNeeded(task);
        task = null;
        if (contents != null) {
            contents.stopLoading();
            ((ViewGroup) contents.getParent()).removeView(contents);
            contents.destroy();
            contents = null;
        }
        progress = null;
        message = null;
        super.onDestroyView();
    }
}
