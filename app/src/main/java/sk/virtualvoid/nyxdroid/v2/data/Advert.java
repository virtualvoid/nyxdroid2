package sk.virtualvoid.nyxdroid.v2.data;

import java.util.ArrayList;

/**
 * 
 * @author Juraj
 *
 */
public class Advert extends BasePoco {
	public String Title;
	public String Category;
	public String Summary;
	public String Description;
	public String Price;
	public String Currency;
	public String Shipping;
	public String Location;
	
	private ArrayList<AdvertComment> Comments;
	private ArrayList<AdvertPhoto> Photos;
	
	public void setComments(ArrayList<AdvertComment> comments) {
		this.Comments = comments;
	}
	
	public ArrayList<AdvertComment> getCommentsOrDefault() {
		if (Comments == null) {
			return new ArrayList<AdvertComment>();
		}
		return Comments;
	}
	
	public void setPhotos(ArrayList<AdvertPhoto> photos) {
		this.Photos = photos;
	}
	
	public ArrayList<AdvertPhoto> getPhotosOrDefault() {
		if (Photos == null) {
			return new ArrayList<AdvertPhoto>();
		}
		return Photos;
	}
}
