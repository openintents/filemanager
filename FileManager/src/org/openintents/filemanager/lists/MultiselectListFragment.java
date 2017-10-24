package org.openintents.filemanager.lists;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.openintents.filemanager.R;
import org.openintents.filemanager.files.FileHolder;
import org.openintents.filemanager.util.MenuUtils;
import org.openintents.filemanager.view.LegacyActionContainer;

import java.util.ArrayList;

/**
 * Dedicated file list fragment, used for multiple selection on platforms older than Honeycomb.
 * OnDestroy sets RESULT_OK on the parent activity so that callers refresh their lists if appropriate.
 *
 * @author George Venios
 */
public class MultiselectListFragment extends FileListFragment {
    private LegacyActionContainer mLegacyActionContainer;
    private TextView mMessageView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.filelist_legacy_multiselect, null);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        super.onViewCreated(view, savedInstanceState);

        // Init members
        mMessageView = (TextView) view.findViewById(R.id.message);
        mMessageView.setText(getString(R.string.error_generic) + "no access");

        mLegacyActionContainer = (LegacyActionContainer) view.findViewById(R.id.action_container);
        mLegacyActionContainer.setMenuResource(R.menu.multiselect);
        mLegacyActionContainer.setOnActionSelectedListener(new LegacyActionContainer.OnActionSelectedListener() {
            @Override
            public void actionSelected(MenuItem item) {
                if (getListView().getCheckItemIds().length == 0) {
                    Toast.makeText(getActivity(), R.string.no_selection, Toast.LENGTH_SHORT).show();
                    return;
                }

                ArrayList<FileHolder> fItems = new ArrayList<>();

                for (long i : getListView().getCheckItemIds()) {
                    fItems.add((FileHolder) mAdapter.getItem((int) i));
                }

                MenuUtils.handleMultipleSelectionAction(MultiselectListFragment.this, item, fItems, getActivity());
            }
        });
    }

    @Override
    protected
    @LayoutRes
    int getItemLayout() {
        return R.layout.item_filelist_multiselect;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.options_multiselect, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        ListView list = getListView();

        switch (item.getItemId()) {
            case R.id.check_all:
                for (int i = 0; i < mAdapter.getCount(); i++) {
                    list.setItemChecked(i, true);
                }
                return true;
            case R.id.uncheck_all:
                for (int i = 0; i < mAdapter.getCount(); i++) {
                    list.setItemChecked(i, false);
                }
                return true;
            default:
                return false;
        }
    }

    @Override
    protected void updateNoAccessMessage(boolean showMessage) {
        mMessageView.setVisibility(showMessage ? View.VISIBLE : View.GONE);
    }
}