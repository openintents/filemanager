package org.openintents.filemanager.compatibility;

import android.widget.ListView;

/**
 * Avoid {@link VerifyError}s down to 1.6.
 * @author George Venios
 */
public class ListViewMethodHelper {
	private ListViewMethodHelper() {
	}

	public static long[] listView_getCheckedItemIds(ListView l){
		return l.getCheckedItemIds();
	}
	
	public static int listView_getCheckedItemCount(ListView l){
		return l.getCheckedItemCount();
	}
}