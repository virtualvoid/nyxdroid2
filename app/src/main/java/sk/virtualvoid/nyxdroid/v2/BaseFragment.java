package sk.virtualvoid.nyxdroid.v2;


import android.os.Bundle;
import android.widget.ListView;

import androidx.fragment.app.Fragment;

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
