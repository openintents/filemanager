package org.openintents.filemanager.bookmarks;

import java.io.File;
import java.util.ArrayList;

import org.openintents.filemanager.R;
import org.openintents.filemanager.ThumbnailLoader;
import org.openintents.filemanager.files.FileHolder;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * @author George Venios
 */
public class BookmarkListAdapter extends BaseAdapter{
	private ArrayList<Bookmark> items;
	private LayoutInflater inflater;
	private Activity act;

	// Thumbnail specific
    private ThumbnailLoader mThumbnailLoader;
    private boolean scrolling = false;
    
	public BookmarkListAdapter(Activity activity){
		items = new ArrayList<Bookmark>();
		
		act = activity;
		refreshItems();
		inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		mThumbnailLoader = new ThumbnailLoader(act);
	}
	
	private void refreshItems() {
		items.clear();
		
		String[] projection = {BookmarksProvider._ID, BookmarksProvider.NAME, BookmarksProvider.PATH};
		Cursor c = act.managedQuery(BookmarksProvider.CONTENT_URI, 
				projection, 
				null, 
				null, 
				null);
		
		Bookmark b = null;
		while(c.moveToNext()){
			b = new Bookmark();
			
			b.id = c.getLong(0);
			b.name = c.getString(1);
			b.path = c.getString(2);
			
			items.add(b);
		}
	}

	@Override
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();
		refreshItems();
	}
	
	@Override
	public int getCount() {
		return items.size();
	}

	@Override
	public Object getItem(int position) {
		return items.get(position);
	}

	@Override
	public long getItemId(int position) {
		return items.get(position).id;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		FileHolder item = new FileHolder(new File(items.get(position).path), act);
		
		if(convertView==null)
			convertView = inflater.inflate(R.layout.item_filelist, null);
		
		((TextView) convertView.findViewById(R.id.primary_info)).setText(items.get(position).name);
		((TextView) convertView.findViewById(R.id.secondary_info)).setText(items.get(position).path);
        
		if(item.getFile().isDirectory()) {
			((ImageView) convertView.findViewById(R.id.icon)).setImageResource(R.drawable.ic_launcher_folder);
		}
		
		if (shouldLoadIcon(item)) {
			if (mThumbnailLoader != null) {
				mThumbnailLoader.loadImage(item, (ImageView) convertView.findViewById(R.id.icon));
			}
		}
		
		return convertView;
	}
	
	/**
	 * Inform this adapter about scrolling state of list so that lists don't lag due to cache ops.
	 * @param isScrolling True if the ListView is still scrolling.
	 */
	public void setScrolling(boolean isScrolling){
		scrolling = isScrolling;
		if(!isScrolling)
			notifyDataSetChanged();
	}
	
	private boolean shouldLoadIcon(FileHolder item){
		return !scrolling && item.getFile().isFile() && !item.getMimeType().equals("video/mpeg");
	}
	
	protected class Bookmark{
		long id;
		String name;
		String path;
	}
}