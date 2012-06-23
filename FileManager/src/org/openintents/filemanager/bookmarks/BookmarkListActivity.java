package org.openintents.filemanager.bookmarks;

import android.content.Intent;
import android.os.Bundle;
import android.support.v2.app.FragmentActivity;

public class BookmarkListActivity extends FragmentActivity {
	public static String KEY_RESULT_PATH = "path";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportFragmentManager().beginTransaction().add(android.R.id.content, new BookmarkListFragment()).commit();
	}

	public void onListItemClick(String path) {
		setResult(RESULT_OK, new Intent().putExtra(KEY_RESULT_PATH, path));
		finish();
	}
}