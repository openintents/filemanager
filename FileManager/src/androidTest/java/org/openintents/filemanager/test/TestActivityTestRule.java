/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidTest.java.org.openintents.filemanager.test;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import androidx.annotation.Nullable;
import androidx.test.InstrumentationRegistry;
import androidx.test.annotation.Beta;
import androidx.test.rule.UiThreadTestRule;
import android.util.Log;

import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import static androidx.test.internal.util.Checks.checkNotNull;

/**
 * This rule provides functional testing of a single activity. The activity under test will be
 * launched before each test annotated with
 * <a href="http://junit.org/javadoc/latest/org/junit/Test.html"><code>Test</code></a> and before
 * methods annotated with
 * <a href="http://junit.sourceforge.net/javadoc/org/junit/Before.html"><code>Before</code></a>. It
 * will be terminated after the test is completed and methods annotated with
 * <a href="http://junit.sourceforge.net/javadoc/org/junit/After.html"><code>After</code></a> are
 * finished. During the duration of the test you will be able to manipulate your Activity directly.
 *
 * @param <T> The activity to test
 */
@Beta
public class TestActivityTestRule<T extends Activity> extends UiThreadTestRule {

    private static final String TAG = "TAInstrRule";

    private final Class<T> mActivityClass;

    private Instrumentation mInstrumentation;

    private boolean mInitialTouchMode = false;

    private boolean mLaunchActivity = false;

    private T mActivity;

    /**
     * Similar to {@link #TestActivityTestRule(Class, boolean, boolean)} but with "touch mode" disabled.
     *
     * @param activityClass The activity under test. This must be a class in the instrumentation
     *                      targetPackage specified in the AndroidManifest.xml
     * @see TestActivityTestRule#TestActivityTestRule(Class, boolean, boolean)
     */
    public TestActivityTestRule(Class<T> activityClass) {
        this(activityClass, false);
    }

    /**
     * Similar to {@link #TestActivityTestRule(Class, boolean, boolean)} but defaults to launch the
     * activity under test once per
     * <a href="http://junit.org/javadoc/latest/org/junit/Test.html"><code>Test</code></a> method.
     * It is launched before the first
     * <a href="http://junit.sourceforge.net/javadoc/org/junit/Before.html"><code>Before</code></a>
     * method, and terminated after the last
     * <a href="http://junit.sourceforge.net/javadoc/org/junit/After.html"><code>After</code></a>
     * method.
     *
     * @param activityClass    The activity under test. This must be a class in the instrumentation
     *                         targetPackage specified in the AndroidManifest.xml
     * @param initialTouchMode true if the Activity should be placed into "touch mode" when started
     * @see TestActivityTestRule#TestActivityTestRule(Class, boolean, boolean)
     */
    public TestActivityTestRule(Class<T> activityClass, boolean initialTouchMode) {
        this(activityClass, initialTouchMode, true);
    }

    /**
     * Creates an {@link TestActivityTestRule} for the Activity under test.
     *
     * @param activityClass    The activity under test. This must be a class in the instrumentation
     *                         targetPackage specified in the AndroidManifest.xml
     * @param initialTouchMode true if the Activity should be placed into "touch mode" when started
     * @param launchActivity   true if the Activity should be launched once per
     *                         <a href="http://junit.org/javadoc/latest/org/junit/Test.html">
     *                         <code>Test</code></a> method. It will be launched before the first
     *                         <a href="http://junit.sourceforge.net/javadoc/org/junit/Before.html">
     *                         <code>Before</code></a> method, and terminated after the last
     *                         <a href="http://junit.sourceforge.net/javadoc/org/junit/After.html">
     *                         <code>After</code></a> method.
     */
    public TestActivityTestRule(Class<T> activityClass, boolean initialTouchMode,
                                boolean launchActivity) {
        mActivityClass = activityClass;
        mInitialTouchMode = initialTouchMode;
        mLaunchActivity = launchActivity;
        mInstrumentation = InstrumentationRegistry.getInstrumentation();
    }

    /**
     * Override this method to set up Intent as if supplied to
     * {@link android.content.Context#startActivity}.
     * <p>
     * The default Intent (if this method returns null or is not overwritten) is:
     * action = {@link Intent#ACTION_MAIN}
     * flags = {@link Intent#FLAG_ACTIVITY_NEW_TASK}
     * All other intent fields are null or empty.
     *
     * @return The Intent as if supplied to {@link android.content.Context#startActivity}.
     */
    protected Intent getActivityIntent() {
        return new Intent(Intent.ACTION_MAIN);
    }

