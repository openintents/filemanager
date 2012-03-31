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

package org.openintents.filemanager.test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.test.InstrumentationTestCase;
import android.test.suitebuilder.annotation.Smoke;

import com.jayway.android.robotium.solo.Solo;

public class TestFileManagerActivity extends InstrumentationTestCase {
	
	private static final String TAG = "TestFileManagerActivity";
	
	private Solo solo;
	private Activity activity;
	private Random random = new Random();
	private Intent intent;
	private String sdcardPath = "/mnt/sdcard/";
	
	public TestFileManagerActivity() {
		super();
	}

	protected void setUp() throws Exception {
		super.setUp();
		
		// need to do this before creating activity
		cleanDirectory(new File(sdcardPath + "oi-filemanager-tests"));
		createDirectory(sdcardPath + "oi-filemanager-tests");
		
		intent = new Intent();
		intent.setAction("android.intent.action.MAIN");
		intent.setClassName("org.openintents.filemanager",
				"org.openintents.filemanager.FileManagerActivity");
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		activity = getInstrumentation().startActivitySync(intent);

		this.solo = new Solo(getInstrumentation(), activity);
	}

	protected void tearDown() throws Exception {
		try {
			this.solo.finishOpenedActivities();
		} catch (Throwable e) {
			e.printStackTrace();
		}
		super.tearDown();
		cleanDirectory(new File(sdcardPath + "oi-filemanager-tests"));
	}

	private String getAppString(int resId) {
		return activity.getString(resId);
	}

	@Smoke
	public void test000Eula() {
		String accept = getAppString(org.openintents.distribution.R.string.oi_distribution_eula_accept);
		String cancel = getAppString(org.openintents.distribution.R.string.oi_distribution_eula_refuse);
		boolean existsAccept = solo.searchButton(accept);
		boolean existsCancel = solo.searchButton(cancel);
		
		if (existsAccept && existsCancel) {
			solo.clickOnButton(accept);
		}
	}

	@Smoke
	public void test001RecentChanges() {
		String recentChanges = getAppString(org.openintents.distribution.R.string.oi_distribution_newversion_recent_changes);
		String cont = getAppString(org.openintents.distribution.R.string.oi_distribution_newversion_continue);
		while(solo.scrollUp());
		boolean existsRecentChanges = solo.searchText(recentChanges);
		boolean existsCont = solo.searchButton(cont);
		
		if (existsRecentChanges && existsCont) {
			solo.clickOnButton(cont);
		}
	}

	private void cleanDirectory(File file) {
		if(!file.exists()) return;
		for(String name:file.list()) {
			if(!name.startsWith("oi-") && !name.startsWith(".oi-")) {
				throw new RuntimeException(file + " contains unexpected file");
			}
			File child = new File(file, name);
			if(child.isDirectory())
				cleanDirectory(child);
			else
				child.delete();
		}
		file.delete();
		if(file.exists()) {
			throw new RuntimeException("Deletion of " + file + " failed");
		}
	}

	private void createFile(String path, String content) throws IOException {
		File file = new File(path);
		FileWriter wr = new FileWriter(file);
		wr.write(content);
		wr.close();
	}
	
	private void createDirectory(String path) throws IOException {
		File file = new File(path);
		file.mkdir();
		if(!file.exists())
			throw new IOException("Creation of " + path + " failed");
	}
	
	private void deleteDirectory(String path) {
		File file = new File(path);
		if(file.exists())
			if(file.isDirectory())
				cleanDirectory(file);
			file.delete();
	}

	public void testNavigation() throws IOException {
//		if(solo.searchText("Accept")) {
//			solo.clickOnButton("Accept");
//			if(solo.searchButton("Continue"))
//				solo.clickOnButton("Continue");
//		}
		createDirectory(sdcardPath + "oi-filemanager-tests");
		createFile(sdcardPath + "oi-filemanager-tests/oi-test.txt", "");
		createDirectory(sdcardPath + "oi-filemanager-tests/oi-test-dir");
		createFile(sdcardPath + "oi-filemanager-tests/oi-test-dir/oi-fff.txt", "");
		
		solo.clickOnText("oi-filemanager-tests");
		assertTrue(solo.searchText("oi-test.txt"));
		solo.clickOnText("oi-test-dir");
		assertTrue(solo.searchText("oi-fff.txt"));

		solo.goBack();
		solo.goBack();
		solo.clickOnText("oi-filemanager-tests");
		assertTrue(solo.searchText("oi-test.txt"));
		
		solo.clickOnText("oi-test-dir");
		solo.goBack();
		assertTrue(solo.searchText("oi-test.txt"));
		
		solo.goBack();
	}
	
