/* 
 * Copyright (C) 2007-2008 OpenIntents.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openintents.filemanager.util;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;

import org.openintents.filemanager.IntentFilterActivity;
import org.openintents.filemanager.R;
import org.openintents.filemanager.files.FileHolder;
import org.openintents.intents.FileManagerIntents;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.provider.MediaStore.Audio;
import android.provider.MediaStore.Video;
import android.text.format.DateFormat;
import android.text.format.Formatter;
import android.util.Log;
import android.widget.Toast;

/**
 * @version 2009-07-03
 * 
 * @author Peli
 *
 */
public class FileUtils {
	
	/** TAG for log messages. */
	static final String TAG = "FileUtils";
	private static final int X_OK = 1;
	public static final String NOMEDIA_FILE_NAME = ".nomedia";
	
	private static boolean libLoadSuccess;
	
	static {
		try {
			System.loadLibrary("access");
			libLoadSuccess = true;
		} catch(UnsatisfiedLinkError e) {
			libLoadSuccess = false;
			Log.d(TAG, "libaccess.so failed to load.");
		}
	}

    /**
     * use it to calculate file count in the directory recursively
     */
    private static int fileCount = 0;

	private FileUtils() {
	}

	/**
	 * Whether the filename is a video file.
	 * 
	 * @param filename
	 * @return
	 *//*
	public static boolean isVideo(String filename) {
		String mimeType = getMimeType(filename);
		if (mimeType != null && mimeType.startsWith("video/")) {
			return true;
		} else {
			return false;
		}
	}*/

	/**
	 * Whether the URI is a local one.
	 * 
	 * @param uri
	 * @return
	 */
	public static boolean isLocal(String uri) {
		if (uri != null && !uri.startsWith("http://")) {
			return true;
		}
		return false;
	}

	/**
	 * Gets the extension of a file name, like ".png" or ".jpg".
	 * 
	 * @param uri
	 * @return Extension including the dot("."); "" if there is no extension;
	 *         null if uri was null.
	 */
	public static String getExtension(String uri) {
		if (uri == null) {
			return null;
		}

		int dot = uri.lastIndexOf(".");
		if (dot >= 0) {
			return uri.substring(dot);
		} else {
			// No extension.
			return "";
		}
	}

	/**
	 * Returns true if uri is a media uri.
	 * 
	 * @param uri
	 * @return
	 */
	public static boolean isMediaUri(String uri) {
		return uri.startsWith(Audio.Media.INTERNAL_CONTENT_URI.toString())
				|| uri.startsWith(Audio.Media.EXTERNAL_CONTENT_URI.toString())
				|| uri.startsWith(Video.Media.INTERNAL_CONTENT_URI.toString())
				|| uri.startsWith(Video.Media.EXTERNAL_CONTENT_URI.toString());
	}
	
	/**
	 * Convert File into Uri.
	 * @param file
	 * @return uri
	 */
	public static Uri getUri(File file) {
		if (file != null) {
			return Uri.fromFile(file);
		}
		return null;
	}
	
	/**
	 * Convert Uri into File.
	 * @param uri
	 * @return file
	 */
	public static File getFile(Uri uri) {
		if (uri != null) {
			String filepath = uri.getPath();
			if (filepath != null) {
				return new File(filepath);
			}
		}
		return null;
	}
	
	/**
	 * Returns the path only (without file name).
	 * @param file
	 * @return
	 */
	public static File getPathWithoutFilename(File file) {
		 if (file != null) {
			 if (file.isDirectory()) {
				 // no file to be split off. Return everything
				 return file;
			 } else {
				 String filename = file.getName();
				 String filepath = file.getAbsolutePath();
	  
				 // Construct path without file name.
				 String pathwithoutname = filepath.substring(0, filepath.length() - filename.length());
				 if (pathwithoutname.endsWith("/")) {
					 pathwithoutname = pathwithoutname.substring(0, pathwithoutname.length() - 1);
				 }
				 return new File(pathwithoutname);
			 }
		 }
		 return null;
	}

	/**
	 * Constructs a file from a path and file name.
	 * 
	 * @param curdir
	 * @param file
	 * @return
	 */
	public static File getFile(String curdir, String file) {
		String separator = "/";
		if (curdir.endsWith("/")) {
			separator = "";
		}
		return new File(curdir + separator + file);
	}
	
	public static File getFile(File curdir, String file) {
		return getFile(curdir.getAbsolutePath(), file);
	}
	
	public static String formatSize(Context context, long sizeInBytes) {
		return Formatter.formatFileSize(context, sizeInBytes);
	}

	public static long folderSize(File directory) {
		long length = 0;
		File[] files = directory.listFiles();
		if(files != null)
			for (File file : files)
				if (file.isFile())
					length += file.length();
				else
					length += folderSize(file);
		return length;
	}
	
