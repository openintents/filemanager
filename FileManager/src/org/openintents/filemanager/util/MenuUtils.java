package org.openintents.filemanager.util;

import java.io.File;

import org.openintents.filemanager.FileManagerActivity;
import org.openintents.filemanager.R;
import org.openintents.filemanager.dialogs.SingleDeleteDialog;
import org.openintents.filemanager.files.FileHolder;
import org.openintents.filemanager.lists.SimpleFileListFragment;
import org.openintents.intents.FileManagerIntents;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Parcelable;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public abstract class MenuUtils {

	/**
	 * Fill the passed Menu attribute with the proper single selection actions for the passed {@link FileHolder} object.
	 * @param m The {@link Menu} to fill.
	 * @param mi The {@link MenuInflater} to use. This is a parameter since the ActionMode provides a context-based inflater and we could possibly lose functionality with a common MenuInflater.
	 */
	static public void fillContextMenu(FileHolder item, Menu m, MenuInflater mi, Context context){
		// Inflate all actions
		mi.inflate(R.menu.context, m);
		
		// Get the selected file
        if(m instanceof ContextMenu){
			((ContextMenu) m).setHeaderTitle(item.getName());
			((ContextMenu) m).setHeaderIcon(item.getIcon());
        }
		File file = item.getFile();

		// If selected item is a directory
		if (file.isDirectory()) {
// TODO pick fragment			if (mState != STATE_PICK_FILE) {
//				m.removeItem(R.id.menu_open);
//			}
			m.removeItem(R.id.menu_send);
			m.removeItem(R.id.menu_copy);
		}

		// If selected item is a zip archive
        if (!FileUtils.checkIfZipArchive(file)){
        	m.removeItem(R.id.menu_extract);
        } else {
        	m.removeItem(R.id.menu_compress);
        }
        
        // If we are not showing a ContextMenu dialog, remove the open action, as it's overkill.
        if (VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB)
        	m.removeItem(R.id.menu_open);
		
		// Add CATEGORY_SELECTED_ALTERNATIVE intent options
        Uri data = Uri.fromFile(file);
        Intent intent = new Intent(null, data);

        intent.setDataAndType(data, item.getMimeType());
        intent.addCategory(Intent.CATEGORY_SELECTED_ALTERNATIVE);

        if (item.getMimeType() != null) {
        	// Add additional options for the MIME type of the selected file.
			m.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0,
					new ComponentName(context, FileManagerActivity.class), null, intent, 0, null);
        }
	}

	/**
	 * Central point where we handle actions for single selection, for every API level.
	 * @param mItem The selected menu option/action.
	 * @param fItem The data to act upon.
	 */
	public static boolean handleSingleSelectionAction(final SimpleFileListFragment navigator, MenuItem mItem, FileHolder fItem, Context context){
		switch (mItem.getItemId()) {
		case R.id.menu_open:
			navigator.open(fItem);
			return true;
			
		case R.id.menu_create_shortcut:
            createShortcut(fItem.getFile(), context);
			return true;
			
		case R.id.menu_move:
			pickDestinationAndMove();
			return true;
			
		case R.id.menu_copy:
			pickDestinationAndCopy();
			return true;
			
		case R.id.menu_delete:
			new SingleDeleteDialog(fItem, new SingleDeleteDialog.OnDeleteListener() {
				@Override
				public void deleted() {
					navigator.refresh();
				}
			}).show(navigator.getFragmentManager(), "SingleDeleteDialog");
			return true;

//		case R.id.menu_rename:
//			showDialog(DIALOG_RENAME);
//			return true;
//			
//		case R.id.menu_send:
//			sendFile(mContextFile);
//			return true;
//		
//		case R.id.menu_details:
//			showDialog(DIALOG_DETAILS);
//			return true;
//
//        case R.id.menu_compress:
//            showDialog(DIALOG_COMPRESSING);
//            return true;
//
//        case R.id.menu_extract:
//            pickDestinationAndExtract();            
//            return true;
//			
//		case R.id.menu_bookmark:
//			String path = fItem.getFile().getAbsolutePath();
//			Cursor query = managedQuery(BookmarksProvider.CONTENT_URI,
//										new String[]{BookmarksProvider._ID},
//										BookmarksProvider.PATH + "=?",
//										new String[]{path},
//										null);
//			if(!query.moveToFirst()){
//				ContentValues values = new ContentValues();
//				values.put(BookmarksProvider.NAME, fItem.getName());
//				values.put(BookmarksProvider.PATH, path);
//				context.getContentResolver().insert(BookmarksProvider.CONTENT_URI, values);
//				Toast.makeText(context, R.string.bookmark_added, Toast.LENGTH_SHORT).show();
//			}
//			else{
//				Toast.makeText(context, R.string.bookmark_already_exists, Toast.LENGTH_SHORT).show();
//			}
//			return true;
//
//		case R.id.menu_more:
//			if (!PreferenceActivity.getShowAllWarning(FileManagerActivity.this)) {
//				showMoreCommandsDialog();
//				return true;
//			}
//
//			showWarningDialog();
//
//			return true;
		}

		return false;
	}
 	
    /**
     * Creates a home screen shortcut.
     * @param file The file to create the shortcut to.
     */
    static private void createShortcut(File file, Context context) {
		Intent shortcutintent = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
		shortcutintent.putExtra("duplicate", false);
		shortcutintent.putExtra(Intent.EXTRA_SHORTCUT_NAME, file.getName());
		Parcelable icon = Intent.ShortcutIconResource.fromContext(context.getApplicationContext(), R.drawable.ic_launcher_shortcut);
		shortcutintent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);
		shortcutintent.putExtra(FileManagerIntents.EXTRA_SHORTCUT_TARGET, file.getAbsolutePath());

		// Intent to load
		Intent itl = new Intent(context.getApplicationContext(), FileManagerActivity.class);
		itl.putExtra(FileManagerIntents.EXTRA_SHORTCUT_TARGET, file.getAbsolutePath());
		
		shortcutintent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, itl);
		context.sendBroadcast(shortcutintent);
    }
    
    static private void pickDestinationAndMove() {
// TODO implement once pick directory fragment is done.
// 		Intent intent = new Intent(FileManagerIntents.ACTION_PICK_DIRECTORY);
// 		
// 		intent.setData(FileUtils.getUri(mPathBar.getCurrentDirectory()));
//
// 		intent.putExtra(FileManagerIntents.EXTRA_TITLE, getString(R.string.move_title));
// 		intent.putExtra(FileManagerIntents.EXTRA_BUTTON_TEXT, getString(R.string.move_button));
// 		intent.putExtra(FileManagerIntents.EXTRA_WRITEABLE_ONLY, true);
// // TODO		intent.putExtra("checked_files", getSelectedItemsFiles());
//
// 		startActivityForResult(intent, REQUEST_CODE_MOVE);
 	}

    static private void pickDestinationAndExtract() {
// TODO implement once pick directory fragment is done.
//        Intent intent = new Intent(FileManagerIntents.ACTION_PICK_DIRECTORY);
//        intent.setData(FileUtils.getUri(mPathBar.getCurrentDirectory()));
//        intent.putExtra(FileManagerIntents.EXTRA_TITLE, getString(R.string.extract_title));
//        intent.putExtra(FileManagerIntents.EXTRA_BUTTON_TEXT, getString(R.string.extract_button));
//        intent.putExtra(FileManagerIntents.EXTRA_WRITEABLE_ONLY, true);
//        startActivityForResult(intent, REQUEST_CODE_EXTRACT);
    }
    
	static private void pickDestinationAndCopy() {
// TODO implement once pick directory fragment is done.
//		Intent intent = new Intent(FileManagerIntents.ACTION_PICK_DIRECTORY);
//		
//		intent.setData(FileUtils.getUri(mPathBar.getCurrentDirectory()));
//		
//		intent.putExtra(FileManagerIntents.EXTRA_TITLE, getString(R.string.copy_title));
//		intent.putExtra(FileManagerIntents.EXTRA_BUTTON_TEXT, getString(R.string.copy_button));
//		intent.putExtra(FileManagerIntents.EXTRA_WRITEABLE_ONLY, true);
//// TODO		intent.putExtra("checked_files", getSelectedItemsFiles());
//		
//		startActivityForResult(intent, REQUEST_CODE_COPY);
	}
}