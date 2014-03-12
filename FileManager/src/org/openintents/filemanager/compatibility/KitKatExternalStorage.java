package org.openintents.filemanager.compatibility;

import android.content.Context;
import java.io.File;
import java.util.ArrayList;

public class KitKatExternalStorage {

	public static File[] getExternalStoragePaths(final Context c) {
		ArrayList<File> ret = new ArrayList<File>();
		for (File dir: c.getExternalFilesDirs(null)) {
			if (dir != null && !dir.equals(c.getExternalFilesDir(null))) {
				ret.add(dir);
			}
		}
		return ret.toArray(new File[0]);
	}

	public static void createWritableDirs(final Context c) {
		for (File dir: c.getExternalFilesDirs(null)) {
			if (dir != null && !dir.equals(c.getExternalFilesDir(null))) {
				dir.mkdirs();
			}
		}
	}

}
