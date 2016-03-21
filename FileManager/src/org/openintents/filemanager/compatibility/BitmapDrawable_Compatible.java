package org.openintents.filemanager.compatibility;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;

public class BitmapDrawable_Compatible {
	
	private static boolean use_SDK_1_6 = true;
	
	/**
	 * Replaces "new BitmapDrawable(context.getResources(), bitmap)" available only in SDK 1.6 and higher.
	 * 
	 * @param resources
	 * @param bitmap
	 * @return
	 */
	public static BitmapDrawable getNewBitmapDrawable(Resources resources, Bitmap bitmap) {
		BitmapDrawable b;
		if (use_SDK_1_6) {
			try {
				// SDK 1.6 compatible version
				b = BitmapDrawable_SDK_1_6.getNewBitmapDrawable(resources, bitmap);
			} catch (VerifyError e) {
				// SDK 1.5 compatible version:
				use_SDK_1_6 = false;
				b = new BitmapDrawable(bitmap);
			}
		} else {
			// SDK 1.5 compatible version:
			b = new BitmapDrawable(bitmap);
		}
		return b;
	}
}
