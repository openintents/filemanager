package org.openintents.filemanager.files;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
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
	
	public FileHolder(File f, Context c){
		mFile = f;
		mMimeType = MimeTypes.newInstance(c).getMimeType(f.getName());
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
		mMimeType = MimeTypes.newInstance(c).getMimeType(f.getName());
		mContext = c;
	}
	
	/**
	 * Slow constructor. Creates the mIcon on construction time.
	 */
	public FileHolder(File f, String m, Context c) {
		mFile = f;
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
		mMimeType = m;
		mContext = c;
	}
	
	public FileHolder(Parcel in){
		mFile = new File(in.readString());
		mMimeType = in.readString();
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
	 * @return The held item's mime type.
	 */
	public String getMimeType() {
		return mMimeType;
	}
	
	public String getFormattedModificationDate(){
		return SimpleDateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(new Date(mFile.lastModified()));
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
	}
	
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
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

}