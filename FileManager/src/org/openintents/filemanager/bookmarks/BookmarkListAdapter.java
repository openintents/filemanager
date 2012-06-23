package org.openintents.filemanager.bookmarks;

import java.util.ArrayList;

import org.openintents.filemanager.R;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * @author George Venios
 */
public class BookmarkListAdapter extends BaseAdapter{
	private ArrayList<Bookmark> items;
	private LayoutInflater inflater;
	private Activity act;
	
	public BookmarkListAdapter(Activity activity){
		items = new ArrayList<Bookmark>();
		
		act = activity;
		refreshItems();
		inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
		if(convertView==null)
			convertView = inflater.inflate(R.layout.bookmarklist_item, null);
		
		((TextView) convertView.findViewById(android.R.id.text1)).setText(items.get(position).name);
		((TextView) convertView.findViewById(android.R.id.text2)).setText(items.get(position).path);
		
		return convertView;
	}
	
	protected class Bookmark{
		long id;
		String name;
		String path;
	}
}