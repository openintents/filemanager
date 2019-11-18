package org.openintents.filemanager;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import org.openintents.filemanager.util.CopyHelper;
import org.openintents.filemanager.util.MimeTypes;

public class FileManagerApplication extends Application {
    private CopyHelper mCopyHelper;

    public static boolean hideDonateMenu(Context context) {
        ApplicationInfo ai;
        try {
            ai = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            return ai.metaData.getBoolean("hideDonate");
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mCopyHelper = new CopyHelper(this);
        MimeTypes.initInstance(this);
    }

    public CopyHelper getCopyHelper() {
        return mCopyHelper;
    }
}