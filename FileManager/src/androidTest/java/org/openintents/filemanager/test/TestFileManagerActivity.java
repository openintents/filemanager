/*
 * This is an example test project created in Eclipse to test NotePad which is a sample 
 * project located in AndroidSDK/samples/android-11/NotePad
 * Just click on File --> New --> Project --> Android Project --> Create Project from existing source and
 * select NotePad.
 * 
 * Then you can run these test cases either on the emulator or on device. You right click
 * the test project and select Run As --> Run As Android JUnit Test
 * 
 * @author Renas Reda, renas.reda@jayway.com
 * 
 */

package androidTest.java.org.openintents.filemanager.test;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import androidx.test.InstrumentationRegistry;
import androidx.test.espresso.Espresso;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;
import android.text.format.Formatter;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ListView;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openintents.filemanager.FileHolderListAdapter;
import org.openintents.filemanager.FileManagerActivity;
import org.openintents.filemanager.PreferenceActivity;
import org.openintents.filemanager.R;
import org.openintents.filemanager.files.FileHolder;
import org.openintents.util.VersionUtils;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static androidx.test.espresso.Espresso.openContextualActionModeOverflowMenu;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.longClick;
import static androidx.test.espresso.action.ViewActions.pressKey;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.assertThat;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

@RunWith(AndroidJUnit4.class)
public class TestFileManagerActivity extends BaseTestFileManager {

    private static String filenameIsInRightDirectory;
    @Rule
    public ActivityTestRule<FileManagerActivity> rule = new ActivityTestRule<>(FileManagerActivity.class);
    private Random random = new Random();

    @BeforeClass
    public static void setUp() throws Exception {

        Context context = InstrumentationRegistry.getTargetContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit()
                .putBoolean("eula_accepted", true)
                .putInt("org.openintents.distribution.version_number_check", VersionUtils.getVersionCode(context))
                .commit();
        sdcardPath = Environment.getExternalStorageDirectory().getAbsolutePath() + '/';

        // need to do this before creating activity
        cleanDirectory(new File(sdcardPath + TEST_DIRECTORY));
        createDirectory(sdcardPath + TEST_DIRECTORY);

        createFile(sdcardPath + TEST_DIRECTORY + "/oi-rem-test.txt", "");
        filenameIsInRightDirectory = "oi-test-is-in-right-directory";
        createFile(sdcardPath + TEST_DIRECTORY + "/" + filenameIsInRightDirectory, "");

        createDirectory(sdcardPath + "oi-filemanager-tests/oi-move-target");
        createFile(sdcardPath + "oi-filemanager-tests/oi-file-1.txt", "");
        createFile(sdcardPath + "oi-filemanager-tests/oi-file-2.txt", "");
        createFile(sdcardPath + "oi-filemanager-tests/oi-file-3.txt", "");
        createFile(sdcardPath + "oi-filemanager-tests/oi-file-4.txt", "");
        createFile(sdcardPath + "oi-filemanager-tests/oi-file-5.txt", "");
        createFile(sdcardPath + "oi-filemanager-tests/.oi-hidden.txt", "");
    }

    private static Matcher<View> isSortedInThisOrder(final String a, final String b, final String c) {
        return new TypeSafeMatcher<View>() {

            @Override
            protected boolean matchesSafely(View item) {
                ListView fileList = (ListView) item;
                FileHolderListAdapter adapter = (FileHolderListAdapter) fileList.getAdapter();
                int positionOfA = find(adapter, a);
                int positionOfB = find(adapter, b);
                int positionOfC = find(adapter, c);
                return positionOfA < positionOfB && positionOfB < positionOfC;
            }

            private int find(FileHolderListAdapter adapter, String fileName) {
                int size = adapter.getCount();
                for (int i = 0; i < size; i++) {
                    if (((FileHolder) adapter.getItem(i)).getName().equals(fileName)) {
                        return i;
                    }
                }
                return -1;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("has items sorted in this order: " + a + " " + b + " " + c);
            }
        };
    }

    @Before
    public void setUpTest() {
        Espresso.registerIdlingResources(new DirectoryScannerIdlingResource(rule.getActivity()));
    }

    @After
    public void tearDown() throws Exception {
        //cleanDirectory(new File(sdcardPath + TEST_DIRECTORY));
    }

