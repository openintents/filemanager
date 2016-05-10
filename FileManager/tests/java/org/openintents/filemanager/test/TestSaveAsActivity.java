package org.openintents.filemanager.test;

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.test.espresso.action.ViewActions;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.text.InputType;
import android.view.KeyEvent;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openintents.filemanager.SaveAsActivity;

import java.io.File;
import java.io.IOException;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.longClick;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
public class TestSaveAsActivity extends BaseTestFileManager {

    @Rule
    public ActivityTestRule<SaveAsActivity> rule = new ActivityTestRule<SaveAsActivity>(SaveAsActivity.class) {
        @Override
        protected Intent getActivityIntent() {
            Uri uri = Uri.parse("file://" + sdcardPath + "oi-filemanager-tests/oi-to-open.txt");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.setClassName("org.openintents.filemanager", SaveAsActivity.class.getCanonicalName());
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            return intent;
        }
    };

    @Before
    public void setup() throws IOException {
        sdcardPath = Environment.getExternalStorageDirectory().getAbsolutePath()+'/';
        createDirectory(sdcardPath + TEST_DIRECTORY);
        createFile(sdcardPath + "oi-filemanager-tests/oi-to-open.txt", "bbb");
    }

    @Test
    public void testIntentSaveAs() {
        onView(withText(Environment.getExternalStorageDirectory().getParentFile().getName())).perform(longClick());
        onView(ViewMatchers.withInputType(InputType.TYPE_CLASS_TEXT)).perform(typeText("oi-target.txt"));
        onView(ViewMatchers.withInputType(InputType.TYPE_CLASS_TEXT)).perform(ViewActions.pressKey(KeyEvent.KEYCODE_ENTER));

        assertThat(new File(sdcardPath + "oi-filemanager-tests/oi-to-open.txtoi-target.txt").exists(), is(true));
    }
}
