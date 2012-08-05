package org.openintents.filemanager.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.openintents.filemanager.files.FileHolder;

import android.content.Context;
import android.widget.Toast;

/**
 * This class helps simplify copying and moving of files and folders by providing a simple interface to the operations and handling the actual operation transparently.
 * @author George Venios
 *
 */
public class CopyHelper {
	private static final int COPY_BUFFER_SIZE = 32 * 1024;
	public static enum Operation {
		COPY, CUT
	}
	
	private Context mContext;
	private List<FileHolder> mClipboard;
	private Operation mOperation;
	
	public CopyHelper(Context c){
		mContext = c;
	}
	
	public void copy(List<FileHolder> tbc){
		mOperation = Operation.COPY;
		
		mClipboard = tbc;
	}
	
	public void copy(FileHolder tbc){
		ArrayList<FileHolder> tbcl = new ArrayList<FileHolder>();
		tbcl.add(tbc);
		copy(tbcl);
	}
	
	public void cut(List<FileHolder> tbc){
		mOperation = Operation.CUT;
		
		mClipboard = tbc;
	}
	
	public void cut(FileHolder tbc){
		ArrayList<FileHolder> tbcl = new ArrayList<FileHolder>();
		tbcl.add(tbc);
		cut(tbcl);
	}
	
	public boolean paste(File copyTo){
		// Quick check just to make sure. Normally this should never be the case as the path we get is not user-generated.
		if(!copyTo.isDirectory())
			return false;
		
		boolean res;
		
		switch (mOperation) {
		case COPY:
			// TODO async this ;)
			Toast.makeText(mContext, "COPYING", Toast.LENGTH_SHORT).show();
			res = performCopy(copyTo);
			Toast.makeText(mContext, "COPIED:"+res, Toast.LENGTH_SHORT).show();
			break;
		case CUT:
			// TODO async this ;)
			Toast.makeText(mContext, "MOVING", Toast.LENGTH_SHORT).show();
			res = performCut(copyTo);
			Toast.makeText(mContext, "MOVED:"+res, Toast.LENGTH_SHORT).show();
			break;
		default:
			res = false;
			break;
		}
		
		// Clear as the references have been invalidated.
		mClipboard.clear();
		return res;
	}
	
	/**
	 * Call this to check whether there are file references on the clipboard. 
	 */
	public boolean canPaste(){
		return mClipboard != null && !mClipboard.isEmpty();
	}
	
	/**
	 * Call this to actually copy.
	 * @param dest The path to copy the clipboard into.
	 * @return false if ANY error has occurred. This may mean that some files have been successfully copied, but not all. 
	 */
	private boolean performCopy(File dest){
		boolean res = true;
		
		for(FileHolder fh : mClipboard){
			if(fh.getFile().isFile())
				res &= copyFile(fh.getFile(), FileUtils.createUniqueCopyName(mContext, dest, fh.getName()));
			else
				res &= copyFolder(fh.getFile(), FileUtils.createUniqueCopyName(mContext, dest, fh.getName()));
		}
		return res;
	}	

	/**
	 * Copy a file.
	 * @param oldFile File to copy.
	 * @param newFile The file to be created.
	 * @return Was copy successful?
	 */
	private boolean copyFile(File oldFile, File newFile) {
		try {
			FileInputStream input = new FileInputStream(oldFile);
			FileOutputStream output = new FileOutputStream(newFile);
		
			byte[] buffer = new byte[COPY_BUFFER_SIZE];
			
			while (true) {
				int bytes = input.read(buffer);
				
				if (bytes <= 0) {
					break;
				}
				
				output.write(buffer, 0, bytes);
			}
			
			output.close();
			input.close();
			
		} catch (Exception e) {
		    return false;
		}
		return true;
	}
	
	/**
	 * Recursively copy a folder.
	 * @param oldFile Folder to copy.
	 * @param newFile The dir to be created.
	 * @return Was copy successful?
	 */
    private boolean copyFolder(File oldFile, File newFile) {
    	boolean res = true;
    	
		if (oldFile.isDirectory()) {
			// if directory not exists, create it
			if (!newFile.exists()) {
				newFile.mkdir();
				// System.out.println("Directory copied from " + src + "  to " + dest);
			}

			// list all the directory contents
			String files[] = oldFile.list();

			for (String file : files) {
				// construct the src and dest file structure
				File srcFile = new File(oldFile, file);
				File destFile = new File(newFile, file);
				// recursive copy
				res &= copyFolder(srcFile, destFile);
			}
		} else {
			res &= copyFile(oldFile, newFile);
		}
		
		return res;
	}

	/**
	 * Call this to actually move.
	 * @param dest The path to move the clipboard into.
	 * @return false if ANY error has occurred. This may mean that some files have been successfully moved, but not all. 
	 */
	private boolean performCut(File dest){
		boolean res = true;
		
		for(FileHolder fh : mClipboard){
			res &= fh.getFile().renameTo(FileUtils.getFile(dest, fh.getName()));
		}
		return res;
	}
}