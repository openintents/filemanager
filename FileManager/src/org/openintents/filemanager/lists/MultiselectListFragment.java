package org.openintents.filemanager.lists;

import org.openintents.filemanager.MultiselectFileHolderListAdapter;

import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

/**
 * Dedicated file list fragment, used for multiple selection on platforms older than Honeycomb.
 * @author George Venios
 */
public class MultiselectListFragment extends FileListFragment {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mAdapter = new MultiselectFileHolderListAdapter(mFiles, getActivity());
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		// TODO change checked state
		mAdapter.notifyDataSetChanged();
	}
	
	/**
	 * Performs the "Compress and send" action.
	 */
	public void actionMultiSend(){
		final String sendFileName = "multisend-attachment.zip";
//		compressMultiFile(sendFileName, new CompressManager.OnCompressFinishedListener(){
//			@Override
//			public void compressFinished() {
//				Intent i = new Intent();
//				i.setAction(Intent.ACTION_SEND);
//				i.setType("application/zip");
//				i.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(mPathBar.getCurrentDirectory().getAbsolutePath() + "/" + sendFileName)));
//				startActivity(Intent.createChooser(i, getString(R.string.send_chooser_title)));
//			}
//        });
	}

	/**
	 * Simple wrapper around {@link MultiselectFileHolderListAdapter#hasCheckedItems()}.
	 */
	private boolean hasCheckedItems() {
		return ((MultiselectFileHolderListAdapter) mAdapter).hasCheckedItems();
	}
}