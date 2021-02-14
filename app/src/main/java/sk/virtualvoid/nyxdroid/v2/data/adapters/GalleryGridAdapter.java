package sk.virtualvoid.nyxdroid.v2.data.adapters;

import java.util.ArrayList;
import java.util.Collection;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;


import sk.virtualvoid.nyxdroid.library.Constants;
import sk.virtualvoid.nyxdroid.v2.R;
import sk.virtualvoid.nyxdroid.v2.internal.Appearance;
import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;

/**
 * 
 * @author Juraj
 * 
 */
public class GalleryGridAdapter extends BaseAdapter {
	private static Drawable defaultDrawable;	
	private LayoutInflater layoutInflater;
	private ArrayList<Bundle> model;
	private ImageLoader imageLoader;
	private DisplayImageOptions options;

	public GalleryGridAdapter(Activity context) {
		Appearance appearance = Appearance.getAppearance(context);
		Resources resources = context.getResources();
		if (appearance.getUseDarkTheme()) {
			defaultDrawable = resources.getDrawable(R.drawable.empty_photo_inv);
		} else {
			defaultDrawable = resources.getDrawable(R.drawable.empty_photo);
		}

		defaultDrawable.setBounds(0, 0, defaultDrawable.getIntrinsicWidth(), defaultDrawable.getIntrinsicHeight());
		
		layoutInflater = LayoutInflater.from(context);
		model = new ArrayList<Bundle>();
		imageLoader = ImageLoader.getInstance();

		options = new DisplayImageOptions.Builder()
			.showImageForEmptyUri(defaultDrawable)
			.showImageOnFail(defaultDrawable)
			.showImageOnLoading(defaultDrawable)
			.resetViewBeforeLoading(true)
			.cacheInMemory(true)
			.cacheOnDisc(true)
			.considerExifParams(true)
			.build();
	}

	public void pauseImageLoader() {
		if (imageLoader != null) {
			imageLoader.pause();
		}
	}
	
	public void resumeImageLoader() {
		if (imageLoader != null) {
			imageLoader.resume();
		}
	}
	
	@Override
	public int getCount() {
		return model.size();
	}
	
	public int getItemPosition(long id) {
		for (int pos = 0; pos < model.size(); pos++) {
			Bundle item = model.get(pos);
			long itemId = item.getLong(Constants.KEY_WU_ID);
			if (itemId == id) {
				return pos;
			}
		}
		return -1;
	}

	@Override
	public Object getItem(int position) {
		return model.get(position);
	}

	public ArrayList<Bundle> getItems() {
		return model;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public void addItems(Bundle[] bundles) {
		for (Bundle bundle : bundles) {
			model.add(bundle);
		}
	}

	public void addItems(Collection<? extends Bundle> bundles) {
		model.addAll(bundles);
	}

	public void clearItems() {
		model.clear();
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		final ViewHolder holder;

		if (row == null) {
			row = layoutInflater.inflate(R.layout.gallerygrid_item, parent, false);

			holder = new ViewHolder();
			holder.imageView = (ImageView) row.findViewById(R.id.gallerygrid_item_image);
			holder.progressBar = (ProgressBar) row.findViewById(R.id.gallerygrid_item_progress);

			row.setTag(holder);
		} else {
			holder = (ViewHolder) row.getTag();
		}

		Bundle bundle = (Bundle) getItem(position);
		String url = bundle.getString(Constants.KEY_URL);

		imageLoader.displayImage(url, holder.imageView, options, new SimpleImageLoadingListener() {
			@Override
			public void onLoadingStarted(String imageUri, View view) {
				holder.progressBar.setProgress(0);
				holder.progressBar.setVisibility(View.VISIBLE);
			}

			@Override
			public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
				holder.progressBar.setVisibility(View.GONE);
			}

			@Override
			public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
				holder.progressBar.setVisibility(View.GONE);
			}
		}, new ImageLoadingProgressListener() {
			@Override
			public void onProgressUpdate(String imageUri, View view, int current, int total) {
				holder.progressBar.setProgress(Math.round(100.0f * current / total));
			}
		});

		return row;
	}

	static class ViewHolder {
		ImageView imageView;
		ProgressBar progressBar;
	}
}
