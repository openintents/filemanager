package org.openintents.filemanager.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.openintents.filemanager.FileManagerActivity;
import org.openintents.filemanager.FileManagerProvider;
import org.openintents.filemanager.PreferenceActivity;
import org.openintents.filemanager.R;
import org.openintents.filemanager.bookmarks.BookmarksProvider;
import org.openintents.filemanager.dialogs.CompressDialog;
import org.openintents.filemanager.dialogs.DetailsDialog;
import org.openintents.filemanager.dialogs.MultiDeleteDialog;
import org.openintents.filemanager.dialogs.RenameDialog;
import org.openintents.filemanager.dialogs.SingleDeleteDialog;
import org.openintents.filemanager.files.FileHolder;
import org.openintents.filemanager.lists.SimpleFileListFragment;
import org.openintents.intents.FileManagerIntents;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Parcelable;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;

/**
 * Utility class that helps centralize multiple and single selection actions for all API levels.
 * @author George Venios
 */
public abstract class MenuUtils {
	
	/**
	 * Fill <code>m</code> with multiselection actions, using <code>mi</code>.
	 * @param m The {@link Menu} to fill.
	 * @param mi The {@link MenuInflater} to use. This is a parameter since the ActionMode provides a context-based inflater and we could possibly lose functionality with a common MenuInflater.
	 */
	static public void fillMultiselectionMenu(Menu m, MenuInflater mi) {
		mi.inflate(R.menu.multiselect, m);
	}
	
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
	 * Central point where we handle actions for multiple selection, for every API level.
	 * @param mItem The selected menu option/action.
	 * @param fItems The data to act upon.
	 */
	public static boolean handleMultipleSelectionAction(final SimpleFileListFragment navigator, MenuItem mItem, List<FileHolder> fItems, Context context) {
		switch (mItem.getItemId()) {
			case R.id.menu_send:
				Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
				ArrayList<Uri> uris = new ArrayList<Uri>();
				intent.setType("text/plain");
				
				for(FileHolder fh : fItems)
					uris.add(FileUtils.getUri(fh.getFile()));
				
				intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
				
				context.startActivity(Intent.createChooser(intent, context.getString(R.string.send_chooser_title)));
				
				break;
			case R.id.menu_delete:
				new MultiDeleteDialog(fItems, new MultiDeleteDialog.OnDeleteListener() {
					
					@Override
					public void deleted() {
						navigator.refresh();
					}
				}).show(navigator.getFragmentManager(), "MultiDeleteDialog");
				break;
			case R.id.menu_move:
				break;
			case R.id.menu_copy:
				break;
			case R.id.menu_compress:
				break;
			default:
				return false;
		}
	
		return true;
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

		case R.id.menu_rename:
			new RenameDialog(fItem, new RenameDialog.OnRenamedListener() {
				
				@Override
				public void renamed() {
					navigator.refresh();
				}
			}).show(navigator.getFragmentManager(), "RenameDialog");
			return true;

		case R.id.menu_send:
			sendFile(fItem, context);
			return true;
		
		case R.id.menu_details:
			new DetailsDialog(fItem).show(navigator.getFragmentManager(), "DetailsDialog");
			return true;

        case R.id.menu_compress:
            new CompressDialog(fItem, new CompressManager.OnCompressFinishedListener() {
            	
				@Override
				public void compressFinished() {
					navigator.refresh();
				}
			}).show(navigator.getFragmentManager(), "CompressDialog");
            return true;

        case R.id.menu_extract:
            pickDestinationAndExtract();            
            return true;
			
		case R.id.menu_bookmark:
			String path = fItem.getFile().getAbsolutePath();
			Cursor query = context.getContentResolver().query(BookmarksProvider.CONTENT_URI,
										new String[]{BookmarksProvider._ID},
										BookmarksProvider.PATH + "=?",
										new String[]{path},
										null);
			if(!query.moveToFirst()){
				ContentValues values = new ContentValues();
				values.put(BookmarksProvider.NAME, fItem.getName());
				values.put(BookmarksProvider.PATH, path);
				context.getContentResolver().insert(BookmarksProvider.CONTENT_URI, values);
				Toast.makeText(context, R.string.bookmark_added, Toast.LENGTH_SHORT).show();
			}
			else{
				Toast.makeText(context, R.string.bookmark_already_exists, Toast.LENGTH_SHORT).show();
			}
			query.close();
			return true;

		case R.id.menu_more:
			if (!PreferenceActivity.getShowAllWarning(context)){
				showMoreCommandsDialog(fItem, context);
				return true;
			}
			showWarningDialog(fItem, context);

			return true;
		}

