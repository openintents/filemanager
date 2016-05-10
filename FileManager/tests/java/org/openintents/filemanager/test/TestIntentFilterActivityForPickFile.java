package org.openintents.filemanager.test;

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openintents.filemanager.IntentFilterActivity;
import org.openintents.intents.FileManagerIntents;

import java.io.IOException;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class TestIntentFilterActivityForPickFile extends BaseTestFileManager {

    @Rule
    public ActivityTestRule<IntentFilterActivity> rule = new ActivityTestRule<IntentFilterActivity>(IntentFilterActivity.class) {
        @Override
        protected Intent getActivityIntent() {
            Uri uri = Uri.parse("file://" + sdcardPath + TEST_DIRECTORY);
            Intent intent = new Intent(FileManagerIntents.ACTION_PICK_FILE, uri);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            return intent;
        }
    };

    @Before
    public void setup() throws IOException {
        sdcardPath = Environment.getExternalStorageDirectory().getAbsolutePath()+'/';
        createDirectory(sdcardPath + TEST_DIRECTORY);
        createFile(sdcardPath + "oi-filemanager-tests/oi-pick-file", "");
    }


    @Test
    public void testIntentPickFile() throws IOException {
        // startActivityForResult is, I think, impossible to test on Robotinium
        onView(withText("oi-pick-file")).check(matches(isDisplayed()));
    }
}
