package org.openintents.filemanager.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

public final class ImageUtils {

	/**
	 * Resizes specific a Bitmap with keeping ratio.
	 */
	public static Bitmap resizeBitmap(Bitmap drawable, int desireWidth,
			int desireHeight) {
		int width = drawable.getWidth();
		int height = drawable.getHeight();

		if (0 < width && 0 < height && desireWidth < width
				|| desireHeight < height) {
			// Calculate scale
			float scale;
			if (width < height) {
				scale = (float) desireHeight / (float) height;
				if (desireWidth < width * scale) {
					scale = (float) desireWidth / (float) width;
				}
			} else {
				scale = (float) desireWidth / (float) width;
			}

			// Draw resized image
			Matrix matrix = new Matrix();
			matrix.postScale(scale, scale);
			Bitmap bitmap = Bitmap.createBitmap(drawable, 0, 0, width, height,
					matrix, true);
			Canvas canvas = new Canvas(bitmap);
			canvas.drawBitmap(bitmap, 0, 0, null);

			drawable = bitmap;
		}

		return drawable;
	}

	/**
	 * Resizes specific a Drawable with keeping ratio.
	 */
	public static Drawable resizeDrawable(Drawable drawable, int desireWidth,
			int desireHeight) {
		int width = drawable.getIntrinsicWidth();
		int height = drawable.getIntrinsicHeight();

		if (0 < width && 0 < height && desireWidth < width
				|| desireHeight < height) {
			drawable = new BitmapDrawable(resizeBitmap(
					((BitmapDrawable) drawable).getBitmap(), desireWidth,
					desireHeight));
		}

		return drawable;
	}

}