	public static String formatDate(Context context, long dateTime) {
		return DateFormat.getDateFormat(context).format(new Date(dateTime));
	}

    public static int getFileCount(File file){
        fileCount = 0;
        calculateFileCount(file);
        return fileCount;
    }

    /**
     * @param f  - file which need be checked
     * @return if is archive - returns true othewise
     */
    public static boolean checkIfZipArchive(File f){
    	int l = f.getName().length();
    	// TODO test
    	if(f.isFile() && FileUtils.getExtension(f.getAbsolutePath()).equals(".zip"))
    		return true;
    	return false;
    	
    	// Old way. REALLY slow. Too slow for realtime action loading.
//        try {
//            new ZipFile(f);
//            return true;
//        } catch (Exception e){
//            return false;
//        }
    }

    /**
     * Recursively count all files in the <code>file</code>'s subtree.
     * @param file The root of the tree to count.
     */
    private static void calculateFileCount(File file){
        if (!file.isDirectory()){
            fileCount++;
            return;
        }
        if (file.list() == null){
            return;
        }
        for (String fileName: file.list()){
            File f = new File(file.getAbsolutePath()+File.separator+fileName);
            calculateFileCount(f);
        }
    }
	
	/**
	 * Native helper method, returns whether the current process has execute privilages.
	 * @param a File
	 * @return returns TRUE if the current process has execute privilages.
	 */
	public static boolean canExecute(File mContextFile) {
		try {
			// File.canExecute() was introduced in API 9.  If it doesn't exist, then
			// this will throw an exception and the NDK version will be used.
			Method m = File.class.getMethod("canExecute", new Class[] {} );
			return (Boolean)m.invoke(mContextFile);
		} catch (Exception e) {
			if(libLoadSuccess){
				return access(mContextFile.getPath(), X_OK);
			} else {
				return false;
			}
		}
	}
	
	// Native interface to unistd.h's access(*char, int) method.
	public static native boolean access(String path, int mode);
	
	/**
	 * @param path The path that the file is supposed to be in.
	 * @param fileName Desired file name. This name will be modified to create a unique file if necessary.
	 * @return A file name that is guaranteed to not exist yet. MAY RETURN NULL!
	 */
	public static File createUniqueCopyName(Context context, File path, String fileName) {
		// Does that file exist?
		File file = FileUtils.getFile(path, fileName);
		
		if (!file.exists()) {
			// Nope - we can take that.
			return file;
		}
		
		// Split file's name and extension to fix internationalization issue #307
		int fromIndex = fileName.lastIndexOf('.');
		String extension = "";
		if (fromIndex > 0) {
			extension = fileName.substring(fromIndex);
			fileName = fileName.substring(0, fromIndex);
		}
		
		// Try a simple "copy of".
		file = FileUtils.getFile(path, context.getString(R.string.copied_file_name, fileName).concat(extension));
		
		if (!file.exists()) {
			// Nope - we can take that.
			return file;
		}
		
		int copyIndex = 2;
		
		// Well, we gotta find a unique name at some point.
		while (copyIndex < 500) {
			file = FileUtils.getFile(path, context.getString(R.string.copied_file_name_2, copyIndex, fileName).concat(extension));
			
			if (!file.exists()) {
				// Nope - we can take that.
				return file;
			}

			copyIndex++;
		}
	
		// I GIVE UP.
		return null;
	}	
	
	/**
	 * Attempts to open a file for viewing.
	 * 
	 * @param fileholder The holder of the file to open.
	 */
	public static void openFile(FileHolder fileholder, Context c) {
		Intent intent = new Intent(android.content.Intent.ACTION_VIEW);

		Uri data = FileUtils.getUri(fileholder.getFile());
		String type = fileholder.getMimeType();
		
		if ("*/*".equals(type)){
			intent.setData(data);
            intent.putExtra(FileManagerIntents.EXTRA_FROM_OI_FILEMANAGER, true);
		} else {
			intent.setDataAndType(data, type);
		}
		
		

		try {
            List<ResolveInfo> activities = c.getPackageManager().queryIntentActivities(intent, PackageManager.GET_ACTIVITIES);
            if (activities.isEmpty() || (activities.size() == 1 && c.getApplicationInfo().packageName.equals(activities.get(0).activityInfo.packageName))){
                Toast.makeText(c, R.string.application_not_available, Toast.LENGTH_SHORT).show();
                return;
            } else {
                c.startActivity(intent);
            }
		} catch (ActivityNotFoundException|SecurityException e) {
			Toast.makeText(c.getApplicationContext(), R.string.application_not_available, Toast.LENGTH_SHORT).show();
		}
	}

	public static String getNameWithoutExtension(File f) {
		return f.getName().substring(0, f.getName().length() - getExtension(getUri(f).toString()).length());
	}
}
