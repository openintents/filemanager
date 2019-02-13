package org.openintents.filemanager.test;

import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.SystemClock;
import androidx.test.InstrumentationRegistry;
import androidx.test.espresso.InjectEventSecurityException;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.rule.ActivityTestRule;
import android.view.KeyEvent;
import android.view.View;

import org.hamcrest.Matcher;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.openintents.filemanager.R;
import org.openintents.filemanager.SaveAsActivity;

import java.io.File;
import java.io.IOException;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.actionWithAssertions;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withHint;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class TestSaveAsActivity extends BaseTestFileManager {

    @Rule
    public SaveAsActivityTestRule rule = new SaveAsActivityTestRule();

    @BeforeClass
    public static void setup() throws IOException {
        sdcardPath = Environment.getExternalStorageDirectory().getAbsolutePath() + '/';
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

    private static class SaveAsActivityTestRule extends ActivityTestRule<SaveAsActivity> {
        public SaveAsActivityTestRule() {
            super(SaveAsActivity.class);
        }

        @Override
        protected Intent getActivityIntent() {
            Uri uri = Uri.parse("file://" + sdcardPath + "oi-filemanager-tests/oi-to-open.txt");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.setComponent(new ComponentName(InstrumentationRegistry.getTargetContext(), SaveAsActivity.class));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            return intent;
        }
    }
}
