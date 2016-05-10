package org.openintents.filemanager.test;

import android.content.Intent;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openintents.filemanager.R;
import org.openintents.intents.FileManagerIntents;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class TestPickFileResult {

    @Rule
    public ActivityTestRule<ActivityResultTestActivity> rule
            = new ActivityTestRule<>(ActivityResultTestActivity.class);

    @Test
    public void testPickResult() {
        rule.getActivity().startActivity(new Intent(FileManagerIntents.ACTION_PICK_FILE));
        onView(withText("oi-test.txt")).perform(click());
        onView(withText(R.string.pick_button_default)).perform(click());

        onView(withText("oi-test.txt")).check(matches(isDisplayed()));
    }
}