    @Test
    public void testNavigation() throws IOException {
//		if(solo.searchText("Accept")) {
//			solo.clickOnButton("Accept");
//			if(solo.searchButton("Continue"))
//				solo.clickOnButton("Continue");
//		}
        createDirectory(sdcardPath + TEST_DIRECTORY);
        createFile(sdcardPath + "oi-filemanager-tests/oi-test.txt", "");
        createDirectory(sdcardPath + "oi-filemanager-tests/oi-test-dir");
        createFile(sdcardPath + "oi-filemanager-tests/oi-test-dir/oi-fff.txt", "");

        clickOnTestDirectory();
        Espresso.onView(ViewMatchers.withText(TEST_DIRECTORY)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        clickOnFile("oi-test-dir");
        Espresso.onView(ViewMatchers.withText("oi-fff.txt")).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        Espresso.pressBack();
        Espresso.pressBack();

        clickOnTestDirectory();
        Espresso.onData(hasName("oi-test.txt")).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        clickOnFile("oi-test-dir");
        Espresso.pressBack();
        Espresso.onData(hasName("oi-test.txt")).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        Espresso.pressBack();
    }

    @Test
    public void testModification() throws IOException {
        clickOnTestDirectory();
        longClickOnFile("oi-rem-test.txt");

        Espresso.onView(ViewMatchers.withContentDescription(R.string.menu_delete)).perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withText(android.R.string.ok)).perform(ViewActions.click());

        Espresso.openActionBarOverflowOrOptionsMenu(rule.getActivity());

        Espresso.onView(ViewMatchers.withText(R.string.menu_create_folder)).perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withId(R.id.foldername)).perform(ViewActions.replaceText("oi-created-folder"));
        Espresso.onView(ViewMatchers.withText(android.R.string.ok)).perform(ViewActions.click());

        checkFile("oi-created-folder", ViewAssertions.matches(ViewMatchers.isDisplayed()));
        Espresso.pressBack();

