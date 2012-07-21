package org.openintents.filemanager.files;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.openintents.filemanager.FileManagerActivity;
import org.openintents.filemanager.PreferenceActivity;
import org.openintents.filemanager.R;
import org.openintents.filemanager.util.FileUtils;
import org.openintents.filemanager.util.ImageUtils;
import org.openintents.filemanager.util.MimeTypes;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Resources.NotFoundException;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

public class DirectoryScanner extends Thread {

	private static final String TAG = "OIFM_DirScanner";
	
	private File currentDirectory;
	boolean cancelled;

	private String mSdCardPath;
	private Context context;
    private MimeTypes mMimeTypes;
	private Handler handler;
	private long operationStartTime;
	private String mFilterFiletype;
	private String mFilterMimetype;

	private boolean mWriteableOnly;

	private boolean mDirectoriesOnly;
	
	// Update progress bar every n files
	static final private int PROGRESS_STEPS = 50;

	// APK MIME type
	private static final String MIME_APK = "application/vnd.android.package-archive";
	
	// Cupcake-specific methods
    static Method formatter_formatFileSize;

    static {
    	initializeCupcakeInterface();
    }
    


	public DirectoryScanner(File directory, Context context, Handler handler, MimeTypes mimeTypes, String filterFiletype, String filterMimetype, String sdCardPath, boolean writeableOnly, boolean directoriesOnly) {
		super("Directory Scanner");
		currentDirectory = directory;
		this.context = context;
		this.handler = handler;
		this.mMimeTypes = mimeTypes;
		this.mFilterFiletype = filterFiletype;
		this.mFilterMimetype = filterMimetype;
		this.mSdCardPath = sdCardPath;
		this.mWriteableOnly = writeableOnly;
		this.mDirectoriesOnly = directoriesOnly;
	}
	
	private void clearData() {
		// Remove all references so we don't delay the garbage collection.
		context = null;
		mMimeTypes = null;
		handler = null;
	}

