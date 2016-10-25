package uk.co.senab.photoview;

import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.view.View;

public class Compat {

	private static final int SIXTY_FPS_INTERVAL = 1000 / 60;

	public static void postOnAnimation(View view, Runnable runnable) {
		view.postDelayed(runnable, SIXTY_FPS_INTERVAL);
	}

}
