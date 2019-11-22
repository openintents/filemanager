package org.openintents.filemanager.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.openintents.filemanager.R;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ExtractManager {
    /**
     * TAG for log messages.
     */
    static final String TAG = "ExtractManager";

    private static final int BUFFER_SIZE = 1024;
    private Context context;
    private ProgressDialog progressDialog;
    private OnExtractFinishedListener onExtractFinishedListener = null;

    public ExtractManager(Context context) {
        this.context = context;
    }

    public void extract(File f, String destinationPath) {
        new ExtractTask().execute(f, destinationPath);
    }

    public ExtractManager setOnExtractFinishedListener(OnExtractFinishedListener listener) {
        this.onExtractFinishedListener = listener;
        return this;
    }

    public interface OnExtractFinishedListener {
        void extractFinished();
    }

    private class ExtractTask extends AsyncTask<Object, Integer, Integer> {
        private static final int SUCCESS = 0;
        private static final int ERROR = 1;

        /**
         * count of extracted files to update the progress bar
         */
        private int isExtracted = 0;
        private int fileCount;

        /**
         * Recursively extract file or directory
         */
        public boolean extract(File archive, String destinationPath) {
            ZipFile zipfile = null;
            try {
                zipfile = new ZipFile(archive);
                fileCount = zipfile.size();
                for (Enumeration e = zipfile.entries(); e.hasMoreElements(); ) {
                    ZipEntry entry = (ZipEntry) e.nextElement();
                    unzipEntry(zipfile, entry, destinationPath);
                    isExtracted++;
                    publishProgress();
                }
                return true;
            } catch (Exception e) {
                Log.e(TAG, "Error while extracting file " + archive, e);
                return false;
            } finally {
                if (zipfile != null) {
                    try {
                        zipfile.close();
                    } catch (IOException e) {
                        // ignore
                    }
                }
            }
        }

        private void createDir(File dir) {
            if (dir.exists()) {
                return;
            }
            Log.i(TAG, "Creating dir " + dir.getName());
            if (!dir.mkdirs()) {
                throw new RuntimeException("Can not create dir " + dir);
            }
        }

        private void unzipEntry(ZipFile zipfile, ZipEntry entry,
                                String outputDir) throws IOException {
            if (entry.isDirectory()) {
                createDir(new File(outputDir, entry.getName()));
                return;
            }
            File outputFile = new File(outputDir, entry.getName());

            String canonicalPath = outputFile.getCanonicalPath();
            if (!canonicalPath.startsWith(outputDir)) {
                throw new IOException("Zip Path Traversal Attack detected for " + canonicalPath);
            }

            File parentFile = outputFile.getParentFile();
            if (parentFile != null && !parentFile.exists()) {
                createDir(parentFile);
            }
            BufferedInputStream inputStream = new BufferedInputStream(zipfile.getInputStream(entry));
            BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outputFile));
            try {
                int len;
                byte[] buf = new byte[BUFFER_SIZE];
                while ((len = inputStream.read(buf)) > 0) {
                    outputStream.write(buf, 0, len);
                }
            } finally {
                outputStream.close();
                inputStream.close();
            }
            outputFile.setLastModified(entry.getTime());
        }

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(context);
            progressDialog.setCancelable(false);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setMessage(context.getResources().getString(R.string.extracting));
            progressDialog.show();
            progressDialog.setProgress(0);
            isExtracted = 0;
        }

        @Override
        protected Integer doInBackground(Object... params) {
            File f = (File) params[0];
            String destination = (String) params[1];
            boolean result = extract(f, destination);
            return result ? SUCCESS : ERROR;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            progressDialog.setProgress((isExtracted * 100) / fileCount);
        }

        @Override
        protected void onPostExecute(Integer result) {
            if (progressDialog.isShowing()) {
                progressDialog.cancel();
            }
            if (result == ERROR) {
                Toast.makeText(context, R.string.extracting_error, Toast.LENGTH_SHORT).show();
            } else if (result == SUCCESS) {
                Toast.makeText(context, R.string.extracting_success, Toast.LENGTH_SHORT).show();
            }

            if (onExtractFinishedListener != null)
                onExtractFinishedListener.extractFinished();
        }
    }
}
