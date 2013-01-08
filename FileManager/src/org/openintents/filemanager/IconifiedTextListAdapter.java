package org.openintents.filemanager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.openintents.filemanager.util.FileUtils;
import org.openintents.filemanager.util.MimeTypes;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;

/** @author Steven Osborn - http://steven.bitsetters.com */
public class IconifiedTextListAdapter extends BaseAdapter implements Filterable {

	/** Remember our context so we can use it when constructing views. */
	private Context mContext;

	private static String lastFilter;

	class IconifiedFilter extends Filter {
		@Override
		protected FilterResults performFiltering(CharSequence arg0) {

			lastFilter = (arg0 != null) ? arg0.toString() : null;

			Filter.FilterResults results = new Filter.FilterResults();

			// No results yet?
			if (mOriginalItems == null) {
				results.count = 0;
				results.values = null;
				return results;
			}

			int count = mOriginalItems.size();

			if (arg0 == null || arg0.length() == 0) {
				results.count = count;
				results.values = mOriginalItems;
				return results;
			}

			List<IconifiedText> filteredItems = new ArrayList<IconifiedText>(
					count);

			int outCount = 0;
			CharSequence lowerCs = arg0.toString().toLowerCase();

			for (int x = 0; x < count; x++) {
				IconifiedText text = mOriginalItems.get(x);

				if (text.getText().toLowerCase().contains(lowerCs)) {
					// This one matches.
					filteredItems.add(text);
					outCount++;
				}
			}

			results.count = outCount;
			results.values = filteredItems;
			return results;
		}

		@Override
		protected void publishResults(CharSequence arg0, FilterResults arg1) {
			mItems = (List<IconifiedText>) arg1.values;
			notifyDataSetChanged();
		}

		List<IconifiedText> synchronousFilter(CharSequence filter) {
			FilterResults results = performFiltering(filter);
			return (List<IconifiedText>) (results.values);
		}
	}

	private IconifiedFilter mFilter = new IconifiedFilter();

	private List<IconifiedText> mItems = new ArrayList<IconifiedText>();
	private List<IconifiedText> mOriginalItems = new ArrayList<IconifiedText>();

	private Drawable mIconChecked;
	private Drawable mIconUnchecked;

	public ThumbnailLoader mThumbnailLoader;

	private File parentFile;

	private MimeTypes mMimeTypes;

	private boolean scrolling = false;

	public IconifiedTextListAdapter(Context context) {
		mContext = context;

		mThumbnailLoader = new ThumbnailLoader(context);

		// Cache the checked and unchecked icons so we're not decoding them
		// everytime getView is called.
		mIconChecked = context.getResources().getDrawable(
				R.drawable.ic_button_checked);
		mIconUnchecked = context.getResources().getDrawable(
				R.drawable.ic_button_unchecked);
	}

	public void addItem(IconifiedText it) {
		mItems.add(it);
	}

	public void setListItems(List<IconifiedText> lit, boolean filter,
			File parentFile, MimeTypes mimeTypes) {
		mOriginalItems = lit;
		this.parentFile = parentFile;
		mMimeTypes = mimeTypes;

		if (filter) {
			mItems = mFilter.synchronousFilter(lastFilter);
		} else {
			mItems = lit;
		}
	}

	/** @return The number of items in the */
	public int getCount() {
		return mItems.size();
	}

	public Object getItem(int position) {
		return mItems.get(position);
	}

	public boolean areAllItemsSelectable() {
		return false;
	}

	/*
	 * public boolean isSelectable(int position) { try{ return
	 * mItems.get(position).isSelectable(); }catch (IndexOutOfBoundsException
	 * aioobe){ return super.isSelectable(position); } }
	 */

	/** Use the array index as a unique id. */
	public long getItemId(int position) {
		return position;
	}

	public ThumbnailLoader getThumbnailLoader() {
		return mThumbnailLoader;
	}

	public void toggleScrolling(boolean isScrolling) {
		scrolling = isScrolling;
	}

	/**
	 * @param convertView
	 *            The old view to overwrite, if one is passed
	 * @returns a IconifiedTextView that holds wraps around an IconifiedText
	 */
	public View getView(int position, View convertView, ViewGroup parent) {
		IconifiedText curItem = mItems.get(position);
		IconifiedTextView btv;
		if (convertView == null) {
			btv = new IconifiedTextView(mContext, curItem);
		} else { // Reuse/Overwrite the View passed
			// We are assuming(!) that it is castable!
			btv = (IconifiedTextView) convertView;
		}
		btv.setText(curItem.getText());
		btv.setInfo(curItem.getInfo());
		if (curItem.isCheckIconVisible()) {
			btv.setCheckVisible(true);
			if (curItem.isSelected()) {
				btv.setCheckDrawable(mIconChecked);
			} else {
				btv.setCheckDrawable(mIconUnchecked);
			}
		} else {
			btv.setCheckVisible(false);
		}

		Object icon = curItem.getIconBitmap();
		if (icon instanceof Bitmap) {
			btv.setIcon((Bitmap) icon);
		} else {
			btv.setIcon((Drawable) icon);
		}

		if (!scrolling
				&& FileUtils.getFile(parentFile, curItem.getText()).isFile()
				&& !"video/mpeg".equals(mMimeTypes.getMimeType(curItem
						.getText()))) {
			if (mThumbnailLoader != null) {
				
			}
		}

		return btv;
	}

	public Filter getFilter() {
		return mFilter;
	}

	public void cancelLoader() {
		mThumbnailLoader.cancel();
	}

	
}
