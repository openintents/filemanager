package org.openintents.filemanager.view;

import java.io.File;

import org.openintents.filemanager.R;

import android.content.Context;
import android.graphics.Color;
import android.os.Environment;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView.ScaleType;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

/**
 * Provides a self contained way to represent the current path and provides a handy way of navigating. </br></br>
 * 
 * <b>Note 1:</b> If you need to allow directory navigation outside of this class (e.g. when the user clicks on a folder from a {@link ListView}), use {@link #cd(File)} or {@link #cd(String)}. This is a requirement for the views of this class to
 * properly refresh themselves. <i>You will get notified through the usual {@link OnDirectoryChangedListener}. </i></br>
 * 
 * <b>Note 2:</b> To switch between {@link Mode Modes} use the {@link #switchToManualInput()} and {@link #switchToStandardInput()} methods!
 * 
 * @author George Venios
 */
public class PathBar extends ViewFlipper {
	private String TAG = this.getClass().getName();

	/**
	 * The available Modes of this PathBar. </br> See {@link PathBar#switchToManualInput() switchToManualInput()} and {@link PathBar#switchToStandardInput() switchToStandardInput()}.
	 */
	public enum Mode {
		/**
		 * The button path selection mode.
		 */
		STANDARD_INPUT,
		/**
		 * The text path input mode.
		 */
		MANUAL_INPUT
	}

	private File mCurrentDirectory = null;
	private Mode mCurrentMode = Mode.STANDARD_INPUT;
	private File mInitialDirectory = null;

	/** ImageButton used to switch to MANUAL_INPUT. */
	private ImageButton mSwitchToManualModeButton = null;
	/** Layout holding all path buttons. */
	private PathButtonLayout mPathButtons = null;
	/** Container of {@link #mPathButtons}. Allows horizontal scrolling. */
	private HorizontalScrollView mPathButtonsContainer = null;
	/** The EditText holding the path in MANUAL_INPUT. */
	private EditText mPathEditText = null;
	/** The ImageButton to confirm the manually entered path. */
	private ImageButton mGoButton = null;

	private OnDirectoryChangedListener mDirectoryChangedListener = new OnDirectoryChangedListener() {
		@Override
		public void directoryChanged(File newCurrentDir) {
		}
	};

	public PathBar(Context context) {
		super(context);
		init();
	}

	public PathBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		mCurrentDirectory = Environment.getExternalStorageDirectory();
		mInitialDirectory = Environment.getExternalStorageDirectory();

		this.setBackgroundResource(R.drawable.bg_pathbar);
		this.setInAnimation(getContext(), R.anim.fade_in);
		this.setOutAnimation(getContext(), R.anim.fade_out);

		// RelativeLayout1
		RelativeLayout standardModeLayout = new RelativeLayout(getContext());
		{ // I use a block here so that layoutParams can be used as a variable name further down.
			android.widget.ViewFlipper.LayoutParams layoutParams = new android.widget.ViewFlipper.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
			standardModeLayout.setLayoutParams(layoutParams);

			this.addView(standardModeLayout);
		}

		// ImageButton -- GONE. Kept this code in case we need to use an right-aligned button in the future.
		mSwitchToManualModeButton = new ImageButton(getContext());
		{
			android.widget.RelativeLayout.LayoutParams layoutParams = new android.widget.RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
			layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

			mSwitchToManualModeButton.setLayoutParams(layoutParams);
			mSwitchToManualModeButton.setId(10);
			mSwitchToManualModeButton
					.setImageResource(R.drawable.ic_navbar_edit);
			mSwitchToManualModeButton
					.setBackgroundResource(R.drawable.bg_navbar_btn_right);
			mSwitchToManualModeButton
					.setVisibility(View.GONE);

			standardModeLayout.addView(mSwitchToManualModeButton);
		}

		// ImageButton -- GONE. Kept this code in case we need to use an left-aligned button in the future.
		ImageButton cdToRootButton = new ImageButton(getContext());
		{
			android.widget.RelativeLayout.LayoutParams layoutParams = new android.widget.RelativeLayout.LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
			layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);

