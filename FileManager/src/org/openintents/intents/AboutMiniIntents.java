/* 
 * Copyright (C) 2008-2009 OpenIntents.org
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

package org.openintents.intents;

/**
 * Intents definition belonging to OI About.
 * 
 * @version 2009-Jan-08
 * 
 * @author pjv
 * @author Peli
 *
 */
public final class AboutMiniIntents {
	
	/**
	 * Empty, preventing instantiation.
	 */
	private AboutMiniIntents() {
		//Empty, preventing instantiation.
	}

	/**
	 * Activity Action: Show an about dialog to display
	 * information about the application.
	 * 
	 * The application information is retrieved from the
	 * application's manifest. In order to send the package
	 * you have to launch this activity through
	 * startActivityForResult().
	 * 
	 * Alternatively, you can specify the package name 
	 * manually through the extra EXTRA_PACKAGE.
	 * 
	 * All data can be replaced using optional intent extras.
	 * 
	 * <p>
	 * Constant Value: "org.openintents.action.SHOW_ABOUT_DIALOG"
	 * </p>
	 */
	public static final String ACTION_SHOW_ABOUT_DIALOG = 
		"org.openintents.action.SHOW_ABOUT_DIALOG";

	/**
	 * Optional intent extra: Specify your application package name.
	 * 
	 * If you start the About dialog through startActivityForResult()
	 * then the application package is sent automatically and does
	 * not need to be supplied here.
	 * 
	 * <p>
	 * Constant Value: "org.openintents.extra.PACKAGE_NAME"
	 * </p>
	 */
	public static final String EXTRA_PACKAGE_NAME = 
		"org.openintents.extra.PACKAGE_NAME";
	
}
