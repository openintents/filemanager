package org.openintents.filemanager.files;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

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
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.format.Formatter;
import android.util.TypedValue;

public class FileHolder implements Parcelable, Comparable<FileHolder> {
	private static final String MIME_APK = "application/vnd.android.package-archive";
	
	private File mFile;
	private Drawable mIcon;
	private String mMimeType = "";
	
	public FileHolder(File f){
		mFile = f;
		mMimeType = new MimeTypes().getMimeType(f.getName());
	}
	
	/**
	 * Only use this to create folders. Better leave icon handling for file thumbnails to the {@link #getIcon(Resources)} method.
	 * @param f The file this object will hold.
	 * @param i The icon representing this file.
	 */
	public FileHolder(File f, Drawable i){
		mFile = f;
		mIcon = i;
		mMimeType = new MimeTypes().getMimeType(f.getName());
	}
	
	/**
	 * Slow constructor. Use to cache the mIcon on creation time.
	 */
	public FileHolder(File f, String m, Context c) {
		mFile = f;
		mMimeType = m;
		getIcon(c);
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
	 * @param res Used in case of missing bitmap.
	 * @return The icon.
	 */
	public Drawable getIcon(Context c){
		Resources res = c.getResources();
		if(mIcon == null){
			if(mFile.isDirectory())
				mIcon = new BitmapDrawable(res, BitmapFactory.decodeResource(res, R.drawable.ic_launcher_folder));
			else if (mFile.getAbsolutePath().equals(Environment.getExternalStorageDirectory().getAbsolutePath()))
				mIcon = new BitmapDrawable(res, BitmapFactory.decodeResource(res, R.drawable.ic_launcher_sdcard));
			else {
				mIcon = getScaledDrawableForMimetype(c);
			}
		}
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
	
	public String getFormattedSize(Context c){
		return Formatter.formatFileSize(c, getSizeInBytes());
	}
	
	private long getSizeInBytes(){
// FIXME Temporarily removed as it made the List hang. It is bad for too deep trees.
//		if (mFile.isDirectory())
//			return FileUtils.folderSize(mFile);
//		else
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

	/**
	 * Return the Drawable that is associated with a specific mime type for the VIEW action.
	 */
	private Drawable getDrawableForMimetype(Context context) {
		if (mMimeType == null) {
			return null;
		}

		PackageManager pm = context.getPackageManager();

		// Returns the icon packaged in files with the .apk MIME type.
		if (mMimeType.equals(MIME_APK)) {
			String path = mFile.getPath();
			PackageInfo pInfo = pm.getPackageArchiveInfo(path,
					PackageManager.GET_ACTIVITIES);
			if (pInfo != null) {
				ApplicationInfo aInfo = pInfo.applicationInfo;

				// Bug in SDK versions >= 8. See here:
				// http://code.google.com/p/android/issues/detail?id=9151
				if (Build.VERSION.SDK_INT >= 8) {
					aInfo.sourceDir = path;
					aInfo.publicSourceDir = path;
				}

				return aInfo.loadIcon(pm);
			}
		}

		int iconResource = new MimeTypes().getIcon(mMimeType);
		Drawable ret = null;
		if (iconResource > 0) {
			try {
				ret = pm.getResourcesForApplication(context.getPackageName())
						.getDrawable(iconResource);
			} catch (NotFoundException e) {
			} catch (NameNotFoundException e) {
			}
		}

		if (ret != null) {
			return ret;
		}

		Uri data = FileUtils.getUri(mFile);

		Intent intent = new Intent(Intent.ACTION_VIEW);
		// intent.setType(mimetype);

		// Let's probe the intent exactly in the same way as the VIEW action
		// is performed in FileManagerActivity.openFile(..)
		intent.setDataAndType(data, mMimeType);

		final List<ResolveInfo> lri = pm.queryIntentActivities(intent,
				PackageManager.MATCH_DEFAULT_ONLY);

		if (lri != null && lri.size() > 0) {
			// Log.i(TAG, "lri.size()" + lri.size());

			// return first element
			int index = 0;

			// Actually first element should be "best match",
			// but it seems that more recently installed applications
			// could be even better match.
			index = lri.size() - 1;

			final ResolveInfo ri = lri.get(index);
			return ri.loadIcon(pm);
		}

		return null;
	}
	
	private Drawable getScaledDrawableForMimetype(Context context){
		Drawable d = getDrawableForMimetype(context);
		
		if (d == null) {
			return new BitmapDrawable(context.getResources(), BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher_sdcard));
		} else {
			int size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, context.getResources().getDisplayMetrics());
			// Resizing image.
			return ImageUtils.resizeDrawable(d, size, size);
		}
	}
}