			cdToRootButton.setLayoutParams(layoutParams);
			cdToRootButton.setId(11);
			cdToRootButton
					.setBackgroundResource(R.drawable.bg_navbar_btn_standard);
			cdToRootButton.setImageResource(R.drawable.ic_navbar_home);
			cdToRootButton.setScaleType(ScaleType.CENTER_INSIDE);
			cdToRootButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					cd("/");
				}
			});
			cdToRootButton.setVisibility(View.GONE);

			standardModeLayout.addView(cdToRootButton);
		}

		// Horizontal ScrollView container
		mPathButtonsContainer = new HorizontalScrollView(getContext());
		{
			android.widget.RelativeLayout.LayoutParams layoutParams = new android.widget.RelativeLayout.LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
			layoutParams.addRule(RelativeLayout.LEFT_OF,
					mSwitchToManualModeButton.getId());
			layoutParams.addRule(RelativeLayout.RIGHT_OF,
					cdToRootButton.getId());
			layoutParams.alignWithParent = true;

			mPathButtonsContainer.setLayoutParams(layoutParams);
			mPathButtonsContainer.setHorizontalScrollBarEnabled(false);
			mPathButtonsContainer.setHorizontalFadingEdgeEnabled(true);

			standardModeLayout.addView(mPathButtonsContainer);
		}

		// PathButtonLayout
		mPathButtons = new PathButtonLayout(getContext());
		{
			android.widget.LinearLayout.LayoutParams layoutParams = new android.widget.LinearLayout.LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);

			mPathButtons.setLayoutParams(layoutParams);
			mPathButtons.setNavigationBar(this);

			mPathButtonsContainer.addView(mPathButtons);
		}

		// RelativeLayout2
		RelativeLayout manualModeLayout = new RelativeLayout(getContext());
		{
			android.widget.ViewFlipper.LayoutParams layoutParams = new android.widget.ViewFlipper.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
			manualModeLayout.setLayoutParams(layoutParams);

			this.addView(manualModeLayout);
		}

		// ImageButton
		mGoButton = new ImageButton(getContext());
		{
			android.widget.RelativeLayout.LayoutParams layoutParams = new android.widget.RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
			layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

			mGoButton.setLayoutParams(layoutParams);
			mGoButton.setId(20);
			mGoButton.setBackgroundResource(R.drawable.bg_navbar_btn_right);
			mGoButton.setImageResource(R.drawable.ic_navbar_accept);
			mGoButton.setScaleType(ScaleType.CENTER_INSIDE);
			mGoButton.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					manualInputCd(mPathEditText.getText().toString());
				}
			});

			manualModeLayout.addView(mGoButton);
		}

		// EditText
		mPathEditText = new EditText(getContext());
		{
			android.widget.RelativeLayout.LayoutParams layoutParams = new android.widget.RelativeLayout.LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
			layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
			layoutParams.addRule(RelativeLayout.LEFT_OF, mGoButton.getId());

			mPathEditText.setLayoutParams(layoutParams);
			mPathEditText.setBackgroundResource(R.drawable.bg_navbar_textfield);
			mPathEditText.setTextColor(Color.BLACK);
			mPathEditText.setInputType(InputType.TYPE_TEXT_VARIATION_URI);
			mPathEditText.setImeOptions(EditorInfo.IME_ACTION_GO);
			mPathEditText
					.setOnEditorActionListener(new TextView.OnEditorActionListener() {
						@Override
						public boolean onEditorAction(TextView v, int actionId,
								KeyEvent event) {
							if (actionId == EditorInfo.IME_ACTION_GO
									|| (event.getAction() == KeyEvent.ACTION_UP && (event
											.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER || event
											.getKeyCode() == KeyEvent.KEYCODE_ENTER))) {
								if (manualInputCd(v.getText().toString()))
									// Since we have successfully navigated.
									return true;
							}

							return false;
						}
					});

			manualModeLayout.addView(mPathEditText);
		}

	}

	/**
	 * Sets the directory the parent activity showed first so that back behavior is fixed.
	 * 
	 * @param initDir
	 *            The directory.
	 */
	public void setInitialDirectory(File initDir) {
		mInitialDirectory = initDir;
		cd(initDir);
	}

	/**
	 * See {@link #setInitialDirectory(File)}.
	 */
	public void setInitialDirectory(String initPath) {
		setInitialDirectory(new File(initPath));
	}

	/**
	 * @see #setInitialDirectory(File)
	 * @return The initial directory.
	 */
	public File getInitialDirectory() {
		return mInitialDirectory;
	}

	/**
	 * Get the currently active directory.
	 * 
	 * @return A {@link File} representing the currently active directory.
	 */
	public File getCurrentDirectory() {
		return mCurrentDirectory;
	}

	/**
	 * Use instead of {@link #cd(String)} when in {@link Mode#MANUAL_INPUT}.
	 * 
	 * @param path
	 *            The path to cd() to.
	 * @return true if the cd succeeded.
	 */
	private boolean manualInputCd(String path) {
		if (!cd(path)) {
			Log.w(TAG, "Input path does not exist or is not a folder!");
			return false;
		} else {
			// if cd() successful, hide the keyboard
			InputMethodManager imm = (InputMethodManager) getContext()
					.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(getWindowToken(), 0);
			switchToStandardInput();
			return true;
		}
	}

	/**
	 * {@code cd} to the passed file. If the file is legal input, sets it as the currently active Directory. Otherwise does nothing.
	 * 
	 * @param file
	 *            The file to {@code cd} to.
	 * @return Whether the path entered exists and can be navigated to.
	 */
	public boolean cd(File file) {
		// Check file state.
		boolean isFileOK = true;
		isFileOK &= file.exists();
		isFileOK &= file.isDirectory();
		if (!isFileOK)
			return false;

		// Set proper current directory.
		mCurrentDirectory = file;

		// Refresh button layout.
		mPathButtons.refresh(mCurrentDirectory);

		// Reset scrolling position. http://stackoverflow.com/questions/3263259/scrollview-scrollto-not-working-saving-scrollview-position-on-rotation
		mPathButtonsContainer.post(new Runnable() {
			@Override
			public void run() {
				mPathButtonsContainer.scrollTo(
						mPathButtonsContainer.getMaxScrollAmount(),
						(int) mPathButtonsContainer.getTop());
			}
		});

		// Refresh manual input field.
		mPathEditText.setText(file.getAbsolutePath());

		mDirectoryChangedListener.directoryChanged(file);

		return true;
	}

	/**
	 * @see {@link org.openintents.filemanager.view.PathBar#cd(File) cd(File)}
	 * @param path
	 *            The path of the Directory to {@code cd} to.
	 * @return Whether the path entered exists and can be navigated to.
	 */
	public boolean cd(String path) {
		return cd(new File(path));
	}

	/**
	 * The same as running {@code File.listFiles()} on the currently active Directory.
	 */
	public File[] ls() {
		return mCurrentDirectory.listFiles();
	}

	public void setOnDirectoryChangedListener(
			OnDirectoryChangedListener listener) {
		if (listener != null)
			mDirectoryChangedListener = listener;
		else
			mDirectoryChangedListener = new OnDirectoryChangedListener() {
				@Override
				public void directoryChanged(File newCurrentDir) {
				}
			};
	}

	/**
	 * Switches to {@link Mode#MANUAL_INPUT}.
	 */
	public void switchToManualInput() {
		setDisplayedChild(1);
		mCurrentMode = Mode.MANUAL_INPUT;
	}

	/**
	 * Switches to {@link Mode#STANDARD_INPUT}.
	 */
	public void switchToStandardInput() {
		setDisplayedChild(0);
		mCurrentMode = Mode.STANDARD_INPUT;
	}

	/**
	 * Activities containing this bar, will have to call this method when the back button is pressed to provide correct backstack redirection and mode switching.
	 * 
	 * @return Whether this view consumed the event.
	 */
	public boolean pressBack() {
		// Switch mode.
		if (mCurrentMode == Mode.MANUAL_INPUT) {
			switchToStandardInput();
		}
		// Go back.
		else if (mCurrentMode == Mode.STANDARD_INPUT) {
			if (!backWillExit(mCurrentDirectory.getAbsolutePath())) {
				cd(mCurrentDirectory.getParent());
				return true;
			} else
				return false;
		}

		return true;
	}

	/**
	 * Returns the current {@link PathBar.Mode}.
	 * 
	 */
	public Mode getMode() {
		return mCurrentMode;
	}

	/**
	 * 
	 * @param dirPath The current directory's absolute path.
	 * @return
	 */
	private boolean backWillExit(String dirPath) {
		// Count tree depths
		String[] dir = dirPath.split("/");
		int dirTreeDepth = dir.length;

		String[] init = mInitialDirectory.getAbsolutePath().split("/");
		int initTreeDepth = init.length;

		// analyze and return
		if (dirTreeDepth > initTreeDepth) {
			return false;
		} else if (dirTreeDepth < initTreeDepth) {
			return true;
		} else {
			if (dirPath.equals(mInitialDirectory.getAbsolutePath())) {
				return true;
			} else {
				return false;
			}
		}
	}

	/**
	 * Interface notifying users of this class when the user has chosen to navigate elsewhere.
	 */
	public interface OnDirectoryChangedListener {
		public void directoryChanged(File newCurrentDir);
	}

	public PathButtonLayout getPathButtonLayout() {
		return mPathButtons;
	}
	
	
}