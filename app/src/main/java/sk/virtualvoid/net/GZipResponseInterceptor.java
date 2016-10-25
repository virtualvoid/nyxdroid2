package sk.virtualvoid.net;

import java.io.IOException;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.protocol.HttpContext;

/**
 * 
 * @author juraj
 *
 */
public class GZipResponseInterceptor implements HttpResponseInterceptor {
	public void process(HttpResponse response, HttpContext context) throws HttpException, IOException {
		HttpEntity entity = response.getEntity();
		Header ceheader = entity.getContentEncoding();
		if (ceheader != null) {
			HeaderElement[] codecs = ceheader.getElements();
			for (int i = 0; i < codecs.length; i++) {
				if (codecs[i].getName().equalsIgnoreCase("gzip")) {
					response.setEntity(new GZipDecompressingEntity(response.getEntity()));
					return;
				}
			}
		}
	}
}
