package sk.virtualvoid.nyxdroid.v2;

import android.app.Fragment;
import android.os.Bundle;
import android.widget.ListView;

/**
 * 
 * @author Juraj
 *
 */
public abstract class BaseFragment extends Fragment {
	public static interface Callbacks {
		void onListViewCreated(ListView listView, Bundle savedInstanceState);
	}
}
