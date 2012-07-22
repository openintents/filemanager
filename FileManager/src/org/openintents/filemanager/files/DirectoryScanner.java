package org.openintents.filemanager.files;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.openintents.filemanager.PreferenceActivity;
import org.openintents.filemanager.R;
import org.openintents.filemanager.util.FileUtils;
import org.openintents.filemanager.util.MimeTypes;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

public class DirectoryScanner extends Thread {
	/** List of contents is ready. */
	public static final int MESSAGE_SHOW_DIRECTORY_CONTENTS = 500;	// List of contents is ready, obj = DirectoryContents
	public static final int MESSAGE_SET_PROGRESS = 501;	// Set progress bar, arg1 = current value, arg2 = max value
	
    private static final String TAG = "OIFM_DirScanner";
	
	private File currentDirectory;
	boolean cancelled;

	private String mSdCardPath;
	private Context context;
    private MimeTypes mMimeTypes;
	private Handler handler;
	private String mFilterFiletype;
	private String mFilterMimetype;

	private boolean mWriteableOnly;

	private boolean mDirectoriesOnly;
	
	// Update progress bar every n files
	static final private int PROGRESS_STEPS = 50;
	
	// Scan related variables.
	private int totalCount, progress;
	private long operationStartTime;
	private boolean noMedia, displayHidden;
	private Drawable sdIcon, folderIcon;
	private File[] files;
	/** We keep all these three instead of one, so that sorting is done separately on each. */
	private List<FileHolder> listDir, listFile, listSdCard;

	public DirectoryScanner(File directory, Context context, Handler handler, MimeTypes mimeTypes, String filterFiletype, String filterMimetype, boolean writeableOnly, boolean directoriesOnly) {
		super("Directory Scanner");
		currentDirectory = directory;
		this.context = context;
		this.handler = handler;
		this.mMimeTypes = mimeTypes;
		this.mFilterFiletype = filterFiletype;
		this.mFilterMimetype = filterMimetype;
		this.mSdCardPath = Environment.getExternalStorageDirectory().getAbsolutePath();
		this.mWriteableOnly = writeableOnly;
		this.mDirectoriesOnly = directoriesOnly;
	}
	
	/**
	 * Remove all references so we don't delay the garbage collection.
	 */
	private void clearData() {
		// Remove all references so we don't delay the garbage collection.
		context = null;
		mMimeTypes = null;
		handler = null;
	}

	private void init(){
		Log.v(TAG, "Scanning directory " + currentDirectory);
		
		if (cancelled) {
			Log.v(TAG, "Scan aborted");
			clearData();
			return;
		}
		
		totalCount = 0;
		progress = 0;
		files = currentDirectory.listFiles();
		noMedia = false;
		displayHidden = PreferenceActivity.getDisplayHiddenFiles(context);
		sdIcon = context.getResources().getDrawable(R.drawable.ic_launcher_sdcard);
		folderIcon = context.getResources().getDrawable(R.drawable.ic_launcher_folder);
		
		operationStartTime = SystemClock.uptimeMillis();
		
		if (files == null) {
			Log.v(TAG, "Returned null - inaccessible directory?");
		} else {
			totalCount = files.length;
		}
		Log.v(TAG, "Total count=" + totalCount + ")");
		
		/** Directory container */
 		listDir = new ArrayList<FileHolder>(totalCount);
		/** File container */
 		listFile = new ArrayList<FileHolder>(totalCount);
		/** External storage container*/
		listSdCard = new ArrayList<FileHolder>(3);
	}
	
