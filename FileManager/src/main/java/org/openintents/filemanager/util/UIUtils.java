package org.openintents.filemanager.util;

import android.app.Activity;

import org.openintents.filemanager.R;

import static androidx.preference.PreferenceManager.getDefaultSharedPreferences;

public abstract class UIUtils {

    public static void setThemeFor(Activity act) {
        if (getDefaultSharedPreferences(act).getBoolean("usedarktheme", true)) {
            act.setTheme(R.style.Theme_Dark);
        } else {
            act.setTheme(R.style.Theme_Light_DarkTitle);
        }
    }

    public static boolean shouldDialogInverseBackground(Activity act) {
        return !getDefaultSharedPreferences(act).getBoolean("usedarktheme", true);
    }
}
