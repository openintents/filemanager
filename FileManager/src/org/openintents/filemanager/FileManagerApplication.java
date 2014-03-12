package org.openintents.filemanager;

import org.openintents.filemanager.util.CopyHelper;
import org.openintents.filemanager.compatibility.KitKatExternalStorage;

import android.app.Application;
import android.content.Context;
import java.io.File;
import android.os.Build;
import java.util.ArrayList;

public class FileManagerApplication extends Application{
	private CopyHelper mCopyHelper;
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		mCopyHelper = new CopyHelper(this);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
			KitKatExternalStorage.createWritableDirs(this);
	}
	
	public CopyHelper getCopyHelper(){
		return mCopyHelper;
	}
}