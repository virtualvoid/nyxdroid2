package sk.virtualvoid.core;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

/**
 * @author Juraj
 */
public class ImageDownloader {

    private Context context;
    private Drawable emptyImage;

    public ImageDownloader(Context context, Drawable emptyImage) {
        this.context = context;

        if (emptyImage != null) {
            this.emptyImage = emptyImage;
            this.emptyImage.setBounds(0, 0, this.emptyImage.getIntrinsicWidth(), this.emptyImage.getIntrinsicHeight());
        }
    }

    public void download(String url, ImageView imageView) {
        Glide.with(context)
                .load(url)
                .fitCenter()
                .placeholder(emptyImage)
                .into(imageView);
    }

}
