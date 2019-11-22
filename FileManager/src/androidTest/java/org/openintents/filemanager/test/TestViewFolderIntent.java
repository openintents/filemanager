package org.openintents.filemanager.test;

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.view.View;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openintents.filemanager.FileManagerActivity;
import org.openintents.filemanager.view.PathBar;

@RunWith(AndroidJUnit4.class)
public class TestViewFolderIntent {

    @Rule
    public IntentsTestRule<FileManagerActivity> rule = new IntentsTestRule<>(FileManagerActivity.class, true, false);

    @Test
    public void testViewDownloadFolder() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setType(DocumentsContract.Document.MIME_TYPE_DIR);
        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
        intent.putExtra("org.openintents.extra.ABSOLUTE_PATH", path);
        rule.launchActivity(intent);

        Espresso.onView(ViewMatchers.withId(org.openintents.filemanager.R.id.pathbar)).check(ViewAssertions.matches(isShowingPath(path)));
    }

    @Test
    public void testNullPath() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setType(DocumentsContract.Document.MIME_TYPE_DIR);
        intent.putExtra("org.openintents.extra.ABSOLUTE_PATH", (String) null);
        rule.launchActivity(intent);

        // assert is showing toast
    }

    @Test
    public void testInvalidPath() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setType(DocumentsContract.Document.MIME_TYPE_DIR);
        intent.putExtra("org.openintents.extra.ABSOLUTE_PATH", "/xyz");
        rule.launchActivity(intent);

        // assert is showing toast
    }

    @Test
    public void testFileUriPath() {
        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(path));
        rule.launchActivity(intent);

        Espresso.onView(ViewMatchers.withId(org.openintents.filemanager.R.id.pathbar)).check(ViewAssertions.matches(isShowingPath(path)));
    }

    private Matcher<? super View> isShowingPath(final String path) {
        return new BaseMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("PathBar shows " + path);
            }

            @Override
            public boolean matches(Object item) {
                if (item != null && item.getClass() == PathBar.class) {
                    return (((PathBar) item).getCurrentDirectory().getAbsolutePath().equalsIgnoreCase(path));
                } else {
                    return false;
                }
            }
        };
    }
}
