package org.openintents.filemanager.bookmarks;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import androidx.fragment.app.FragmentActivity;
import android.view.MenuItem;

import org.openintents.filemanager.compatibility.HomeIconHelper;
import org.openintents.filemanager.util.UIUtils;

public class BookmarkListActivity extends FragmentActivity {
    public static final String KEY_RESULT_PATH = "path";
    private static final String FRAGMENT_TAG = "Fragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        UIUtils.setThemeFor(this);
        super.onCreate(savedInstanceState);

        HomeIconHelper.activity_actionbar_setDisplayHomeAsUpEnabled(this);

        if (getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG) == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(android.R.id.content, new BookmarkListFragment(), FRAGMENT_TAG)
                    .commit();
        }
    }

    public void onListItemClick(String path) {
        setResult(RESULT_OK, new Intent().putExtra(KEY_RESULT_PATH, path));
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                HomeIconHelper.showHome(this);
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}