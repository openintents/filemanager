package org.openintents.filemanager.util;

import android.app.AlertDialog;
import android.content.*;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.DialogFragment;
import android.view.*;
import android.widget.CheckBox;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.openintents.filemanager.*;
import org.openintents.filemanager.bookmarks.BookmarksProvider;
import org.openintents.filemanager.compatibility.ActionbarRefreshHelper;
import org.openintents.filemanager.dialogs.*;
import org.openintents.filemanager.files.FileHolder;
import org.openintents.filemanager.lists.FileListFragment;
import org.openintents.filemanager.lists.SimpleFileListFragment;
import org.openintents.intents.FileManagerIntents;

/**
 * Utility class that helps centralize multiple and single selection actions for all API levels.
 * @author George Venios
 */
public abstract class MenuUtils {
	/**
	 * Fill <code>m</code> with multiselection actions, using <code>mi</code>.
	 * @param m The {@link Menu} to fill.
	 * @param menuResource The resource id of the menu to load.
	 * @param mi The {@link MenuInflater} to use. This is a parameter since the ActionMode provides a context-based inflater and we could possibly lose functionality with a common MenuInflater.
	 */
	static public void fillMultiselectionMenu(Menu m, int menuResource, MenuInflater mi) {
		mi.inflate(menuResource, m);
	}
	
