package org.openintents.filemanager.compatibility;

import org.openintents.filemanager.R;

import android.app.Activity;
import android.net.Uri;
import android.widget.Toast;
import android.media.MediaScannerConnection;
import android.util.Log;
import java.io.File;

// TODO: works only for Froyo and newer
public class MediaScannerConnectionHelper {
	private static final String TAG = "MediaScannerConnectionHelper";

	public static void scanFile(final Activity context, final File path) {
		scanFile(context, path, false);
	}

	public static void scanFile(final Activity context, final File path, final boolean silent) {
		// MediaScanner won't descend into directories, so we'll have to iterate over them recursively
		if (path.isDirectory()) {
			String[] children = path.list();
			for (int i=0; i<children.length; i++)
				scanFile(context, new File(path, children[i]), true);
		}

		String paths[] = { path.getPath() };
		MediaScannerConnection.scanFile(context, paths, null,
			new MediaScannerConnection.OnScanCompletedListener() {
				public void onScanCompleted (final String path, final Uri uri) {
					//Log.v(TAG, "Adding file to media gallery: " + path + " - " + (uri == null ? "failure" : uri.toString()));
					if (silent)
						return;
					context.runOnUiThread( new Runnable() {
						public void run() {
							Toast.makeText(context, (uri == null) ?
								R.string.add_to_media_library_failed :
								R.string.add_to_media_library_success,
								Toast.LENGTH_SHORT).show();
						}
					} );
				}
			} );
	}

}
