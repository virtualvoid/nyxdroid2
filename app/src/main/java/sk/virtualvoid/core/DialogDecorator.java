package sk.virtualvoid.core;

import android.app.Dialog;
import android.graphics.Rect;
import android.view.Window;

public class DialogDecorator {
	public static void decorate(Dialog dialog) {
		decorate(dialog, 0.9, 0.4);
	}

	public static void decorate(Dialog dialog, double wmax, double hmax) {
		decorate(dialog, wmax, hmax, false);
	}
	
	public static void decorate(Dialog dialog, double wmax, double hmax, boolean includingTitle) {
		if (!includingTitle) {
			dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		}
		
		Rect rect = new Rect();
		dialog.getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
		dialog.getWindow().setLayout((int) (rect.width() * wmax), (int) (rect.height() * hmax));
	}
}
