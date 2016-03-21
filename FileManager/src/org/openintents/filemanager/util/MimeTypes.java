/* 
 * Copyright (C) 2008 OpenIntents.org
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

package org.openintents.filemanager.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.openintents.filemanager.R;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.XmlResourceParser;
import android.webkit.MimeTypeMap;

public class MimeTypes {

	private Map<String, String> mMimeTypes = new HashMap<String,String>();
	private Map<String, Integer> mIcons = new HashMap<String,Integer>();

	/**
	 * Use this instead of the default constructor to get a prefilled object.
	 */
	public static MimeTypes newInstance(Context c){
		MimeTypes mimeTypes = null;
		MimeTypeParser mtp = null;
		try {
			mtp = new MimeTypeParser(c, c.getPackageName());
		} catch (NameNotFoundException e) {
			// Should never happen
		}

		XmlResourceParser in = c.getResources().getXml(R.xml.mimetypes);

		try {
			mimeTypes = mtp.fromXmlResource(in);
		} catch (XmlPullParserException|IOException e) {
			e.printStackTrace();
		}
		
		return mimeTypes;
	}
	
	/* I think the type and extension names are switched (type contains .png, extension contains x/y),
	 * but maybe it's on purpouse, so I won't change it.
	 */
	public void put(String type, String extension, int icon){
		put(type, extension);
		mIcons.put(extension, icon);
	}
	
	public void put(String type, String extension) {
		// Convert extensions to lower case letters for easier comparison
		extension = extension.toLowerCase();
		
		mMimeTypes.put(type, extension);
	}
	
	public String getMimeType(String filename) {
		String extension = FileUtils.getExtension(filename);
		
		// Let's check the official map first. Webkit has a nice extension-to-MIME map.
		// Be sure to remove the first character from the extension, which is the "." character.
		if (extension.length() > 0) {
			String webkitMimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.substring(1));
		
			if (webkitMimeType != null) {
				// Found one. Let's take it!
				return webkitMimeType;
			}
		}
		
		// Convert extensions to lower case letters for easier comparison
		extension = extension.toLowerCase();
		
		String mimetype = mMimeTypes.get(extension);
		
		if(mimetype==null) mimetype = "*/*";
		
		return mimetype;
	}
	
	public int getIcon(String mimetype){
		Integer iconResId = mIcons.get(mimetype);
		if(iconResId == null)
			return 0; // Invalid identifier
		return iconResId;
	}
}
