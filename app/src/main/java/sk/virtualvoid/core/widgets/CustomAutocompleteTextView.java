package sk.virtualvoid.core.widgets;

import sk.virtualvoid.nyxdroid.v2.data.BasePoco;
import sk.virtualvoid.nyxdroid.v2.data.UserSearch;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.AutoCompleteTextView;

/**
 * 
 * @author suchan_j
 * 
 */
public class CustomAutocompleteTextView extends AutoCompleteTextView {

	public CustomAutocompleteTextView(Context context) {
		super(context);
	}

	public CustomAutocompleteTextView(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);
	}

	public CustomAutocompleteTextView(Context context, AttributeSet attributeSet, int defStyleAttr) {
		super(context, attributeSet, defStyleAttr);
	}

	@Override
	protected CharSequence convertSelectionToString(Object selectedItem) {
		if (selectedItem == null || !(selectedItem instanceof BasePoco)) {
			return super.convertSelectionToString(selectedItem);
		}

		return ((BasePoco) selectedItem).toString();
	}

	@Override
	public boolean enoughToFilter() {
		return true;
	}
}
