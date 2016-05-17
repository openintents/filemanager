package org.openintents.filemanager;

import java.io.File;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.openintents.filemanager.files.FileHolder;
import org.openintents.filemanager.util.FileUtils;
import org.openintents.filemanager.util.ImageUtils;
import org.openintents.filemanager.util.MimeTypes;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.TypedValue;
import android.widget.ImageView;

public class ThumbnailLoader {
	private static final String MIME_APK = "application/vnd.android.package-archive";
	
	private static final String TAG = "OIFM_ThumbnailLoader";
	
	// Both hard and soft caches are purged after 40 seconds idling. 
	private static final int DELAY_BEFORE_PURGE = 40000;
	private static final int MAX_CACHE_CAPACITY = 40;
	
	// Maximum number of threads in the executor pool.
	// TODO: Tune POOL_SIZE for maximum performance gain
	private static final int POOL_SIZE = 5;
    private final boolean mUseBestMatch;

    private boolean cancel;
    private Context mContext;
	
    //private static int thumbnailWidth = 96;
    //private static int thumbnailHeight = 129;
    private static int thumbnailWidth = 96;
    private static int thumbnailHeight = 96;
    
    private Runnable purger;
    private Handler purgeHandler;
    private PausableThreadPoolExecutor mExecutor;
    
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
		mExecutor = new PausableThreadPoolExecutor(POOL_SIZE);
		
		mBlacklist = new ArrayList<>();
		mSoftBitmapCache = new ConcurrentHashMap<>(MAX_CACHE_CAPACITY / 2);
		mHardBitmapCache = new LinkedHashMap<String, Bitmap>(MAX_CACHE_CAPACITY / 2, 0.75f, true){
			
			/***/
			private static final long serialVersionUID = 1347795807259717646L;
			
			@Override
			protected boolean removeEldestEntry(LinkedHashMap.Entry<String, Bitmap> eldest){
				// Moves the last used item in the hard cache to the soft cache.
				if(size() > MAX_CACHE_CAPACITY){
					mSoftBitmapCache.put(eldest.getKey(), new SoftReference<>(eldest.getValue()));
					return true;
				} else {
					return false;
				}
			}
		};

