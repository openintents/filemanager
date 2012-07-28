package org.openintents.filemanager.dialogs;

import org.openintents.filemanager.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class OverwriteFileDialog extends DialogFragment {
	private OnOverwriteActionListener mListener;
	
	public OverwriteFileDialog(OnOverwriteActionListener listener) {
		mListener = listener;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		return new AlertDialog.Builder(getActivity())
				.setInverseBackgroundForced(true)
				.setTitle(R.string.file_exists)
				.setMessage(R.string.overwrite_question)
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								mListener.overwrite();
							}
						}).setNegativeButton(android.R.string.cancel, null)
				.create();
	}
	
	public interface OnOverwriteActionListener {
		public void overwrite();
	}
}