	public void run() {
		Log.v(TAG, "Scanning directory " + currentDirectory);
		
		File[] files = currentDirectory.listFiles();

		int fileCount = 0;
		int dirCount = 0;
		int sdCount = 0;
		int totalCount = 0;
		
		if (cancelled) {
			Log.v(TAG, "Scan aborted");
			clearData();
			return;
		}
		
		if (files == null) {
			Log.v(TAG, "Returned null - inaccessible directory?");
			totalCount = 0;
		} else {
			totalCount = files.length;
		}
		
		operationStartTime = SystemClock.uptimeMillis();
		
		Log.v(TAG, "Counting files... (total count=" + totalCount + ")");

		int progress = 0;
		
		/** Dir separate for return after sorting*/
 		List<FileHolder> listDir = new ArrayList<FileHolder>(totalCount);
		/** Dir separate for sorting */
		List<File> listDirFile = new ArrayList<File>(totalCount);

		/** Files separate for return after sorting*/
 		List<FileHolder> listFile = new ArrayList<FileHolder>(totalCount);
		/** Files separate for sorting */
		List<File> listFileFile = new ArrayList<File>(totalCount);

		/** SD card separate for sorting - actually not sorted, so we don't need an ArrayList<File>*/
		List<FileHolder> listSdCard = new ArrayList<FileHolder>(3);
		
		boolean noMedia = false;

		// Cache some commonly used icons.
		Drawable sdIcon = context.getResources().getDrawable(R.drawable.ic_launcher_sdcard);
		Drawable folderIcon = context.getResources().getDrawable(R.drawable.ic_launcher_folder);
		Drawable genericFileIcon = context.getResources().getDrawable(R.drawable.icon_file);

		Drawable currentIcon = null; 
		
		boolean displayHiddenFiles = PreferenceActivity.getDisplayHiddenFiles(context);
		
		if (files != null) {
			for (File currentFile : files){ 
				if (cancelled) {
					// Abort!
					Log.v(TAG, "Scan aborted while checking files");
					clearData();
					return;
				}

				progress++;
				updateProgress(progress, totalCount);

				//If the user doesn't want to display hidden files and the file is hidden,
				//skip displaying the file
				if (!displayHiddenFiles && currentFile.isHidden()){
					continue;
				}
				 			
				
				if (currentFile.isDirectory()) { 
					if (currentFile.getAbsolutePath().equals(mSdCardPath)) {
						currentIcon = sdIcon;

						listSdCard.add(new FileHolder(currentFile, currentIcon)); 
					} else {
						if (!mWriteableOnly || currentFile.canWrite()){
							listDirFile.add(currentFile);
						}
					}
				}else{ 
					String fileName = currentFile.getName(); 
					
					// Is this the ".nomedia" file?
					if(fileName.equalsIgnoreCase(".nomedia"))
						noMedia = true;

					String mimetype = mMimeTypes.getMimeType(fileName);

					String filetype = FileUtils.getExtension(fileName);
					boolean ext_allow = filetype.equalsIgnoreCase(mFilterFiletype) || mFilterFiletype == "";
					boolean mime_allow = mFilterMimetype != null && 
							(mimetype.contentEquals(mFilterMimetype) || mFilterMimetype.contentEquals("*/*") ||
									mFilterFiletype == null);
					if (!mDirectoriesOnly && (ext_allow || mime_allow)) {
						listFileFile.add(currentFile);
					}
				} 
			}
		}
		
		Log.v(TAG, "Sorting results...");
		
		//Collections.sort(mListSdCard); 
		int sortBy = PreferenceActivity.getSortBy(context);
		boolean ascending = PreferenceActivity.getAscending(context);
		
		
		Collections.sort(listDirFile, Comparators.getForDirectory(sortBy, ascending)); 
		Collections.sort(listFileFile, Comparators.getForFile(sortBy, ascending)); 
		
		for(File f : listDirFile){
			listDir.add(new FileHolder(f, folderIcon));
		}
		
		for(File currentFile : listFileFile){
			String mimetype = mMimeTypes.getMimeType(currentFile.getName());
			currentIcon = getDrawableForMimetype(currentFile, mimetype);
			if (currentIcon == null) {
				currentIcon = genericFileIcon;
			} else {
				int width = genericFileIcon.getIntrinsicWidth();
				int height = genericFileIcon.getIntrinsicHeight();
				// Resizing image.
				currentIcon = ImageUtils.resizeDrawable(currentIcon, width, height);

			}

			String size = "";

			try {
				size = (String) formatter_formatFileSize.invoke(null, context, currentFile.length());
			} catch (Exception e) {
				// The file size method is probably null (this is most
				// likely not a Cupcake phone), or something else went wrong.
				// Let's fall back to something primitive, like just the number
				// of KB.
				size = Long.toString(currentFile.length() / 1024);
				size +=" KB";

				// Technically "KB" should come from a string resource,
				// but this is just a Cupcake 1.1 callback, and KB is universal
				// enough.
			}
			
			listFile.add(new FileHolder(currentFile, currentIcon));
		}

		if (!cancelled) {
			Log.v(TAG, "Sending data back to main thread");
			
			DirectoryContents contents = new DirectoryContents();

			contents.listDir = listDir;
			contents.listFile = listFile;
			contents.listSdCard = listSdCard;
			contents.noMedia = noMedia;

			Message msg = handler.obtainMessage(FileManagerActivity.MESSAGE_SHOW_DIRECTORY_CONTENTS);
			msg.obj = contents;
			msg.sendToTarget();
		}

		clearData();
	}
		
	private void updateProgress(int progress, int maxProgress) {
		// Only update the progress bar every n steps...
		if ((progress % PROGRESS_STEPS) == 0) {
			// Also don't update for the first second.
			long curTime = SystemClock.uptimeMillis();
			
			if (curTime - operationStartTime < 1000L) {
				return;
			}
			
			// Okay, send an update.
			Message msg = handler.obtainMessage(FileManagerActivity.MESSAGE_SET_PROGRESS);
			msg.arg1 = progress;
			msg.arg2 = maxProgress;
			msg.sendToTarget();
		}
	}

