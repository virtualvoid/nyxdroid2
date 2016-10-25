package sk.virtualvoid.nyxdroid.v2.internal;

import android.text.Editable;
import android.text.TextWatcher;

/**
 * 
 * @author suchan_j
 *
 */
public abstract class DelayedTextWatcher implements TextWatcher  {

	private long delayTime;
	private long lastTime;
	
	public DelayedTextWatcher(long delayTime) {
		super();
		
		this.delayTime = delayTime;
		this.lastTime = System.currentTimeMillis();
	}
	
	@Override
	public void afterTextChanged(Editable s) {
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		long currentTime = System.currentTimeMillis();
		if ((currentTime - lastTime) > delayTime) {
			lastTime = currentTime;
			afterTextChangedDelayed(s.toString());
		}
	}

	public abstract void afterTextChangedDelayed(String s);
}