        mUseBestMatch = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PreferenceActivity.PREFS_USEBESTMATCH, true);
	}  

	public static void setThumbnailHeight(int height) {
		thumbnailHeight = height;
		thumbnailWidth = height * 4 / 3;
	}
	
	/**
	 * @param holder The {@link File} container.
	 * @param imageView The ImageView from the IconifiedTextView.
	 */
	public void loadImage(FileHolder holder, ImageView imageView) {
		if(!cancel && !mBlacklist.contains(holder.getName())){
			// We reset the caches after every 30 or so seconds of inactivity for memory efficiency.
			resetPurgeTimer();
			
			Bitmap bitmap = getBitmapFromCache(holder.getName());
			if(bitmap != null){
				// We're still in the UI thread so we just update the icons from here.
				imageView.setImageBitmap(bitmap);
				holder.setIcon(new BitmapDrawable(bitmap));
			} else {
				// Give a drawable based on mimetype. Generic file drawable for undefined types.
				if(holder.getFile().isFile())
					holder.setIcon(getScaledDrawableForMimetype(holder, mContext));
					
				if (!cancel) {
					// Submit the file for decoding.
					Thumbnail thumbnail = new Thumbnail(imageView, holder);
					ThumbnailRunner thumbnailRunner = new ThumbnailRunner(thumbnail);
					mExecutor.submit(thumbnailRunner);
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
	 * The file to decode.
	 * @return The resized and resampled bitmap, if can not be decoded it returns null.
	 */
	private Bitmap decodeFile(File file) {
		if(!cancel){
			try {
				BitmapFactory.Options options = new BitmapFactory.Options();
				
				options.inJustDecodeBounds = true;
				options.outWidth = 0;
				options.outHeight = 0;
				options.inSampleSize = 1;
				
				String filePath = file.getAbsolutePath();
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
						if (widthFactor > 1 && (widthFactor & (widthFactor - 1)) != 0) {
							while ((widthFactor & (widthFactor - 1)) != 0) {
								widthFactor &= widthFactor - 1;
							}

							widthFactor <<= 1;
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
					if(!mBlacklist.contains(file.getName())){
						mBlacklist.add(filePath);
					}
				}
			} catch(Exception e) { }
		}
		return null;
	}

	public void startProcessingLoaderQueue() {
		mExecutor.resume();
	}

	public void stopProcessingLoaderQueue() {
		mExecutor.pause();
	}

	/**
	 * Holder object for thumbnail information. 
	 */
	private class Thumbnail {
		public ImageView imageView;
		public FileHolder holder;
		
		public Thumbnail(ImageView imageView, FileHolder text) {
			this.imageView = imageView;
			this.holder = text;
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
				Bitmap bitmap = decodeFile(thumb.holder.getFile());
				
				Activity activity = (Activity) mContext;

				if(!cancel){
					if(bitmap != null){
						// Bitmap was successfully decoded so we place it in the hard cache.
						mHardBitmapCache.put(thumb.holder.getName(), bitmap);
						activity.runOnUiThread(new ThumbnailUpdater(bitmap, thumb));
					}
					else {
						activity.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								thumb.imageView.setImageDrawable(thumb.holder.getIcon());
								thumb = null;
							}
						});
					}
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
				thumb.holder.setIcon(new BitmapDrawable(bitmap));
			}
			thumb = null;
		}
	}
	private Drawable getScaledDrawableForMimetype(FileHolder holder, Context context){
		Drawable d = getDrawableForMimetype(holder, context);
		
		if (d == null) {
			return new BitmapDrawable(context.getResources(), BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher_file));
		} else {
			int size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, context.getResources().getDisplayMetrics());
			// Resizing image.
			return ImageUtils.resizeDrawable(d, size, size);
		}
	}
	
	/**
	 * Return the Drawable that is associated with a specific mime type for the VIEW action.
	 */
	private Drawable getDrawableForMimetype(FileHolder holder, Context context) {
		if (holder.getMimeType() == null) {
			return null;
		}

		PackageManager pm = context.getPackageManager();

		// Returns the icon packaged in files with the .apk MIME type.
		if (holder.getMimeType().equals(MIME_APK)) {
			String path = holder.getFile().getPath();
			PackageInfo pInfo = pm.getPackageArchiveInfo(path,
					PackageManager.GET_ACTIVITIES);
			if (pInfo != null) {
				ApplicationInfo aInfo = pInfo.applicationInfo;

				// Bug in SDK versions >= 8. See here:
				// http://code.google.com/p/android/issues/detail?id=9151
				if (Build.VERSION.SDK_INT >= 8) {
					aInfo.sourceDir = path;
					aInfo.publicSourceDir = path;
				}

				return aInfo.loadIcon(pm);
			}
		}

		int iconResource = MimeTypes.getInstance().getIcon(holder.getMimeType());
		Drawable ret = null;
		if (iconResource > 0) {
			try {
				ret = pm.getResourcesForApplication(context.getPackageName())
						.getDrawable(iconResource);
			} catch (NotFoundException|NameNotFoundException e) {
			}
		}

		if (ret != null) {
			return ret;
		}

		if ("*/*".equals(holder.getMimeType())){
			return null;
		}
		
		Uri data = FileUtils.getUri(holder.getFile());

		Intent intent = new Intent(Intent.ACTION_VIEW);
		// intent.setType(mimetype);

		// Let's probe the intent exactly in the same way as the VIEW action
		// is performed in FileManagerActivity.openFile(..)
		intent.setDataAndType(data, holder.getMimeType());

		final List<ResolveInfo> lri = pm.queryIntentActivities(intent,
				PackageManager.MATCH_DEFAULT_ONLY);

		if (lri != null && !lri.isEmpty()) {
			// Log.i(TAG, "lri.size()" + lri.size());

            // Actually first element should be "best match",
            // but it seems that more recently installed applications
            // could be even better match.
            int index = mUseBestMatch ? 0: lri.size() - 1;



			final ResolveInfo ri = lri.get(index);
			return ri.loadIcon(pm);
		}

		return null;
	}
}
