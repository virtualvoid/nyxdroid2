package sk.virtualvoid.core.widgets;

import sk.virtualvoid.nyxdroid.v2.internal.NavigationHandler;
//import android.os.Parcel;
//import android.text.ParcelableSpan;
import android.text.style.ClickableSpan;
import android.view.View;
//import android.view.ViewParent;

/**
 * 
 * @author Juraj
 * 
 */
public class CustomUrlSpan extends ClickableSpan /*implements ParcelableSpan*/ {
	private static final int spanTypeId = 88;
	private String url;
	private boolean isImage;

	public CustomUrlSpan(String url) {
		this.url = url;
		this.isImage = false;
	}

	public CustomUrlSpan(String url, boolean isImage) {
		this.url = url;
		this.isImage = isImage;
	}
	
	public String getUrl() {
		return this.url;
	}
	
//	@Override
//	public int describeContents() {
//		return 0;
//	}
//
//	@Override
//	public void writeToParcel(Parcel dest, int flags) {
//		dest.writeString(url);
//	}
//
//	public int getSpanTypeId() {
//		return spanTypeId;
//	}

	@Override
	public void onClick(View view) {
		View parent = (View) view.getParent();
		Object parentTag = parent.getTag();
		
		NavigationHandler navigation = new NavigationHandler(getUrl(), isImage, parentTag, view.getContext());
		navigation.doNavigation();
	}
}
