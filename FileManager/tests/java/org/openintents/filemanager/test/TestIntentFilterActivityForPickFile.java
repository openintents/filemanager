package org.openintents.filemanager.test;

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openintents.filemanager.IntentFilterActivity;
import org.openintents.intents.FileManagerIntents;

import java.io.IOException;

import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;

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

    @BeforeClass
    public static void setup() throws IOException {
        sdcardPath = Environment.getExternalStorageDirectory().getAbsolutePath() + '/';
        createDirectory(sdcardPath + TEST_DIRECTORY);
        createFile(sdcardPath + TEST_DIRECTORY + "/oi-pick-file", "");
    }


    @Test
    public void testIntentDataIsUsedAsInitialDirectory() throws IOException {
        checkFile("oi-pick-file", matches(isDisplayed()));
    }
}
