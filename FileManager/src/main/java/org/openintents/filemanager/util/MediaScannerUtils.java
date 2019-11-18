package org.openintents.filemanager.util;

import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;

import org.openintents.filemanager.files.FileHolder;

import java.io.File;
import java.util.List;

public abstract class MediaScannerUtils {
    /**
     * Request a MediaScanner scan for a single file.
     */
    public static void informFileAdded(Context c, File f) {
        if (f == null)
            return;

        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        intent.setData(Uri.fromFile(f));
        c.sendBroadcast(intent);
    }

    /**
     * Request a MediaScanner scan for multiple files.
     */
    public static void informFilesAdded(Context c, File[] files) {
        // NOTE: it seemed like overkill having to create a Helper class to
        // avoid VerifyError on 1.6 so that we can use MediaScannerConnection.scanFile()
        // Therefore we just iterate through files and use the compatible-with-every-version broadcast.
        for (int i = 0; i < files.length; i++)
            informFileAdded(c, files[i]);
    }

    /**
     * Request a MediaScanner scan for multiple files.
     */
    public static void informFilesAdded(Context c, List<FileHolder> files) {
        // NOTE: it seemed like overkill having to create a Helper class to
        // avoid VerifyError on 1.6 so that we can use MediaScannerConnection.scanFile()
        // Therefore we just iterate through files and use the compatible-with-every-version broadcast.
        for (FileHolder fh : files)
            informFileAdded(c, fh.getFile());
    }

    public static void informFileDeleted(Context c, File f) {
        String[] file = new String[]{f.getPath()};

        MediaScannerConnection.scanFile(c, file, null, null);
    }

    public static void informFilesDeleted(Context c, File[] files) {
        for (File f : files)
            informFileDeleted(c, f);
    }

    public static void informFilesDeleted(Context c, List<FileHolder> files) {
        for (FileHolder fh : files)
            informFileDeleted(c, fh.getFile());
    }
}