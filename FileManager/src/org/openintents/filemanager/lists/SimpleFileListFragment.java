package org.openintents.filemanager.lists;

import java.io.File;
import java.io.IOException;

import org.openintents.filemanager.PreferenceActivity;
import org.openintents.filemanager.R;
import org.openintents.filemanager.compatibility.FileMultiChoiceModeHelper;
import org.openintents.filemanager.dialogs.CreateDirectoryDialog;
import org.openintents.filemanager.files.FileHolder;
import org.openintents.filemanager.util.FileUtils;
import org.openintents.filemanager.util.MenuUtils;
import org.openintents.filemanager.view.PathBar;
import org.openintents.filemanager.view.PathBar.OnDirectoryChangedListener;
import org.openintents.intents.FileManagerIntents;

import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.Toast;

/**
 * A file list fragment that supports context menu and CAB selection. Check {@link #setPathBar(PathBar)} for info on list item click behavior.
 * 
 * @author George Venios
 */
public class SimpleFileListFragment extends FileListFragment {
	private PathBar mPathBar;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.filelist_browse, null);
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		// Pathbar init.
		mPathBar = (PathBar) view.findViewById(R.id.pathbar);
		// Handle mPath differently if we restore state or just initially create the view.
		if(savedInstanceState == null)
			mPathBar.setInitialDirectory(mPath);
		else
			mPathBar.cd(mPath);
		mPathBar.setOnDirectoryChangedListener(new OnDirectoryChangedListener() {

			@Override
			public void directoryChanged(File newCurrentDir) {
				openDir(new FileHolder(newCurrentDir, getActivity()));
			}
		});

		if (VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			registerForContextMenu(getListView());
		} else {
			FileMultiChoiceModeHelper multiChoiceModeHelper = new FileMultiChoiceModeHelper();
			multiChoiceModeHelper.setListView(getListView());
			multiChoiceModeHelper.setPathBar(mPathBar);
			multiChoiceModeHelper.setContext(this);
			getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		}
		setHasOptionsMenu(true);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View view,
			ContextMenuInfo menuInfo) {
		MenuInflater inflater = new MenuInflater(getActivity());

		// Obtain context menu info
		AdapterView.AdapterContextMenuInfo info;
		try {
			info = (AdapterView.AdapterContextMenuInfo) menuInfo;
		} catch (ClassCastException e) {
			e.printStackTrace();
			return;
		}

		MenuUtils.fillContextMenu((FileHolder) mAdapter.getItem(info.position), menu, inflater, getActivity());
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		FileHolder fh = (FileHolder) mAdapter
				.getItem(((AdapterContextMenuInfo) item.getMenuInfo()).position);
		return MenuUtils.handleSingleSelectionAction(this, item, fh, getActivity());
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		FileHolder item = (FileHolder) mAdapter.getItem(position);

		open(item);
	}

	/**
	 * Point this Fragment to show the contents of the passed file.
	 * 
	 * @param f If same as current, does nothing.
	 */
	public void open(FileHolder f) {
		if (!f.getFile().exists())
			return;

		if (f.getFile().isDirectory()) {
			if (mPathBar != null)
				// Pass through PathBar
				mPathBar.cd(f.getFile());
			else
				// Directly cd
				openDir(f);
		} else if (f.getFile().isFile()) {
			FileUtils.openFile(f, getActivity());
		}	
	}
	
	/**
	 * Attempts to open a directory for browsing.
	 * 
	 * @param fileholder The holder of the directory to open.
	 */
	private void openDir(FileHolder fileholder){
		// Avoid unnecessary attempts to load.
		if(fileholder.getFile().getAbsolutePath().equals(mPath))
			return;
		mPath = fileholder.getFile().getAbsolutePath();
		refresh();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.simple_file_list, menu);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		// We only know about ".nomedia" once scanning is finished.
		boolean showMediaScanMenuItem = PreferenceActivity.getMediaScanFromPreference(getActivity());
		if (!mScanner.isRunning() && showMediaScanMenuItem) {
			menu.findItem(R.id.menu_media_scan_include).setVisible(mScanner.getNoMedia());
			menu.findItem(R.id.menu_media_scan_exclude).setVisible(!mScanner.getNoMedia());
		} else {
			menu.findItem(R.id.menu_media_scan_include).setVisible(false);
			menu.findItem(R.id.menu_media_scan_exclude).setVisible(false);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_create_folder:
			CreateDirectoryDialog dialog = new CreateDirectoryDialog();
			dialog.setTargetFragment(this, 0);
			Bundle args = new Bundle();
			args.putString(FileManagerIntents.EXTRA_DIR_PATH, mPath);
			dialog.setArguments(args);
			dialog.show(getActivity().getSupportFragmentManager(), CreateDirectoryDialog.class.getName());
			return true;
			
		case R.id.menu_media_scan_include:
			includeInMediaScan();
			return true;

		case R.id.menu_media_scan_exclude:
			excludeFromMediaScan();
			return true;

		default:
			return false;
		}
	}

	private void includeInMediaScan() {
		// Delete the .nomedia file.
		File file = FileUtils.getFile(mPathBar.getCurrentDirectory(),
				FileUtils.NOMEDIA_FILE_NAME);
		if (file.delete()) {
			Toast.makeText(getActivity(),
					getString(R.string.media_scan_included), Toast.LENGTH_LONG)
					.show();
		} else {
			// That didn't work.
			Toast.makeText(getActivity(), getString(R.string.error_generic),
					Toast.LENGTH_LONG).show();
		}
		refresh();
	}

	private void excludeFromMediaScan() {
		// Create the .nomedia file.
		File file = FileUtils.getFile(mPathBar.getCurrentDirectory(),
				FileUtils.NOMEDIA_FILE_NAME);
		try {
			if (file.createNewFile()) {
				Toast.makeText(getActivity(),
						getString(R.string.media_scan_excluded),
						Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(getActivity(),
						getString(R.string.error_media_scan), Toast.LENGTH_LONG)
						.show();
			}
		} catch (IOException e) {
			// That didn't work.
			Toast.makeText(getActivity(),
					getString(R.string.error_generic) + e.getMessage(),
					Toast.LENGTH_LONG).show();
		}
		refresh();
	}

	public void browseToHome() {
		mPathBar.cd(mPathBar.getInitialDirectory());
	}

	public boolean pressBack() {
		return mPathBar.pressBack();
	}
}