	public void testModification() throws IOException {
		createDirectory(sdcardPath + "oi-filemanager-tests");
		createFile(sdcardPath + "oi-filemanager-tests/oi-rem-test.txt", "");
		solo.clickOnText("oi-filemanager-tests");
		solo.clickLongOnText("oi-rem-test.txt");
		solo.clickOnText(getAppString(org.openintents.filemanager.R.string.menu_delete)); // Delete
		solo.clickOnText(getAppString(android.R.string.ok));
		
		//when actionbar is present, this test case should find the first ImageButton
		if(android.os.Build.VERSION.SDK_INT < 11)
			solo.clickOnMenuItem(getAppString(org.openintents.filemanager.R.string.menu_new_folder)); // New Folder
		else
			solo.clickOnImageButton(0);
		solo.enterText(0, "oi-created-folder");
		solo.clickOnText(getAppString(android.R.string.ok));
		
		solo.goBack();
		assertTrue(solo.searchText("oi-created-folder"));
		solo.goBack();
		
		File createdFolder = new File(sdcardPath + "oi-filemanager-tests/oi-created-folder");
		assertTrue(createdFolder.exists());
		assertTrue(createdFolder.isDirectory());
		assertFalse(new File(sdcardPath + "oi-filemanager-tests/oi-rem-test.txt").exists());
	}
	
	public void testBookmarks() throws IOException {
		String fn = "oi-bookmark-" + random.nextInt(1000);
		createDirectory(sdcardPath + "oi-filemanager-tests");
		createDirectory(sdcardPath + "oi-filemanager-tests/" + fn);
		createFile(sdcardPath + "oi-filemanager-tests/" + fn + "/oi-inside-book.txt", "");
		
		// create bookmark
		
		solo.clickOnText("oi-filemanager-tests");
		solo.clickLongOnText(fn);
		solo.clickOnText(getAppString(org.openintents.filemanager.R.string.menu_bookmark)); // Add to bookmarks
		
		// navigate to it
		
		solo.clickOnMenuItem(getAppString(org.openintents.filemanager.R.string.bookmarks)); // Bookmarks
		solo.clickOnText(fn);
		assertTrue(solo.searchText("oi-inside-book.txt"));
		solo.goBack();
		solo.goBack();
		
		// remove it
		
		solo.clickOnMenuItem(getAppString(org.openintents.filemanager.R.string.settings));
		solo.clickOnText(getAppString(org.openintents.filemanager.R.string.bookmarks_manage));
		solo.clickOnText(fn);
		solo.clickOnText(getAppString(org.openintents.filemanager.R.string.bookmarks_delete));
		solo.goBack();
		
		// make sure that it is deleted
		
		solo.clickOnMenuItem(getAppString(org.openintents.filemanager.R.string.bookmarks));
		assertFalse(solo.searchText(fn));
		solo.goBack();
		solo.goBack();
	}
	
	public void testActions() throws IOException {
		createDirectory(sdcardPath + "oi-filemanager-tests");
		createDirectory(sdcardPath + "oi-filemanager-tests/oi-move-target");
		createFile(sdcardPath + "oi-filemanager-tests/oi-file-1.txt", "");
		createFile(sdcardPath + "oi-filemanager-tests/oi-file-2.txt", "");
		createFile(sdcardPath + "oi-filemanager-tests/oi-file-3.txt", "");
		createFile(sdcardPath + "oi-filemanager-tests/oi-file-4.txt", "");
		createFile(sdcardPath + "oi-filemanager-tests/oi-file-5.txt", "");
		solo.clickOnText("oi-filemanager-tests");
		
		// copy
		
		solo.clickLongOnText("oi-file-1.txt");
		solo.clickOnText(getAppString(org.openintents.filemanager.R.string.menu_copy));
		selectTargetAndCheck("oi-move-target", "oi-file-1.txt", null);
		assertTrue(solo.searchText("oi-file-1.txt"));
		
		// move
		
		solo.clickLongOnText("oi-file-2.txt");
		solo.clickOnText(getAppString(org.openintents.filemanager.R.string.menu_move));
		selectTargetAndCheck("oi-move-target", "oi-file-2.txt", null);
		assertFalse(solo.searchText("oi-file-2.txt"));
		
		// multi select
		
		solo.clickOnMenuItem(getAppString(org.openintents.filemanager.R.string.menu_multi_select));
		solo.clickOnText("oi-file-3.txt");
		solo.clickOnText("oi-file-4.txt");
		solo.clickOnText(getAppString(org.openintents.filemanager.R.string.move_button_multiselect));
		selectTargetAndCheck("oi-move-target", "oi-file-3.txt", "oi-file-4.txt");
		
		// rename
		
		solo.clickLongOnText("oi-file-5.txt");
		solo.clickOnText(getAppString(org.openintents.filemanager.R.string.menu_rename));
		solo.enterText(0, "oi-renamed-file.txt");
		solo.clickOnText(getAppString(android.R.string.ok)); // not sure what to do
		assertTrue(solo.searchText("oi-renamed-file.txt"));
		
		solo.goBack();
		solo.goBack();
	}
	
