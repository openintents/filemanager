package org.openintents.filemanager;

import org.openintents.filemanager.util.CopyHelper;

import android.app.Application;
import android.content.Context;
import java.io.File;
import android.os.Build;
import java.util.ArrayList;

public class FileManagerApplication extends Application{
	private CopyHelper mCopyHelper;
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		mCopyHelper = new CopyHelper(this);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
			KitKatExternalStorageDirs.Linker.dirs.createDirs(this);
	}
	
	public CopyHelper getCopyHelper(){
		return mCopyHelper;
	}

	public static File[] getExternalStoragePaths(final Context c) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
			return KitKatExternalStorageDirs.Linker.dirs.getExternalStoragePaths(c);
		return new File[0];
	}

	private static class KitKatExternalStorageDirs {
		void createDirs(final Context c) {
			for (File dir: c.getExternalFilesDirs(null)) {
				if (dir != null && !dir.equals(c.getExternalFilesDir(null))) {
					dir.mkdirs();
				}
			}
		}

		File[] getExternalStoragePaths(final Context c) {
			ArrayList<File> ret = new ArrayList<File>();
			for (File dir: c.getExternalFilesDirs(null)) {
				if (dir != null && !dir.equals(c.getExternalFilesDir(null))) {
					ret.add(dir);
				}
			}
			return ret.toArray(new File[0]);
		}

		private static class Linker { // We need a separate class instance here, so app won't crash on older Android versions
			private static KitKatExternalStorageDirs dirs = new KitKatExternalStorageDirs();
		}
	}
}