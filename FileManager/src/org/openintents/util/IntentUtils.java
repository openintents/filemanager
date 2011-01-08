

/**
 * Original method retrieved from:
 * http://android-developers.blogspot.com/2009/01/can-i-use-this-intent.html
 */
package org.openintents.util;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

/**
 * 
 * @author romainguy
 * @author Peli
 *
 */
public class IntentUtils {
	
	/**
	 * Indicates whether the specified action can be used as an intent. This
	 * method queries the package manager for installed packages that can
	 * respond to the specified intent. If no suitable package is
	 * found, this method returns false.
	 *
	 * @param context The application's environment.
	 * @param intent The Intent to check for availability.
	 *
	 * @return True if an Intent with the specified action can be sent and
	 *         responded to, false otherwise.
	 */
	public static boolean isIntentAvailable(final Context context, final Intent intent) {
	    final PackageManager packageManager = context.getPackageManager();
	    List<ResolveInfo> list =
	            packageManager.queryIntentActivities(intent,
	                    PackageManager.MATCH_DEFAULT_ONLY);
	    return list.size() > 0;
	}
}