	private void selectTargetAndCheck(String dirname, String name1, String name2) throws IOException {
		createDirectory(sdcardPath + "oi-filemanager-tests");
		solo.clickOnText(dirname);
		solo.clickOnButton(getAppString(org.openintents.filemanager.R.string.copy_button) + "|" + 
							getAppString(org.openintents.filemanager.R.string.move_button));
		solo.clickOnText(dirname);
		assertTrue(solo.searchText(name1));
		if(name2 != null)
			assertTrue(solo.searchText(name2));
		solo.goBack();
	}
	
	public void testDetails() throws IOException {
		createDirectory(sdcardPath + "oi-filemanager-tests");
		createFile(sdcardPath + "oi-filemanager-tests/oi-detail.txt", "abcdefg");

		solo.clickOnText("oi-filemanager-tests");
		
		solo.clickLongOnText("oi-detail.txt");
		solo.clickOnText(getAppString(org.openintents.filemanager.R.string.menu_details));
		assertTrue(solo.searchText(getAppString(org.openintents.filemanager.R.string.details_type_file)));
		// depending on locale:
		assertTrue(solo.searchText("7.00B") || solo.searchText("7.00 B") || solo.searchText("7,00B") || solo.searchText("7,00 B"));
		
		// not sure:
		//Calendar today = new GregorianCalendar();
		//String todayString = today.get(Calendar.DAY_OF_MONTH) + "/" + today.get(Calendar.MONTH) + "/" + today.get(Calendar.YEAR);
		//assertTrue(solo.searchText(todayString));

		solo.goBack();
		solo.goBack();
		solo.goBack();
	}
	
	public void testFilters() throws IOException {
		createDirectory(sdcardPath + "oi-filemanager-tests");
		createFile(sdcardPath + "oi-filemanager-tests/oi-not-filter.txt", "");
		createFile(sdcardPath + "oi-filemanager-tests/oi-filtered.py", "");
		createDirectory(sdcardPath + "oi-filemanager-tests/oi-f-dir");
		solo.clickOnText("oi-filemanager-tests");
		
		solo.clickOnMenuItem(getAppString(org.openintents.filemanager.R.string.menu_filter));
		solo.enterText(0, ".py");
		solo.clickOnButton(getAppString(android.R.string.ok));
		
		assertTrue(solo.searchText("oi-filtered.py"));
		assertTrue(solo.searchText("oi-f-dir"));
		assertFalse(solo.searchText("oi-not-filter.txt"));
		
		solo.goBack();
		solo.goBack();
	}
	
	public void testHiddenFiles() throws IOException {
		createDirectory(sdcardPath + "oi-filemanager-tests");
		createFile(sdcardPath + "oi-filemanager-tests/.oi-hidden.txt", "");
		solo.clickOnText("oi-filemanager-tests");
		
		boolean origState = solo.searchText(".oi-hidden.txt");
		
		solo.clickOnMenuItem(getAppString(org.openintents.filemanager.R.string.settings));
		
		solo.clickOnText(getAppString(org.openintents.filemanager.R.string.preference_displayhiddenfiles_title));
		solo.goBack();
		assertTrue(origState != solo.searchText(".oi-hidden.txt"));
		
		solo.goBack();
		solo.goBack();
	}
	
