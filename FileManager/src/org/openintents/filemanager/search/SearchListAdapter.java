package org.openintents.filemanager.search;

import org.openintents.filemanager.R;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Uses deprecated requery methods as the other ones are available on 11 and up, plus the actual queries are too cheap to block the thread for too long.
 * 
 * @author George Venios
 * 
 */
public class SearchListAdapter extends CursorAdapter {

	public SearchListAdapter(Context context, Cursor c) {
		super(context, c, true);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		ViewHolder h = (ViewHolder) view.getTag();
		
		h.filename.setText(cursor.getString(cursor.getColumnIndex(SearchResultsProvider.COLUMN_NAME)));
		h.path.setText(cursor.getString(cursor.getColumnIndex(SearchResultsProvider.COLUMN_PATH)));
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		// Inflate the view
		ViewGroup v = (ViewGroup) ((LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(
				R.layout.filelist_item, null);
		v.findViewById(R.id.select_icon).setVisibility(View.GONE);

		// Set the viewholder optimization.
		ViewHolder holder = new ViewHolder();
		holder.icon = (ImageView) v.findViewById(R.id.icon);
		holder.filename = (TextView) v.findViewById(R.id.text);
		holder.path = (TextView) v.findViewById(R.id.info);
		v.setTag(holder);
		
		return v;
	}

	private class ViewHolder {
		ImageView icon;
		TextView filename;
		TextView path;
	}

}