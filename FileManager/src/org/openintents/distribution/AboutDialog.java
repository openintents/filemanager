/* 
 * Copyright (C) 2007-2008 OpenIntents.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openintents.distribution;

import org.openintents.intents.AboutMiniIntents;
import org.openintents.util.IntentUtils;
import org.openintents.util.VersionUtils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

/**
 * About dialog
 *
 * @version 2009-02-04
 * @author Peli
 *
 */
public class AboutDialog extends GetFromMarketDialog {
	private static final String TAG = "About";
    
	public AboutDialog(Context context) {
		super(context,
				RD.string.aboutapp_not_available,
				RD.string.aboutapp_get,
				RD.string.aboutapp_market_uri,
				RD.string.aboutapp_developer_uri);
		
		String version = VersionUtils.getVersionNumber(context);
		String name = VersionUtils.getApplicationName(context);

		setTitle(name);
		setMessage(context.getString(RD.string.aboutapp_not_available, version));
	}
	
	public static void showDialogOrStartActivity(Activity activity, int dialogId) {
		Intent intent = new Intent(AboutMiniIntents.ACTION_SHOW_ABOUT_DIALOG);
		intent.putExtra(AboutMiniIntents.EXTRA_PACKAGE_NAME, activity.getPackageName());
		
		if (IntentUtils.isIntentAvailable(activity, intent)) {
			activity.startActivity(intent);
		} else {
			activity.showDialog(dialogId);
		}
	}

}
