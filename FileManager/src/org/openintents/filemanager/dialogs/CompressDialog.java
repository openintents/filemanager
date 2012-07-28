package org.openintents.filemanager.dialogs;

import java.io.File;

import org.openintents.filemanager.R;
import org.openintents.filemanager.files.FileHolder;
import org.openintents.filemanager.util.CompressManager;

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

public class CompressDialog extends DialogFragment {
	private FileHolder mFileHolder;
	private CompressManager.OnCompressFinishedListener mListener;
	private CompressManager mCompressManager;
	
	/**
	 * @param listener Can be null. A listener that will be informed on compression finish.
	 */
	public CompressDialog(FileHolder fileHolder, CompressManager.OnCompressFinishedListener listener) {
		mFileHolder = fileHolder;
		mListener = listener;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mCompressManager = new CompressManager(getActivity());
		mCompressManager.setOnCompressFinishedListener(mListener);
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		LayoutInflater inflater = LayoutInflater.from(getActivity());
		final EditText v = (EditText) inflater.inflate(R.layout.dialog_text_input, null);
		v.setHint(R.string.compressed_file_name);
		
		v.setOnEditorActionListener(new TextView.OnEditorActionListener(){
			public boolean onEditorAction(TextView text, int actionId, KeyEvent event) {
				   if (actionId == EditorInfo.IME_ACTION_GO)
					   compress(v.getText().toString());
				   return true;
				}
		});
		
		return new AlertDialog.Builder(getActivity())
				.setTitle(R.string.menu_compress)
				.setView(v)
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						compress(v.getText().toString());
					}
				}).setNegativeButton(android.R.string.cancel, null).create();
	}
	
	private void compress(String zipname){
		if (new File(mFileHolder.getFile().getParent() + File.separator + zipname).exists()) {
// TODO			mDialogArgument = zipname;
//			showDialog(DIALOG_WARNING_EXISTS);
		} else {
			mCompressManager.compress(mFileHolder.getFile(), zipname+".zip");
		}
	}
}