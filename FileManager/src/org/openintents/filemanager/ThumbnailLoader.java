package org.openintents.filemanager;

import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.openintents.filemanager.util.FileUtils;
import org.openintents.filemanager.util.ImageUtils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;

public class ThumbnailLoader {
	
	private static final String TAG = "OIFM_ThumbnailLoader";
	
	// Both hard and soft caches are purged after 40 seconds idling. 
	private static final int DELAY_BEFORE_PURGE = 40000;
	private static final int MAX_CACHE_CAPACITY = 40;
	
	// Maximum number of threads in the executor pool.
	// TODO: Tune POOL_SIZE for maximum performance gain
	private static final int POOL_SIZE = 5;
	
    private boolean cancel;
    private Context mContext;
	
    //private static int thumbnailWidth = 96;
    //private static int thumbnailHeight = 129;
    private static int thumbnailWidth = 32;
    private static int thumbnailHeight = 32;
    
    private Runnable purger;
    private Handler purgeHandler;
    private ExecutorService mExecutor;
    
    // Soft bitmap cache for thumbnails removed from the hard cache.
    // This gets cleared by the Garbage Collector everytime we get low on memory.
    private ConcurrentHashMap<String, SoftReference<Bitmap>> mSoftBitmapCache;
    private LinkedHashMap<String, Bitmap> mHardBitmapCache;
    private ArrayList<String> mBlacklist;
    
    /**
     * Used for loading and decoding thumbnails from files.
     * 
     * @author PhilipHayes
     * @param context Current application context.
     */
	public ThumbnailLoader(Context context) {
		mContext = context;
		
		purger = new Runnable(){
			@Override
			public void run() {
				Log.d(TAG, "Purge Timer hit; Clearing Caches.");
				clearCaches();
			}
		};
		
		purgeHandler = new Handler();
		mExecutor = Executors.newFixedThreadPool(POOL_SIZE);
		
		mBlacklist = new ArrayList<String>();
		mSoftBitmapCache = new ConcurrentHashMap<String, SoftReference<Bitmap>>(MAX_CACHE_CAPACITY / 2);
		mHardBitmapCache = new LinkedHashMap<String, Bitmap>(MAX_CACHE_CAPACITY / 2, 0.75f, true){
			
			/***/
			private static final long serialVersionUID = 1347795807259717646L;
			
			@Override
			protected boolean removeEldestEntry(LinkedHashMap.Entry<String, Bitmap> eldest){
				// Moves the last used item in the hard cache to the soft cache.
				if(size() > MAX_CACHE_CAPACITY){
					mSoftBitmapCache.put(eldest.getKey(), new SoftReference<Bitmap>(eldest.getValue()));
					return true;
				} else {
					return false;
				}
			}
		};
	}  

	public static void setThumbnailHeight(int height) {
		thumbnailHeight = height;
		thumbnailWidth = height * 4 / 3;
	}
	
	/**
	 * 
	 * @param parentFile The current directory.
	 * @param text The IconifiedText container.
	 * @param imageView The ImageView from the IconifiedTextView.
	 */
	public void loadImage(String parentFile, IconifiedText text, ImageView imageView) {
		if(!cancel && !mBlacklist.contains(text.getText())){
			// We reset the caches after every 30 or so seconds of inactivity for memory efficiency.
			resetPurgeTimer();
			
			Bitmap bitmap = getBitmapFromCache(text.getText());
			if(bitmap != null){
				// We're still in the UI thread so we just update the icons from here.
				imageView.setImageBitmap(bitmap);
				text.setIcon(bitmap);
			} else {
				if (!cancel) {
					// Submit the file for decoding.
					Thumbnail thumbnail = new Thumbnail(parentFile, imageView, text);
					WeakReference<ThumbnailRunner> runner = new WeakReference<ThumbnailRunner>(new ThumbnailRunner(thumbnail));
					mExecutor.submit(runner.get());
				}
			}
		}
	}
	/**
	 * Cancels any downloads, shuts down the executor pool,
	 * and then purges the caches.
	 */
	public void cancel(){
		cancel = true;
		
		// We could also terminate it immediately,
		// but that may lead to synchronization issues.
		if(!mExecutor.isShutdown()){
			mExecutor.shutdown();
		}
		
		stopPurgeTimer();
		
		mContext = null;
		clearCaches();
	}
	
	/**
	 * Stops the cache purger from running until it is reset again.
	 */
	public void stopPurgeTimer(){
		purgeHandler.removeCallbacks(purger);
	}
	
	/**
	 * Purges the cache every (DELAY_BEFORE_PURGE) milliseconds.
	 * @see DELAY_BEFORE_PURGE
	 */
	private void resetPurgeTimer() {
		purgeHandler.removeCallbacks(purger);
		purgeHandler.postDelayed(purger, DELAY_BEFORE_PURGE);
	}
	
	private void clearCaches(){
		mSoftBitmapCache.clear();
		mHardBitmapCache.clear();
		mBlacklist.clear();
	}
	
