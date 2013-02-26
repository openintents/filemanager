package org.openintents.filemanager.util;

import java.io.File;
import java.util.List;

import org.openintents.filemanager.files.FileHolder;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public abstract class MediaScannerUtils {
	/**
	 * Request a MediaScanner scan for a single file.
	 */
	public static void scanFile(Context c, File f) {
		if(f == null)
			return;
		
		Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
		intent.setData(Uri.fromFile(f));
		c.sendBroadcast(intent);
	}

	/**
	 * Request a MediaScanner scan for multiple files.
	 */
	public static void scanFiles(Context c, File[] files) {
		// NOTE: it seemed like overkill having to create a Helper class to 
		// avoid VerifyError on 1.6 so that we can use MediaScannerConnection.scanFile()
		// Therefore we just iterate through files and use the compatible-with-every-version broadcast.
		for(int i = 0; i < files.length; i++)
			scanFile(c, files[i]);
	}
	
	/**
	 * Request a MediaScanner scan for multiple files.
	 */
	public static void scanFiles(Context c, List<FileHolder> files) {
		// NOTE: it seemed like overkill having to create a Helper class to 
		// avoid VerifyError on 1.6 so that we can use MediaScannerConnection.scanFile()
		// Therefore we just iterate through files and use the compatible-with-every-version broadcast.
		for(FileHolder fh : files)
			scanFile(c, fh.getFile());
	}
}