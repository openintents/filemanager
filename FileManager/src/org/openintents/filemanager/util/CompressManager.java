package org.openintents.filemanager.util;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
import org.openintents.filemanager.FileManagerActivity;
import org.openintents.filemanager.R;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class CompressManager {
    /**
     * TAG for log messages.
     */
    static final String TAG = "CompressManager";

    private static final int BUFFER_SIZE = 1024;
    private FileManagerActivity activity;
    private ProgressDialog progressDialog;
    private int fileCount;
    private String fileOut;

    public CompressManager(FileManagerActivity activity) {
        this.activity = activity;
    }

    public void compress(File f, String out) {
        this.fileOut = f.getParent()+File.separator+out;
        fileCount = FileUtils.getFileCount(f);
        new CompressTask().execute(f);
    }

    private class CompressTask extends AsyncTask<Object, Void, Integer> {
        private static final int success = 0;
        private static final int error = 1;
        private ZipOutputStream zos;

        /**
         * count of compressed file to update the progress bar
         */
        private int isCompressed = 0;

        /**
         * Recursively compress file or directory
         * @returns 0 if successful, error value otherwise.
         */
        private void compressFile(File file, String path) throws IOException {
            if (!file.isDirectory()){
                byte[] buf = new byte[BUFFER_SIZE];
                int len;
                FileInputStream in = new FileInputStream(file);
                zos.putNextEntry(new ZipEntry(path + "/" + file.getName()));
                while ((len = in.read(buf)) > 0) {
                    zos.write(buf, 0, len);
                }
                in.close();
                return;
            }
            for (String fileName: file.list()){
                File f = new File(file.getAbsolutePath()+File.separator+fileName);
                compressFile(f, path + File.separator + file.getName());
                isCompressed++;
                progressDialog.setProgress((isCompressed * 100)/ fileCount);
            }
        }

        @Override
        protected void onPreExecute() {
            FileOutputStream out = null;
            progressDialog = new ProgressDialog(activity);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setMessage(activity.getResources().getString(R.string.compressing));
            progressDialog.show();
            progressDialog.setProgress(0);
            try {
                out = new FileOutputStream(new File(fileOut));
                zos = new ZipOutputStream(new BufferedOutputStream(out));
            } catch (FileNotFoundException e) {
                Log.e(TAG, "error while creating ZipOutputStream");
            }
        }

        @Override
        protected Integer doInBackground(Object... params) {
            if (zos == null){
                return error;
            }
            File file = (File) params[0];
            try {
                compressFile(file, "");
            } catch (IOException e) {
                Log.e(TAG, "Error while compressing", e);
                return error;
            }
            return success;
        }

        @Override
        protected void onPostExecute(Integer result) {
            try {
                zos.flush();
                zos.close();
            } catch (IOException e) {
                Log.e(TAG, "error while closing zos", e);
            }
            progressDialog.cancel();
            if (result == error){
                Toast.makeText(activity, R.string.compressing_error, Toast.LENGTH_SHORT).show();
            } else if (result == success){
                Toast.makeText(activity, R.string.compressing_success, Toast.LENGTH_SHORT).show();
            }
            activity.refreshList();
        }
    }
}
