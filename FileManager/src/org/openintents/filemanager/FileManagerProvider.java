package org.openintents.filemanager;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;

import org.openintents.filemanager.util.MimeTypes;

import java.io.File;
import java.io.FileNotFoundException;

public class FileManagerProvider extends ContentProvider {

	public static final String FILE_PROVIDER_PREFIX = "content://org.openintents.filemanager";
	public static final String AUTHORITY = "org.openintents.filemanager";
	private MimeTypes mMimeTypes;

	@Override
	public boolean onCreate() {
		MimeTypes.initInstance(getContext());
		mMimeTypes = MimeTypes.getInstance();
		return true;
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
	public Cursor query(Uri uri, String[] projection, String s, 
			String[] as1, String s1) {
		if (uri.toString().startsWith(FILE_PROVIDER_PREFIX)) {
			if (projection == null || projection.length == 0) {
				// Standard projection including all supported rows
				projection = new String [] {
								MediaStore.MediaColumns.DATA,
								MediaStore.MediaColumns.MIME_TYPE,
								MediaStore.MediaColumns.DISPLAY_NAME,
								MediaStore.MediaColumns.SIZE};
			} 
			
			MatrixCursor c = new MatrixCursor(projection);
			MatrixCursor.RowBuilder row = c.newRow();
			
			// data = absolute path to file
			String data = uri.getPath();
			
			int fromIndex = data.lastIndexOf(File.separatorChar) + 1;
			if (fromIndex >= data.length()) {
				// Last character was '/' or data is empty, so no file name 
				// was specified and we don't want to raise an 
				// IndexOutOfBoundsException
				throw new RuntimeException("No file name specified: ".concat(data));
			}
			
			// According to Android docs, DISPLAY_NAME should be
			// the last segment of Uri
			String displayName = 
				(fromIndex > 0) ? data.substring(fromIndex) : data;
			
			String mimeType = mMimeTypes.getMimeType(data);
				
			long size = -1;
			File file = new File(data);			
			if (file.exists() && file.isFile()) {
				size = file.length();
			}
			
			for (String col : projection) {
				if (col.equals(MediaStore.MediaColumns.DATA)) {
					row.add(data);
				} else if (col.equals(MediaStore.MediaColumns.MIME_TYPE)) {
					row.add(mimeType);
				} else if (col.equals(MediaStore.MediaColumns.DISPLAY_NAME)) {
					row.add(displayName);
				} else if (col.equals(MediaStore.MediaColumns.SIZE)) {
					if (size >= 0)
						row.add(size);
					else {
						// According to Android docs for unknown size.
						// Standard getLong() won't throw exception and
						// value will be 0.
						row.add(null);
					}
				} else {
					// Unsupported or unknown columns are filled up with null
					row.add(null);
				}
			}
			
			return c;
		} else {
			throw new RuntimeException("Unsupported uri");
		}
	}
	
	@Override
	public ParcelFileDescriptor openFile(Uri uri, String mode)
			throws FileNotFoundException {
		if (uri.toString().startsWith(FILE_PROVIDER_PREFIX)) {
			int m = ParcelFileDescriptor.MODE_READ_ONLY;
			if (mode.equalsIgnoreCase("rw"))
				m = ParcelFileDescriptor.MODE_READ_WRITE;
			File f = new File(uri.getPath());
			return ParcelFileDescriptor.open(f, m);
		} else {
			throw new FileNotFoundException	("Unsupported uri: " + uri.toString());
		}
	}

	@Override
	public int update(Uri uri, ContentValues contentvalues, String s, String[] as) {
		// not supported
		return 0;
	}

	public static Uri getUriForFile(Uri uri) {
		String filePath = uri.getSchemeSpecificPart();
		return getUriForFile(filePath);
	}

	public static Uri getUriForFile(String filePath) {
		if (filePath.startsWith("//")) {
			filePath = filePath.substring(2);
		}
		return Uri.parse(FILE_PROVIDER_PREFIX).buildUpon()
				.appendPath(filePath)
				.build();
	}
}
