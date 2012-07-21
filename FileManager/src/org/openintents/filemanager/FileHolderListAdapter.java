package org.openintents.filemanager;

import java.util.List;

import org.openintents.filemanager.files.FileHolder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class FileHolderListAdapter extends BaseAdapter {
	private List<FileHolder> mItems;
	private LayoutInflater mInflater;
	private Context mContext;
	
	// Thumbnail specific
    public ThumbnailLoader mThumbnailLoader;
    private boolean scrolling = false;
	
	public FileHolderListAdapter(List<FileHolder> files, Context c){
		mItems = files;
		mInflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mContext = c;
		
		mThumbnailLoader = new ThumbnailLoader(c);
	}
	
	@Override
	public boolean hasStableIds() {
		return true;
	}
	
	@Override
	public int getCount() {
		return mItems.size();
	}

	@Override
	public Object getItem(int position) {
		return mItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
	/**
	 * Creates a new list item view, along with it's ViewHolder set as a tag.
	 * @return The new view.
	 */
	private View newView(){
		View view = mInflater.inflate(R.layout.filelist_item, null);
		
		ViewHolder holder = new ViewHolder();
		holder.icon = (ImageView) view.findViewById(R.id.icon);
		holder.name = (TextView) view.findViewById(R.id.primary_info);
		holder.modified = (TextView) view.findViewById(R.id.secondary_info);
		holder.size = (TextView) view.findViewById(R.id.tertiary_info);
		
		view.setTag(holder);
		return view;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		FileHolder item = mItems.get(position);
		
		if(convertView == null)
			convertView = newView();
		
		ViewHolder holder = (ViewHolder) convertView.getTag();
		
		holder.icon.setImageDrawable(item.getIcon(mContext));
		holder.name.setText(item.getName());
		holder.modified.setText(item.getFormattedModificationDate());
		holder.size.setText(item.getFormattedSize(mContext));
        
        if(!scrolling && item.getFile().isFile() && !item.getMimeType().equals("video/mpeg")){
      	  if(mThumbnailLoader != null) {
      		  mThumbnailLoader.loadImage(item.getFile().getParent(), item, holder.icon);
      	  }
        }
        
		return convertView;
	}
	
	private class ViewHolder {
		ImageView icon;
		TextView name;
		TextView modified;
		TextView size;
	}
	
	/**
	 * Inform this adapter about scrolling state of list so that lists don't lag due to cache ops.
	 * @param isScrolling True if the ListView is still scrolling.
	 */
	public void setScrolling(boolean isScrolling){
		scrolling = isScrolling;
	}
}