package org.openintents.filemanager.view;

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

import org.openintents.filemanager.R;

public class LegacyActionContainer extends LinearLayout {
    private Menu menu = null;
    private OnActionSelectedListener mListener;

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
        setGravity(Gravity.CENTER_VERTICAL);
    }

    /**
     * Set the menu resource which contains the actions this view will display.
     *
     * @param menuRes The menu resource id.
     */
    public void setMenuResource(int menuRes) {
        removeAllViews();
        this.menu = new MenuBuilder(getContext());
        MenuInflater inflater = new MenuInflater(getContext());
        inflater.inflate(menuRes, menu);
        loadChildViews();
    }

    /**
     * Add the {@link #menu} members as children in the view. Each view will have the corresponding MenuItem as its tag.
     */
    private void loadChildViews() {
        ImageButton itemView;
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
                    t.setGravity(Gravity.TOP | Gravity.START, v.getLeft() - 50,
                            v.getBottom() + 40);
                    t.show();
                    return true;
                }
            });
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null)
                        mListener.actionSelected((MenuItem) v.getTag());
                }
            });
            addView(itemView);
        }
    }

    /**
     * Set the activity member that contains the multiselect actions.
     */
    public void setOnActionSelectedListener(OnActionSelectedListener listener) {
        mListener = listener;
    }

    public interface OnActionSelectedListener {
        void actionSelected(MenuItem item);
    }
}