    /**
     * Override this method to execute any code that should run before your {@link Activity} is
     * created and launched.
     * This method is called before each test method, including any method annotated with
     * <a href="http://junit.sourceforge.net/javadoc/org/junit/Before.html"><code>Before</code></a>.
     */
    protected void beforeActivityLaunched() {
        // empty by default
    }

    /**
     * Override this method to execute any code that should run after your {@link Activity} is
     * launched, but before any test code is run including any method annotated with
     * <a href="http://junit.sourceforge.net/javadoc/org/junit/Before.html"><code>Before</code></a>.
     * <p>
     * Prefer
     * <a href="http://junit.sourceforge.net/javadoc/org/junit/Before.html"><code>Before</code></a>
     * over this method. This method should usually not be overwritten directly in tests and only be
     * used by subclasses of TestActivityTestRule to get notified when the activity is created and
     * visible but test runs.
     */
    protected void afterActivityLaunched() {
        // empty by default
    }

    /**
     * Override this method to execute any code that should run after your {@link Activity} is
     * finished.
     * This method is called after each test method, including any method annotated with
     * <a href="http://junit.sourceforge.net/javadoc/org/junit/After.html"><code>After</code></a>.
     */
    protected void afterActivityFinished() {
        // empty by default
    }

    /**
     * @return The activity under test.
     */
    public T getActivity() {
        if (mActivity == null) {
            Log.w(TAG, "Activity wasn't created yet");
        }
        return mActivity;
    }

    @Override
    public Statement apply(final Statement base, Description description) {
        return new ActivityStatement(super.apply(base, description));
    }

    /**
     * Launches the Activity under test.
     * <p>
     * Don't call this method directly, unless you explicitly requested not to launch the Activity
     * manually using the launchActivity flag in
     * {@link TestActivityTestRule#TestActivityTestRule(Class, boolean, boolean)}.
     * <p>
     * Usage:
     * <pre>
     *    &#064;Test
     *    public void customIntentToStartActivity() {
     *        Intent intent = new Intent(Intent.ACTION_PICK);
     *        mActivity = mActivityRule.launchActivity(intent);
     *    }
     * </pre>
     *
     * @param startIntent The Intent that will be used to start the Activity under test. If
     *                    {@code startIntent} is null, the Intent returned by
     *                    {@link TestActivityTestRule#getActivityIntent()} is used.
     * @return the Activity launched by this rule.
     * @see TestActivityTestRule#getActivityIntent()
     */
    public T launchActivity(@Nullable Intent startIntent) {
        // set initial touch mode
        mInstrumentation.setInTouchMode(mInitialTouchMode);

        final String targetPackage = mInstrumentation.getContext().getPackageName();
        // inject custom intent, if provided
        if (null == startIntent) {
            startIntent = getActivityIntent();
            if (null == startIntent) {
                Log.w(TAG, "getActivityIntent() returned null using default: " +
                        "Intent(Intent.ACTION_MAIN)");
                startIntent = new Intent(Intent.ACTION_MAIN);
            }
        }
        startIntent.setClassName(targetPackage, mActivityClass.getName());
        startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Log.d(TAG, String.format("Launching activity %s",
                mActivityClass.getName()));

        beforeActivityLaunched();
        // The following cast is correct because the activity we're creating is of the same type as
        // the one passed in
        mInstrumentation.getContext().startActivity(startIntent);

        mInstrumentation.waitForIdleSync();

        afterActivityLaunched();
        return mActivity;
    }

    // Visible for testing
    void setInstrumentation(Instrumentation instrumentation) {
        mInstrumentation = Checks.checkNotNull(instrumentation, "instrumentation cannot be null!");
    }

    void finishActivity() {
        if (mActivity != null) {
            mActivity.finish();
            mActivity = null;
        }
    }

    /**
     * <a href="http://junit.org/apidocs/org/junit/runners/model/Statement.html">
     * <code>Statement</code></a> that finishes the activity after the test was executed
     */
    private class ActivityStatement extends Statement {

        private final Statement mBase;

        public ActivityStatement(Statement base) {
            mBase = base;
        }

        @Override
        public void evaluate() throws Throwable {
            try {
                if (mLaunchActivity) {
                    mActivity = launchActivity(getActivityIntent());
                }
                mBase.evaluate();
            } finally {
                finishActivity();
                afterActivityFinished();
            }
        }
    }
}
