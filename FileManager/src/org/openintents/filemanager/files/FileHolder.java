package org.openintents.filemanager.files;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.openintents.filemanager.R;

import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;

public class FileHolder implements Parcelable, Comparable<FileHolder> {
	private File mFile;
	private Drawable mIcon;
	
	public FileHolder(File f){
		mFile = f;
	}
	
	public FileHolder(File f, Drawable i){
		mFile = f;
		mIcon = i;
	}
	
	public FileHolder(Parcel in){
		mFile = new File(in.readString());
	}
	
	public File getFile(){
		return mFile;
	}
	
	/**
	 * Gets the icon representation of this file. Creates it if it's not already stored.
	 * @param res Used in case of missing bitmap.
	 * @return The icon.
	 */
	public Drawable getIcon(Resources res){
		// TODO get real icon.
		if(mIcon == null)
			mIcon = new BitmapDrawable(res, BitmapFactory.decodeResource(res, R.drawable.ic_launcher_file));
		return mIcon;
	}
	
	/**
	 * Shorthand for getFile().getName().
	 * @return This file's name. 
	 */
	public String getName(){
		return mFile.getName();
	}
	
	public String getFormatedModificationDate(){
		return SimpleDateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(new Date(mFile.lastModified()));
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(mFile.getAbsolutePath());
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