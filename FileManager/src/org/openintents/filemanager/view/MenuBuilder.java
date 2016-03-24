/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openintents.filemanager.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;

/**
 * Implementation of the {@link android.view.Menu} interface for creating a
 * standard menu UI.
 */
public class MenuBuilder implements Menu {
    /** The number of different menu types */
    public static final int NUM_TYPES = 3;
    /** The menu type that represents the icon menu view */
    public static final int TYPE_ICON = 0;
    /** The menu type that represents the expanded menu view */
    public static final int TYPE_EXPANDED = 1;
    /**
     * The menu type that represents a menu dialog. Examples are context and sub
     * menus. This menu type will not have a corresponding MenuView, but it will
     * have an ItemView.
     */
    public static final int TYPE_DIALOG = 2;

    private static final int[]  sCategoryToOrder = new int[] {
        1, /* No category */
        4, /* CONTAINER */
        5, /* SYSTEM */
        3, /* SECONDARY */
        2, /* ALTERNATIVE */
        0, /* SELECTED_ALTERNATIVE */
    };

    private final Context mContext;
    private final Resources mResources;

    /**
     * Whether the shortcuts should be qwerty-accessible. Use isQwertyMode()
     * instead of accessing this directly.
     */
    private boolean mQwertyMode;
        
    /** Contains all of the items for this menu */
    private ArrayList<MenuItemImpl> mItems;

    /** Contains only the items that are currently visible.  This will be created/refreshed from
     * {@link #getVisibleItems()} */
    private ArrayList<MenuItemImpl> mVisibleItems;
    /**
     * Whether or not the items (or any one item's shown state) has changed since it was last
     * fetched from {@link #getVisibleItems()}
     */ 
    private boolean mIsVisibleItemsStale;

    /**
     * Current use case is Context Menus: As Views populate the context menu, each one has
     * extra information that should be passed along.  This is the current menu info that
     * should be set on all items added to this menu.
     */
    private ContextMenuInfo mCurrentMenuInfo;
    
    /** Header title for menu types that have a header (context and submenus) */
    CharSequence mHeaderTitle;
    /** Header icon for menu types that have a header and support icons (context) */
    Drawable mHeaderIcon;
    /** Header custom view for menu types that have a header and support custom views (context) */
    View mHeaderView;

    /**
     * Prevents onItemsChanged from doing its junk, useful for batching commands
     * that may individually call onItemsChanged.
     */
    private boolean mPreventDispatchingItemsChanged = false;
    
    private MenuType[] mMenuTypes;
    class MenuType {
        MenuType(int menuType) {
        }
        
        boolean hasMenuView() {
        	return false;
        }
    }
    
    /**
     * Called by menu items to execute their associated action
     */
    public interface ItemInvoker {
        public boolean invokeItem(MenuItemImpl item);
    }

    public MenuBuilder(Context context) {
        mMenuTypes = new MenuType[NUM_TYPES];
        
        mContext = context;
        mResources = context.getResources();
        
        mItems = new ArrayList<>();
        
        mVisibleItems = new ArrayList<>();
        mIsVisibleItemsStale = true;
    }
    
    MenuType getMenuType(int menuType) {
        if (mMenuTypes[menuType] == null) {
            mMenuTypes[menuType] = new MenuType(menuType);
        }
        
        return mMenuTypes[menuType];
    }
    
    /**
     * Adds an item to the menu.  The other add methods funnel to this.
     */
    private MenuItem addInternal(int group, int id, int categoryOrder, CharSequence title) {
        final int ordering = getOrdering(categoryOrder);
        
        final MenuItemImpl item = new MenuItemImpl(this, group, id, categoryOrder, ordering, title);

        if (mCurrentMenuInfo != null) {
            // Pass along the current menu info
            item.setMenuInfo(mCurrentMenuInfo);
        }
        
        mItems.add(findInsertIndex(mItems, ordering), item);
        onItemsChanged(false);
        
        return item;
    }
    
