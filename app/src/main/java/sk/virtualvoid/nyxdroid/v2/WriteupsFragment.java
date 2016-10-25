package sk.virtualvoid.nyxdroid.v2;

import sk.virtualvoid.nyxdroid.library.Constants;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

/**
 * 
 * @author Juraj
 * 
 */
public class WriteupsFragment extends BaseFragment {	
	public static final String TAG = "wulist";
	
	private BaseFragment.Callbacks callbacks;

	public WriteupsFragment() {
		setRetainInstance(false);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		if (activity == null || !(activity instanceof BaseFragment.Callbacks)) {
			return;
		}

		try {
			callbacks = (BaseFragment.Callbacks) activity;
		} catch (ClassCastException e) {
			Log.e(Constants.TAG, "ClassCastException: " + e.getMessage());
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.generic_listview, container, false);
		return view;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		if (callbacks != null) {
			callbacks.onListViewCreated((ListView) view.findViewById(R.id.list), savedInstanceState);
		}
	}	
}
