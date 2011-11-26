package org.openintents.filemanager;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.openintents.filemanager.util.FileUtils;
import org.openintents.filemanager.util.MimeTypes;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

public class DirectoryScanner extends Thread {

	private static final String TAG = "OIFM_DirScanner";
	
	private File currentDirectory;
	boolean cancel;

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

	// Cupcake-specific methods
    static Method formatter_formatFileSize;

    static {
    	initializeCupcakeInterface();
    }
    


	DirectoryScanner(File directory, Context context, Handler handler, MimeTypes mimeTypes, String filterFiletype, String filterMimetype, String sdCardPath, boolean writeableOnly, boolean directoriesOnly) {
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
		
		if (cancel) {
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
		
		/** Dir separate for sorting */
		List<IconifiedText> listDir = new ArrayList<IconifiedText>(totalCount);

		/** Files separate for sorting */
		List<IconifiedText> listFile = new ArrayList<IconifiedText>(totalCount);

		/** SD card separate for sorting */
		List<IconifiedText> listSdCard = new ArrayList<IconifiedText>(3);
		
		boolean noMedia = false;

		// Cache some commonly used icons.
		Drawable sdIcon = context.getResources().getDrawable(R.drawable.icon_sdcard);
		Drawable folderIcon = context.getResources().getDrawable(R.drawable.ic_launcher_folder);
		Drawable genericFileIcon = context.getResources().getDrawable(R.drawable.icon_file);

		Drawable currentIcon = null; 
		
		boolean displayHiddenFiles = PreferenceActivity.getDisplayHiddenFiles(context);
		
		if (files != null) {
			for (File currentFile : files){ 
				if (cancel) {
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

						listSdCard.add(new IconifiedText( 
								currentFile.getName(), "", currentIcon)); 
					} else {
						currentIcon = folderIcon;
						
						if (!mWriteableOnly || currentFile.canWrite()){
							listDir.add(new IconifiedText( 
									currentFile.getName(), "", currentIcon));
						}
					}
				}else{ 
					String fileName = currentFile.getName(); 
					
					// Is this the ".nomedia" file?
					if (!noMedia) {
						if (fileName.equalsIgnoreCase(".nomedia")) {
							// It is!
							noMedia = true;
						}
					}

					String mimetype = mMimeTypes.getMimeType(fileName);

					currentIcon = getDrawableForMimetype(currentFile, mimetype);
					if (currentIcon == null) {
						currentIcon = genericFileIcon;
					} else {
						int width = genericFileIcon.getIntrinsicWidth();
						int height = genericFileIcon.getIntrinsicHeight();
						// Resizing image.
						currentIcon = resizeDrawable(currentIcon, width, height);

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

					String filetype = FileUtils.getExtension(fileName);
					boolean ext_allow = filetype.equalsIgnoreCase(mFilterFiletype) || mFilterFiletype == "";
					boolean mime_allow = mFilterMimetype != null && 
							(mimetype.contentEquals(mFilterMimetype) || mFilterMimetype.contentEquals("*/*") ||
									mFilterFiletype == null);
					if (!mDirectoriesOnly && (ext_allow || mime_allow)) { 
						listFile.add(new IconifiedText( 
							currentFile.getName(), size + " , " + FileUtils.formatDate(
									context, currentFile.lastModified()), currentIcon));
					}
				} 
			}
		}
		
		Log.v(TAG, "Sorting results...");
		
		//Collections.sort(mListSdCard); 
		Collections.sort(listDir, new ICComparator()); 
		Collections.sort(listFile, new ICComparator()); 

		if (!cancel) {
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

    /**		
     * Resizes specific a Drawable with keeping ratio.		
     * Added for the issue #319.		
     * 
     * @since 2011-09-28
	 */		
    private Drawable resizeDrawable(Drawable drawable, int desireWidth, int desireHeight) {		
        int width = drawable.getIntrinsicWidth();		
    	int height = drawable.getIntrinsicHeight();	
    		
        if (0 < width && 0 < height && desireWidth < width || desireHeight < height) {		
            // Calculate scale		
        	float scale = Math.min((float) desireWidth / (float) width, 
                    (float) desireHeight / (float) height);

            // Draw resized image		
        	Matrix matrix = new Matrix();	
        	matrix.postScale(scale, scale);	
        	Bitmap bitmap = Bitmap.createBitmap(((BitmapDrawable) drawable).getBitmap(), 0, 0, width, height, matrix, true);	
        	Canvas canvas = new Canvas(bitmap);	
        	canvas.drawBitmap(bitmap, 0, 0, null);	
            		
            drawable = new BitmapDrawable(bitmap);		
        }		
    		
        return drawable;		
    }		

    private static void initializeCupcakeInterface() {
        try {
            formatter_formatFileSize = Class.forName("android.text.format.Formatter").getMethod("formatFileSize", Context.class, long.class);
        } catch (Exception ex) {
       	 // This is not cupcake.
       	 return;
        }
    }
}

class ICComparator implements Comparator{
	public int compare(Object o1, Object o2) {
		IconifiedText it1 = (IconifiedText) o1;
		IconifiedText it2 = (IconifiedText) o2;

	    String s1 = it1.getText();
	    String s2 = it2.getText();
	    return s1.toLowerCase().compareTo(s2.toLowerCase());
	  }
	}

