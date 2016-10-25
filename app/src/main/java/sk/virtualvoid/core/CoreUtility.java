package sk.virtualvoid.core;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.log4j.Logger;

import sk.virtualvoid.nyxdroid.library.Constants;

import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;

/**
 * 
 * @author Juraj
 * 
 */
public class CoreUtility {
	private final static Logger log = Logger.getLogger(CoreUtility.class);

	static void copyStream(InputStream is, OutputStream os) {
		final int buffer_size = 1024;
		try {
			byte[] bytes = new byte[buffer_size];
			for (;;) {
				int count = is.read(bytes, 0, buffer_size);
				if (count == -1)
					break;
				os.write(bytes, 0, count);
			}
		} catch (Exception ex) {
			log.error("copyStream: " + ex.getMessage());
		}
	}

	static byte[] copyStream(InputStream is) {
		try {
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();

			int read;
			byte[] data = new byte[2048];

			while ((read = is.read(data, 0, data.length)) != -1) {
				buffer.write(data, 0, read);
			}
			return buffer.toByteArray();
		} catch (IOException e) {
			log.error("copyStream to byteArray" + e.getMessage());
		}
		return null;
	}

	static String getFileName(Uri extUrl) {
		String filename = "";
		String path = extUrl.getPath();
		String[] pathContents = path.split("[\\\\/]");
		if (pathContents != null) {
			int pathContentsLength = pathContents.length;
			String lastPart = pathContents[pathContentsLength - 1];
			String[] lastPartContents = lastPart.split("\\.");
			if (lastPartContents != null && lastPartContents.length > 1) {
				int lastPartContentLength = lastPartContents.length;
				String name = "";
				for (int i = 0; i < lastPartContentLength; i++) {
					if (i < (lastPartContents.length - 1)) {
						name += lastPartContents[i];
						if (i < (lastPartContentLength - 2)) {
							name += ".";
						}
					}
				}
				String extension = lastPartContents[lastPartContentLength - 1];
				filename = name + "." + extension;
			}
		}
		return filename;
	}

	public static File getNyxClientDataDirectory(String directory) {
		File root = Environment.getExternalStorageDirectory();
		File nyx = new File(root, directory);
		if (!nyx.isDirectory()) {
			nyx.mkdir();
		}

		final String nomedia = ".nomedia";

		File f = new File(nyx, nomedia);
		try {
			f.createNewFile();
		} catch (IOException e) {
			log.error("getNyxClientDataDirectory: " + e.getMessage());
		}

		return nyx;
	}

	public static String getRealPathFromURI(Context context, Uri contentUri) {
		try {
			String[] proj = { MediaStore.Images.Media.DATA };
			CursorLoader loader = new CursorLoader(context, contentUri, proj, null, null, null);
			Cursor cursor = loader.loadInBackground();
			int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			cursor.moveToFirst();
			return cursor.getString(column_index);
		} catch (Throwable t) {
			log.error("getRealPathFromURI: " + t.getMessage());
		}
		return null;
	}

	public static Bitmap getBitmapFromFile(File f) {
		return getBitmapFromFile(f, true);
	}

	public static Bitmap getBitmapFromFile(File f, boolean rescale) {
		final int REQUIRED_SIZE = 70;
		try {
			// decode image size
			BitmapFactory.Options opts = new BitmapFactory.Options();
			opts.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(new FileInputStream(f), null, opts);

			// Find the correct scale value. It should be the power of 2.
			int scale = 1;
			if (rescale) {
				int width_tmp = opts.outWidth, height_tmp = opts.outHeight;
				while (true) {
					if (width_tmp / 2 < REQUIRED_SIZE || height_tmp / 2 < REQUIRED_SIZE) {
						break;
					}
					width_tmp /= 2;
					height_tmp /= 2;
					scale *= 2;
				}
			}
			// decode with inSampleSize
			BitmapFactory.Options outopts = new BitmapFactory.Options();
			outopts.inSampleSize = scale;
			Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f), null, outopts);
			return b;
		} catch (Throwable t) {
			log.error("getBitmapFromFile: " + t.getMessage());
		}
		return null;
	}

	public static File downloadFileWeb(String source) {
		try {
			String fileName = getFileName(Uri.parse(source));
			File homeDir = getNyxClientDataDirectory("nyxdroid");
			File cacheFile = new File(homeDir, fileName);
			URL url = new URL(source);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setConnectTimeout(5000);
			connection.setReadTimeout(5000);
			connection.setInstanceFollowRedirects(true);
			InputStream inputStream = connection.getInputStream();
			OutputStream outputStream = new FileOutputStream(cacheFile);
			copyStream(inputStream, outputStream);
			outputStream.close();
			return cacheFile;
		} catch (Throwable t) {
			log.warn("downloadFileWeb: " + t.getMessage());
		}
		return null;
	}

	public static InputStream downloadStreamWeb(String source) {
		try {
			URL url = new URL(source);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setConnectTimeout(5000);
			connection.setReadTimeout(5000);
			connection.setInstanceFollowRedirects(true);
			InputStream inputStream = connection.getInputStream();
			return inputStream;
		} catch (Throwable t) {
			log.warn("downloadFileWeb: " + t.getMessage());
		}
		return null;
	}

	public static File downloadFileCache(String source) {
		String shortFileName = getFileName(Uri.parse(source));
		try {
			File homeDir = getNyxClientDataDirectory("nyxdroid");
			File src = new File(homeDir, shortFileName);
			if (src.exists()) {
				return src;
			}
		} catch (Throwable t) {
			log.warn("downloadFileCache: " + t.getMessage());
		}
		return null;
	}

	public static Bitmap drawableToBitmap(Drawable drawable) {
		if (drawable instanceof BitmapDrawable) {
			return ((BitmapDrawable) drawable).getBitmap();
		}

		Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
		drawable.draw(canvas);

		return bitmap;
	}

	public static boolean launchBrowser(Context context, String url) {
		boolean success = true;
		try {
			Uri uri = Uri.parse(url);

			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(uri);
			context.startActivity(intent);
		} catch (Throwable t) {
			success = false;
		}
		return success;
	}
	
	public static Tuple<String, String> splitSearch(String text) {
		String nick = null;
		String content = null;

		if (text.length() == 0) {
			return null;
		}

		if (text.startsWith("@")) {
			int start = 1;
			int end = text.indexOf(" ");

			nick = end > 0 ? text.substring(start, end) : text.substring(start);
			content = end > 0 ? text.substring(end + 1) : null;
		} else {
			content = text;
		}

		return new Tuple<String, String>(nick, content);
	}
}
