package sk.virtualvoid.core;

import android.graphics.drawable.Drawable;

/**
 * 
 * @author Juraj
 * 
 */
public interface ImageGetterHandler {
	boolean onDone(ImageGetterData data, Drawable result);
}
