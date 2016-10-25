package sk.virtualvoid.nyxdroid.v2;

import sk.virtualvoid.core.CustomHtml;
import sk.virtualvoid.core.ImageGetterAsync;
import sk.virtualvoid.nyxdroid.v2.AdvertActivity.AdvertFragmentHandler;
import sk.virtualvoid.nyxdroid.v2.data.Advert;
import android.app.Activity;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * 
 * @author Juraj
 * 
 */
public class AdvertDetailFragment extends BaseFragment implements AdvertFragmentHandler {
	public static final String TAG = "addetail";

	private int linkColor;
	private ImageGetterAsync imageGetterAsync;

	private TextView title;
	private TextView category;
	private TextView summary;
	private TextView description;
	private TextView price;
	private TextView currency;
	private TextView shipping;
	private TextView location;

	public AdvertDetailFragment() {
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		AdvertActivity parent = (AdvertActivity) activity;

		linkColor = parent.getLinkColor();
		imageGetterAsync = parent.getImageGetter();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.advert_detail, container, false);

		title = (TextView) view.findViewById(R.id.advert_title);
		category = (TextView) view.findViewById(R.id.advert_category);
		summary = (TextView) view.findViewById(R.id.advert_summary);

		description = (TextView) view.findViewById(R.id.advert_description);
		description.setTag(0);
		description.setLinkTextColor(linkColor);
		description.setMovementMethod(LinkMovementMethod.getInstance());
		description.setFocusable(false);

		price = (TextView) view.findViewById(R.id.advert_price);
		currency = (TextView) view.findViewById(R.id.advert_currency);
		shipping = (TextView) view.findViewById(R.id.advert_shipping);
		location = (TextView) view.findViewById(R.id.advert_location);

		return view;
	}

	@Override
	public void setData(Advert data) {
		title.setText(data.Title);
		category.setText(data.Category);
		summary.setText(data.Summary);
		
		description.setTag(0);
		description.setText(CustomHtml.fromHtml(data.Description, imageGetterAsync.spawn(0,  data.Description, description)));
		
		price.setText(data.Price);
		currency.setText(data.Currency);
		shipping.setText(data.Shipping);
		location.setText(data.Location);
	}
}
