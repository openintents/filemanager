package org.openintents.filemanager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.openintents.filemanager.util.MimeTypeParser;
import org.openintents.filemanager.util.MimeTypes;
import org.xmlpull.v1.XmlPullParserException;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.res.XmlResourceParser;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore.Images;
import android.util.Log;

public class FileManagerProvider extends ContentProvider {

	static final String MIME_TYPE_PREFIX = "content://org.openintents.filemanager/mimetype/";
	private static final String TAG = "FileManagerProvider";
	public static final String AUTHORITY = "org.openintents.filemanager";
	private MimeTypes mMimeTypes;

	@Override
	public boolean onCreate() {
		getMimeTypes();
		return true;
	}

	/**
	 * 
	 */
	private void getMimeTypes() {
		MimeTypeParser mtp = new MimeTypeParser();

		XmlResourceParser in = getContext().getResources().getXml(
				R.xml.mimetypes);

		try {
			mMimeTypes = mtp.fromXmlResource(in);
		} catch (XmlPullParserException e) {
			Log
					.e(
							TAG,
							"PreselectedChannelsActivity: XmlPullParserException",
							e);
			throw new RuntimeException(
					"PreselectedChannelsActivity: XmlPullParserException");
		} catch (IOException e) {
			Log.e(TAG, "PreselectedChannelsActivity: IOException", e);
			throw new RuntimeException(
					"PreselectedChannelsActivity: IOException");
		}
	}

	@Override
	public int delete(Uri uri, String s, String[] as) {
		// not supported
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		// return file extension (uri.lastIndexOf("."))
		return mMimeTypes.getMimeType(uri.toString());
	}

	@Override
	public Uri insert(Uri uri, ContentValues contentvalues) {
		// not supported
		return null;
	}

	@Override
	public Cursor query(Uri uri, String[] as, String s, String[] as1, String s1) {
		if (uri.toString().startsWith(
				MIME_TYPE_PREFIX)) {
			MatrixCursor c = new MatrixCursor(new String[] { Images.Media.DATA,
					Images.Media.MIME_TYPE });
			// data = absolute path = uri - content://authority/mimetype
			String data = uri.toString().substring(20 + AUTHORITY.length());
			String mimeType = mMimeTypes.getMimeType(data);
			c.addRow(new String[] { data, mimeType });
			return c;
		} else {
			throw new RuntimeException("Unsupported uri");
		}
	}
	
	@Override
	public ParcelFileDescriptor openFile(Uri uri, String mode)
			throws FileNotFoundException {
		if (uri.toString().startsWith(
				MIME_TYPE_PREFIX)) {
			int m = ParcelFileDescriptor.MODE_READ_ONLY;
			if (mode.equalsIgnoreCase("rw"))
				m = ParcelFileDescriptor.MODE_READ_WRITE;			
			File f = new File(uri.toString().substring(20 + AUTHORITY.length()));
			ParcelFileDescriptor pfd = ParcelFileDescriptor.open(f, m);
			return pfd;
		} else {
			throw new FileNotFoundException	("Unsupported uri: " + uri.toString());
		}
	}

	@Override
	public int update(Uri uri, ContentValues contentvalues, String s,
			String[] as) {
		// not supported
		return 0;
	}

}
