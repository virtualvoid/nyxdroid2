package sk.virtualvoid.core;

import sk.virtualvoid.nyxdroid.library.Constants;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;

/**
 * 
 * @author Juraj
 * 
 */
public class ImageDownloader implements ImageLoadingListener {

	private Context context;
	private Drawable emptyImage;
	private ImageLoader imageLoader;

	public ImageDownloader(Context context, Drawable emptyImage) {
		this.context = context;

		if (emptyImage != null) {
			this.emptyImage = emptyImage;
			this.emptyImage.setBounds(0, 0, this.emptyImage.getIntrinsicWidth(), this.emptyImage.getIntrinsicHeight());
		}

		this.imageLoader = ImageLoader.getInstance();
	}

	public void cancel(ImageView imageView) {
		imageLoader.cancelDisplayTask(imageView);
	}

	public void download(String url, ImageView imageView) {
		if (emptyImage != null) {
			imageView.setImageDrawable(emptyImage);
		}
		imageLoader.displayImage(url, imageView, this);
	}

	@Override
	public void onLoadingCancelled(String url, View view) {
		Log.w(Constants.TAG, String.format("ImageDownloader: cancelled: %s", url));
	}

	@Override
	public void onLoadingComplete(String url, View view, Bitmap bitmap) {
		Log.i(Constants.TAG, String.format("ImageDownloader: completed: %s", url));
	}

	@Override
	public void onLoadingFailed(String url, View view, FailReason failReason) {
		Log.e(Constants.TAG, String.format("ImageDownloader: failed: %s", url));
	}

	@Override
	public void onLoadingStarted(String url, View view) {
		Log.i(Constants.TAG, String.format("ImageDownloader: started: %s", url));
	}
}
