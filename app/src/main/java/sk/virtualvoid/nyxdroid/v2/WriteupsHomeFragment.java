package sk.virtualvoid.nyxdroid.v2;

import sk.virtualvoid.core.CustomHtml;
import sk.virtualvoid.core.ImageGetterAsync;
import sk.virtualvoid.core.Task;
import sk.virtualvoid.core.TaskListener;
import sk.virtualvoid.core.TaskManager;
import sk.virtualvoid.nyxdroid.v2.data.WriteupHomeResponse;
import sk.virtualvoid.nyxdroid.v2.data.dac.WriteupDataAccess;
import sk.virtualvoid.nyxdroid.v2.data.query.WriteupQuery;
import android.app.Activity;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * 
 * @author Juraj
 * 
 */
public class WriteupsHomeFragment extends BaseFragment {
	public static final String TAG = "wuhome";
	
	private TextView contents;
	private boolean loaded = false;

	public WriteupsHomeFragment() {
		setRetainInstance(false);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.writeup_home, container, false);
		return view;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		contents = (TextView) view.findViewById(R.id.writeup_home_contents);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}
	
	public void load(long id) {
		if (loaded) {
			return;
		}

		BaseActivity context = (BaseActivity) getActivity();
		final int linkColor = context.appearance.getLinkColor();
		final ImageGetterAsync imageGetterAsync = new ImageGetterAsync(context);

		contents.setLinkTextColor(linkColor);
		contents.setMovementMethod(LinkMovementMethod.getInstance());
		contents.setFocusable(false);
		contents.setTag(0);

		WriteupQuery query = new WriteupQuery();
		query.Id = id;

		Task<WriteupQuery, WriteupHomeResponse> task = WriteupDataAccess.getHome(context, new TaskListener<WriteupHomeResponse>() {
			@Override
			public void done(WriteupHomeResponse output) {
				contents.setText(CustomHtml.fromHtml(output.Header, imageGetterAsync.spawn(0, output.Header, contents)));
			}
		});

		TaskManager.startTask(task, query);

		loaded = true;
	}
}
