package sk.virtualvoid.nyxdroid.v2.data.dac;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import sk.virtualvoid.core.NyxException;
import sk.virtualvoid.core.Task;
import sk.virtualvoid.core.TaskListener;
import sk.virtualvoid.core.TaskWorker;
import sk.virtualvoid.net.nyx.Connector;
import sk.virtualvoid.nyxdroid.v2.data.Advert;
import sk.virtualvoid.nyxdroid.v2.data.AdvertComment;
import sk.virtualvoid.nyxdroid.v2.data.AdvertPhoto;
import sk.virtualvoid.nyxdroid.v2.data.query.AdvertQuery;
import android.app.Activity;

/**
 * 
 * @author Juraj
 * 
 */
public class AdvertDataAccess {
	private final static Logger log = Logger.getLogger(AdvertDataAccess.class);

	public static Task<AdvertQuery, Advert> getAdvertDetails(Activity context, TaskListener<Advert> listener) {
		return new Task<AdvertQuery, Advert>(context, new GetAdvertDetailsTaskWorker(), listener);
	}
	
	private static Advert convertAdvert(JSONObject item) throws JSONException {
		Advert advert = new Advert();

		advert.Id = item.getLong("id_item");
		advert.Nick = item.getString("nick_seller");
		advert.Title = item.getString("title");
		advert.Category = item.getString("category");
		advert.Summary = item.getString("summary");
		advert.Description = item.getString("description");
		advert.Price = item.getString("price");
		advert.Currency = item.getString("currency");
		advert.Shipping = item.getString("shipping");
		advert.Location = item.getString("location");

		return advert;
	}

	private static ArrayList<AdvertComment> convertComments(JSONArray array) throws JSONException {
		ArrayList<AdvertComment> comments = new ArrayList<AdvertComment>();

		for (int i = 0; i < array.length(); i++) {
			JSONObject obj = array.getJSONObject(i);
			
			AdvertComment comment = new AdvertComment();
			comment.Id = obj.getLong("id_comment");
			comment.Nick = obj.getString("nick");
			comment.Time = obj.getLong("time");
			comment.Content = obj.getString("content");
			
			comments.add(comment);
		}
		
		return comments;
	}
	
	private static ArrayList<AdvertPhoto> convertPhotos(JSONArray array) throws JSONException {
		ArrayList<AdvertPhoto> photos = new ArrayList<AdvertPhoto>();

		for (int i = 0; i < array.length(); i++) {
			JSONObject obj = array.getJSONObject(i);
			
			AdvertPhoto photo = new AdvertPhoto();
			photo.Id = (long) i;
			photo.Thumbnail = obj.getString("thumb");
			photo.FullSize = obj.getString("image");
			
			photos.add(photo);
		}
		
		return photos;
	}
	
	public static class GetAdvertDetailsTaskWorker extends TaskWorker<AdvertQuery, Advert> {
		@Override
		public Advert doWork(AdvertQuery input) throws NyxException {
			Advert advert = null;

			Connector connector = new Connector(getContext());

			HashMap<String, Object> params = new HashMap<String, Object>();
			params.put("id", Long.toString(input.Id));

			JSONObject json = connector.call("market", "show", params, this);
			if (json == null) {
				throw new NyxException("Json result was null ?");
			} else {
				if (json.has("item") && !json.isNull("item")) {
					try {
						JSONObject item = json.getJSONObject("item");

						advert = convertAdvert(item);
						
						if (item.has("comments") && !item.isNull("comments")) {
							JSONArray comments = item.getJSONArray("comments");
							advert.setComments(convertComments(comments));
						}
						
						if (item.has("photos") && !item.isNull("photos")) {
							JSONArray photos = item.getJSONArray("photos");
							advert.setPhotos(convertPhotos(photos));
						}
					} catch (JSONException e) {
						log.error("GetAdvertDetailsTaskWorker: item", e);
						throw new NyxException(e);
					}
				}
			}

			return advert;
		}
	}
}
