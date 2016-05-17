package org.openintents.filemanager.files;

import java.io.File;
import java.text.DateFormat;
import java.util.Date;

import org.openintents.filemanager.util.FileUtils;
import org.openintents.filemanager.util.MimeTypes;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.format.Formatter;

public class FileHolder implements Parcelable, Comparable<FileHolder> {
	private File mFile;
	private Drawable mIcon;
	private String mMimeType = "";
	private Context mContext;
	private String mExtension;
	
	public FileHolder(File f, Context c){
		mFile = f;
		mExtension = parseExtension();
		mMimeType = MimeTypes.getInstance().getMimeType(f.getName());
		mContext = c;
	}
	
	/**
	 * Only use this to create folders. Better leave icon handling for file thumbnails to the {@link #getIcon(Resources)} method.
	 * @param f The file this object will hold.
	 * @param i The icon representing this file.
	 */
	public FileHolder(File f, Drawable i, Context c){
		mFile = f;
		mIcon = i;
		mExtension = parseExtension();
		mMimeType = MimeTypes.getInstance().getMimeType(f.getName());
		mContext = c;
	}
	
	/**
	 * Slow constructor. Creates the mIcon on construction time.
	 */
	public FileHolder(File f, String m, Context c) {
		mFile = f;
		mExtension = parseExtension();
		mMimeType = m;
		mContext = c;
		getIcon();
	}
	
	/**
	 * Fastest constructor as it takes everything ready.
	 */
	public FileHolder(File f, String m, Drawable i, Context c){
		mFile = f;
		mIcon = i;
		mExtension = parseExtension();
		mMimeType = m;
		mContext = c;
	}
	
	public FileHolder(Parcel in){
		mFile = new File(in.readString());
		mMimeType = in.readString();
		mExtension = in.readString();
	}

	public File getFile(){
		return mFile;
	}
	
	/**
	 * Gets the icon representation of this file. Creates it if it's not already stored. -- In case of loss through parcel-unparcel.
	 * @return The icon.
	 */
	public Drawable getIcon(){
		return mIcon;
	}

	public void setIcon(Drawable icon) {
		mIcon = icon;
	}
	
	/**
	 * Shorthand for getFile().getName().
	 * @return This file's name. 
	 */
	public String getName(){
		return mFile.getName();
	}
	
	/**
	 * Get the contained file's extension.
	 */
	public String getExtension() {
		return mExtension;
	}
	
	/**
	 * @return The held item's mime type.
	 */
	public String getMimeType() {
		return mMimeType;
	}
	
	public String getFormattedModificationDate(Context c){
		DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(c);
		DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(c);
		Date date = new Date(mFile.lastModified());
		return dateFormat.format(date) + " " + timeFormat.format(date);		
	}
	
	/**
	 * @param recursive Whether to return size of the whole tree below this file (Directories only).
	 */
	public String getFormattedSize(Context c, boolean recursive){
		return Formatter.formatFileSize(c, getSizeInBytes(recursive));
	}
	
	private long getSizeInBytes(boolean recursive){
		if (recursive && mFile.isDirectory())
			return FileUtils.folderSize(mFile);
		else
			return mFile.length();
	}
	
	@Override
	public int describeContents() {
		return 0;
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(mFile.getAbsolutePath());
		dest.writeString(mMimeType);
		dest.writeString(mExtension);
	}
	
    public static final Parcelable.Creator<FileHolder> CREATOR = new Parcelable.Creator<FileHolder>() {
        public FileHolder createFromParcel(Parcel in) {
            return new FileHolder(in);
        }
 
        public FileHolder[] newArray(int size) {
            return new FileHolder[size];
        }
    };

	@Override
	public int compareTo(FileHolder another) {
		return mFile.compareTo(another.getFile());
	}
	
	/**
	 * Parse the extension from the filename of the mFile member.
	 */  
	private String parseExtension() {
	    String ext = "";
	    String name = mFile.getName();
	    
	    int i = name.lastIndexOf('.');

	    if (i > 0 &&  i < name.length() - 1) {
	        ext = name.substring(i+1).toLowerCase();
	    }
	    return ext;
	}

	@Override
	public String toString() {
		return super.toString() + "-" + getName();
	}
}