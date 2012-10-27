package org.openintents.filemanager.view;

import org.openintents.filemanager.R;

import android.content.Context;
import android.graphics.Color;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class SaveAsBar extends RelativeLayout {
	private EditText mEditText;
	private onSaveRequestedListener mListener;
	
	public SaveAsBar(Context context) {
		super(context);
		init();
	}
	public SaveAsBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	public SaveAsBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}
	
	private void init() {
		// ImageButton
		ImageButton mSaveButton = new ImageButton(getContext());
		{
			android.widget.RelativeLayout.LayoutParams layoutParams = new android.widget.RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
			layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

			mSaveButton.setLayoutParams(layoutParams);
			mSaveButton.setId(50);
			mSaveButton.setBackgroundResource(R.drawable.bg_navbar_btn);
			mSaveButton.setImageResource(R.drawable.ic_action_save);
			mSaveButton.setScaleType(ScaleType.CENTER_INSIDE);
			mSaveButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if(mListener!=null)
						mListener.saveRequested(mEditText.getText().toString());
				}
			});

			addView(mSaveButton);
		}

		// EditText
		mEditText = new EditText(getContext());
		{
			android.widget.RelativeLayout.LayoutParams layoutParams = new android.widget.RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
			layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
			layoutParams.alignWithParent = true;
			layoutParams.addRule(RelativeLayout.LEFT_OF, mSaveButton.getId());

			mEditText.setLayoutParams(layoutParams);
			mEditText.setBackgroundResource(R.drawable.bg_navbar_textfield);
			mEditText.setTextColor(Color.BLACK);
			mEditText.setHint(R.string.saveas_hint);
			mEditText.setInputType(InputType.TYPE_TEXT_VARIATION_URI);
			mEditText.setImeOptions(EditorInfo.IME_ACTION_GO);
			mEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
						@Override
						public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
							if (actionId == EditorInfo.IME_ACTION_GO || (event.getAction() == KeyEvent.ACTION_DOWN && (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER || event.getKeyCode() == KeyEvent.KEYCODE_ENTER))) {
								if(mListener!=null)
									mListener.saveRequested(mEditText.getText().toString());
								return true;
							}

							return false;
						}
					});

			addView(mEditText);
		}
	}

	public void setText(CharSequence name) {
		mEditText.setText(name);
	}
	
	public void setOnSaveRequestedListener(onSaveRequestedListener listener) {
		mListener = listener;
	}
	
	public interface onSaveRequestedListener {
		public void saveRequested(String filename);
	}
}