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

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.*;

/**
 * @hide
 */
public final class MenuItemImpl implements MenuItem {
	private final int mId;
	private final int mGroup;
	private final int mCategoryOrder;
	private final int mOrdering;
	private CharSequence mTitle;
	private CharSequence mTitleCondensed;
	private Intent mIntent;
	private char mShortcutNumericChar;
	private char mShortcutAlphabeticChar;

	/** The icon's drawable which is only created as needed */
	private Drawable mIconDrawable;
	/**
	 * The icon's resource ID which is used to get the Drawable when it is
	 * needed (if the Drawable isn't already obtained--only one of the two is
	 * needed).
	 */
	private int mIconResId = NO_ICON;

	/** The menu to which this item belongs */
	private MenuBuilder mMenu;

	private Runnable mItemCallback;

	private int mFlags = ENABLED;
	private static final int CHECKABLE = 0x00000001;
	private static final int CHECKED = 0x00000002;
	private static final int EXCLUSIVE = 0x00000004;
	private static final int HIDDEN = 0x00000008;
	private static final int ENABLED = 0x00000010;

	/** Used for the icon resource ID if this item does not have an icon */
	static final int NO_ICON = 0;

	/**
	 * Current use case is for context menu: Extra information linked to the
	 * View that added this item to the context menu.
	 */
	private ContextMenuInfo mMenuInfo;

	private static String sPrependShortcutLabel;
	private static String sEnterShortcutLabel;
	private static String sDeleteShortcutLabel;
	private static String sSpaceShortcutLabel;

	/**
	 * Instantiates this menu item. The constructor {@link #MenuItemData(MenuBuilder, int, int, int, CharSequence, int)} is
	 * preferred due to lazy loading of the icon Drawable.
	 * 
	 * @param menu
	 * @param group
	 *            Item ordering grouping control. The item will be added after
	 *            all other items whose order is <= this number, and before any
	 *            that are larger than it. This can also be used to define
	 *            groups of items for batch state changes. Normally use 0.
	 * @param id
	 *            Unique item ID. Use 0 if you do not need a unique ID.
	 * @param categoryOrder
	 *            The ordering for this item.
	 * @param title
	 *            The text to display for the item.
	 */
	MenuItemImpl(MenuBuilder menu, int group, int id, int categoryOrder, int ordering, CharSequence title) {
		mMenu = menu;
		mId = id;
		mGroup = group;
		mCategoryOrder = categoryOrder;
		mOrdering = ordering;
		mTitle = title;
	}

	@Override
	public boolean isEnabled() {
		return (mFlags & ENABLED) != 0;
	}

	@Override
	public MenuItem setEnabled(boolean enabled) {
		if (enabled) {
			mFlags |= ENABLED;
		} else {
			mFlags &= ~ENABLED;
		}

		return this;
	}

	@Override
	public int getGroupId() {
		return mGroup;
	}

	@Override
	@ViewDebug.CapturedViewProperty
	public int getItemId() {
		return mId;
	}

	@Override
	public int getOrder() {
		return mCategoryOrder;
	}

	public int getOrdering() {
		return mOrdering;
	}

	@Override
	public Intent getIntent() {
		return mIntent;
	}

	@Override
	public MenuItem setIntent(Intent intent) {
		mIntent = intent;
		return this;
	}

	Runnable getCallback() {
		return mItemCallback;
	}

	public MenuItem setCallback(Runnable callback) {
		mItemCallback = callback;
		return this;
	}

	@Override
	public char getAlphabeticShortcut() {
		return mShortcutAlphabeticChar;
	}

	@Override
	public MenuItem setAlphabeticShortcut(char alphaChar) {
		if (mShortcutAlphabeticChar == alphaChar) return this;

		mShortcutAlphabeticChar = Character.toLowerCase(alphaChar);

		return this;
	}

	@Override
	public char getNumericShortcut() {
		return mShortcutNumericChar;
	}

	@Override
	public MenuItem setNumericShortcut(char numericChar) {
		if (mShortcutNumericChar == numericChar) return this;

		mShortcutNumericChar = numericChar;

		return this;
	}

	@Override
	public MenuItem setShortcut(char numericChar, char alphaChar) {
		mShortcutNumericChar = numericChar;
		mShortcutAlphabeticChar = Character.toLowerCase(alphaChar);

		return this;
	}

	/**
	 * @return The active shortcut (based on QWERTY-mode of the menu).
	 */
	char getShortcut() {
		return (mMenu.isQwertyMode() ? mShortcutAlphabeticChar : mShortcutNumericChar);
	}

	/**
	 * @return The label to show for the shortcut. This includes the chording
	 *         key (for example 'Menu+a'). Also, any non-human readable
	 *         characters should be human readable (for example 'Menu+enter').
	 */
	String getShortcutLabel() {

		char shortcut = getShortcut();
		if (shortcut == 0) {
			return "";
		}

		StringBuilder sb = new StringBuilder(sPrependShortcutLabel);
		switch (shortcut) {

		case '\n':
			sb.append(sEnterShortcutLabel);
			break;

		case '\b':
			sb.append(sDeleteShortcutLabel);
			break;

		case ' ':
			sb.append(sSpaceShortcutLabel);
			break;

		default:
			sb.append(shortcut);
			break;
		}

		return sb.toString();
	}