	public void run() {
		init();
		
		if (files != null) {
			for (File currentFile : files){ 
				if (cancelled) {
					Log.v(TAG, "Scan aborted while checking files");
					clearData();
					return;
				}
				
				progress++;
				updateProgress(progress, totalCount);
				
				//If the user doesn't want to display hidden files and the file is hidden, ignore this file.
				if (!displayHidden && currentFile.isHidden()){
					continue;
				}
				
				// It's a directory. Handle it.
				if (currentFile.isDirectory()) { 
					// It's the sd card.
					if (currentFile.getAbsolutePath().equals(mSdCardPath)) {
						listSdCard.add(new FileHolder(currentFile, sdIcon, context));
					}
					// It's a normal directory.
					else {
						if (!mWriteableOnly || currentFile.canWrite())
							listDir.add(new FileHolder(currentFile, folderIcon, context));
					} 
				// It's a file. Handle it too :P
				} else { 
					String fileName = currentFile.getName(); 
					
					// It's the noMedia file. Raise the flag.
					if(fileName.equalsIgnoreCase(".nomedia"))
						noMedia = true;

					// Get the file's mimetype.
					String mimetype = mMimeTypes.getMimeType(fileName);
					String filetype = FileUtils.getExtension(fileName);
					
					boolean ext_allow = filetype.equalsIgnoreCase(mFilterFiletype) || mFilterFiletype == "";
					boolean mime_allow = mFilterMimetype != null && 
							(mimetype.contentEquals(mFilterMimetype) || mFilterMimetype.contentEquals("*/*") ||
									mFilterFiletype == null);
					if (!mDirectoriesOnly && (ext_allow || mime_allow)) {
						// Take advantage of the already parsed mimeType to set a specific icon. 
						listFile.add(new FileHolder(currentFile,  mimetype, context));
					}
				} 
			}
		}
		
		Log.v(TAG, "Sorting results...");
		int sortBy = PreferenceActivity.getSortBy(context);
		boolean ascending = PreferenceActivity.getAscending(context);

		Collections.sort(listSdCard); 
		Collections.sort(listDir, Comparators.getForDirectory(sortBy, ascending)); 
		Collections.sort(listFile, Comparators.getForFile(sortBy, ascending)); 

		if (!cancelled) {
			Log.v(TAG, "Sending data back to main thread");
			
			DirectoryContents contents = new DirectoryContents();

			contents.listDir = listDir;
			contents.listFile = listFile;
			contents.listSdCard = listSdCard;
			contents.noMedia = noMedia;

			Message msg = handler.obtainMessage(MESSAGE_SHOW_DIRECTORY_CONTENTS);
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
			Message msg = handler.obtainMessage(MESSAGE_SET_PROGRESS);
			msg.arg1 = progress;
			msg.arg2 = maxProgress;
			msg.sendToTarget();
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
	
	
	public static Comparator<FileHolder> getForFile(int comparator, boolean ascending){
		switch(comparator){
		case NAME: return new NameComparator(ascending);
		case SIZE: return new SizeComparator(ascending);
		case LAST_MODIFIED: return new LastModifiedComparator(ascending);
		default: return null;
		}
	}
	public static Comparator<FileHolder> getForDirectory(int comparator, boolean ascending){
		switch(comparator){
		case NAME: return new NameComparator(ascending);
		case SIZE: return new NameComparator(ascending); //Not a bug! Getting directory's size is verry slow
		case LAST_MODIFIED: return new LastModifiedComparator(ascending);
		default: return null;
		}
	}
}


abstract class FileHolderComparator implements Comparator<FileHolder>{
	protected boolean ascending = true;
	
	public FileHolderComparator(boolean asc){
		ascending = asc;
	}
	
	public FileHolderComparator(){
		this(true);
	}
	
	public int compare(FileHolder f1, FileHolder f2){
		return comp((ascending ? f1 : f2), (ascending ? f2 : f1));
	}
	
	protected abstract int comp(FileHolder f1, FileHolder f2);
}

class NameComparator extends FileHolderComparator{
	public NameComparator(boolean asc){
		super(asc);
	}
	
	@Override
	protected int comp(FileHolder f1, FileHolder f2) {
	    return f1.getName().toLowerCase().compareTo(f2.getName().toLowerCase());
	}
}

class SizeComparator extends FileHolderComparator{
	public SizeComparator(boolean asc){
		super(asc);
	}

	@Override
	protected int comp(FileHolder f1, FileHolder f2) {
	    return ((Long)f1.getFile().length()).compareTo(f2.getFile().length());
	}
}

class LastModifiedComparator extends FileHolderComparator{
	public LastModifiedComparator(boolean asc){
		super(asc);
	}

	@Override
	protected int comp(FileHolder f1, FileHolder f2) {
	    return ((Long)f1.getFile().lastModified()).compareTo(f2.getFile().lastModified());
	}
}