package org.openintents.filemanager;

import java.util.List;

import org.openintents.filemanager.files.FileHolder;
import org.openintents.filemanager.view.ViewHolder;

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
	private int mItemLayoutId = R.layout.item_filelist;
	
	// Thumbnail specific
    private ThumbnailLoader mThumbnailLoader;
    private boolean scrolling = false;
	
	public FileHolderListAdapter(List<FileHolder> files, Context c){
		mItems = files;
		mInflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mContext = c;
		
		mThumbnailLoader = new ThumbnailLoader(c);
	}
	
	public Context getContext(){
		return mContext;
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
	 * Set the layout to be used for item drawing. 
	 * @param resId The item layout id. 0 to reset.
	 */
	public void setItemLayout(int resId){
		if(resId > 0)
			mItemLayoutId = resId;
		else
			mItemLayoutId = R.layout.item_filelist;
	}
	
	/**
	 * Creates a new list item view, along with it's ViewHolder set as a tag.
	 * @return The new view.
	 */
	protected View newView(){
		View view = mInflater.inflate(mItemLayoutId, null);
		
		ViewHolder holder = new ViewHolder();
		holder.icon = (ImageView) view.findViewById(R.id.icon);
		holder.primaryInfo = (TextView) view.findViewById(R.id.primary_info);
		holder.secondaryInfo = (TextView) view.findViewById(R.id.secondary_info);
		holder.tertiaryInfo = (TextView) view.findViewById(R.id.tertiary_info);
		
		view.setTag(holder);
		return view;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		FileHolder item = mItems.get(position);
		
		if(convertView == null)
			convertView = newView();
		
		ViewHolder holder = (ViewHolder) convertView.getTag();
		
		holder.icon.setImageDrawable(item.getIcon());
		holder.primaryInfo.setText(item.getName());
		holder.secondaryInfo.setText(item.getFormattedModificationDate(mContext));
		// Hide directories' size as it's irrelevant if we can't recursively find it.
		holder.tertiaryInfo.setText(item.getFile().isDirectory()? "" : item.getFormattedSize(mContext, false));
        
        if(shouldLoadIcon(item)){
	        if(mThumbnailLoader != null) {
	        	mThumbnailLoader.loadImage(item, holder.icon);
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
}