	/**
	 * @param key In this case the file name (used as the mapping id).
	 * @return bitmap The cached bitmap or null if it could not be located.
	 * 
	 * As the name suggests, this method attemps to obtain a bitmap stored
	 * in one of the caches. First it checks the hard cache for the key.
	 * If a key is found, it moves the cached bitmap to the head of the cache
	 * so it gets moved to the soft cache last.
	 * 
	 * If the hard cache doesn't contain the bitmap, it checks the soft cache
	 * for the cached bitmap. If neither of the caches contain the bitmap, this
	 * returns null.
	 */
	private Bitmap getBitmapFromCache(String key){
		synchronized(mHardBitmapCache) {
			Bitmap bitmap = mHardBitmapCache.get(key);
			if(bitmap != null){
				// Put bitmap on top of cache so it's purged last.
				mHardBitmapCache.remove(key);
				mHardBitmapCache.put(key, bitmap);
				return bitmap;
			}
		}
		
		SoftReference<Bitmap> bitmapRef = mSoftBitmapCache.get(key);
		if(bitmapRef != null){
			Bitmap bitmap = bitmapRef.get();
			if(bitmap != null){
				return bitmap;
			} else {
				// Must have been collected by the Garbage Collector 
				// so we remove the bucket from the cache.
				mSoftBitmapCache.remove(key);
			}
		}
		
		// Could not locate the bitmap in any of the caches, so we return null.
		return null;
	}
	
	/**
	 * @param parentFile The parentFile, so we can obtain the full path of the bitmap
	 * @param fileName The name of the file, also the text in the list item.
	 * @return The resized and resampled bitmap, if can not be decoded it returns null.
	 */
	private Bitmap decodeFile(String parentFile, String fileName) {
		if(!cancel){
			try {
				BitmapFactory.Options options = new BitmapFactory.Options();
				
				options.inJustDecodeBounds = true;
				options.outWidth = 0;
				options.outHeight = 0;
				options.inSampleSize = 1;
				
				String filePath = FileUtils.getFile(parentFile, fileName).getPath();
		
				BitmapFactory.decodeFile(filePath, options);
				
				if(options.outWidth > 0 && options.outHeight > 0){
					if (!cancel) {
						// Now see how much we need to scale it down.
						int widthFactor = (options.outWidth + thumbnailWidth - 1)
								/ thumbnailWidth;
						int heightFactor = (options.outHeight + thumbnailHeight - 1)
								/ thumbnailHeight;
						widthFactor = Math.max(widthFactor, heightFactor);
						widthFactor = Math.max(widthFactor, 1);
						// Now turn it into a power of two.
						if (widthFactor > 1) {
							if ((widthFactor & (widthFactor - 1)) != 0) {
								while ((widthFactor & (widthFactor - 1)) != 0) {
									widthFactor &= widthFactor - 1;
								}

								widthFactor <<= 1;
							}
						}
						options.inSampleSize = widthFactor;
						options.inJustDecodeBounds = false;
						Bitmap bitmap = ImageUtils.resizeBitmap(
								BitmapFactory.decodeFile(filePath, options),
								72, 72);
						if (bitmap != null) {
							return bitmap;
						}
					}
				} else {
					// Must not be a bitmap, so we add it to the blacklist.
					if(!mBlacklist.contains(fileName)){
						mBlacklist.add(fileName);
					}
				}
			} catch(Exception e) { }
		}
		return null;
	}
	
	/**
	 * Holder object for thumbnail information.
	 */
	private class Thumbnail {
		public String parentFile;
		public ImageView imageView;
		public IconifiedText text;
		
		public Thumbnail(String parentFile, ImageView imageView, IconifiedText text) {
			this.parentFile = parentFile;
			this.imageView = imageView;
			this.text = text;
		}
	}
	
	/**
	 * Decodes the bitmap and sends a ThumbnailUpdater on the UI Thread
	 * to update the listitem and iconified text.
	 * 
	 * @see ThumbnailUpdater
	 */
	private class ThumbnailRunner implements Runnable {
		Thumbnail thumb;
		ThumbnailRunner(Thumbnail thumb){
			this.thumb = thumb;
		}
		
		@Override
		public void run() {
			if(!cancel){
				Bitmap bitmap = decodeFile(thumb.parentFile, thumb.text.getText());
				if(bitmap != null && !cancel){
					// Bitmap was successfully decoded so we place it in the hard cache.
					mHardBitmapCache.put(thumb.text.getText(), bitmap);
					Activity activity = ((Activity) mContext);
					activity.runOnUiThread(new ThumbnailUpdater(bitmap, thumb));
					thumb = null;
				}
			}
		}
	}
	
	/**
	 * When run on the UI Thread, this updates the 
	 * thumbnail in the corresponding iconifiedtext and imageview.
	 */
	private class ThumbnailUpdater implements Runnable {
		private Bitmap bitmap;
		private Thumbnail thumb;
		
		public ThumbnailUpdater(Bitmap bitmap, Thumbnail thumb) {
			this.bitmap = bitmap;
			this.thumb = thumb;
		}
		
		@Override
		public void run() {
			if(bitmap != null && mContext != null && !cancel){
				thumb.imageView.setImageBitmap(bitmap);
				thumb.text.setIcon(bitmap);
			}
		}
	}
}
