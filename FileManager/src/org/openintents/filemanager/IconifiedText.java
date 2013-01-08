package org.openintents.filemanager;

/* 
 * Copyright 2007 Steven Osborn 
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

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable; 
import android.os.Parcel;
import android.os.Parcelable;

/** @author Steven Osborn - http://steven.bitsetters.com 
 *  @author George Venios - implemented Parcelable */ 
public class IconifiedText implements Comparable<IconifiedText>, Parcelable{ 
    
     private String mText = ""; 
     private String mInfo = "";
     private Drawable mIcon; 
     private boolean mSelectable = true; 
     private boolean mSelected; 
     private boolean mCheckBoxVisible;

     public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
         public IconifiedText createFromParcel(Parcel in) {
             return new IconifiedText(in);
         }
  
         public IconifiedText[] newArray(int size) {
             return new IconifiedText[size];
         }
     };
     
     public IconifiedText(String text, String info, Drawable bullet) { 
          mIcon = bullet; 
          mText = text; 
          mInfo = info;
     }      
     
     public IconifiedText(Parcel in){
    	 mText = in.readString();
    	 mInfo = in.readString();
    	 mSelectable = in.readInt()==1?true:false;
    	 mSelected = in.readInt()==1?true:false;
    	 mCheckBoxVisible = in.readInt()==1?true:false;
     }
      
     public boolean isSelected() {
     	return mSelected;
     }

 	public void setSelected(boolean selected) {
     	this.mSelected = selected;
     }

 	public boolean isSelectable() { 
          return mSelectable; 
     } 
      
     public void setSelectable(boolean selectable) { 
          mSelectable = selectable; 
     } 
      
     public String getText() { 
         return mText; 
    } 
     
    public void setText(String text) { 
         mText = text; 
    } 
     
    public String getInfo() { 
        return mInfo; 
   } 
    
   public void setInfo(String info) { 
        mInfo = info; 
   } 
    
     public void setIcon(Drawable icon) { 
          mIcon = icon; 
     } 
     
     public void setIcon(Bitmap bitmap) {
    	 mIcon = (new BitmapDrawable(bitmap));
     }
      
     public Drawable getIcon() { 
          return mIcon; 
     } 
     
     // Used by the adapter
     public Object getIconBitmap() {
		if(mIcon instanceof BitmapDrawable){
			return ((BitmapDrawable) mIcon).getBitmap();
		}

		return mIcon;
     }

     /** Make IconifiedText comparable by its name */ 
     
     public int compareTo(IconifiedText other) { 
          if(this.mText != null) 
               return this.mText.compareTo(other.getText()); 
          else 
               throw new IllegalArgumentException(); 
     } 
     
     public void setCheckIconVisible(boolean visible) {
    	 mCheckBoxVisible = visible;
     }
     
     public boolean isCheckIconVisible() {
    	 return mCheckBoxVisible;
     }

	@Override
	public int describeContents() {
		// Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(mText);
		dest.writeString(mInfo);
		dest.writeInt(mSelectable?1:0);
		dest.writeInt(mSelected?1:0);
		dest.writeInt(mCheckBoxVisible?1:0);
	}
} 