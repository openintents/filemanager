package org.openintents.filemanager.bookmarks;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.openintents.filemanager.R;
import org.openintents.filemanager.ThumbnailLoader;
import org.openintents.filemanager.files.FileHolder;
import org.openintents.filemanager.util.FileUtils;

import java.io.File;
import java.util.ArrayList;

/**
 * @author George Venios
 */
public class BookmarkListAdapter extends BaseAdapter implements LoaderManager.LoaderCallbacks<Cursor> {
    private final String[] projection = {BookmarksProvider._ID, BookmarksProvider.NAME, BookmarksProvider.PATH};
    private ArrayList<Bookmark> items;
    private LayoutInflater inflater;
    private FragmentActivity act;
    // Thumbnail specific
    private ThumbnailLoader mThumbnailLoader;
    private boolean scrolling = false;

    public BookmarkListAdapter(FragmentActivity activity) {
        items = new ArrayList<>();

        act = activity;
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        mThumbnailLoader = new ThumbnailLoader(act);
        act.getSupportLoaderManager().initLoader(0, null, this);
    }

    private void refreshItems(Cursor c) {
        items.clear();
        addExternalStorages();
        addFromBookmarksProvider(c);
        notifyDataSetChanged();
    }

    private void addFromBookmarksProvider(Cursor c) {
        if (c == null) {
            return;
        }


        Bookmark b;
        if (c.moveToFirst()) {
            do {
                b = new Bookmark();

                b.id = c.getLong(0);
                b.name = c.getString(1);
                b.path = c.getString(2);

                items.add(b);
            } while (c.moveToNext());
        }
    }

    private void addExternalStorages() {
        File[] externalStorageFiles = ContextCompat.getExternalFilesDirs(act, null);
        Bookmark b;
        File rootFile;
        int id = -1;
        if (externalStorageFiles != null) {
            for (File f : externalStorageFiles) {
                rootFile = new File(FileUtils.getRootOfInnerSdCardFolder(f));
                b = new Bookmark();
                b.id = id;
                b.name = rootFile.getName();
                b.path = rootFile.getAbsolutePath();
                id--;
                items.add(b);
            }
        }

    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).id;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Bookmark bookmark = items.get(position);
        FileHolder fileHolder = new FileHolder(new File(bookmark.path));

        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_filelist, null);

            viewHolder = new ViewHolder();
            viewHolder.icon = (ImageView) convertView.findViewById(R.id.icon);
            viewHolder.primaryInfo = (TextView) convertView.findViewById(R.id.primary_info);
            viewHolder.secondaryInfo = (TextView) convertView.findViewById(R.id.secondary_info);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.primaryInfo.setText(bookmark.name);
        viewHolder.secondaryInfo.setText(bookmark.path);
        if (bookmark.id < 0) {
            viewHolder.icon.setImageResource(R.drawable.ic_launcher_sdcard);
        } else {
            if (fileHolder.getFile().isDirectory()) {
                viewHolder.icon.setImageResource(R.drawable.ic_launcher_folder);
            } else {
                if (shouldLoadIcon(fileHolder) && mThumbnailLoader != null) {
                    mThumbnailLoader.loadImage(fileHolder, viewHolder.icon);
                }
            }
        }

        return convertView;
    }

    /**
     * Inform this adapter about scrolling state of list so that lists don't lag due to cache ops.
     *
     * @param isScrolling True if the ListView is still scrolling.
     */
    public void setScrolling(boolean isScrolling) {
        scrolling = isScrolling;
        if (!isScrolling)
            notifyDataSetChanged();
    }

    private boolean shouldLoadIcon(FileHolder item) {
        return !scrolling && item.getFile().isFile() && !item.getMimeType().equals("video/mpeg");
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == 0) {
            return new CursorLoader(act,  BookmarksProvider.CONTENT_URI,
                    projection,
                    null,
                    null,
                    null);
        } else {
            return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == 0) {
            refreshItems(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    public static class Bookmark {
        long id;
        String name;
        String path;

        public String getName() {
            return name;
        }
    }

    private static class ViewHolder {
        private TextView primaryInfo;
        private TextView secondaryInfo;
        private ImageView icon;
    }
}