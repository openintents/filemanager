package org.openintents.filemanager.dialogs;

import java.io.File;

import org.openintents.filemanager.R;
import org.openintents.filemanager.files.FileHolder;
import org.openintents.filemanager.lists.FileListFragment;
import org.openintents.intents.FileManagerIntents;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.Toast;

public class SingleDeleteDialog extends DialogFragment {
	private FileHolder mFileHolder;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mFileHolder = getArguments().getParcelable(FileManagerIntents.EXTRA_DIALOG_FILE_HOLDER);
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		return new AlertDialog.Builder(getActivity())
				.setInverseBackgroundForced(true)
				.setTitle(getString(R.string.really_delete, mFileHolder.getName()))
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
							new RecursiveDeleteTask().execute(mFileHolder.getFile());
					}
				})
				.setIcon(mFileHolder.getIcon())
				.setNegativeButton(android.R.string.cancel, null)
				.create();
	}
	
	private class RecursiveDeleteTask extends AsyncTask<File, Void, Void> {
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
		protected Void doInBackground(File... params) {
			recursiveDelete(params[0]);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			Toast.makeText(dialog.getContext(), mResult == 0 ? R.string.delete_failure : R.string.delete_success, Toast.LENGTH_LONG).show();
			((FileListFragment) getTargetFragment()).refresh();
			dialog.dismiss();
		}
	}

	public interface OnDeleteListener{
		public void deleted();
	}
}