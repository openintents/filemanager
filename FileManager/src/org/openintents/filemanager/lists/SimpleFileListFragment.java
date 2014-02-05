package org.openintents.filemanager.lists;

import java.io.File;
import java.io.IOException;

import org.openintents.filemanager.FileManagerApplication;
import org.openintents.filemanager.PreferenceActivity;
import org.openintents.filemanager.R;
import org.openintents.filemanager.compatibility.ActionbarRefreshHelper;
import org.openintents.filemanager.compatibility.FileMultiChoiceModeHelper;
import org.openintents.filemanager.dialogs.CreateDirectoryDialog;
import org.openintents.filemanager.files.FileHolder;
import org.openintents.filemanager.util.CopyHelper;
import org.openintents.filemanager.util.FileUtils;
import org.openintents.filemanager.util.MenuUtils;
import org.openintents.filemanager.view.PathBar;
import org.openintents.filemanager.view.PathBar.Mode;
import org.openintents.filemanager.view.PathBar.OnDirectoryChangedListener;
import org.openintents.intents.FileManagerIntents;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
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
 * A file list fragment that supports context menu and CAB selection.
 *
 * @author George Venios
 */
public class SimpleFileListFragment extends FileListFragment {
	private static final String INSTANCE_STATE_PATHBAR_MODE = "pathbar_mode";
	protected static final int REQUEST_CODE_MOVE = 1;
	protected static final int REQUEST_CODE_MULTISELECT = 2;
	private static File currentDirectory = new File("");

