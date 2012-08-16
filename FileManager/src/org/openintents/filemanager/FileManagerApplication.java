package org.openintents.filemanager;

import org.openintents.filemanager.util.CopyHelper;

import android.app.Application;

public class FileManagerApplication extends Application{
	private CopyHelper mCopyHelper;
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		mCopyHelper = new CopyHelper(this);
	}
	
	public CopyHelper getCopyHelper(){
		return mCopyHelper;
	}
}