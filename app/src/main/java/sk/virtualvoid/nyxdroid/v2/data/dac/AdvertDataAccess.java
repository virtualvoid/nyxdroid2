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
import sk.virtualvoid.nyxdroid.library.Constants;
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

	public static class GetAdvertDetailsTaskWorker extends TaskWorker<AdvertQuery, Advert> {
		@Override
		public Advert doWork(AdvertQuery input) throws NyxException {
	   		throw new NyxException(Constants.NOT_IMPLEMENTED_YET);
		}
	}
}
