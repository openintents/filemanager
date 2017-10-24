package org.openintents.filemanager.files;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.format.Formatter;

import org.openintents.filemanager.util.FileUtils;
import org.openintents.filemanager.util.MimeTypes;

import java.io.File;
import java.text.DateFormat;
import java.util.Date;

public class FileHolder implements Parcelable, Comparable<FileHolder> {
    public static final Parcelable.Creator<FileHolder> CREATOR = new Parcelable.Creator<FileHolder>() {
        public FileHolder createFromParcel(Parcel in) {
            return new FileHolder(in);
        }

        public FileHolder[] newArray(int size) {
            return new FileHolder[size];
        }
    };
    private File mFile;
    private Drawable mIcon;
    private String mMimeType = "";
    private String mExtension;
    private Boolean mIsDirectory;

    public FileHolder(File f) {
        mFile = f;
        mExtension = parseExtension();
        mMimeType = MimeTypes.getInstance().getMimeType(f.getName());
    }

    public FileHolder(File f, boolean isDirectory) {
        mFile = f;
        mExtension = parseExtension();
        mMimeType = MimeTypes.getInstance().getMimeType(f.getName());
        mIsDirectory = isDirectory;
    }

    /**
     * Fastest constructor as it takes everything ready.
     */
    public FileHolder(File f, String m, Drawable i, boolean isDirectory) {
        mFile = f;
        mIcon = i;
        mExtension = parseExtension();
        mMimeType = m;
        mIsDirectory = isDirectory;
    }

    public FileHolder(Parcel in) {
        mFile = new File(in.readString());
        mMimeType = in.readString();
        mExtension = in.readString();
        byte directoryFlag = in.readByte();
        if (directoryFlag == -1) {
            mIsDirectory = null;
        } else {
            mIsDirectory = (directoryFlag == 1);
        }
    }

    public File getFile() {
        return mFile;
    }

    /**
     * Gets the icon representation of this file. In case of loss through parcel-unparcel.
     *
     * @return The icon.
     */
    public Drawable getIcon() {
        return mIcon;
    }

    public void setIcon(Drawable icon) {
        mIcon = icon;
    }

    /**
     * Shorthand for getFile().getName().
     *
     * @return This file's name.
     */
    public String getName() {
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

    public String getFormattedModificationDate(Context c) {
        DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(c);
        DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(c);
        Date date = new Date(mFile.lastModified());
        return dateFormat.format(date) + " " + timeFormat.format(date);
    }

    /**
     * @param recursive Whether to return size of the whole tree below this file (Directories only).
     */
    public String getFormattedSize(Context c, boolean recursive) {
        return Formatter.formatFileSize(c, getSizeInBytes(recursive));
    }

    private long getSizeInBytes(boolean recursive) {
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
        dest.writeByte((byte) (mIsDirectory == null ? -1 : (mIsDirectory ? 1 : 0)));
    }

    @Override
    public int compareTo(FileHolder another) {
        return mFile.compareTo(another.getFile());
    }

    /**
     * Parse the extension from the filename of the mFile member.
     */
    private String parseExtension() {
        return FileUtils.getExtension(mFile.getName()).toLowerCase();
    }

    @Override
    public String toString() {
        return super.toString() + "-" + getName();
    }
}