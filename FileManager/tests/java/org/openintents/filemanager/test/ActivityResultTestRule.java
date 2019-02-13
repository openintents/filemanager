package org.openintents.filemanager.test;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.test.rule.ActivityTestRule;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.lang.reflect.Field;

import static android.app.Instrumentation.ActivityResult;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ActivityResultTestRule<T extends Activity> extends ActivityTestRule<T> {

    public ActivityResultTestRule(Class<T> activityClass) {
        this(activityClass, false);
    }

    public ActivityResultTestRule(Class<T> activityClass, boolean initialTouchMode) {
        this(activityClass, initialTouchMode, true);
    }

    public ActivityResultTestRule(Class<T> activityClass, boolean initialTouchMode, boolean launchActivity) {
        super(activityClass, initialTouchMode, launchActivity);
    }

    public static Matcher<? super ActivityResult> hasResultData(final Matcher<Intent> intentMatcher) {
        return new TypeSafeMatcher<ActivityResult>(ActivityResult.class) {

            @Override
            public void describeTo(Description description) {
                description.appendDescriptionOf(intentMatcher);
            }

            @Override
            protected boolean matchesSafely(ActivityResult item) {
                return intentMatcher.matches(item.getResultData());
            }

            @Override
            protected void describeMismatchSafely(ActivityResult item, Description mismatchDescription) {
                intentMatcher.describeMismatch(item.getResultData(), mismatchDescription);
            }
        };
    }

    @NonNull
    public static Matcher<? super ActivityResult> hasResultCode(final int resultCode) {
        return new TypeSafeMatcher<ActivityResult>(ActivityResult.class) {
            @Override
            public void describeTo(Description description) {
                description.appendText("has result code " + resultCode);
            }

            @Override
            protected boolean matchesSafely(ActivityResult activityResult) {
                return activityResult.getResultCode() == resultCode;
            }
        };
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public ActivityResult getActivityResult() {
        T activity = getActivity();
        assertThat("Activity did not finish (destroyed: " + activity.isDestroyed() + ")", activity.isFinishing(), is(true));


        try {
            Field resultCodeField = Activity.class.getDeclaredField("mResultCode");
            resultCodeField.setAccessible(true);

            Field resultDataField = Activity.class.getDeclaredField("mResultData");
            resultDataField.setAccessible(true);

            return new ActivityResult((int) resultCodeField.get(activity),
                    (Intent) resultDataField.get(activity));

        } catch (NoSuchFieldException e) {
            throw new RuntimeException("Looks like the Android Activity class has changed it's" +
                    "private fields for mResultCode or mResultData. Time to update the reflection code.", e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}