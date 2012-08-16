package org.openintents.filemanager.util;

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

import org.openintents.filemanager.R;
import org.openintents.filemanager.files.FileHolder;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

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
        List<FileHolder> list = new ArrayList<FileHolder>(1);
        list.add(f);
        compress(list, out);
    }    

    public void compress(List<FileHolder> list, String out) {
        if (list.isEmpty()){
            Log.v(TAG, "couldn't compress empty file list");
            return;
        }
        this.fileOut = list.get(0).getFile().getParent() + File.separator + out;
        fileCount = 0;
        for (FileHolder f: list){
            fileCount += FileUtils.getFileCount(f.getFile());
        }
        new CompressTask().execute(list);
    }

    private class CompressTask extends AsyncTask<List<FileHolder>, Void, Integer> {
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
                if(path.length() > 0)
                	zos.putNextEntry(new ZipEntry(path + "/" + file.getName()));
                else
                	zos.putNextEntry(new ZipEntry(file.getName()));
                while ((len = in.read(buf)) > 0) {
                    zos.write(buf, 0, len);
                }
                in.close();
                return;
            }
            if (file.list() == null){
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
            progressDialog = new ProgressDialog(mContext);
            progressDialog.setCancelable(false);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setMessage(mContext.getString(R.string.compressing));
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
        protected Integer doInBackground(List<FileHolder>... params) {
            if (zos == null){
                return error;
            }
            List<FileHolder> list = params[0]; 
            for (FileHolder file : list){
                try {
                    compressFile(file.getFile(), "");
                } catch (IOException e) {
                    Log.e(TAG, "Error while compressing", e);
                    return error;
                }
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
                Toast.makeText(mContext, R.string.compressing_error, Toast.LENGTH_SHORT).show();
            } else if (result == success){
                Toast.makeText(mContext, R.string.compressing_success, Toast.LENGTH_SHORT).show();
            }
            
            if(onCompressFinishedListener != null)
            	onCompressFinishedListener.compressFinished();
        }
    }
    
    public interface OnCompressFinishedListener{
    	public abstract void compressFinished();
    }

	public CompressManager setOnCompressFinishedListener(OnCompressFinishedListener listener) {
		this.onCompressFinishedListener = listener;
		return this;
	}
}
