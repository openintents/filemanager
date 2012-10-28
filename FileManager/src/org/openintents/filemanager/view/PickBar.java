package org.openintents.filemanager.view;

import org.openintents.filemanager.R;

import android.content.Context;
import android.os.Build.VERSION;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class PickBar extends LinearLayout {
	private EditText mEditText;
	private Button mButton;
	private OnPickRequestedListener mListener;
	
	public PickBar(Context context) {
		super(context);
		init();
	}
	public PickBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	private void init() {
		// Apply borderless style when applicable.
		if(VERSION.SDK_INT >= 11)
			mButton = new Button(getContext(), null, android.R.attr.buttonBarButtonStyle);
		else
			mButton = new Button(getContext());
		{
			mButton.setText(R.string.pick_button_default);
			mButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if(mListener!=null)
						mListener.pickRequested(mEditText.getText().toString());
				}
			});
		}

		// EditText
		mEditText = new EditText(getContext());
		{
			LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
			// Take up as much space as possible.
			layoutParams.weight = 1;

			mEditText.setLayoutParams(layoutParams);
			mEditText.setBackgroundResource(R.drawable.bg_navbar_textfield);
			mEditText.setHint(R.string.filename_hint);
			mEditText.setInputType(InputType.TYPE_TEXT_VARIATION_URI);
			mEditText.setImeOptions(EditorInfo.IME_ACTION_GO);
			mEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
						@Override
						public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
							if (actionId == EditorInfo.IME_ACTION_GO || (event.getAction() == KeyEvent.ACTION_DOWN && (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER || event.getKeyCode() == KeyEvent.KEYCODE_ENTER))) {
								if(mListener!=null)
									mListener.pickRequested(mEditText.getText().toString());
								return true;
							}

							return false;
						}
					});
		}
		
		addView(mEditText);
		addView(mButton);
	}

	public void setText(CharSequence name) {
		mEditText.setText(name);
	}
	
	public void setOnPickRequestedListener(OnPickRequestedListener listener) {
		mListener = listener;
	}
	
	public interface OnPickRequestedListener {
		public void pickRequested(String filename);
	}

	public void setButtonText(CharSequence text) {
		mButton.setText( (text == null || text.toString().trim().length() == 0) ? getResources().getString(R.string.pick_button_default) : text);
	}
}