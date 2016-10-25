package sk.virtualvoid.nyxdroid.v2;

import java.util.ArrayList;

import sk.virtualvoid.core.CoreUtility;
import sk.virtualvoid.core.ImageDownloader;
import sk.virtualvoid.nyxdroid.v2.AdvertActivity.AdvertFragmentHandler;
import sk.virtualvoid.nyxdroid.v2.data.Advert;
import sk.virtualvoid.nyxdroid.v2.data.AdvertPhoto;
import sk.virtualvoid.nyxdroid.v2.data.adapters.BasePocoAdapter;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

/**
 * 
 * @author suchan_j
 *
 */
public class AdvertPhotosFragment extends BaseFragment implements AdvertFragmentHandler, AdapterView.OnItemClickListener {
	public static final String TAG = "adphotos";
	
	private ImageDownloader imageDownloader;
	private ListView listView;
	
	public AdvertPhotosFragment() {
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		AdvertActivity parent = (AdvertActivity)activity;
		
		//imageDownloader = parent.getImageDownloader();
		Drawable emptyPhoto = parent.getResources().getDrawable(R.drawable.empty_photo);
		imageDownloader = new ImageDownloader(parent, emptyPhoto);
		
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.generic_listview, container, false);
		return view;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		listView = (ListView) view.findViewById(R.id.list);
		listView.setEmptyView(view.findViewById(R.id.list_empty));
	}
	
	@Override
	public void setData(Advert data) {
		if (listView == null) {
			return;
		}
		
		listView.setAdapter(new PhotosAdapter(getActivity(), data.getPhotosOrDefault()));
		listView.setOnItemClickListener(this);
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		AdvertPhoto photo = (AdvertPhoto) parent.getItemAtPosition(position);
		if (photo == null) {
			return;
		}

		CoreUtility.launchBrowser(getActivity(), photo.FullSize);
	}
	
	private class PhotosAdapter extends BasePocoAdapter<AdvertPhoto> {
		public PhotosAdapter(Activity context, ArrayList<AdvertPhoto> model) {
			super(context, model);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View row = convertView;
			
			ViewHolder holder = null;
			
			if (row == null) {
				holder = new ViewHolder();
				
				row = context.getLayoutInflater().inflate(R.layout.advert_photo_row, parent, false);
				holder.Thumbnail = (ImageView)row.findViewById(R.id.advert_photo_row_image);
				
				row.setTag(holder);
			} else {
				holder = (ViewHolder) row.getTag();
			}
			
			AdvertPhoto photo = (AdvertPhoto)getItem(position);
			
			imageDownloader.download(photo.Thumbnail, holder.Thumbnail);
			
			return row;
		}
	}
	
	private static class ViewHolder {
		public ImageView Thumbnail;
	}
}
