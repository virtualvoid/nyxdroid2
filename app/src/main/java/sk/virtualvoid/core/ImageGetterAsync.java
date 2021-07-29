package sk.virtualvoid.core;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import sk.virtualvoid.nyxdroid.library.Constants;
import sk.virtualvoid.nyxdroid.v2.R;
import sk.virtualvoid.nyxdroid.v2.internal.Appearance;


/**
 * @author Juraj
 */
public class ImageGetterAsync {
    private Drawable defaultDrawable;
    private AppCompatActivity context;

    public ImageGetterAsync(AppCompatActivity context) {
        this.context = context;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();

        Appearance appearance = Appearance.getAppearance(context);
        if (appearance.getUseDarkTheme()) {
            defaultDrawable = resources.getDrawable(R.drawable.empty_photo_inv);
        } else {
            defaultDrawable = resources.getDrawable(R.drawable.empty_photo);
        }

        defaultDrawable.setBounds(0, 0, defaultDrawable.getIntrinsicWidth(), defaultDrawable.getIntrinsicHeight());

    }

    public Html.ImageGetter spawn(int spawnPosition, String message, TextView textView) {
        final int _position = spawnPosition;
        final String _message = message;
        final TextView _textView = textView;

        return new UrlImageGetter(context, textView);
    }

    public void setActive(boolean nowActive) {
    }

    private void loadPendingImages() {
    }
}
