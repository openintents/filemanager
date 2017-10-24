package org.openintents.filemanager.compatibility;

import android.content.Context;
import android.os.IBinder;
import android.view.inputmethod.InputMethodManager;

/*
 * Wraper class for closing the software keyboard, which appeared in API 3.
 */

public class SoftKeyboard {
    /* class initialization fails when this throws an exception */
    static {
        try {
            Class.forName("android.view.inputmethod.InputMethodManager");
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private InputMethodManager inputMethodManager;

    public SoftKeyboard(Context ctx) {
        inputMethodManager = (InputMethodManager) ctx.getSystemService(
                Context.INPUT_METHOD_SERVICE);
    }

    /* calling here forces class initialization */
    public static void checkAvailable() {
    }

    public boolean hideSoftInputFromWindow(IBinder windowToken, int flags) {
        return inputMethodManager.hideSoftInputFromWindow(windowToken, flags);
    }
}