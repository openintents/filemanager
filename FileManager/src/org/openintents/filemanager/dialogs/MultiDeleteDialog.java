package org.openintents.filemanager.dialogs;

import java.io.File;
import java.util.List;

import org.openintents.filemanager.R;
import org.openintents.filemanager.files.FileHolder;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.Toast;

public class MultiDeleteDialog extends DialogFragment {
	private List<FileHolder> mFileHolders;
	private OnDeleteListener mListener;
	
	public MultiDeleteDialog(List<FileHolder> holders, OnDeleteListener listener){
		mFileHolders = holders;
		mListener = listener;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		return new AlertDialog.Builder(getActivity())
				.setInverseBackgroundForced(true)
				.setTitle(getString(R.string.really_delete_multiselect, mFileHolders.size()))
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
							new RecursiveDeleteTask().execute();
					}
				})
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setNegativeButton(android.R.string.cancel, null)
				.create();
	}
	
	private class RecursiveDeleteTask extends AsyncTask<Void, Void, Void> {
		/**
		 * If 0 some failed, if 1 all succeeded. 
		 */
		private int mResult = 1;
		private ProgressDialog dialog = new ProgressDialog(getActivity());

		/**
		 * Recursively delete a file or directory and all of its children.
		 * 
		 * @returns 0 if successful, error value otherwise.
		 */
		private void recursiveDelete(File file) {
			File[] files = file.listFiles();
			if (files != null && files.length != 0) 
				// If it's a directory delete all children.
				for (File childFile : files) {
					if (childFile.isDirectory()) {
						recursiveDelete(childFile);
					} else {
						mResult *= childFile.delete() ? 1 : 0;
					}
				}
				
				// And then delete parent. -- or just delete the file.
				mResult *= file.delete() ? 1 : 0;
		}
		
		@Override
		protected void onPreExecute() {		
			dialog.setMessage(getActivity().getString(R.string.deleting));
			dialog.setIndeterminate(true);
			dialog.show();
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			for(FileHolder fh : mFileHolders)
				recursiveDelete(fh.getFile());
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			Toast.makeText(dialog.getContext(), mResult == 0 ? R.string.delete_failure : R.string.delete_success, Toast.LENGTH_LONG).show();
			dialog.dismiss();
			if(mListener != null)
				mListener.deleted();
		}
	}

	public interface OnDeleteListener{
		public void deleted();
	}
}