    @Override
	public MenuItem add(CharSequence title) {
        return addInternal(0, 0, 0, title);
    }

    @Override
	public MenuItem add(int titleRes) {
        return addInternal(0, 0, 0, mResources.getString(titleRes));
    }

    @Override
	public MenuItem add(int group, int id, int categoryOrder, CharSequence title) {
        return addInternal(group, id, categoryOrder, title);
    }

    @Override
	public MenuItem add(int group, int id, int categoryOrder, int title) {
        return addInternal(group, id, categoryOrder, mResources.getString(title));
    }

    @Override
	public SubMenu addSubMenu(CharSequence title) {
        return addSubMenu(0, 0, 0, title);
    }

    @Override
	public SubMenu addSubMenu(int titleRes) {
        return addSubMenu(0, 0, 0, mResources.getString(titleRes));
    }

    @Override
	public SubMenu addSubMenu(int group, int id, int categoryOrder, CharSequence title) {
        throw new UnsupportedOperationException("No submenu for context menu");
    }

    @Override
	public SubMenu addSubMenu(int group, int id, int categoryOrder, int title) {
        return addSubMenu(group, id, categoryOrder, mResources.getString(title));
    }

    @Override
	public int addIntentOptions(int group, int id, int categoryOrder, ComponentName caller,
            Intent[] specifics, Intent intent, int flags, MenuItem[] outSpecificItems) {
        PackageManager pm = mContext.getPackageManager();
        final List<ResolveInfo> lri =  pm.queryIntentActivityOptions(caller, specifics, intent, 0);
        final int N = lri != null ? lri.size() : 0;

        if ((flags & FLAG_APPEND_TO_GROUP) == 0) {
            removeGroup(group);
        }

        for (int i=0; i<N; i++) {
            final ResolveInfo ri = lri.get(i);
            Intent rintent = new Intent(ri.specificIndex < 0 ? intent : specifics[ri.specificIndex]);
            rintent.setComponent(new ComponentName(ri.activityInfo.applicationInfo.packageName, ri.activityInfo.name));
            final MenuItem item = add(group, id, categoryOrder, ri.loadLabel(pm))
                    .setIcon(ri.loadIcon(pm))
                    .setIntent(rintent);
            if (outSpecificItems != null && ri.specificIndex >= 0) {
                outSpecificItems[ri.specificIndex] = item;
            }
        }

        return N;
    }

    @Override
	public void removeItem(int id) {
        removeItemAtInt(findItemIndex(id), true);
    }

    @Override
	public void removeGroup(int group) {
        final int i = findGroupIndex(group);

        if (i >= 0) {
            final int maxRemovable = mItems.size() - i;
            int numRemoved = 0;
            while ((numRemoved++ < maxRemovable) && (mItems.get(i).getGroupId() == group)) {
                // Don't force update for each one, this method will do it at the end
                removeItemAtInt(i, false);
            }
            // Notify menu views
            onItemsChanged(false);
        }
    }

    /**
     * Remove the item at the given index and optionally forces menu views to
     * update.
     * 
     * @param index The index of the item to be removed. If this index is
     *            invalid an exception is thrown.
     * @param updateChildrenOnMenuViews Whether to force update on menu views.
     *            Please make sure you eventually call this after your batch of
     *            removals.
     */
    private void removeItemAtInt(int index, boolean updateChildrenOnMenuViews) {
        if ((index < 0) || (index >= mItems.size())) return;

        mItems.remove(index);
        
        if (updateChildrenOnMenuViews) onItemsChanged(false);
    }
    
    @Override
	public void clear() {
        mItems.clear();
        
        onItemsChanged(true);
    }

