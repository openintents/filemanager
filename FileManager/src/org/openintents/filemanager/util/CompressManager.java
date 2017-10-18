package org.openintents.filemanager.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.openintents.filemanager.R;
import org.openintents.filemanager.files.FileHolder;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class CompressManager {
    /**
     * TAG for log messages.
     */
    static final String TAG = "CompressManager";

    private static final int BUFFER_SIZE = 1024;
    private Context mContext;
    private ProgressDialog progressDialog;
    private int fileCount;
    private String fileOut;
    private OnCompressFinishedListener onCompressFinishedListener = null;

    public CompressManager(Context context) {
        mContext = context;
    }

    public void compress(FileHolder f, String out) {
        List<FileHolder> list = new ArrayList<>(1);
        list.add(f);
        compress(list, out);
    }

    public void compress(List<FileHolder> list, String out) {
        if (list.isEmpty()) {
            Log.v(TAG, "couldn't compress empty file list");
            return;
        }
        this.fileOut = list.get(0).getFile().getParent() + File.separator + out;
        fileCount = 0;
        for (FileHolder f : list) {
            fileCount += FileUtils.getFileCount(f.getFile());
        }
        new CompressTask().execute(list);
    }

    public CompressManager setOnCompressFinishedListener(
            OnCompressFinishedListener listener) {
        this.onCompressFinishedListener = listener;
        return this;
    }

    public interface OnCompressFinishedListener {
        public abstract void compressFinished();
    }

    private class CompressTask extends
            AsyncTask<List<FileHolder>, Void, Integer> {
        private static final int SUCCESS = 0;
        private static final int ERROR = 1;
        private ZipOutputStream zos;
        private File zipDirectory;
        private boolean cancelCompression = false;

        /**
         * count of compressed file to update the progress bar
         */
        private int isCompressed = 0;

        /**
         * Recursively compress file or directory
         *
         * @returns 0 if successful, error value otherwise.
         */
        private void compressFile(File file, String path) throws IOException {
            progressDialog.setOnDismissListener(new OnDismissListener() {

                @Override
                public void onDismiss(DialogInterface progressDialog) {
                    if (!cancelCompression) {
                        Log.e(TAG, "Dialog Dismissed");
                        Log.e(TAG, "Compression Cancel Attempted");
                        cancelCompression = true;
                        cancel(true);
                    }
                }
            });
            if (!file.isDirectory()) {
                byte[] buf = new byte[BUFFER_SIZE];
                int len;
                FileInputStream in = new FileInputStream(file);
                ZipEntry entry;
                if (path.length() > 0)
                    entry = new ZipEntry(path + "/" + file.getName());
                else
                    entry = new ZipEntry(file.getName());
                entry.setTime(file.lastModified());
                zos.putNextEntry(entry);
                while ((len = in.read(buf)) > 0) {
                    zos.write(buf, 0, len);
                }
                zos.closeEntry();
                in.close();
                return;
            }
            if (file.list() == null || cancelCompression) {
                return;
            }
            for (String fileName : file.list()) {
                if (cancelCompression) {
                    return;
                }
                File f = new File(file.getAbsolutePath() + File.separator
                        + fileName);
                compressFile(f, path + File.separator + file.getName());
                isCompressed++;
                progressDialog.setProgress((isCompressed * 100) / fileCount);
            }
        }

        @Override
        protected void onPreExecute() {
            FileOutputStream out = null;
            zipDirectory = new File(fileOut);
            progressDialog = new ProgressDialog(mContext);
            progressDialog.setCancelable(false);
            progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface progressDialog,
                                            int which) {
                            progressDialog.dismiss();
                            Log.e(TAG, "Dialog Dismiss Detected");
                        }
                    });
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setMessage(mContext.getString(R.string.compressing));
            progressDialog.show();
            progressDialog.setProgress(0);
            try {
                out = new FileOutputStream(zipDirectory);
                zos = new ZipOutputStream(new BufferedOutputStream(out));
            } catch (FileNotFoundException e) {
                Log.e(TAG, "error while creating ZipOutputStream");
            }
        }

        @Override
        protected Integer doInBackground(List<FileHolder>... params) {
            if (zos == null) {
                return ERROR;
            }
            List<FileHolder> list = params[0];
            for (FileHolder file : list) {
                if (cancelCompression) {
                    return ERROR;
                }
                try {
                    compressFile(file.getFile(), "");
                } catch (IOException e) {
                    Log.e(TAG, "Error while compressing", e);
                    return ERROR;
                }
            }
            return SUCCESS;
        }

        @Override
        protected void onCancelled(Integer result) {
            Log.e(TAG, "onCancelled Initialised");
            try {
                zos.flush();
                zos.close();
            } catch (IOException e) {
                Log.e(TAG, "error while closing zos", e);
            }
            if (zipDirectory.delete()) {
                Log.e(TAG, "test deleted successfully");
            } else {
                Log.e(TAG, "error while deleting test");
            }
            Toast.makeText(mContext, "Compression Canceled", Toast.LENGTH_SHORT)
                    .show();

            if (onCompressFinishedListener != null)
                onCompressFinishedListener.compressFinished();
        }

        @Override
        protected void onPostExecute(Integer result) {
            try {
                zos.flush();
                zos.close();
            } catch (IOException e) {
                Log.e(TAG, "error while closing zos", e);
            } catch (NullPointerException e) {
                Log.e(TAG, "zos was null and couldn't be closed", e);
            }
            cancelCompression = true;
            progressDialog.cancel();
            if (result == ERROR) {
                Toast.makeText(mContext, R.string.compressing_error,
                        Toast.LENGTH_SHORT).show();
            } else if (result == SUCCESS) {
                Toast.makeText(mContext, R.string.compressing_success,
                        Toast.LENGTH_SHORT).show();
            }

            if (onCompressFinishedListener != null)
                onCompressFinishedListener.compressFinished();
        }
    }
}
