package org.openintents.filemanager.test;

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.view.View;

import androidx.test.InstrumentationRegistry;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.BoundedMatcher;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.rule.UiThreadTestRule;
import androidx.test.runner.AndroidJUnit4;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openintents.filemanager.IntentFilterActivity;
import org.openintents.filemanager.R;
import org.openintents.filemanager.view.PathBar;
import org.openintents.intents.FileManagerIntents;

import java.io.File;
import java.io.IOException;

@RunWith(AndroidJUnit4.class)
public class TestPickFilePathHistory extends BaseTestFileManager {

    public static final String OI_TO_PICK_TEST_FOLDER_DELETED = "oi-to-pick-test-folder-deleted";
    @Rule
    public UiThreadTestRule rule = new UiThreadTestRule();

    private static Matcher<View> hasInitialDirectory(final Matcher<String> directoryNameMatcher) {
        return new BoundedMatcher<View, PathBar>(PathBar.class) {
            @Override
            public void describeTo(Description description) {
                description.appendText("with initial directory: ");
                directoryNameMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(PathBar view) {
                return directoryNameMatcher.matches(view.getInitialDirectory().getAbsolutePath());
            }
        };
    }

    @Before
    public void setup() {
        sdcardPath = Environment.getExternalStorageDirectory().getAbsolutePath() + '/';
    }

    @Test
    public void testIntentPickFileRememberPickFilePath() throws IOException {
        testRememberPickFilePathWithAction(FileManagerIntents.ACTION_PICK_FILE);
    }

    @Test
    public void testIntentPickDirectoryRememberPickFilePath() throws IOException {
        testRememberPickFilePathWithAction(FileManagerIntents.ACTION_PICK_DIRECTORY);
    }

    @Test
    public void testIntentGetContentRememberPickFilePath() throws IOException {
        testRememberPickFilePathWithAction(Intent.ACTION_GET_CONTENT);
    }

    private void testRememberPickFilePathWithAction(String action) throws IOException {
        createDirectory(sdcardPath + TEST_DIRECTORY);
        if (isPickDirectory(action)) { //Pick directory
            createDirectory(sdcardPath + TEST_DIRECTORY + "/oi-dir-to-pick");
        } else {
            createFile(sdcardPath + TEST_DIRECTORY + "/oi-file-to-pick.txt", "bbb");
        }
        //Directory because PICK_DIRECTORY doesn't show files
        createDirectory(sdcardPath + OI_TO_PICK_TEST_FOLDER_DELETED);


        // Pick a file first
        Uri uri = Uri.parse("file://" + sdcardPath); //If there was already a remembered pick file path
        Intent intent = new Intent(action, uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        startActivity(intent);
        clickOnTestDirectory();

        if (isPickDirectory(action)) { //Pick directory
            checkFile("oi-dir-to-pick", ViewAssertions.matches(ViewMatchers.isDisplayed()));
            clickOnFile("oi-dir-to-pick");
        } else {
            checkFile("oi-file-to-pick.txt", ViewAssertions.matches(ViewMatchers.isDisplayed()));
            clickOnFile("oi-file-to-pick.txt");
        }

        if (isPickDirectory(action)) {
            Espresso.onView(ViewMatchers.withId(R.id.button)).perform(ViewActions.click());
        } else {
            Espresso.onView(ViewMatchers.withText(R.string.pick_button_default)).perform(ViewActions.click());
        }

        // Check, if we are in the oi-filemanager-tests directory on restart
        Intent intentWithoutUrl = new Intent(action);
        intentWithoutUrl.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        IntentFilterActivity activity = startActivity(intentWithoutUrl);
        Espresso.onView(ViewMatchers.withId(R.id.pathbar)).check(ViewAssertions.matches(hasInitialDirectory(Matchers.endsWith(TEST_DIRECTORY))));

        //Delete the oi-filemanager-tests directory, so that the default path is used
        deleteDirectory(sdcardPath + TEST_DIRECTORY);

        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        activity.finish();

        //Check, if the current directory is the default (sdcardPath) on restart
        startActivity(intentWithoutUrl);

        checkFile(OI_TO_PICK_TEST_FOLDER_DELETED, ViewAssertions.matches(ViewMatchers.isDisplayed()));

        //Clean up
        new File(sdcardPath + OI_TO_PICK_TEST_FOLDER_DELETED).delete();
    }

    private IntentFilterActivity startActivity(Intent intent) {
        IntentFilterActivity activity = (IntentFilterActivity) InstrumentationRegistry.getInstrumentation().startActivitySync(intent);
        Espresso.registerIdlingResources(new DirectoryScannerIdlingResource(activity));
        return activity;
    }

    private boolean isPickDirectory(String action) {
        return action.equals(FileManagerIntents.ACTION_PICK_DIRECTORY);
    }
}
