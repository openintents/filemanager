package org.openintents.filemanager;

import java.util.List;

import org.openintents.filemanager.files.FileHolder;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

/**
 * Same as {@link FileHolderListAdapter}, but keeps checkable state of items.
 * @author George Venios.
 */
public class MultiselectFileHolderListAdapter extends FileHolderListAdapter{
	public MultiselectFileHolderListAdapter(List<FileHolder> files, Context c) {
		super(files, c);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		return super.getView(position, convertView, parent);
	}
	
	// TODO implement
	public boolean hasCheckedItems(){
		return false;
	}
}