	@Override
	@ViewDebug.CapturedViewProperty
	public CharSequence getTitle() {
		return mTitle;
	}

	@Override
	public MenuItem setTitle(CharSequence title) {
		mTitle = title;

		return this;
	}

	@Override
	public MenuItem setTitle(int title) {
		return setTitle(mMenu.getContext().getString(title));
	}

	@Override
	public CharSequence getTitleCondensed() {
		return mTitleCondensed != null ? mTitleCondensed : mTitle;
	}

	@Override
	public MenuItem setTitleCondensed(CharSequence title) {
		mTitleCondensed = title;

		// Could use getTitle() in the loop below, but just cache what it would do here
		if (title == null) {
			title = mTitle;
		}

		return this;
	}

	@Override
	public Drawable getIcon() {
		if (mIconDrawable != null) {
			return mIconDrawable;
		}

		if (mIconResId != NO_ICON) {
			return mMenu.getResources().getDrawable(mIconResId);
		}

		return null;
	}

	@Override
	public MenuItem setIcon(Drawable icon) {
		mIconResId = NO_ICON;
		mIconDrawable = icon;

		return this;
	}

	@Override
	public MenuItem setIcon(int iconResId) {
		mIconDrawable = null;
		mIconResId = iconResId;

		return this;
	}

	@Override
	public boolean isCheckable() {
		return (mFlags & CHECKABLE) == CHECKABLE;
	}

	@Override
	public MenuItem setCheckable(boolean checkable) {
		mFlags = (mFlags & ~CHECKABLE) | (checkable ? CHECKABLE : 0);

		return this;
	}

	public void setExclusiveCheckable(boolean exclusive) {
		mFlags = (mFlags & ~EXCLUSIVE) | (exclusive ? EXCLUSIVE : 0);
	}

	public boolean isExclusiveCheckable() {
		return (mFlags & EXCLUSIVE) != 0;
	}

	@Override
	public boolean isChecked() {
		return (mFlags & CHECKED) == CHECKED;
	}

	@Override
	public MenuItem setChecked(boolean checked) {
		if ((mFlags & EXCLUSIVE) != 0) {
			// Call the method on the Menu since it knows about the others in this
			// exclusive checkable group
			mMenu.setExclusiveItemChecked(this);
		} else {
			setCheckedInt(checked);
		}

		return this;
	}

	void setCheckedInt(boolean checked) {
		mFlags = (mFlags & ~CHECKED) | (checked ? CHECKED : 0);
	}

	@Override
	public boolean isVisible() {
		return (mFlags & HIDDEN) == 0;
	}

	/**
	 * Changes the visibility of the item. This method DOES NOT notify the
	 * parent menu of a change in this item, so this should only be called from
	 * methods that will eventually trigger this change. If unsure, use {@link #setVisible(boolean)} instead.
	 * 
	 * @param shown
	 *            Whether to show (true) or hide (false).
	 * @return Whether the item's shown state was changed
	 */
	boolean setVisibleInt(boolean shown) {
		final int oldFlags = mFlags;
		mFlags = (mFlags & ~HIDDEN) | (shown ? 0 : HIDDEN);
		return oldFlags != mFlags;
	}

	@Override
	public MenuItem setVisible(boolean shown) {
		// Try to set the shown state to the given state. If the shown state was changed
		// (i.e. the previous state isn't the same as given state), notify the parent menu that
		// the shown state has changed for this item
		if (setVisibleInt(shown)) mMenu.onItemVisibleChanged(this);

		return this;
	}

	@Override
	public MenuItem setOnMenuItemClickListener(MenuItem.OnMenuItemClickListener clickListener) {
		return this;
	}

	@Override
	public String toString() {
		return mTitle.toString();
	}

	void setMenuInfo(ContextMenuInfo menuInfo) {
		mMenuInfo = menuInfo;
	}

	@Override
	public ContextMenuInfo getMenuInfo() {
		return mMenuInfo;
	}

	@Override
	public SubMenu getSubMenu() {
		return null;
	}

	@Override
	public boolean hasSubMenu() {
		return false;
	}

	@Override
	public boolean collapseActionView() {
		return false;
	}

	@Override
	public boolean expandActionView() {
		return false;
	}

	@Override
	public ActionProvider getActionProvider() {
		return null;
	}

	@Override
	public View getActionView() {
		return null;
	}

	@Override
	public boolean isActionViewExpanded() {
		return false;
	}

	@Override
	public MenuItem setActionProvider(ActionProvider actionProvider) {
		return null;
	}

	@Override
	public MenuItem setActionView(View view) {
		return null;
	}

	@Override
	public MenuItem setActionView(int resId) {
		return null;
	}

	@Override
	public MenuItem setOnActionExpandListener(OnActionExpandListener listener) {
		return null;
	}

	@Override
	public void setShowAsAction(int actionEnum) {
	}

	@Override
	public MenuItem setShowAsActionFlags(int actionEnum) {
		return null;
	}
}