package org.openintents.filemanager.lists;

import android.view.Menu;
import android.view.MenuInflater;
import android.widget.ListView;

public abstract class SelectionHelper {

	/**
	 * Fill the passed Menu attribute with the proper single selection actions,
	 * taking the selected item into account.
	 * 
	 * @param l
	 *            The {@link ListView} containing the selected item.
	 * @param m
	 *            The {@link Menu} to fill.
	 * @param mi
	 *            The {@link MenuInflater} to use. This is a parameter since the
	 *            ActionMode provides a context-based inflater and we could
	 *            possibly lose functionality with a common MenuInflater.
	 * @param position
	 *            The position of the selected item in the list.
	 */
	public static void fillContextMenu(ListView l, Menu m, MenuInflater mi,
			int position) {
//		// Inflate all actions
//		mi.inflate(R.menu.context, m);
//
//		// Get the selected file
//		FileHolder item = (FileHolder) l.getAdapter().getItem(position);
//		if (m instanceof ContextMenu) {
//			((ContextMenu) m).setHeaderTitle(item.getName());
//			((ContextMenu) m).setHeaderIcon(item.getIcon());
//		}
//		File file = item.getFile();
//
//		// If selected item is a directory
//		if (file.isDirectory()) {
//			if (mState != STATE_PICK_FILE) {
//				m.removeItem(R.id.menu_open);
//			}
//			m.removeItem(R.id.menu_send);
//			m.removeItem(R.id.menu_copy);
//		}
//
//		// If selected item is a zip archive
//		if (!FileUtils.checkIfZipArchive(file)) {
//			m.removeItem(R.id.menu_extract);
//		} else {
//			m.removeItem(R.id.menu_compress);
//		}
//
//		// If we are not showing a ContextMenu dialog, remove the open action,
//		// as it's overkill.
//		if (VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB)
//			m.removeItem(R.id.menu_open);
//
//		// Add CATEGORY_SELECTED_ALTERNATIVE intent options
//		Uri data = Uri.fromFile(file);
//		Intent intent = new Intent(null, data);
//		String type = mMimeTypes.getMimeType(file.getName());
//
//		intent.setDataAndType(data, type);
//		intent.addCategory(Intent.CATEGORY_SELECTED_ALTERNATIVE);
//
//		if (type != null) {
//			// Add additional options for the MIME type of the selected file.
//			m.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0,
//					new ComponentName(this, FileManagerActivity.class), null,
//					intent, 0, null);
//		}
	}

}