        File createdFolder = new File(sdcardPath + "oi-filemanager-tests/oi-created-folder");
        ViewMatchers.assertThat(createdFolder.exists(), Matchers.is(true));
        ViewMatchers.assertThat(createdFolder.isDirectory(), Matchers.is(true));
        ViewMatchers.assertThat(new File(sdcardPath + "oi-filemanager-tests/oi-rem-test.txt").exists(), Matchers.is(false));
    }

    @Test
    public void testBookmarks() throws IOException {
        String fn = "oi-bookmark-" + random.nextInt(1000);
        createDirectory(sdcardPath + TEST_DIRECTORY);
        createDirectory(sdcardPath + "oi-filemanager-tests/" + fn);
        createFile(sdcardPath + "oi-filemanager-tests/" + fn + "/oi-inside-book.txt", "");

        // create bookmark
        clickOnTestDirectory();
        longClickOnFile(fn);

        Espresso.openContextualActionModeOverflowMenu();
        Espresso.onView(ViewMatchers.withText(R.string.menu_bookmark)).perform(ViewActions.click());
        clickOnFile(fn);

        checkFile("oi-inside-book.txt", ViewAssertions.matches(ViewMatchers.isDisplayed()));


        // remove it
        Espresso.openActionBarOverflowOrOptionsMenu(rule.getActivity());
        Espresso.onView(ViewMatchers.withText(R.string.menu_bookmarks)).perform(ViewActions.click());
        longClickOnBookmark(fn);
        Espresso.onView(ViewMatchers.withContentDescription(R.string.menu_delete)).perform(ViewActions.click());

        Espresso.pressBack();

        // make sure that it is deleted
        Espresso.openActionBarOverflowOrOptionsMenu(rule.getActivity());
        Espresso.onView(ViewMatchers.withText(R.string.menu_bookmarks)).perform(ViewActions.click());

        checkIsNotContainedInList(hasBookmarkName(fn));
        Espresso.pressBack();
        Espresso.pressBack();
    }

    private void checkIsNotContainedInList(Matcher<Object> matches) {
        Espresso.onView(ViewMatchers.withId(android.R.id.list))
                .check(ViewAssertions.matches(Matchers.not(withAdaptedData(matches))));
    }

    @Test
    public void testActions() throws IOException {

        clickOnTestDirectory();
        // copy
        longClickOnFile("oi-file-1.txt");
        Espresso.openContextualActionModeOverflowMenu();
        Espresso.onView(ViewMatchers.withText(R.string.menu_copy)).perform(ViewActions.click());

        navigateToTargetAndPasteAndCheck("oi-move-target", "oi-file-1.txt", null);
        checkFile("oi-file-1.txt", ViewAssertions.matches(ViewMatchers.isDisplayed()));

        // move
        longClickOnFile("oi-file-2.txt");
        Espresso.openContextualActionModeOverflowMenu();
        Espresso.onView(ViewMatchers.withText(R.string.menu_move)).perform(ViewActions.click());
        navigateToTargetAndPasteAndCheck("oi-move-target", "oi-file-2.txt", null);
        checkIsNotContainedInList(hasName("oi-file-2.txt"));

        // multi select
        if (android.os.Build.VERSION.SDK_INT < 11) {
            Espresso.onView(ViewMatchers.withText(R.id.menu_multiselect)).perform(ViewActions.click());
            clickOnFile("oi-file-3.txt");
            clickOnFile("oi-file-4.txt");
            Espresso.onView(ViewMatchers.withId(R.id.menu_copy)); // TODO verify solo.clickOnImageButton(1);
            Espresso.pressBack();

            navigateToTargetAndPasteAndCheck("oi-move-target", "oi-file-3.txt", "oi-file-4.txt");
        }

        // rename
        longClickOnFile("oi-file-5.txt");
        Espresso.openContextualActionModeOverflowMenu();
        Espresso.onView(ViewMatchers.withText(R.string.menu_rename)).perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withId(R.id.foldername)).perform(ViewActions.replaceText("oi-renamed-file.txt"));
        Espresso.onView(ViewMatchers.withText(android.R.string.ok)).perform(ViewActions.click());
        checkFile("oi-renamed-file.txt", ViewAssertions.matches(ViewMatchers.isDisplayed()));

        Espresso.pressBack();
    }

    private void navigateToTargetAndPasteAndCheck(String dirname, String name1, String name2) throws IOException {
        createDirectory(sdcardPath + "oi-filemanager-tests/");
        clickOnFile(dirname);

        Espresso.openActionBarOverflowOrOptionsMenu(rule.getActivity());
        Espresso.onView(ViewMatchers.withText(R.string.menu_paste)).perform(ViewActions.click());

        checkFile(name1, ViewAssertions.matches(ViewMatchers.isDisplayed()));

        if (name2 != null) {
            checkFile(name2, ViewAssertions.matches(ViewMatchers.isDisplayed()));
        }

        Espresso.pressBack();
    }

    @Test
    public void testDetails() throws IOException {
        createDirectory(sdcardPath + TEST_DIRECTORY);
        createFile(sdcardPath + "oi-filemanager-tests/oi-detail.txt", "abcdefg");

        clickOnTestDirectory();


        longClickOnFile("oi-detail.txt");
        Espresso.openContextualActionModeOverflowMenu();
        Espresso.onView(ViewMatchers.withText(R.string.menu_details)).perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withText(R.string.details_type_file)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        Espresso.onView(ViewMatchers.withId(R.id.details_size_value)).check(ViewAssertions.matches(ViewMatchers.withText(Formatter.formatFileSize(rule.getActivity(), 7))));


        // not sure:
        //Calendar today = new GregorianCalendar();
        //String todayString = today.get(Calendar.DAY_OF_MONTH) + "/" + today.get(Calendar.MONTH) + "/" + today.get(Calendar.YEAR);
        //assertTrue(solo.searchText(todayString));

        Espresso.pressBack();
        Espresso.pressBack();
    }

    @Test
    public void testHiddenFiles() throws IOException {
        clickOnTestDirectory();

        PreferenceActivity.setDisplayHiddenFiles(rule.getActivity(), true);
        checkFile(".oi-hidden.txt", ViewAssertions.matches(ViewMatchers.isDisplayed()));

        Espresso.openActionBarOverflowOrOptionsMenu(rule.getActivity());
        Espresso.onView(ViewMatchers.withText(R.string.settings)).perform(ViewActions.click());

        Espresso.onView(Matchers.allOf(ViewMatchers.withText(R.string.preference_displayhiddenfiles_title), withResourceName("android:id/title"))).perform(ViewActions.click());

        Espresso.pressBack();
        checkIsNotContainedInList(hasName(".oi-hidden.txt"));

        Espresso.pressBack();
    }

    @Test
    public void testOrder() throws IOException, InterruptedException {
        createDirectory(sdcardPath + TEST_DIRECTORY);
        createFile(sdcardPath + "oi-filemanager-tests/oi-b.txt", "bbb");
        Thread.sleep(10); // make sure that next file is younger
        createFile(sdcardPath + "oi-filemanager-tests/oi-a.txt", "aaaaaa");
        Thread.sleep(10);
        createFile(sdcardPath + "oi-filemanager-tests/oi-c.txt", "");
        clickOnTestDirectory();

        String[] sortOrders = rule.getActivity().getResources().getStringArray(org.openintents.filemanager.R.array.preference_sortby_names);

        setAscending(true);
        setSortOrder(sortOrders[0]);
        isSortedInThisOrder("oi-a.txt", "oi-b.txt", "oi-c.txt");

        setSortOrder(sortOrders[1]);
        isSortedInThisOrder("oi-c.txt", "oi-b.txt", "oi-a.txt");

        setSortOrder(sortOrders[2]);
        isSortedInThisOrder("oi-b.txt", "oi-a.txt", "oi-c.txt");

        setAscending(false);
        setSortOrder(sortOrders[0]);
        isSortedInThisOrder("oi-c.txt", "oi-b.txt", "oi-a.txt");
    }

    private void setSortOrder(String name) {
        Espresso.openActionBarOverflowOrOptionsMenu(rule.getActivity());
        Espresso.onView(ViewMatchers.withText(R.string.settings)).perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withText(R.string.preference_sortby)).perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withText(name)).perform(ViewActions.click());
        Espresso.pressBack();
    }

    private void setAscending(boolean enabled) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(rule.getActivity());
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("ascending", enabled);
        editor.commit();
    }

    @Test
    public void testBrowseToOnPressEnter() throws IOException {

		/*
         *  We start at the SD card.
		 */
        Espresso.onView(ViewMatchers.withText(Environment.getExternalStorageDirectory().getParentFile().getName())).perform(ViewActions.longClick());
        Espresso.onView(ViewMatchers.withId(R.id.path_bar_path_edit_text)).perform(ViewActions.click()); // Let the editText have focus to be able to send the enter key.
        Espresso.onView(ViewMatchers.withId(R.id.path_bar_path_edit_text)).perform(ViewActions.replaceText(sdcardPath + TEST_DIRECTORY));
        Espresso.onView(ViewMatchers.withId(R.id.path_bar_path_edit_text)).perform(ViewActions.pressKey(KeyEvent.KEYCODE_ENTER));


        checkFile(filenameIsInRightDirectory, ViewAssertions.matches(ViewMatchers.isDisplayed()));

        Espresso.pressBack();
        Espresso.pressBack();
    }

