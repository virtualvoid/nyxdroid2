package sk.virtualvoid.nyxdroid.v2.data;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 
 * @author Juraj
 *
 */
public class WriteupHomeResponse extends BaseResponse {
	public String Home;
	public String Header;
	
	public WriteupHomeResponse() {
		Home = "";
		Header = "";
		Success = true;
	}

	public static WriteupHomeResponse fromBoardJSONObject(JSONObject root) throws JSONException {
		WriteupHomeResponse result = new WriteupHomeResponse();
		JSONArray items = root.getJSONArray("items");
		StringBuilder html = new StringBuilder();
		for (int i = 0; i < items.length(); i++) {
			JSONObject item = items.getJSONObject(i);
			if (item.has("content") && !item.isNull("content")) {
				String content = item.getString("content");
				if (!content.trim().isEmpty()) {
					html.append("<section>").append(content).append("</section>");
				}
			}
		}
		result.Home = html.toString();
		return result;
	}
}
