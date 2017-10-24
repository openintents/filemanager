/* 
 * Copyright (C) 2007-2008 OpenIntents.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openintents.filemanager.util;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import org.openintents.filemanager.FileManagerProvider;
import org.openintents.filemanager.R;
import org.openintents.filemanager.files.FileHolder;
import org.openintents.intents.FileManagerIntents;

import java.io.File;
import java.util.List;

/**
 * @author Peli
 * @version 2009-07-03
 */
public class FileUtils {

    public static final String NOMEDIA_FILE_NAME = ".nomedia";
    /**
     * TAG for log messages.
     */
    static final String TAG = "FileUtils";

    private FileUtils() {
    }

    /**
     * Gets the extension of a file name, like ".png" or ".jpg".
     *
     * @param uri
     * @return Extension including the dot("."); "" if there is no extension;
     * null if uri was null.
     */
    public static String getExtension(String uri) {
        if (uri == null) {
            return null;
        }

        int dot = uri.lastIndexOf(".");
        if (dot >= 0) {
            return uri.substring(dot);
        } else {
            // No extension.
            return "";
        }
    }

    /**
     * Convert File into Uri.
     *
     * @param file
     * @return uri
     */
    public static Uri getUri(File file) {
        if (file != null) {
            return Uri.fromFile(file);
        }
        return null;
    }

    /**
     * Convert Uri into File.
     *
     * @param uri
     * @return file
     */
    public static File getFile(Uri uri) {
        if (uri != null) {
            String filepath = uri.getPath();
            if (filepath != null) {
                return new File(filepath);
            }
        }
        return null;
    }

    /**
     * Returns the path only (without file name).
     *
     * @param file
     * @return
     */
    public static File getPathWithoutFilename(File file) {
        if (file != null) {
            if (file.isDirectory()) {
                // no file to be split off. Return everything
                return file;
            } else {
                String filename = file.getName();
                String filepath = file.getAbsolutePath();

                // Construct path without file name.
                String pathwithoutname = filepath.substring(0, filepath.length() - filename.length());
                if (pathwithoutname.endsWith("/")) {
                    pathwithoutname = pathwithoutname.substring(0, pathwithoutname.length() - 1);
                }
                return new File(pathwithoutname);
            }
        }
        return null;
    }

    /**
     * Constructs a file from a path and file name.
     *
     * @param curdir
     * @param file
     * @return
     */
    public static File getFile(String curdir, String file) {
        String separator = "/";
        if (curdir.endsWith("/")) {
            separator = "";
        }
        return new File(curdir + separator + file);
    }

    public static File getFile(File curdir, String file) {
        return getFile(curdir.getAbsolutePath(), file);
    }

    public static long folderSize(File directory) {
        long length = 0;
        File[] files = directory.listFiles();
        if (files != null)
            for (File file : files)
                if (file.isFile())
                    length += file.length();
                else
                    length += folderSize(file);
        return length;
    }

    public static int getFileCount(File file) {
        return calculateFileCount(file, 0);
    }

    /**
     * @param f - file which need be checked
     * @return if is archive - returns true otherwise
     */
    public static boolean checkIfZipArchive(File f) {
        int l = f.getName().length();
        // TODO test
        if (f.isFile() && FileUtils.getExtension(f.getAbsolutePath()).equalsIgnoreCase(".zip"))
            return true;
        return false;

        // Old way. REALLY slow. Too slow for realtime action loading.
//        try {
//            new ZipFile(f);
//            return true;
//        } catch (Exception e){
//            return false;
//        }
    }

    /**
     * Recursively count all files in the <code>file</code>'s subtree.
     *
     * @param countSoFar file count of previous counting
     * @param file The root of the tree to count.
     */
    private static int calculateFileCount(File file, int countSoFar) {
        if (!file.isDirectory()) {
            countSoFar++;
            return countSoFar;
        }
        if (file.list() == null) {
            return countSoFar;
        }
        for (String fileName : file.list()) {
            File f = new File(file.getAbsolutePath() + File.separator + fileName);
            countSoFar += calculateFileCount(f, 0);
        }
        return countSoFar;
    }

    /**
     * Native helper method, returns whether the current process has execute privilages.
     *
     * @param file File
     * @return returns TRUE if the current process has execute privilages.
     */
    public static boolean canExecute(File file) {
        return file.canExecute();
    }

    /**
     * @param path     The path that the file is supposed to be in.
     * @param fileName Desired file name. This name will be modified to create a unique file if necessary.
     * @return A file name that is guaranteed to not exist yet. MAY RETURN NULL!
     */
    public static File createUniqueCopyName(Context context, File path, String fileName) {
        // Does that file exist?
        File file = FileUtils.getFile(path, fileName);

        if (!file.exists()) {
            // Nope - we can take that.
            return file;
        }

        // Split file's name and extension to fix internationalization issue #307
        int fromIndex = fileName.lastIndexOf('.');
        String extension = "";
        if (fromIndex > 0) {
            extension = fileName.substring(fromIndex);
            fileName = fileName.substring(0, fromIndex);
        }

        // Try a simple "copy of".
        file = FileUtils.getFile(path, context.getString(R.string.copied_file_name, fileName).concat(extension));

        if (!file.exists()) {
            // Nope - we can take that.
            return file;
        }

        int copyIndex = 2;

        // Well, we gotta find a unique name at some point.
        while (copyIndex < 500) {
            file = FileUtils.getFile(path, context.getString(R.string.copied_file_name_2, copyIndex, fileName).concat(extension));

            if (!file.exists()) {
                // Nope - we can take that.
                return file;
            }

            copyIndex++;
        }

        // I GIVE UP.
        return null;
    }

    /**
     * Attempts to open a file for viewing.
     *
     * @param fileholder The holder of the file to open.
     */
    public static void openFile(FileHolder fileholder, Context c) {
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW);

        Uri data = FileManagerProvider.getUriForFile(fileholder.getFile().getAbsolutePath());
        String type = fileholder.getMimeType();

        if ("*/*".equals(type)) {
            intent.setData(data);
            intent.putExtra(FileManagerIntents.EXTRA_FROM_OI_FILEMANAGER, true);
        } else {
            intent.setDataAndType(data, type);
        }

        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        try {
            List<ResolveInfo> activities = c.getPackageManager().queryIntentActivities(intent, 0);
            if (activities.isEmpty() || (activities.size() == 1 && c.getApplicationInfo().packageName.equals(activities.get(0).activityInfo.packageName))) {
                Toast.makeText(c, R.string.application_not_available, Toast.LENGTH_SHORT).show();
                return;
            } else {
                c.startActivity(intent);
            }
        } catch (ActivityNotFoundException | SecurityException e) {
            Toast.makeText(c.getApplicationContext(), R.string.application_not_available, Toast.LENGTH_SHORT).show();
        } catch (RuntimeException e) {
            Log.d(TAG, "Couldn't open file", e);
            Toast.makeText(c, "Couldn't open file " + e, Toast.LENGTH_SHORT).show();
        }
    }

    public static String getNameWithoutExtension(File f) {
        return f.getName().substring(0, f.getName().length() - getExtension(getUri(f).toString()).length());
    }
}
