package org.openintents.filemanager.bookmarks;

import org.openintents.filemanager.compatibility.HomeIconHelper;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.MenuItem;

public class BookmarkListActivity extends FragmentActivity {
	public static String KEY_RESULT_PATH = "path";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if(Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB){
			HomeIconHelper.activity_actionbar_setDisplayHomeAsUpEnabled(this);
		}
		
		getSupportFragmentManager().beginTransaction().add(android.R.id.content, new BookmarkListFragment()).commit();
	}

	public void onListItemClick(String path) {
		setResult(RESULT_OK, new Intent().putExtra(KEY_RESULT_PATH, path));
		finish();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case android.R.id.home:
			HomeIconHelper.showHome(this);
			break;
		}
		return super.onOptionsItemSelected(item);
	}
}