	private PathBar mPathBar;
	private boolean mActionsEnabled = true;
	FileHolder fh;
	private int mSingleSelectionMenu = R.menu.context;
	private int mMultiSelectionMenu = R.menu.multiselect;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.filelist_browse, null);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		// Pathbar init.
		mPathBar = (PathBar) view.findViewById(R.id.pathbar);
		// Handle mPath differently if we restore state or just initially create the view.
		if(savedInstanceState == null)
			mPathBar.setInitialDirectory(getPath());
		else
			mPathBar.cd(getPath());
		mPathBar.setOnDirectoryChangedListener(new OnDirectoryChangedListener() {

			@Override
			public void directoryChanged(File newCurrentDir) {
				open(new FileHolder(newCurrentDir, getActivity()));
			}
		});
		if(savedInstanceState != null && savedInstanceState.getBoolean(INSTANCE_STATE_PATHBAR_MODE))
			mPathBar.switchToManualInput();
		// Removed else clause as the other mode is the default. It seems faster this way on Nexus S.

		initContextualActions();
	}

	/**
	 * Override this to handle initialization of list item long clicks.
	 */
	void initContextualActions(){
		if(mActionsEnabled){
			if (VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
				registerForContextMenu(getListView());
			} else {
				FileMultiChoiceModeHelper multiChoiceModeHelper = new FileMultiChoiceModeHelper(mSingleSelectionMenu, mMultiSelectionMenu);
				multiChoiceModeHelper.setListView(getListView());
				multiChoiceModeHelper.setPathBar(mPathBar);
				multiChoiceModeHelper.setContext(this);
				getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
			}
			setHasOptionsMenu(true);
		}
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

		MenuUtils.fillContextMenu((FileHolder) mAdapter.getItem(info.position), menu, mSingleSelectionMenu, inflater, getActivity());
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		fh = (FileHolder) mAdapter.getItem(((AdapterContextMenuInfo) item.getMenuInfo()).position);

		// if move option is selected
		if (item.getItemId() == R.id.menu_moveaction) {
			Intent intent = new Intent(FileManagerIntents.ACTION_PICK_DIRECTORY);
			intent.setData(FileUtils.getUri(currentDirectory));
			intent.putExtra(FileManagerIntents.EXTRA_TITLE, "Move");
			intent.putExtra(FileManagerIntents.EXTRA_BUTTON_TEXT, "Move Here");
			intent.putExtra(FileManagerIntents.EXTRA_WRITEABLE_ONLY, true);
			startActivityForResult(intent, REQUEST_CODE_MOVE);
			if (VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB)
				ActionbarRefreshHelper.activity_invalidateOptionsMenu(this.getActivity());
			return true;
		} else {
			//if not "move" then it will check in menuUtils
			return MenuUtils.handleSingleSelectionAction(this, item, fh, getActivity());
		}
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		FileHolder item = (FileHolder) mAdapter.getItem(position);
		openInformingPathBar(item);
	}

	/**
	 * Use this to open files and folders using this fragment. Appropriately handles pathbar updates.
	 * @param item The dir/file to open.
	 */
	public void openInformingPathBar(FileHolder item) {
		if(mPathBar == null)
			open(item);
		else
			mPathBar.cd(item.getFile());
	}

	/**
	 * Point this Fragment to show the contents of the passed file.
	 *
	 * @param f If same as current, does nothing.
	 */
	private void open(FileHolder f) {
		if (!f.getFile().exists())
			return;

		if (f.getFile().isDirectory()) {
			openDir(f);
		} else if (f.getFile().isFile()) {
			openFile(f);
		}
	}

	private void openFile(FileHolder fileholder){
		FileUtils.openFile(fileholder, getActivity());
	}

	/**
	 * Attempts to open a directory for browsing.
	 * Override this to handle folder click behavior.
	 *
	 * @param fileholder The holder of the directory to open.
	 */
	protected void openDir(FileHolder fileholder){
		// Avoid unnecessary attempts to load.
		if(fileholder.getFile().getAbsolutePath().equals(getPath()))
			return;

		setPath(fileholder.getFile());
		refresh();
	}

	protected void setLongClickMenus(int singleSelectionResource, int multiSelectionResource) {
		mSingleSelectionMenu = singleSelectionResource;
		mMultiSelectionMenu = multiSelectionResource;
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

		if(((FileManagerApplication) getActivity().getApplication()).getCopyHelper().canPaste()) {
			menu.findItem(R.id.menu_paste).setVisible(true);
		} else {
			menu.findItem(R.id.menu_paste).setVisible(false);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case R.id.menu_create_folder:
			CreateDirectoryDialog dialog = new CreateDirectoryDialog();
			dialog.setTargetFragment(this, 0);
			Bundle args = new Bundle();
			args.putString(FileManagerIntents.EXTRA_DIR_PATH, getPath());
			dialog.setArguments(args);
			dialog.show(getActivity().getSupportFragmentManager(), CreateDirectoryDialog.class.getName());
			return true;

		case R.id.menu_media_scan_include:
			includeInMediaScan();
			return true;

		case R.id.menu_media_scan_exclude:
			excludeFromMediaScan();
			return true;

		case R.id.menu_paste:
			if(((FileManagerApplication) getActivity().getApplication()).getCopyHelper().canPaste())
				((FileManagerApplication) getActivity().getApplication()).getCopyHelper().paste(new File(getPath()), new CopyHelper.OnOperationFinishedListener() {
					@Override
					public void operationFinished(boolean success) {
						refresh();

						// Refresh options menu
						if(VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB)
							ActionbarRefreshHelper.activity_invalidateOptionsMenu(getActivity());
					}
				});
			else
				Toast.makeText(getActivity(), R.string.nothing_to_paste, Toast.LENGTH_LONG).show();
			return true;

		case R.id.menu_multiselect:
	        Intent intent = new Intent(FileManagerIntents.ACTION_MULTI_SELECT);
	        intent.putExtra(FileManagerIntents.EXTRA_DIR_PATH, getPath());
	        startActivityForResult(intent, REQUEST_CODE_MULTISELECT);
			return true;

		default:
			return false;
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Automatically refresh to display possible changes done through the multiselect fragment.
		switch (requestCode) {
		case REQUEST_CODE_MOVE:
			// obtain the filename
			if (data != null & resultCode == Activity.RESULT_OK) {
				File moveto = FileUtils.getFile(data.getData());
				File movefrom = fh.getFile();
				if (moveto != null) {
					// Move single file.
					moveto = FileUtils.getFile(moveto, movefrom.getName());
					int toast = 0;
					if (movefrom.renameTo(moveto)) {
						// Move was successful.
						// refreshList();
						if (moveto.isDirectory()) {
							toast = R.string.folder_moved;
						} else {
							toast = R.string.file_moved;
						}
					} else {
						if (moveto.isDirectory()) {
							toast = R.string.error_moving_folder;
						} else {
							toast = R.string.error_moving_file;
						}
					}
					Toast.makeText(getActivity(), getString(toast),
							Toast.LENGTH_LONG).show();
				}
			}
		case REQUEST_CODE_MULTISELECT:
			refresh();
		default:
			super.onActivityResult(requestCode, resultCode, data);
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
					getString(R.string.error_generic) + e.getMessage(), Toast.LENGTH_LONG).show();
		}
		refresh();
	}

	public void browseToHome() {
		mPathBar.cd(mPathBar.getInitialDirectory());
	}

	public boolean pressBack() {
		return mPathBar.pressBack();
	}

	/**
	 * Set whether to show menu and selection actions. Must be set before OnViewCreated is called.
	 * @param enabled
	 */
	public void setActionsEnabled(boolean enabled){
		mActionsEnabled = enabled;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putBoolean(INSTANCE_STATE_PATHBAR_MODE, mPathBar.getMode() == Mode.MANUAL_INPUT);
	}
}