		return false;
	}
 	
    /**
     * Creates a home screen shortcut.
     * @param file The {@link File} to create the shortcut to.
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
    
    private static void pickDestinationAndMove() {
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

    private static void pickDestinationAndExtract() {
// TODO implement once pick directory fragment is done.
//        Intent intent = new Intent(FileManagerIntents.ACTION_PICK_DIRECTORY);
//        intent.setData(FileUtils.getUri(mPathBar.getCurrentDirectory()));
//        intent.putExtra(FileManagerIntents.EXTRA_TITLE, getString(R.string.extract_title));
//        intent.putExtra(FileManagerIntents.EXTRA_BUTTON_TEXT, getString(R.string.extract_button));
//        intent.putExtra(FileManagerIntents.EXTRA_WRITEABLE_ONLY, true);
//        startActivityForResult(intent, REQUEST_CODE_EXTRACT);
    }
    
	private static void pickDestinationAndCopy() {
// TODO implement once pick directory fragment is done.
//		Intent intent = new Intent(FileManagerIntents.ACTION_PICK_DIRECTORY);
//		
//		intent.setData(FileUtils.getUri(mPathBar.getCurrentDirectory()));
//		
//		intent.putExtra(FileManagerIntents.EXTRA_TITLE, getString(R.string.copy_title));
//		intent.putExtra(FileManagerIntents.EXTRA_BUTTON_TEXT, getString(R.string.copy_button));
//		intent.putExtra(FileManagerIntents.EXTRA_WRITEABLE_ONLY, true);
// TODO 		intent.putExtra("checked_files", getSelectedItemsFiles());
//		
//		startActivityForResult(intent, REQUEST_CODE_COPY);
	}
	
	/**
	 * Creates an activity picker to send a file.
	 * @param fHolder A {@link FileHolder} containing the {@link File} to send.
	 * @param context {@link Context} in which to create the picker.
	 */
	private static void sendFile(FileHolder fHolder, Context context) {
		String filename = fHolder.getName();
		
		Intent i = new Intent();
		i.setAction(Intent.ACTION_SEND);
		i.setType(fHolder.getMimeType());
		i.putExtra(Intent.EXTRA_SUBJECT, filename);
		i.putExtra(Intent.EXTRA_STREAM, FileUtils.getUri(fHolder.getFile()));
		i.putExtra(Intent.EXTRA_STREAM, Uri.parse("content://" + FileManagerProvider.AUTHORITY + fHolder.getFile().getAbsolutePath()));

		i = Intent.createChooser(i, context.getString(R.string.menu_send));
		
		try {
			context.startActivity(i);
		} catch (ActivityNotFoundException e) {
			Toast.makeText(context, R.string.send_not_available, Toast.LENGTH_SHORT).show();
		}
	}
	
	/**
	 * Call this to show the dialog that informs the user about possibly broken options in the "More" dialog.
	 * @param context The context that will be used for this dialog.
	 * @param holder A {@link FileHolder} containing the file to act upon.
	 */
	private static void showWarningDialog(final FileHolder holder, final Context context) {
		LayoutInflater li = LayoutInflater.from(context);
		View warningView = li.inflate(R.layout.dialog_warning, null);
		final CheckBox showWarningAgain = (CheckBox)warningView.findViewById(R.id.showagaincheckbox);
		
		showWarningAgain.setChecked(PreferenceActivity.getShowAllWarning(context));
		
		new AlertDialog.Builder(context).setView(warningView).setTitle(context.getString(R.string.title_warning_some_may_not_work))
				.setMessage(context.getString(R.string.warning_some_may_not_work))
		    	.setIcon(android.R.drawable.ic_dialog_alert).setPositiveButton(
					android.R.string.ok, new OnClickListener() {
						
						public void onClick(DialogInterface dialog, int which) {
							PreferenceActivity.setShowAllWarning(context, showWarningAgain.isChecked());

							showMoreCommandsDialog(holder, context);
						}
						
					}).create()
				.show();
	}

	/**
	 * Call this to show the "More" dialog for the passed {@link FileHolder}.
	 * @param context Always useful, isn't it?
	 */
	private static void showMoreCommandsDialog(FileHolder holder, final Context context) {
		final Uri data = Uri.fromFile(holder.getFile());
		final Intent intent = new Intent();
		intent.setDataAndType(data, holder.getMimeType());

		if (holder.getMimeType() != null) {
			// Add additional options for the MIME type of the selected file.
			PackageManager pm = context.getPackageManager();
			final List<ResolveInfo> lri = pm.queryIntentActivityOptions(
					new ComponentName(context, FileManagerActivity.class),
					null, intent, 0);
			final int N = lri != null ? lri.size() : 0;

			// Create name list for menu item.
			final List<CharSequence> items = new ArrayList<CharSequence>();
			/* Some of the options don't go to the list hence we have to remove them
			 * to keep the lri correspond with the menu items. In the addition, we have
			 * to remove them after the first iteration, otherwise the iteration breaks.
			 */
			List<ResolveInfo> toRemove = new ArrayList<ResolveInfo>();
			for (int i = 0; i < N; i++) {
				final ResolveInfo ri = lri.get(i);
				Intent rintent = new Intent(intent);
				rintent.setComponent(
						new ComponentName(
								ri.activityInfo.applicationInfo.packageName,
								ri.activityInfo.name));
				ActivityInfo info = rintent.resolveActivityInfo(pm, 0);
				String permission = info.permission;
				if(info.exported && (permission == null || context.checkCallingPermission(permission) == PackageManager.PERMISSION_GRANTED))
					items.add(ri.loadLabel(pm));
				else
					toRemove.add(ri);
			}

			for(ResolveInfo ri : toRemove){
				lri.remove(ri);
			}

			new AlertDialog.Builder(context)
					.setTitle(holder.getName())
					.setIcon(holder.getIcon())
					.setItems(items.toArray(new CharSequence[0]),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int item) {
									final ResolveInfo ri = lri.get(item);
									Intent rintent = new Intent(intent)
											.setComponent(new ComponentName(
													ri.activityInfo.applicationInfo.packageName,
													ri.activityInfo.name));
									context.startActivity(rintent);
								}
							}).create()
						.show();
		}
	}
}