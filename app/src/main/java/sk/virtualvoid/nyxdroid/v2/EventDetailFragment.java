package sk.virtualvoid.nyxdroid.v2;

import org.apache.log4j.Logger;

import sk.virtualvoid.core.CustomHtml;
import sk.virtualvoid.core.ImageGetterAsync;
import sk.virtualvoid.nyxdroid.v2.EventActivity.EventFragmentHandler;
import sk.virtualvoid.nyxdroid.v2.data.BasePoco;
import sk.virtualvoid.nyxdroid.v2.data.Event;
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
public class EventDetailFragment extends BaseFragment implements EventFragmentHandler {
	public static final String TAG = "evdetail";

	private static final Logger log = Logger.getLogger(EventDetailFragment.class);
	
	private int linkColor;
	private ImageGetterAsync imageGetterAsync;

	private TextView description;
	private TextView location;
	private TextView timeStart;
	private TextView timeEnd;

	public EventDetailFragment() {
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		EventActivity parent = (EventActivity) activity;

		linkColor = parent.getLinkColor();
		imageGetterAsync = parent.getImageGetter();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.event_detail, container, false);

		description = (TextView) view.findViewById(R.id.event_description);
		description.setTag(0);
		description.setLinkTextColor(linkColor);
		description.setMovementMethod(LinkMovementMethod.getInstance());
		description.setFocusable(false);

		timeStart = (TextView) view.findViewById(R.id.event_time_start);
		timeEnd = (TextView) view.findViewById(R.id.event_time_end);
		location = (TextView) view.findViewById(R.id.event_location);
		return view;
	}

	@Override
	public void setData(Event data, Object tag) {
		try {
			description.setTag(0);
			description.setText(CustomHtml.fromHtml(data.Description, imageGetterAsync.spawn(0, data.Description, description)));

			timeStart.setText(BasePoco.timeToString(getActivity(), data.Time));
			timeEnd.setText(BasePoco.timeToString(getActivity(), data.TimeEnd));
			location.setText(data.Location);
		} catch (Throwable t) {
			log.fatal(String.format("setData: %s", data != null ? data.toString() : "null"), t);
		}
	}
}
