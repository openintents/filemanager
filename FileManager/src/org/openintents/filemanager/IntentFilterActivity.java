package org.openintents.filemanager;

import org.openintents.filemanager.lists.FileListFragment;
import org.openintents.filemanager.lists.MultiselectListFragment;
import org.openintents.intents.FileManagerIntents;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public class IntentFilterActivity extends FragmentActivity {
	private FileListFragment mFragment;
	
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		Intent intent = getIntent();
		
		// Multiselect intent
		if(intent.getAction().equals(FileManagerIntents.ACTION_MULTI_SELECT)){
			String tag = "Multi_select_fragment";
			mFragment = (MultiselectListFragment) getSupportFragmentManager().findFragmentByTag(tag);
			
			// Only add if it doesn't exist
			if(mFragment == null){
				mFragment = new MultiselectListFragment();
				mFragment.setArguments(intent.getExtras());
				
				setTitle(R.string.multiselect_title);
				
				getSupportFragmentManager().beginTransaction().add(android.R.id.content, mFragment, tag).commit();
			}
		}
	}
}