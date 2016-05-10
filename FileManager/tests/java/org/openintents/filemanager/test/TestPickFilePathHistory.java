package org.openintents.filemanager.test;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.UiThreadTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class TestPickFilePathHistory extends BaseTestFileManager {

    @Rule
    public UiThreadTestRule rule = new UiThreadTestRule();

    @Before
    public void setup(){
        sdcardPath = Environment.getExternalStorageDirectory().getAbsolutePath()+'/';
    }

    @Test
    public void testIntentRememberPickFilePath() throws IOException {
        String[] actions = new String[]{
                org.openintents.intents.FileManagerIntents.ACTION_PICK_FILE,
                org.openintents.intents.FileManagerIntents.ACTION_PICK_DIRECTORY,
                Intent.ACTION_GET_CONTENT
        };

        for(int i=0;i<3;i++){
            createDirectory(sdcardPath + TEST_DIRECTORY);
            if(i==1){ //Pick directory
                createDirectory(sdcardPath + "oi-filemanager-tests/oi-dir-to-pick");
            }
            else{
                createFile(sdcardPath + "oi-filemanager-tests/oi-file-to-pick.txt", "bbb");
            }
            //Directory because PICK_DIRECTORY doesn't show files
            createDirectory(sdcardPath + "oi-to-pick-test-folder-deleted");


            // Pick a file first
            Uri uri = Uri.parse("file://" + sdcardPath); //If there was already a remembered pick file path
            Intent intent = new Intent(actions[i], uri);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            InstrumentationRegistry.getInstrumentation().startActivitySync(intent);
            onView(withText(TEST_DIRECTORY)).perform(click());

            if(i==1) //Pick directory
                onView(withText("oi-dir-to-pick")).perform(click());
            else
                onView(withText("oi-file-to-pick.txt")).perform(click());

            if(i == 2) // When ACTION_GET_CONTENT, the file is picked automatically, when clicked
                onView(withText(org.openintents.filemanager.R.string.directory_pick)).perform(click());

            // Check, if we are in the oi-filemanager-tests directory
            intent = new Intent(actions[i]);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            InstrumentationRegistry.getInstrumentation().startActivitySync(intent);

            pressBack();


            //Delete the oi-filemanager-tests directory
            deleteDirectory(sdcardPath + TEST_DIRECTORY);

            //Check, if the current directory is the default (sdcardPath)
            intent = new Intent(actions[i]);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            InstrumentationRegistry.getInstrumentation().startActivitySync(intent);

            onView(withText("oi-to-pick-test-folder-deleted")).check(matches(isDisplayed()));

            //Clean up
            (new File(sdcardPath + "oi-to-pick-test-folder-deleted")).delete();

            pressBack();
            pressBack();
        }
    }
}
