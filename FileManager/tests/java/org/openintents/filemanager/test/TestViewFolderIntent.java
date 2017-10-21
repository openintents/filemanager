package org.openintents.filemanager.test;

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openintents.filemanager.FileManagerActivity;
import org.openintents.filemanager.view.PathBar;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

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

        onView(withId(org.openintents.filemanager.R.id.pathbar)).check(matches(isShowingPath(path)));
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

        onView(withId(org.openintents.filemanager.R.id.pathbar)).check(matches(isShowingPath(path)));
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
