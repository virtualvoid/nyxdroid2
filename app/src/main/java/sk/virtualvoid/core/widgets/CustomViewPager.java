package sk.virtualvoid.core.widgets;

import android.content.Context;
import androidx.viewpager.widget.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * 
 * @author Juraj
 * 
 */
public class CustomViewPager extends ViewPager {

	public CustomViewPager(Context context) {
		super(context);
	}

	public CustomViewPager(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		try {
			return super.onInterceptTouchEvent(ev);
		} catch (IllegalArgumentException e) {
			return false;
		}
	}
}
