package org.openintents.filemanager.test;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.SystemClock;
import android.support.test.espresso.InjectEventSecurityException;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.action.EspressoKey;
import android.support.test.espresso.action.ViewActions;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.View;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openintents.filemanager.*;
import org.openintents.filemanager.R;

import java.io.File;
import java.io.IOException;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.actionWithAssertions;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.longClick;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withHint;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
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

    @BeforeClass
    public static void setup() throws IOException {
        sdcardPath = Environment.getExternalStorageDirectory().getAbsolutePath()+'/';
        createDirectory(sdcardPath + TEST_DIRECTORY);
        createFile(sdcardPath + "oi-filemanager-tests/oi-to-open.txt", "bbb");
    }

    @Test
    public void testIntentSaveAs() {
        onView(withHint(R.string.filename_hint)).perform(closeSoftKeyboard(), replaceText("oi-target.txt"));
        onView(withHint(R.string.filename_hint)).perform(actionWithAssertions(new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isDisplayed();
            }

            @Override
            public String getDescription() {
                return null;
            }

            @Override
            public void perform(UiController uiController, View view) {
                try {
                    sendKeyEvent(uiController);
                } catch (InjectEventSecurityException e) {
                    e.printStackTrace();
                }
            }

            private boolean sendKeyEvent(UiController controller)
                    throws InjectEventSecurityException {

                boolean injected = false;
                long eventTime = SystemClock.uptimeMillis();
                for (int attempts = 0; !injected && attempts < 4; attempts++) {
                    injected = controller.injectKeyEvent(new KeyEvent(eventTime,
                            eventTime,
                            KeyEvent.ACTION_DOWN,
                            KeyEvent.KEYCODE_ENTER,
                            0,
                            0));
                }
                return injected;
            }
        }));
        //onView(withId(R.id.pickbar_button)).perform(click());

        assertThat(new File(sdcardPath + "oi-filemanager-tests/oi-target.txt").exists(), is(true));
    }
}