	public void testOrder() throws IOException, InterruptedException {
		createDirectory(sdcardPath + "oi-filemanager-tests");
		createFile(sdcardPath + "oi-filemanager-tests/oi-b.txt", "bbb");
		Thread.sleep(10); // make sure that next file is younger
		createFile(sdcardPath + "oi-filemanager-tests/oi-a.txt", "aaaaaa");
		Thread.sleep(10);
		createFile(sdcardPath + "oi-filemanager-tests/oi-c.txt", "");
		solo.clickOnText("oi-filemanager-tests");
		
		String[] sortOrders = activity.getResources().getStringArray(org.openintents.filemanager.R.array.preference_sortby_names);
		
		setAscending(true);
		setSortOrder(sortOrders[0]);
		assertItemsInOrder("oi-a.txt", "oi-b.txt", "oi-c.txt");
		
		setSortOrder(sortOrders[1]);
		assertItemsInOrder("oi-c.txt", "oi-b.txt", "oi-a.txt");
		
		setSortOrder(sortOrders[2]);
		assertItemsInOrder("oi-b.txt", "oi-a.txt", "oi-c.txt");
		
		setAscending(false);
		setSortOrder(sortOrders[0]);
		assertItemsInOrder("oi-c.txt", "oi-b.txt", "oi-a.txt");
	}
	
	private void setSortOrder(String name) {
		solo.clickOnMenuItem(getAppString(org.openintents.filemanager.R.string.settings));
		solo.clickOnText(getAppString(org.openintents.filemanager.R.string.preference_sortby));
		solo.clickOnText(name);
		solo.goBack();
	}
	