	/**
	 * Fill the passed Menu attribute with the proper single selection actions for the passed {@link FileHolder} object.
	 * @param m The {@link Menu} to fill.
	 * @param menuResource The resource id of the menu to load.
	 * @param mi The {@link MenuInflater} to use. This is a parameter since the ActionMode provides a context-based inflater and we could possibly lose functionality with a common MenuInflater.
	 */
	static public void fillContextMenu(FileHolder item, Menu m, int menuResource, MenuInflater mi, Context context){
		// Inflate all actions
		mi.inflate(menuResource, m);
		
        if(m instanceof ContextMenu){
			((ContextMenu) m).setHeaderTitle(item.getName());
			((ContextMenu) m).setHeaderIcon(item.getIcon());
        }
        
		// Get the selected file
		File file = item.getFile();

		// If selected item is a directory
		if (file.isDirectory()) {
			m.removeItem(R.id.menu_send);
		}
		
		// If selected item is a zip archive
		if (!FileUtils.checkIfZipArchive(file)) {
			m.removeItem(R.id.menu_extract);
		} else {
			m.removeItem(R.id.menu_compress);
		}
        
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
	public static boolean handleMultipleSelectionAction(final FileListFragment navigator, MenuItem mItem, List<FileHolder> fItems, Context context) {
		DialogFragment dialog;
		Bundle args;
		
		switch (mItem.getItemId()) {
			case R.id.menu_send:
				Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
				ArrayList<Uri> uris = new ArrayList<>();
				intent.setType("text/plain");
				
				for(FileHolder fh : fItems)
					uris.add(FileUtils.getUri(fh.getFile()));
				
				intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
				
				context.startActivity(Intent.createChooser(intent, context.getString(R.string.send_chooser_title)));
				break;
			case R.id.menu_delete:
				dialog = new MultiDeleteDialog();
				dialog.setTargetFragment(navigator, 0);
				args = new Bundle();
				args.putParcelableArrayList(FileManagerIntents.EXTRA_DIALOG_FILE_HOLDER, new ArrayList<Parcelable>(fItems));
				dialog.setArguments(args);
				dialog.show(navigator.getFragmentManager(), MultiDeleteDialog.class.getName());
				break;
			case R.id.menu_move:
				((FileManagerApplication) navigator.getActivity().getApplication()).getCopyHelper().cut(fItems);
                navigator.updateClipboardInfo();

				// Refresh options menu
				if(VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB)
					ActionbarRefreshHelper.activity_invalidateOptionsMenu(navigator.getActivity());
				break;
			case R.id.menu_copy:
				((FileManagerApplication) navigator.getActivity().getApplication()).getCopyHelper().copy(fItems);
                navigator.updateClipboardInfo();
				
				// Refresh options menu
				if(VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB)
					ActionbarRefreshHelper.activity_invalidateOptionsMenu(navigator.getActivity());
				break;
			case R.id.menu_compress:
				dialog = new MultiCompressDialog();
				dialog.setTargetFragment(navigator, 0);
				args = new Bundle();
				args.putParcelableArrayList(FileManagerIntents.EXTRA_DIALOG_FILE_HOLDER, new ArrayList<Parcelable>(fItems));
				dialog.setArguments(args);
				dialog.show(navigator.getFragmentManager(), MultiCompressDialog.class.getName());
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
		DialogFragment dialog;
		Bundle args;
		
		switch (mItem.getItemId()) {
		case R.id.menu_open:
			navigator.openInformingPathBar(fItem);
			return true;
			
		case R.id.menu_create_shortcut:
            createShortcut(fItem, context);
			return true;
			
		case R.id.menu_move:
			((FileManagerApplication) navigator.getActivity().getApplication()).getCopyHelper().cut(fItem);
            navigator.updateClipboardInfo();

			// Refresh options menu
			if(VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB)
				ActionbarRefreshHelper.activity_invalidateOptionsMenu(navigator.getActivity());
			return true;
			
		case R.id.menu_copy:
			((FileManagerApplication) navigator.getActivity().getApplication()).getCopyHelper().copy(fItem);
            navigator.updateClipboardInfo();

			// Refresh options menu
			if(VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB)
				ActionbarRefreshHelper.activity_invalidateOptionsMenu(navigator.getActivity());
			return true;
			
		case R.id.menu_delete:
			dialog = new SingleDeleteDialog();
			dialog.setTargetFragment(navigator, 0);
			args = new Bundle();
			args.putParcelable(FileManagerIntents.EXTRA_DIALOG_FILE_HOLDER, fItem);
			dialog.setArguments(args);
			dialog.show(navigator.getFragmentManager(), SingleDeleteDialog.class.getName());
			return true;

		case R.id.menu_rename:
			dialog = new RenameDialog();
			dialog.setTargetFragment(navigator, 0);
			args = new Bundle();
			args.putParcelable(FileManagerIntents.EXTRA_DIALOG_FILE_HOLDER, fItem);
			dialog.setArguments(args);
			dialog.show(navigator.getFragmentManager(), RenameDialog.class.getName());
			return true;

		case R.id.menu_send:
			sendFile(fItem, context);
			return true;
		
		case R.id.menu_details:
			dialog = new DetailsDialog();
			dialog.setTargetFragment(navigator, 0);
			args = new Bundle();
			args.putParcelable(FileManagerIntents.EXTRA_DIALOG_FILE_HOLDER, fItem);
			dialog.setArguments(args);
			dialog.show(navigator.getFragmentManager(), DetailsDialog.class.getName());
			return true;

        case R.id.menu_compress:
			dialog = new SingleCompressDialog();
			dialog.setTargetFragment(navigator, 0);
			args = new Bundle();
			args.putParcelable(FileManagerIntents.EXTRA_DIALOG_FILE_HOLDER, fItem);
			dialog.setArguments(args);
			dialog.show(navigator.getFragmentManager(), SingleCompressDialog.class.getName());
            return true;

        case R.id.menu_extract:
        	File dest = new File(fItem.getFile().getParentFile(), FileUtils.getNameWithoutExtension(fItem.getFile()));
        	dest.mkdirs();
        	
        	// Changed from the previous behavior.
        	// We just extract on the current directory. If the user needs to put it in another dir, 
        	// he/she can copy/cut the file with the new, equally easy to use way.
        	new ExtractManager(context)
        	.setOnExtractFinishedListener(new ExtractManager.OnExtractFinishedListener() {
				
				@Override
				public void extractFinished() {
					navigator.refresh();
				}
			})
        	.extract(fItem.getFile(), dest.getAbsolutePath());            
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
     * @param fileholder The {@link File} to create the shortcut to.
     */
    static private void createShortcut(FileHolder fileholder, Context context) {
		Intent shortcutintent = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
		shortcutintent.putExtra("duplicate", false);
		shortcutintent.putExtra(Intent.EXTRA_SHORTCUT_NAME, fileholder.getName());
		Parcelable icon = Intent.ShortcutIconResource.fromContext(context.getApplicationContext(), R.drawable.ic_launcher_shortcut);
		shortcutintent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);

		// Intent to load
		Intent itl = new Intent(Intent.ACTION_VIEW);
		if(fileholder.getFile().isDirectory())
			itl.setData(Uri.fromFile(fileholder.getFile()));
		else
			itl.setDataAndType(Uri.fromFile(fileholder.getFile()), fileholder.getMimeType());
		itl.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		
		shortcutintent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, itl);
		context.sendBroadcast(shortcutintent);
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
			final List<CharSequence> items = new ArrayList<>();
			/* Some of the options don't go to the list hence we have to remove them
			 * to keep the lri correspond with the menu items. In the addition, we have
			 * to remove them after the first iteration, otherwise the iteration breaks.
			 */
			List<ResolveInfo> toRemove = new ArrayList<>();
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