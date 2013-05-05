package org.openintents.filemanager.lists;

import java.io.File;
import java.util.ArrayList;

import org.openintents.filemanager.R;
import org.openintents.filemanager.compatibility.ActionbarRefreshHelper;
import org.openintents.filemanager.files.FileHolder;
import org.openintents.filemanager.util.FileUtils;
import org.openintents.filemanager.util.MenuUtils;
import org.openintents.filemanager.view.LegacyActionContainer;
import org.openintents.intents.FileManagerIntents;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

/**
 * Dedicated file list fragment, used for multiple selection on platforms older
 * than Honeycomb. OnDestroy sets RESULT_OK on the parent activity so that
 * callers refresh their lists if appropriate.
 *
 * @author George Venios
 */
public class MultiselectListFragment extends FileListFragment {
	private LegacyActionContainer mLegacyActionContainer;
	File currentDirectory = new File("");
	protected static final int REQUEST_CODE_MOVE = 1;
	Intent intent;
	ArrayList<FileHolder> fItems;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.filelist_legacy_multiselect, null);
	}

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setHasOptionsMenu(true);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		super.onViewCreated(view, savedInstanceState);
		mAdapter.setItemLayout(R.layout.item_filelist_multiselect);
		// Init members
		mLegacyActionContainer = (LegacyActionContainer) view
				.findViewById(R.id.action_container);
		mLegacyActionContainer.setMenuResource(R.menu.multiselect);
		mLegacyActionContainer
				.setOnActionSelectedListener(new LegacyActionContainer.OnActionSelectedListener() {
					@Override
					public void actionSelected(MenuItem item) {
						if (getListView().getCheckItemIds().length == 0) {
							Toast.makeText(getActivity(),
									R.string.no_selection, Toast.LENGTH_SHORT)
									.show();
							return;
						}
						fItems = new ArrayList<FileHolder>();

						for (long i : getListView().getCheckItemIds()) {
							fItems.add((FileHolder) mAdapter.getItem((int) i));
						}

						//if Move is selected while in multiselect
						if (item.getItemId() == R.id.menu_moveaction) {
							Intent intent = new Intent(
									FileManagerIntents.ACTION_PICK_DIRECTORY);
							intent.setData(FileUtils.getUri(currentDirectory));
							intent.putExtra(
									FileManagerIntents.EXTRA_BUTTON_TEXT,
									"Move Here");
							intent.putExtra(FileManagerIntents.EXTRA_TITLE,
									"Move");
							intent.putExtra(
									FileManagerIntents.EXTRA_WRITEABLE_ONLY,
									true);
							startActivityForResult(intent, REQUEST_CODE_MOVE);

							if (VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB)
								ActionbarRefreshHelper
										.activity_invalidateOptionsMenu(MultiselectListFragment.this
												.getActivity());
						} else {
							MenuUtils.handleMultipleSelectionAction(
									MultiselectListFragment.this, item, fItems,
									getActivity());
						}
					}
				});
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.options_multiselect, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		ListView list = getListView();
		if (item.getItemId() == R.id.check_all) {
			for (int i = 0; i < mAdapter.getCount(); i++) {
				list.setItemChecked(i, true);
			}
			return true;
		} else if (item.getItemId() == R.id.uncheck_all) {
			for (int i = 0; i < mAdapter.getCount(); i++) {
				list.setItemChecked(i, false);
			}
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_CODE_MOVE) {
			if (data != null & resultCode == Activity.RESULT_OK) {
				File movefrom = null;
				File moveto = FileUtils.getFile(data.getData());
				int toast = 0;
				for (FileHolder fh : fItems) {
					movefrom = fh.getFile();
					File newPath = FileUtils
							.getFile(moveto, movefrom.getName());
					if (!movefrom.renameTo(newPath)) {
						// move unsuccessful
						if (moveto.isDirectory()) {
							toast = R.string.error_moving_folder;
						} else {
							toast = R.string.error_moving_file;
						}
						break;
					}
				}

				if (toast == 0) {
					// Move was successful.
					toast = R.string.file_moved;
				}
				Toast.makeText(getActivity(), toast, Toast.LENGTH_SHORT).show();
				File parent = movefrom.getParentFile();
				Intent i = new Intent();
				i.setAction(android.content.Intent.ACTION_VIEW);
				i.setData(Uri.fromFile(parent));
				startActivity(i);
			}
		}
	}
}
