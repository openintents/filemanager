package org.openintents.filemanager.compatibility;

import android.annotation.TargetApi;
import android.os.Build;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.ListView;

import org.openintents.filemanager.R;
import org.openintents.filemanager.files.FileHolder;
import org.openintents.filemanager.lists.SimpleFileListFragment;
import org.openintents.filemanager.util.MenuUtils;
import org.openintents.filemanager.view.PathBar;

import java.util.ArrayList;

/**
 * This class helps wrap some of the platform specific logic of MultiChoiceMode of Honeycomb and up,
 * while keeping the app compliant with API levels that do not ignore {@link VerifyError}s  and crash the app.
 *
 * @author George Venios
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class FileMultiChoiceModeHelper {
    private ListView list;
    private PathBar pathbar;
    private SimpleFileListFragment fragment;

    private int mSingleItemMenuResource;
    private int mMultipleItemsMenuResource;
    public MultiChoiceModeListener listener = new MultiChoiceModeListener() {

        @Override
        public boolean onPrepareActionMode(android.view.ActionMode mode,
                                           Menu menu) {
            menu.clear();

            switch (list.getCheckedItemCount()) {
                // Single selection
                case 1:
                    MenuUtils.fillContextMenu((FileHolder) list.getAdapter().getItem(getSelectedPosition()), menu, mSingleItemMenuResource, mode.getMenuInflater(), list.getContext());
                    break;
                // Multiple selection
                default:
                    MenuUtils.fillMultiselectionMenu(menu, mMultipleItemsMenuResource, mode.getMenuInflater());
                    break;
            }
            return true;
        }

        @Override
        public void onDestroyActionMode(android.view.ActionMode mode) {
            pathbar.setEnabled(true);
        }

        @Override
        public boolean onCreateActionMode(android.view.ActionMode mode,
                                          Menu menu) {
            pathbar.setEnabled(false);
            return true;
        }

        @Override
        public boolean onActionItemClicked(android.view.ActionMode mode,
                                           MenuItem item) {
            boolean res;
            switch (item.getItemId()) {
                case R.id.menu_select_all:
                    for (int i = 0; i < list.getAdapter().getCount(); i++) {
                        list.setItemChecked(i, true);
                    }
                    res = true;
                    break;
                default:
                    switch (list.getCheckedItemCount()) {
                        // Single selection
                        case 1:
                            res = MenuUtils.handleSingleSelectionAction(fragment, item,
                                    (FileHolder) list.getAdapter().getItem(getSelectedPosition()), fragment.getActivity());
                            break;
                        // Multiple selection
                        default:
                            res = MenuUtils.handleMultipleSelectionAction(fragment, item, getCheckedItems(), fragment.getActivity());
                            break;
                    }
                    mode.finish();
            }

            return res;
        }

        @Override
        public void onItemCheckedStateChanged(android.view.ActionMode mode,
                                              int position, long id, boolean checked) {
            mode.setTitle(list.getCheckedItemCount() + " "
                    + fragment.getActivity().getResources().getString(R.string.selected));

            // Force actions' refresh
            mode.invalidate();
        }
    };

    /**
     * @param singleSelectMenuResource The menu to use on single selection.
     * @param multiSelectMenuResource  The menu to use on multiple selection.
     */
    public FileMultiChoiceModeHelper(int singleSelectMenuResource, int multiSelectMenuResource) {
        mSingleItemMenuResource = singleSelectMenuResource;
        mMultipleItemsMenuResource = multiSelectMenuResource;
    }

    public void setListView(ListView list) {
        this.list = list;
        list.setMultiChoiceModeListener(listener);
    }

    public void setPathBar(PathBar p) {
        pathbar = p;
    }

    public void setContext(SimpleFileListFragment f) {
        fragment = f;
    }

    /**
     * This is error free only when FileHolderListAdapter uses stableIds and getItemId(int) returns the int passed (the position of the item).
     *
     * @return
     */
    private int getSelectedPosition() {
        return (int) list.getCheckedItemIds()[0];
    }

    /**
     * @return A {@link FileHolder} list with the currently selected items.
     */
    private ArrayList<FileHolder> getCheckedItems() {
        ArrayList<FileHolder> items = new ArrayList<>();

        for (long pos : list.getCheckedItemIds()) {
            items.add((FileHolder) list.getAdapter().getItem((int) pos));
        }

        return items;
    }
}