package org.openintents.filemanager.dialogs;

import java.io.File;

import org.openintents.filemanager.R;

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

public class CreateDirectoryDialog extends DialogFragment {
	private File mIn;
	private OnDirectoryCreatedListener mListener;
	
	/**
	 * @param currentDir The directory which to create the file into.
	 * @param listener A listener to inform the caller about successful directory creation. Can be null.
	 */
	public CreateDirectoryDialog(File currentDir, OnDirectoryCreatedListener listener){
		mIn = currentDir;
		mListener = listener;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		LayoutInflater inflater = LayoutInflater.from(getActivity());
		final EditText v = (EditText) inflater.inflate(R.layout.dialog_text_input, null);
		v.setHint(R.string.folder_name);
		
		v.setOnEditorActionListener(new TextView.OnEditorActionListener(){
			public boolean onEditorAction(TextView text, int actionId, KeyEvent event) {
				   if (actionId == EditorInfo.IME_ACTION_GO)
					   createFolder(text.getText());
				   return true;
				}
		});
		
		return new AlertDialog.Builder(getActivity())
				.setInverseBackgroundForced(true)
				.setTitle(R.string.create_new_folder)
				.setIcon(android.R.drawable.ic_dialog_alert).setView(v)
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						createFolder(v.getText());
					}
				})
				.setNegativeButton(android.R.string.cancel, null)
				.create();
	}

	private void createFolder(CharSequence text) {
		if(text.length() != 0){
			if(new File(mIn, text.toString()).mkdirs())
				Toast.makeText(getActivity(), R.string.create_dir_success, Toast.LENGTH_SHORT).show();
			else
				Toast.makeText(getActivity(), R.string.create_dir_failure, Toast.LENGTH_SHORT).show();
			if(mListener!=null)
				mListener.directoryCreated();
			dismiss();
		}
	}
	
	public interface OnDirectoryCreatedListener {
		public void directoryCreated();
	}
}