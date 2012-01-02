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

import android.graphics.drawable.Drawable; 

/** @author Steven Osborn - http://steven.bitsetters.com */ 
public class IconifiedText implements Comparable<IconifiedText>{ 
    
     private String mText = ""; 
     private String mInfo = "";
     private Drawable mIcon; 
     private boolean mSelectable = true; 
     private boolean mSelected; 
     private boolean mCheckBoxVisible;

     public IconifiedText(String text, String info, Drawable bullet) { 
          mIcon = bullet; 
          mText = text; 
          mInfo = info;
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
      
     public Drawable getIcon() { 
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
} 

