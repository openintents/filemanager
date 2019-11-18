package org.openintents.filemanager.view;

import android.content.Context;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import org.openintents.filemanager.R;
import org.openintents.filemanager.view.PathBar.Mode;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * This class handles the displaying of children in {@link Mode.STANDARD_INPUT}, including choosing which children to display, how, and where. It automatically uses the {@link PathBar#mCurrentDirectory} field. <b>Note: </b> Never use this with
 * a width of WRAP_CONTENT.
 */
class PathButtonLayout extends LinearLayout implements OnLongClickListener {
    /**
     * <absolute path, R.drawable id of image to use>
     */
    public static Map<String, Integer> mPathDrawables = new HashMap<>();
    private PathBar mPathBar = null;

    public PathButtonLayout(Context context) {
        super(context);
        init();
    }

    public PathButtonLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public static Map<String, Integer> getPathDrawables() {
        return mPathDrawables;
    }

    private void init() {
        this.setOrientation(LinearLayout.HORIZONTAL);
        this.setOnLongClickListener(this);

        mPathDrawables.put(Environment.getExternalStorageDirectory().getAbsolutePath(), R.drawable.ic_navbar_sdcard);
        mPathDrawables.put("/sdcard", R.drawable.ic_navbar_sdcard);
        mPathDrawables.put("/mnt/sdcard", R.drawable.ic_navbar_sdcard);
        mPathDrawables.put("/mnt/sdcard-ext", R.drawable.ic_navbar_sdcard);
        mPathDrawables.put("/mnt/sdcard0", R.drawable.ic_navbar_sdcard);
        mPathDrawables.put("/mnt/sdcard1", R.drawable.ic_navbar_sdcard);
        mPathDrawables.put("/mnt/sdcard2", R.drawable.ic_navbar_sdcard);
        mPathDrawables.put("/storage/sdcard0", R.drawable.ic_navbar_sdcard);
        mPathDrawables.put("/storage/sdcard1", R.drawable.ic_navbar_sdcard);
        mPathDrawables.put("/storage/sdcard2", R.drawable.ic_navbar_sdcard);
        mPathDrawables.put("/", R.drawable.ic_navbar_home);
    }

    public void setNavigationBar(PathBar pathbar) {
        mPathBar = pathbar;
    }

    /**
     * Call to properly refresh this {@link PathButtonLayout}'s contents based on the fPath parameter.
     */
    public void refresh(File fPath) {
        // Reload buttons.
        this.removeAllViews();
        addPathButtons(fPath);

        // Redraw.
        invalidate();
    }

    /**
     * Adds the proper buttons according to the fPath parameter.
     */
    private void addPathButtons(File fPath) {
        StringBuilder cPath = new StringBuilder();
        char cChar;
        String path = fPath.getAbsolutePath();

        for (int i = 0; i < path.length(); i++) {
            cChar = path.charAt(i);
            cPath.append(cChar);

            if (cChar == '/' || i == path.length() - 1) { // if folder name ended, or path string ended but not if we 're on root
                // add a button
                this.addView(PathButtonFactory.newButton(cPath.toString(),
                        mPathBar));
            }
        }
    }

    /**
     * Provides a modified implementation of the layoutHorizontal() method of LinearLayout. Removes all children that don't fully fit in this {@link PathButtonLayout}.
     */
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final boolean isLayoutRtl = false;
        final int paddingTop = getPaddingTop();

        if (this.getChildCount() > 0)
            keepFittingChildren();

        int childTop;
        int childLeft;

        // Where bottom of child should go
        final int height = getBottom() - getTop();

        // Space available for child
        int childSpace = height - paddingTop - getPaddingBottom();

        final int count = getChildCount();

        childLeft = getPaddingLeft();

        int start = 0;
        int dir = 1;
        // In case of RTL, start drawing from the last child.
        if (isLayoutRtl) {
            start = count - 1;
            dir = -1;
        }

        for (int i = 0; i < count; i++) {
            int childIndex = start + dir * i;
            final View child = getChildAt(childIndex);

            if (child == null) {
                childLeft += 0;
            } else if (child.getVisibility() != GONE) {
                final int childWidth = child.getMeasuredWidth();
                final int childHeight = child.getMeasuredHeight();

                final LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) child
                        .getLayoutParams();

                childTop = paddingTop + ((childSpace - childHeight) / 2)
                        + lp.topMargin - lp.bottomMargin;

                childLeft += lp.leftMargin;
                setChildFrame(child, childLeft, // originally childLeft + getLocationOffset(child)
                        childTop, childWidth, childHeight);
                childLeft += childWidth + lp.rightMargin; // originally childLeft += childWidth + lp.rightMargin + getNextLocationOffset(child);

                i += 0; // originally getChildrenSkipCount(child, childIndex);
            }
        }
    }

    @Override
    protected void measureChildWithMargins(View child,
                                           int parentWidthMeasureSpec, int widthUsed,
                                           int parentHeightMeasureSpec, int heightUsed) {
        final MarginLayoutParams lp = (MarginLayoutParams) child
                .getLayoutParams();

        final int childWidthMeasureSpec = getChildMeasureSpec(
                parentWidthMeasureSpec, getPaddingLeft() + getPaddingRight()
                        + lp.leftMargin + lp.rightMargin, lp.width);
        final int childHeightMeasureSpec = getChildMeasureSpec(
                parentHeightMeasureSpec, getPaddingTop() + getPaddingBottom()
                        + lp.topMargin + lp.bottomMargin, lp.height);

        child.measure(childWidthMeasureSpec, childHeightMeasureSpec);

    }

    private void setChildFrame(View child, int left, int top, int width,
                               int height) {
        child.layout(left, top, left + width, top + height);
    }

    /**
     * Checks this {@link ViewGroup}'s children and keeps the ones that fit inside it.
     */
    private void keepFittingChildren() {
        View child;
        int childrenToDraw = 0;
        int sumWidth = 0;
        int index = this.getChildCount() - 1;

        do {
            child = this.getChildAt(index);
            sumWidth += child.getMeasuredWidth();
            childrenToDraw++;

            index--;
        } while (sumWidth <= this.getMeasuredWidth() && index >= 0);

        if (sumWidth > this.getMeasuredWidth()) { // if the view width has been passed
            // keep one child less
            childrenToDraw--;
        }

        int i;
        int childrenCount = this.getChildCount();
        for (i = 0; i < childrenCount - childrenToDraw; i++) {
            this.removeViewAt(0);
        }
    }

    /**
     * Add an icon to be shown instead of a the directory name.
     *
     * @param path               The path on which to display the icon.
     * @param drawableResourceId The icon' resource id.
     */
    public void addPathDrawable(String path, int drawableResourceId) {
        mPathDrawables.put(path, drawableResourceId);
    }

    @Override
    public boolean onLongClick(View v) {
        mPathBar.switchToManualInput();
        return true;
    }

    private static class PathButtonFactory {
        /**
         * Creates a Button or ImageButton according to the path. e.g. {@code if(file.getAbsolutePath() == '/')}, it should return an ImageButton with the home drawable on it.
         *
         * @param file   The directory this button will represent.
         * @param navbar The {@link PathBar} which will contain the created buttons.
         * @return An {@link ImageButton} or a {@link Button}.
         */
        private static View newButton(File file, final PathBar navbar) {
            View btn;

            if (mPathDrawables.containsKey(file.getAbsolutePath())) {
                btn = new ImageButton(navbar.getContext());
                ((ImageButton) btn).setImageResource(mPathDrawables.get(file.getAbsolutePath()));
            } else {
                btn = new Button(navbar.getContext());

                ((Button) btn).setText(file.getName());
                ((Button) btn).setMaxLines(1);
                ((Button) btn).setTextColor(navbar.getResources().getColor(R.color.navbar_details));
                ((Button) btn).setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            }

            android.widget.LinearLayout.LayoutParams params = new android.widget.LinearLayout.LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
            params.rightMargin = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 4, navbar.getResources()
                            .getDisplayMetrics());

            btn.setLayoutParams(params);
            btn.setTag(file);
            btn.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    navbar.cd((File) v.getTag());
                }
            });
            btn.setOnLongClickListener(navbar.getPathButtonLayout());
            btn.setBackgroundDrawable(navbar.getItemBackground());

            // We have to set this after adding the background as it'll cancel the padding out.
            if (btn instanceof Button) {
                int sidePadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, navbar.getContext().getResources().getDisplayMetrics());
                btn.setPadding(sidePadding, btn.getPaddingTop(), sidePadding, btn.getPaddingBottom());
            }

            return btn;
        }

        /**
         * @see {@link #newButton(File)}
         */
        private static View newButton(String path, PathBar navbar) {
            return newButton(new File(path), navbar);
        }
    }
}