    void setExclusiveItemChecked(MenuItem item) {
        final int group = item.getGroupId();
        
        final int N = mItems.size();
        for (int i = 0; i < N; i++) {
            MenuItemImpl curItem = mItems.get(i);
            if (curItem.getGroupId() == group) {
                if (!curItem.isExclusiveCheckable()) continue;
                if (!curItem.isCheckable()) continue;
                
                // Check the item meant to be checked, uncheck the others (that are in the group)
                curItem.setCheckedInt(curItem == item);
            }
        }
    }
    
    @Override
	public void setGroupCheckable(int group, boolean checkable, boolean exclusive) {
        final int N = mItems.size();
       
        for (int i = 0; i < N; i++) {
            MenuItemImpl item = mItems.get(i);
            if (item.getGroupId() == group) {
                item.setExclusiveCheckable(exclusive);
                item.setCheckable(checkable);
            }
        }
    }

    @Override
	public void setGroupVisible(int group, boolean visible) {
        final int N = mItems.size();

        // We handle the notification of items being changed ourselves, so we use setVisibleInt rather
        // than setVisible and at the end notify of items being changed
        
        boolean changedAtLeastOneItem = false;
        for (int i = 0; i < N; i++) {
            MenuItemImpl item = mItems.get(i);
            if (item.getGroupId() == group && item.setVisibleInt(visible)) {
                changedAtLeastOneItem = true;
            }
        }

        if (changedAtLeastOneItem) onItemsChanged(false);
    }

    @Override
	public void setGroupEnabled(int group, boolean enabled) {
        final int N = mItems.size();

        for (int i = 0; i < N; i++) {
            MenuItemImpl item = mItems.get(i);
            if (item.getGroupId() == group) {
                item.setEnabled(enabled);
            }
        }
    }

    @Override
	public boolean hasVisibleItems() {
        final int size = size();

        for (int i = 0; i < size; i++) {
            MenuItemImpl item = mItems.get(i);
            if (item.isVisible()) {
                return true;
            }
        }

        return false;
    }

    @Override
	public MenuItem findItem(int id) {
        final int size = size();
        for (int i = 0; i < size; i++) {
            MenuItemImpl item = mItems.get(i);
            if (item.getItemId() == id) {
                return item;
            } else if (item.hasSubMenu()) {
                MenuItem possibleItem = item.getSubMenu().findItem(id);
                
                if (possibleItem != null) {
                    return possibleItem;
                }
            }
        }
        
        return null;
    }

    public int findItemIndex(int id) {
        final int size = size();

        for (int i = 0; i < size; i++) {
            MenuItemImpl item = mItems.get(i);
            if (item.getItemId() == id) {
                return i;
            }
        }

        return -1;
    }

    public int findGroupIndex(int group) {
        return findGroupIndex(group, 0);
    }

    public int findGroupIndex(int group, int start) {
        final int size = size();
        
        if (start < 0) {
            start = 0;
        }
        
        for (int i = start; i < size; i++) {
            final MenuItemImpl item = mItems.get(i);
            
            if (item.getGroupId() == group) {
                return i;
            }
        }

        return -1;
    }
    
    @Override
	public int size() {
        return mItems.size();
    }

    /** {@inheritDoc} */
    @Override
	public MenuItem getItem(int index) {
        return mItems.get(index);
    }

    @Override
	public boolean isShortcutKey(int keyCode, KeyEvent event) {
        return findItemWithShortcutForKey(keyCode, event) != null;
    }