	private void setAscending(boolean enabled) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(activity);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean("ascending", enabled);
		editor.commit();
	}
	
	private void assertItemsInOrder(String a, String b, String c) {
		int aPos = solo.getText(a).getTop();
		int bPos = solo.getText(b).getTop();
		int cPos = solo.getText(c).getTop();
		if(aPos > bPos)
			fail("aPos > bPos");
		if(bPos > cPos)
			fail("bpos > cPos");
	}
	
	public void testIntentSaveAs() throws IOException {
		createDirectory(sdcardPath + "oi-filemanager-tests");
		createFile(sdcardPath + "oi-filemanager-tests/oi-to-open.txt", "bbb");

		Uri uri = Uri.parse("file:///mnt/sdcard/oi-filemanager-tests/oi-to-open.txt");
		intent = new Intent("android.intent.action.VIEW", uri);
		intent.setClassName("org.openintents.filemanager",
				"org.openintents.filemanager.SaveAsActivity");
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		
		activity = getInstrumentation().startActivitySync(intent);
		
		solo.enterText(0, "oi-target.txt");
		solo.clickOnButton(getAppString(android.R.string.ok));
		assertTrue(new File(sdcardPath + "oi-filemanager-tests/oi-to-open.txtoi-target.txt").exists());
		solo.goBack();
		solo.goBack();
	}
	
	public void testIntentUrl() throws IOException {
		createDirectory(sdcardPath + "oi-filemanager-tests");
		createDirectory(sdcardPath + "oi-filemanager-tests/oi-dir-to-open");
		createDirectory(sdcardPath + "oi-filemanager-tests/oi-dir-to-open/oi-intent");
		
		Uri uri = Uri.parse("file:///mnt/sdcard/oi-filemanager-tests/oi-dir-to-open");
		intent = new Intent("android.intent.action.VIEW", uri);
		intent.setClassName("org.openintents.filemanager",
				"org.openintents.filemanager.FileManagerActivity");
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		
		activity = getInstrumentation().startActivitySync(intent);
		
		assertTrue(solo.searchText("oi-intent"));
		solo.goBack();
		solo.goBack();
	}
	
	public void testIntentUri() throws IOException {
		createDirectory(sdcardPath + "oi-filemanager-tests");
		createFile(sdcardPath + "oi-filemanager-tests/oi-to-open.txt", "bbb");		
		
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(Uri.parse("file://" + sdcardPath + "oi-filemanager-tests/oi-to-open.txt"));
		intent.setPackage("org.openintents.filemanager");
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		
		activity = getInstrumentation().startActivitySync(intent);
		
		assertTrue(solo.searchText("oi-to-open.txt"));
		solo.goBack();
		solo.goBack();	
	}
	
	public void testIntentPickFile() throws IOException {
		// startActivityForResult is, I think, impossible to test on Robotinium
		createDirectory(sdcardPath + "oi-filemanager-tests");
		createFile(sdcardPath + "oi-filemanager-tests/oi-pick-file", "");
		
		Uri uri = Uri.parse("file:///mnt/sdcard/oi-filemanager-tests/oi-dir-to-open");
		intent = new Intent("org.openintents.action.PICK_FILE", uri);
		intent.setClassName("org.openintents.filemanager",
				"org.openintents.filemanager.FileManagerActivity");
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		
		activity = getInstrumentation().startActivitySync(intent);
		
		solo.clickOnText("oi-pick-file");
		solo.clickOnButton(getAppString(android.R.string.ok));
		
		solo.goBack();
	}
	
	public void testIntentRememberPickFilePath() throws IOException {
		String[] actions = new String[]{
			"org.openintents.action.PICK_FILE",
			"org.openintents.action.PICK_DIRECTORY",
			Intent.ACTION_GET_CONTENT
		};
		
		for(int i=0;i<3;i++){
			createDirectory(sdcardPath + "oi-filemanager-tests");
			if(i==1){ //Pick directory
				createDirectory(sdcardPath + "oi-filemanager-tests/oi-dir-to-pick");
			}
			else{
				createFile(sdcardPath + "oi-filemanager-tests/oi-file-to-pick.txt", "bbb");
			}
			//Directory because PICK_DIRECTORY doesn't show files
			createDirectory(sdcardPath + "oi-to-pick-test-folder-deleted");
			

			// Pick a file first
			Uri uri = Uri.parse("file:///mnt/sdcard"); //If there was already a remembered pick file path
			intent = new Intent(actions[i], uri);
			intent.setClassName("org.openintents.filemanager",
					"org.openintents.filemanager.FileManagerActivity");
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			
			activity = getInstrumentation().startActivitySync(intent);
			
			solo.clickOnText("oi-filemanager-tests");
			if(i==1) //Pick directory
				solo.clickOnText("oi-dir-to-pick");
			else
				solo.clickOnText("oi-file-to-pick.txt");
			
			if(i != 2) // When ACTION_GET_CONTENT, the file is picked automatically, when clicked
				solo.clickOnButton(getAppString(android.R.string.ok));
			
			// Check, if we are in the oi-filemanager-tests directory
			intent = new Intent(actions[i]);
			intent.setClassName("org.openintents.filemanager",
					"org.openintents.filemanager.FileManagerActivity");
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			activity = getInstrumentation().startActivitySync(intent);
			
			solo.goBack();
			
			
			//Delete the oi-filemanager-tests directory
			deleteDirectory(sdcardPath + "oi-filemanager-tests");
			
			//Check, if the current directory is the default (sdcardPath)
			intent = new Intent(actions[i]);
			intent.setClassName("org.openintents.filemanager",
					"org.openintents.filemanager.FileManagerActivity");
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			activity = getInstrumentation().startActivitySync(intent);
			
			assertTrue(solo.searchText("oi-to-pick-test-folder-deleted"));
			
			//Clean up
			(new File(sdcardPath + "oi-to-pick-test-folder-deleted")).delete();
			
			solo.goBack();
			solo.goBack();
		}
	}

	public void testBrowseToOnPressEnter() throws IOException {
		String dirPath = "oi-filemanager-tests";
		String filename = "oi-test-is-in-right-directory";
		createDirectory(sdcardPath + dirPath);
		createFile(sdcardPath + dirPath + "/" + filename, "");
		
		/*
		 *  We start at the SD card. Home ImageButton has index 0. Then there's a mnt classic button.
		 *  And finally SD card ImageButton with index 1. (Android 1.x and 2.x)
		 *  
		 *  Remark: On Android 3.x(?) and 4.x, the index may have to be set to 2?
		 */
		if(android.os.Build.VERSION.SDK_INT < 11)
			solo.clickOnImageButton(1);
		else
			solo.clickOnImageButton(2);
		
		solo.clickOnEditText(0); // Let the editText has focus to be able to send the enter key.
		solo.enterText(0, "/"+dirPath);
		solo.sendKey(Solo.ENTER);

		assertTrue(solo.searchText(filename));
		
		solo.goBack();
		solo.goBack();
	}
	
	// Other possible tests:
	// 		testSend
	// 		testMore
	// 		testKeyboardFilter
}