package org.openintents.filemanager.util;

import java.io.File;

import org.openintents.filemanager.FileManagerActivity;
import org.openintents.filemanager.R;
import org.openintents.filemanager.files.FileHolder;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;

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
	
}