    /*
     * We want to return the menu item associated with the key, but if there is no
     * ambiguity (i.e. there is only one menu item corresponding to the key) we want
     * to return it even if it's not an exact match; this allow the user to
     * _not_ use the ALT key for example, making the use of shortcuts slightly more
     * user-friendly. An example is on the G1, '!' and '1' are on the same key, and
     * in Gmail, Menu+1 will trigger Menu+! (the actual shortcut).
     *
     * On the other hand, if two (or more) shortcuts corresponds to the same key,
     * we have to only return the exact match.
     */
    MenuItemImpl findItemWithShortcutForKey(int keyCode, KeyEvent event) {
        // Get all items that can be associated directly or indirectly with the keyCode
        List<MenuItemImpl> items = findItemsWithShortcutForKey(keyCode, event);

        if (items == null) {
            return null;
        }

        final int metaState = event.getMetaState();
        final KeyCharacterMap.KeyData possibleChars = new KeyCharacterMap.KeyData();
        // Get the chars associated with the keyCode (i.e using any chording combo)
        event.getKeyData(possibleChars);

        // If we have only one element, we can safely returns it
        if (items.size() == 1) {
            return items.get(0);
        }

        final boolean qwerty = isQwertyMode();
        // If we found more than one item associated with the key,
        // we have to return the exact match
        for (MenuItemImpl item : items) {
            final char shortcutChar = qwerty ? item.getAlphabeticShortcut() : item.getNumericShortcut();
            if ((shortcutChar == possibleChars.meta[0] &&
                    (metaState & KeyEvent.META_ALT_ON) == 0)
                || (shortcutChar == possibleChars.meta[2] &&
                    (metaState & KeyEvent.META_ALT_ON) != 0)
                || (qwerty && shortcutChar == '\b' &&
                    keyCode == KeyEvent.KEYCODE_DEL)) {
                return item;
            }
        }
        return null;
    }
    
    @Override
	public void setQwertyMode(boolean isQwerty) {
        mQwertyMode = isQwerty;
    }

    /**
     * This is the part of an order integer that supplies the category of the
     * item.
     * @hide
     */
    static final int CATEGORY_MASK = 0xffff0000;
    /**
     * Bit shift of the category portion of the order integer.
     * @hide
     */
    static final int CATEGORY_SHIFT = 16;
    

    /**
     * This is the part of an order integer that the user can provide.
     * @hide
     */
    static final int USER_MASK = 0x0000ffff;
    
    
    /**
     * Returns the ordering across all items. This will grab the category from
     * the upper bits, find out how to order the category with respect to other
     * categories, and combine it with the lower bits.
     * 
     * @param categoryOrder The category order for a particular item (if it has
     *            not been or/add with a category, the default category is
     *            assumed).
     * @return An ordering integer that can be used to order this item across
     *         all the items (even from other categories).
     */
    private static int getOrdering(int categoryOrder)
    {
        final int index = (categoryOrder & CATEGORY_MASK) >> CATEGORY_SHIFT;
        
        if (index < 0 || index >= sCategoryToOrder.length) {
            throw new IllegalArgumentException("order does not contain a valid category.");
        }
        
        return (sCategoryToOrder[index] << CATEGORY_SHIFT) | (categoryOrder & USER_MASK);
    }

    /**
     * @return whether the menu shortcuts are in qwerty mode or not
     */
    boolean isQwertyMode() {
        return mQwertyMode;
    }

    Resources getResources() {
        return mResources;
    }

    private static int findInsertIndex(ArrayList<MenuItemImpl> items, int ordering) {
        for (int i = items.size() - 1; i >= 0; i--) {
            MenuItemImpl item = items.get(i);
            if (item.getOrdering() <= ordering) {
                return i + 1;
            }
        }
        
        return 0;
    }
    
    @Override
	public boolean performShortcut(int keyCode, KeyEvent event, int flags) {
        final MenuItemImpl item = findItemWithShortcutForKey(keyCode, event);

        boolean handled = false;
        
        if (item != null) {
            handled = performItemAction(item, flags);
        }
        
        if ((flags & FLAG_ALWAYS_PERFORM_CLOSE) != 0) {
            close(true);
        }
        
        return handled;
    }

