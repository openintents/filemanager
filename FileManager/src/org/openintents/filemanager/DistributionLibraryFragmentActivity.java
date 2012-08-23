package org.openintents.filemanager;

import org.openintents.distribution.DistributionLibrary;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;

public class DistributionLibraryFragmentActivity extends FragmentActivity{

	static final int MENU_DISTRIBUTION_START = Menu.FIRST;
	
	static final int DIALOG_DISTRIBUTION_START = 1;

	protected DistributionLibrary mDistribution;
	
    /** Called when the activity is first created. */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mDistribution = new DistributionLibrary(this, MENU_DISTRIBUTION_START, DIALOG_DISTRIBUTION_START);
    }

 	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
 		super.onCreateOptionsMenu(menu);
 		mDistribution.onCreateOptionsMenu(menu);
 		return true;
 	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (mDistribution.onOptionsItemSelected(item)) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		return mDistribution.onCreateDialog(id);
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		super.onPrepareDialog(id, dialog);
		mDistribution.onPrepareDialog(id, dialog);
	}
}
