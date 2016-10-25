package sk.virtualvoid.core;

import java.lang.ref.WeakReference;

import android.widget.TextView;

/**
 * 
 * @author Juraj
 *
 */
public class ImageGetterData {
	private int spawnposition;
	private String source;
	private String content;
	private WeakReference<TextView> textViewRef;

	public ImageGetterData(int spawnPosition, String source, String content, TextView textView) {
		this.spawnposition = spawnPosition;
		this.source = source;
		this.content = content;
		this.textViewRef = new WeakReference<TextView>(textView);
	}

	public int getSpawnPosition() {
		return this.spawnposition;
	}
	
	public String getSource() {
		return this.source;
	}
	
	public String getContent() {
		return this.content;
	}
	
	public WeakReference<TextView> getTextViewRef() {
		return this.textViewRef;
	}
}