    /*
     * This function will return all the menu and sub-menu items that can
     * be directly (the shortcut directly corresponds) and indirectly
     * (the ALT-enabled char corresponds to the shortcut) associated
     * with the keyCode.
     */
    List<MenuItemImpl> findItemsWithShortcutForKey(int keyCode, KeyEvent event) {
        final boolean qwerty = isQwertyMode();
        final int metaState = event.getMetaState();
        final KeyCharacterMap.KeyData possibleChars = new KeyCharacterMap.KeyData();
        // Get the chars associated with the keyCode (i.e using any chording combo)
        final boolean isKeyCodeMapped = event.getKeyData(possibleChars);
        // The delete key is not mapped to '\b' so we treat it specially
        if (!isKeyCodeMapped && (keyCode != KeyEvent.KEYCODE_DEL)) {
            return null;
        }

        Vector<MenuItemImpl> items = new Vector<>();
        // Look for an item whose shortcut is this key.
        final int N = mItems.size();
        for (int i = 0; i < N; i++) {
            MenuItemImpl item = mItems.get(i);
            if (item.hasSubMenu()) {
                List<MenuItemImpl> subMenuItems = ((MenuBuilder)item.getSubMenu())
                    .findItemsWithShortcutForKey(keyCode, event);
                items.addAll(subMenuItems);
            }
            final char shortcutChar = qwerty ? item.getAlphabeticShortcut() : item.getNumericShortcut();
            if (((metaState & (KeyEvent.META_SHIFT_ON | KeyEvent.META_SYM_ON)) == 0) &&
                  (shortcutChar != 0) &&
                  (shortcutChar == possibleChars.meta[0]
                      || shortcutChar == possibleChars.meta[2]
                      || (qwerty && shortcutChar == '\b' &&
                          keyCode == KeyEvent.KEYCODE_DEL)) &&
                  item.isEnabled()) {
                items.add(item);
            }
        }
        return items;
    }


    @Override
	public boolean performIdentifierAction(int id, int flags) {
        // Look for an item whose identifier is the id.
        return performItemAction(findItem(id), flags);           
    }

    public boolean performItemAction(MenuItem item, int flags) {
        MenuItemImpl itemImpl = (MenuItemImpl) item;
        
        if (itemImpl == null || !itemImpl.isEnabled()) {
            return false;
        }        
        
        if (item.hasSubMenu()) {
        } else {
            if ((flags & FLAG_PERFORM_NO_CLOSE) == 0) {
                close(true);
            }
        }
        
        return true;
    }
    
    /**
     * Closes the visible menu.
     * 
     * @param allMenusAreClosing Whether the menus are completely closing (true),
     *            or whether there is another menu coming in this menu's place
     *            (false). For example, if the menu is closing because a
     *            sub menu is about to be shown, <var>allMenusAreClosing</var>
     *            is false.
     */
    final void close(boolean allMenusAreClosing) {
    }

    /** {@inheritDoc} */
    @Override
	public void close() {
        close(true);
    }

    /**
     * Called when an item is added or removed.
     * 
     * @param cleared Whether the items were cleared or just changed.
     */
    private void onItemsChanged(boolean cleared) {
        if (!mPreventDispatchingItemsChanged && !mIsVisibleItemsStale) {
            mIsVisibleItemsStale = true;
        }
    }

    /**
     * Called by {@link MenuItemImpl} when its visible flag is changed.
     * @param item The item that has gone through a visibility change.
     */
    void onItemVisibleChanged(MenuItemImpl item) {
        // Notify of items being changed
        onItemsChanged(false);
    }
    
    ArrayList<MenuItemImpl> getVisibleItems() {
        if (!mIsVisibleItemsStale) return mVisibleItems;
        
        // Refresh the visible items
        mVisibleItems.clear();
        
        final int itemsSize = mItems.size(); 
        MenuItemImpl item;
        for (int i = 0; i < itemsSize; i++) {
            item = mItems.get(i);
            if (item.isVisible()) mVisibleItems.add(item);
        }
        
        mIsVisibleItemsStale = false;
        
        return mVisibleItems;
    }

	public Context getContext() {
		return mContext;
	}
}
