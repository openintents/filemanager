package org.openintents.filemanager.compatibility;

import android.support.v4.app.FragmentActivity;

/**
 * Allow actionbar refreshing, while preventing VerifyError in 1.6.
 */
public abstract class ActionbarRefreshHelper {
	public static void activity_invalidateOptionsMenu(FragmentActivity act){
		act.supportInvalidateOptionsMenu();
	}
}