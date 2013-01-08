package org.openintents.filemanager;

import android.content.Context; 
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable; 
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView; 
import android.widget.LinearLayout; 
import android.widget.TextView; 

public class IconifiedTextView extends LinearLayout { 
      
    private TextView mText; 
    private TextView mInfo; 
     private ImageView mIcon; 
     private ImageView mCheckIcon;
      
     public IconifiedTextView(Context context, final IconifiedText aIconifiedText) { 
          super(context); 

		// inflate rating
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		inflater.inflate(
				R.layout.filelist_item, this, true);

		mIcon = (ImageView) findViewById(R.id.icon);
		mText = (TextView) findViewById(R.id.text);
		mInfo = (TextView) findViewById(R.id.info);
		mCheckIcon = (ImageView) findViewById(R.id.select_icon);
     } 

     public ImageView getImageView() {
    	 return mIcon;
     }
     
     public void setText(String words) { 
         mText.setText(words); 

         int height = getHeight();
         
         if (height > 0) {
        	 ThumbnailLoader.setThumbnailHeight(height);
         }
    } 
     
     public void setInfo(String info) { 
         mInfo.setText(info);
    } 
     
     public void setIcon(Drawable bullet) { 
          mIcon.setImageDrawable(bullet); 
     }
     
    public void setIcon(Bitmap bitmap) {
    	mIcon.setImageBitmap(bitmap);
    }

	public void setCheckVisible(boolean visible) {
		mCheckIcon.setVisibility((visible) ? View.VISIBLE : View.GONE);
	}

	public void setCheckDrawable(Drawable icon) {
		mCheckIcon.setImageDrawable(icon);
	}
}