package sk.virtualvoid.net;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import org.apache.http.HttpEntity;
import org.apache.http.entity.HttpEntityWrapper;

/**
 * 
 * @author juraj
 *
 */
public class GZipDecompressingEntity extends HttpEntityWrapper {
	public GZipDecompressingEntity(HttpEntity wrapped) {
		super(wrapped);
	}

	@Override
	public InputStream getContent() throws IOException, IllegalStateException {
		// the wrapped entity's getContent() decides about repeatability
		InputStream wrappedin = wrappedEntity.getContent();
		return new GZIPInputStream(wrappedin);
	}

	@Override
	public long getContentLength() {
		// length of ungzipped content is not known
		return -1;
	}
}
