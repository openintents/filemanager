package org.openintents.filemanager.test;

import androidx.test.espresso.IdlingResource;

import org.openintents.filemanager.FileManagerActivity;
import org.openintents.filemanager.IntentFilterActivity;
import org.openintents.filemanager.lists.PickFileListFragment;
import org.openintents.filemanager.lists.SimpleFileListFragment;

public class DirectoryScannerIdlingResource implements IdlingResource {
    private final SimpleFileListFragment fragment;
    private ResourceCallback callback;

    public DirectoryScannerIdlingResource(FileManagerActivity activity) {
        fragment = (SimpleFileListFragment) activity.getSupportFragmentManager().findFragmentByTag(FileManagerActivity.FRAGMENT_TAG);
    }

    public DirectoryScannerIdlingResource(IntentFilterActivity activity) {
        fragment = (SimpleFileListFragment) activity.getSupportFragmentManager().findFragmentByTag(PickFileListFragment.class.getName());
    }

    @Override
    public String getName() {
        return DirectoryScannerIdlingResource.class.getName();
    }

    @Override
    public boolean isIdleNow() {
        boolean idle = !fragment.isLoading();
        if (idle && callback != null) {
            callback.onTransitionToIdle();
        }
        return idle;
    }

    @Override
    public void registerIdleTransitionCallback(ResourceCallback callback) {
        this.callback = callback;
        fragment.setResourceCallback(callback);
    }
}
