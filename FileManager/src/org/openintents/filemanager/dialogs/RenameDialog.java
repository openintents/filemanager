package org.openintents.filemanager.dialogs;

import java.io.File;

import org.openintents.filemanager.R;
import org.openintents.filemanager.files.FileHolder;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class RenameDialog extends DialogFragment {
	private FileHolder mFileHolder;
	private OnRenamedListener mListener;
	
	/**
	 * @param fHolder The holder that keeps the file to rename.
	 */
	public RenameDialog(FileHolder fHolder, OnRenamedListener listener) {
		mFileHolder = fHolder;
		mListener = listener;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		LayoutInflater inflater = LayoutInflater.from(getActivity());
		final EditText v = (EditText) inflater.inflate(R.layout.dialog_text_input, null);
		v.setText(mFileHolder.getName());

		v.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			public boolean onEditorAction(TextView text, int actionId,
					KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_GO)
					renameTo(text.getText().toString());
				dismiss();
				return true;
			}
		});
		
		return new AlertDialog.Builder(getActivity())
				.setInverseBackgroundForced(true)
				.setTitle(R.string.menu_rename)
				.setIcon(mFileHolder.getIcon())
				.setView(v)
				.setNegativeButton(android.R.string.cancel, null)
				.setPositiveButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								renameTo(v.getText().toString());

							}
						}).create();
	}
	
	private void renameTo(String to){
		boolean res = false;
		if(to.length() > 0){
			File dest = new File(mFileHolder.getFile().getParent() + File.separator + to);
			res = mFileHolder.getFile().renameTo(dest);
			mListener.renamed();
		}
		
		Toast.makeText(getActivity(), res ? R.string.rename_success : R.string.rename_failure, Toast.LENGTH_SHORT).show();
	}

	public interface OnRenamedListener {
		public void renamed();
	}
}