package org.openintents.filemanager.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.openintents.filemanager.R;
import org.openintents.filemanager.files.FileHolder;
import org.openintents.filemanager.lists.FileListFragment;
import org.openintents.filemanager.util.MediaScannerUtils;
import org.openintents.filemanager.util.UIUtils;
import org.openintents.intents.FileManagerIntents;

import java.io.File;

public class RenameDialog extends DialogFragment {
    private FileHolder mFileHolder;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mFileHolder = getArguments().getParcelable(FileManagerIntents.EXTRA_DIALOG_FILE_HOLDER);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        LinearLayout view = (LinearLayout) inflater.inflate(R.layout.dialog_text_input, null);
        final EditText v = (EditText) view.findViewById(R.id.foldername);
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
                .setInverseBackgroundForced(UIUtils.shouldDialogInverseBackground(getActivity()))
                .setTitle(R.string.menu_rename)
                .setIcon(mFileHolder.getIcon())
                .setView(view)
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

    private void renameTo(String to) {
        boolean res = false;

        if (to.length() > 0) {
            File from = mFileHolder.getFile();

            File dest = new File(mFileHolder.getFile().getParent() + File.separator + to);
            if (!dest.exists()) {
                res = mFileHolder.getFile().renameTo(dest);
                ((FileListFragment) getTargetFragment()).refresh();

                // Inform media scanner
                MediaScannerUtils.informFileDeleted(getActivity().getApplicationContext(), from);
                MediaScannerUtils.informFileAdded(getActivity().getApplicationContext(), dest);
            }
        }

        Toast.makeText(getActivity(), res ? R.string.rename_success : R.string.rename_failure, Toast.LENGTH_SHORT).show();
    }
}