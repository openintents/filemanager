package org.openintents.filemanager.view;

import org.openintents.filemanager.FileManagerActivity;
import org.openintents.filemanager.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.Toast;

public class LegacyActionContainer extends LinearLayout {
	private Menu menu = null;
	private FileManagerActivity act;

	public LegacyActionContainer(Context context) {
		super(context);
		init();
	}

	public LegacyActionContainer(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	/**
	 * Init this view.
	 */
	private void init() {
		setOrientation(HORIZONTAL);
		setBackgroundResource(R.drawable.bg_navbar_btn_standard);
	}

	/**
	 * Set the menu resource which contains the actions this view will display.
	 * 
	 * @param menuRes
	 *            The menu resource id.
	 */
	public void setMenuResource(int menuRes) {
		removeAllViews();
		this.menu = new MenuBuilder(getContext());
		MenuInflater inflater = new MenuInflater(getContext());
		inflater.inflate(menuRes, menu);
		loadChildViews();
	}

	public boolean executeAction(int menuItemId) {
		// TODO we'll check for android version and correctly act upon the item.
		switch (menuItemId) {
		case R.id.menu_send:
			act.actionSend();
			break;
		case R.id.menu_delete:
			act.actionDelete();
			break;
		case R.id.menu_move:
			act.actionMove();
			break;
		case R.id.menu_copy:
			act.actionCopy();
			break;
		case R.id.menu_compress:
			act.actionCompress();
			break;
		default:
			return false;
		}
		return true;
	}

	/**
	 * Add the {@link #menu} members as children in the view. Each view will have the corresponding MenuItem as its tag.
	 */
	private void loadChildViews() {
		ImageButton itemView = null;
		LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		params.weight = 1;
		for (int i = 0; i < menu.size(); i++) {
			itemView = new ImageButton(getContext());
			itemView.setLayoutParams(params);
			itemView.setImageDrawable(menu.getItem(i).getIcon());
			itemView.setBackgroundResource(R.drawable.bg_action_container_button);
			itemView.setTag(menu.getItem(i));
			itemView.setScaleType(ScaleType.CENTER_INSIDE);
			itemView.setAdjustViewBounds(true);
			itemView.setOnLongClickListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View v) {
					Toast t = Toast.makeText(getContext(),
							((MenuItem) v.getTag()).getTitle(),
							Toast.LENGTH_SHORT);
					// Position the toast near the item but not on it so that the user can see it appearing.
					t.setGravity(Gravity.TOP | Gravity.LEFT, v.getLeft()-50,
							v.getBottom() + 40);
					t.show();
					return true;
				}
			});
			itemView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					executeAction(((MenuItem) v.getTag()).getItemId());
				}
			});
			addView(itemView);
		}
	}

	/**
	 * Set the activity member that contains the multiselect actions.
	 */
	public void setFileManagerActivity(FileManagerActivity act) {
		this.act = act;
	}
}