// Current implementation directly opens the file and therefore can't be tested.
//	public void testIntentUri() throws IOException {
//		createDirectory(sdcardPath + "oi-filemanager-tests");
//		createFile(sdcardPath + "oi-filemanager-tests/oi-to-open.txt", "bbb");		
//		
//		Intent intent = new Intent(Intent.ACTION_VIEW);
//		intent.setData(Uri.parse("file://" + sdcardPath + "oi-filemanager-tests/oi-to-open.txt"));
//		intent.setClass(activity, org.openintents.filemanager.FileManagerActivity.class);
//		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//		
//		activity = getInstrumentation().startActivitySync(intent);
//		
//		assertTrue(solo.searchText("oi-to-open.txt"));
//		pressBack();
//		pressBack();	
//	}

//	Removed as Filter action is obsolete and removed.
//	public void testFilters() throws IOException {
//		createDirectory(sdcardPath + "oi-filemanager-tests");
//		createFile(sdcardPath + "oi-filemanager-tests/oi-not-filter.txt", "");
//		createFile(sdcardPath + "oi-filemanager-tests/oi-filtered.py", "");
//		createDirectory(sdcardPath + "oi-filemanager-tests/oi-f-dir");
//		solo.clickOnText("oi-filemanager-tests");
//		
//		solo.clickOnMenuItem(getAppString(org.openintents.filemanager.R.string.menu_filter));
//		solo.enterText(0, ".py");
//		solo.clickOnButton(getAppString(android.R.string.ok));
//		
//		assertTrue(solo.searchText("oi-filtered.py"));
//		assertTrue(solo.searchText("oi-f-dir"));
//		assertFalse(solo.searchText("oi-not-filter.txt"));
//		
//		pressBack();
//		pressBack();
//	}

    // Other possible tests:
    // 		testSend
    // 		testMore
    // 		testKeyboardFilter
}