package sk.virtualvoid.net;

import java.io.IOException;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.protocol.HttpContext;

/**
 * 
 * @author juraj
 *
 */
public class GZipRequestInterceptor implements HttpRequestInterceptor {
	public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
		if (!request.containsHeader("Accept-Encoding")) {
			request.addHeader("Accept-Encoding", "gzip");
		}
	}
}