	/**
     * Return the Drawable that is associated with a specific mime type
     * for the VIEW action.
     * 
     * @param mimetype
     * @return
     */
    Drawable getDrawableForMimetype(File file, String mimetype) {
     if (mimetype == null) {
    	 return null;
     }
     
   	 PackageManager pm = context.getPackageManager();
   	 
   	 // Returns the icon packaged in files with the .apk MIME type.
   	 if(mimetype.equals(MIME_APK)){
   		 String path = file.getPath();
   		 PackageInfo pInfo = pm.getPackageArchiveInfo(path, PackageManager.GET_ACTIVITIES);
   		 if (pInfo!=null) {
	   		 ApplicationInfo aInfo = pInfo.applicationInfo;
	   		 
	   		 // Bug in SDK versions >= 8. See here: http://code.google.com/p/android/issues/detail?id=9151
	   		 if(Build.VERSION.SDK_INT >= 8){
	   			 aInfo.sourceDir = path;
	   			 aInfo.publicSourceDir = path;
	   		 }
	   		 
	   		 return aInfo.loadIcon(pm);
   		 }
   	 }
   	 
   	 int iconResource = mMimeTypes.getIcon(mimetype);
   	 Drawable ret = null;
   	 if(iconResource > 0){
   		 try {
   			 ret = pm.getResourcesForApplication(context.getPackageName()).getDrawable(iconResource);
   		 }catch(NotFoundException e){}
   		 catch(NameNotFoundException e){}
   	 }
   	 
   	 if(ret != null){
   		 return ret;
   	 }
   	 
   	 Uri data = FileUtils.getUri(file);
   	
   	 Intent intent = new Intent(Intent.ACTION_VIEW);
   	 //intent.setType(mimetype);
   	 
   	 // Let's probe the intent exactly in the same way as the VIEW action
   	 // is performed in FileManagerActivity.openFile(..)
     intent.setDataAndType(data, mimetype);
     
   	 final List<ResolveInfo> lri = pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
   	 
   	 if (lri != null && lri.size() > 0) {
   		 //Log.i(TAG, "lri.size()" + lri.size());
   		 
   		 // return first element
   		 int index = 0;
   		 
   		 // Actually first element should be "best match",
   		 // but it seems that more recently installed applications
   		 // could be even better match.
   		 index = lri.size()-1;
   		 
   		 final ResolveInfo ri = lri.get(index);
   		 return ri.loadIcon(pm);
   	 }
   	 
   	 return null;
    }

    private static void initializeCupcakeInterface() {
        try {
            formatter_formatFileSize = Class.forName("android.text.format.Formatter").getMethod("formatFileSize", Context.class, long.class);
        } catch (Exception ex) {
       	 // This is not cupcake.
       	 return;
        }
    }
	
	public void cancel(){
		cancelled = true;
	}
}

/**
 * The container class for all comparators.
 */
class Comparators{
	public static final int NAME = 1;
	public static final int SIZE = 2;
	public static final int LAST_MODIFIED = 3;
	
	
	public static Comparator<File> getForFile(int comparator, boolean ascending){
		switch(comparator){
		case NAME: return new NameComparator(ascending);
		case SIZE: return new SizeComparator(ascending);
		case LAST_MODIFIED: return new LastModifiedComparator(ascending);
		default: return null;
		}
	}
	public static Comparator<File> getForDirectory(int comparator, boolean ascending){
		switch(comparator){
		case NAME: return new NameComparator(ascending);
		case SIZE: return new NameComparator(ascending); //Not a bug! Getting directory's size is verry slow
		case LAST_MODIFIED: return new LastModifiedComparator(ascending);
		default: return null;
		}
	}
}


abstract class FileComparator implements Comparator<File>{
	protected boolean ascending = true;
	
	public FileComparator(boolean asc){
		ascending = asc;
	}
	
	public FileComparator(){
		this(true);
	}
	
	public int compare(File f1, File f2){
		return comp((ascending ? f1 : f2), (ascending ? f2 : f1));
	}
	
	protected abstract int comp(File f1, File f2);
}

class NameComparator extends FileComparator{
	public NameComparator(boolean asc){
		super(asc);
	}
	
	protected int comp(File f1, File f2) {
	    return f1.getName().toLowerCase().compareTo(f2.getName().toLowerCase());
	}
}

class SizeComparator extends FileComparator{
	public SizeComparator(boolean asc){
		super(asc);
	}
	
	protected int comp(File f1, File f2) {
	    return ((Long)f1.length()).compareTo(f2.length());
	}
	
	/*//Very inefficient
	private long getFileSize(File f){
    	if(f.isFile())
    		return f.length();
    	int ret = 0;
    	for(File file : f.listFiles())
    		ret += getFileSize(file);
    	
    	return ret;
    }
    */
}

class LastModifiedComparator extends FileComparator{
	public LastModifiedComparator(boolean asc){
		super(asc);
	}
	
	protected int comp(File f1, File f2) {
	    return ((Long)f1.lastModified()).compareTo(f2.lastModified());
	}
}