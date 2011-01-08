/* 
 * Copyright (C) 2008 OpenIntents.org
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

package org.openintents.samples.TestFileManager;

import org.openintents.intents.FileManagerIntents;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

public class TestFileManager extends Activity {
	
	protected static final int REQUEST_CODE_PICK_FILE_OR_DIRECTORY = 1;

	protected EditText mEditText;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        mEditText = (EditText) findViewById(R.id.file_path);

        ImageButton buttonFileManager = (ImageButton) findViewById(R.id.file_manager);
        buttonFileManager.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View arg0) {
				openFile();
			}
        });
        
        Button button = (Button) findViewById(R.id.open);
        button.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View arg0) {
				openFile();
			}
        });

        button = (Button) findViewById(R.id.save);
        button.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View arg0) {
				saveFile();
			}
        });

        button = (Button) findViewById(R.id.pick_directory);
        button.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View arg0) {
				pickDirectory();
			}
        });
    }
    
    /**
     * Opens the file manager to select a file to open.
     */
    private void openFile() {
		String fileName = mEditText.getText().toString();
		
		Intent intent = new Intent(FileManagerIntents.ACTION_PICK_FILE);
		
		// Construct URI from file name.
		intent.setData(Uri.parse("file://" + fileName));
		
		// Set fancy title and button (optional)
		intent.putExtra(FileManagerIntents.EXTRA_TITLE, getString(R.string.open_title));
		intent.putExtra(FileManagerIntents.EXTRA_BUTTON_TEXT, getString(R.string.open_button));
		
		try {
			startActivityForResult(intent, REQUEST_CODE_PICK_FILE_OR_DIRECTORY);
		} catch (ActivityNotFoundException e) {
			// No compatible file manager was found.
			Toast.makeText(this, R.string.no_filemanager_installed, 
					Toast.LENGTH_SHORT).show();
		}
	}

    /**
     * Opens the file manager to select a location for saving a file.
     */
    private void saveFile() {
		String fileName = mEditText.getText().toString();
		
		Intent intent = new Intent(FileManagerIntents.ACTION_PICK_FILE);
		
		// Construct URI from file name.
		intent.setData(Uri.parse("file://" + fileName));
		
		// Set fancy title and button (optional)
		intent.putExtra(FileManagerIntents.EXTRA_TITLE, getString(R.string.save_title));
		intent.putExtra(FileManagerIntents.EXTRA_BUTTON_TEXT, getString(R.string.save_button));
		
		try {
			startActivityForResult(intent, REQUEST_CODE_PICK_FILE_OR_DIRECTORY);
		} catch (ActivityNotFoundException e) {
			// No compatible file manager was found.
			Toast.makeText(this, R.string.no_filemanager_installed, 
					Toast.LENGTH_SHORT).show();
		}
	}

    /**
     * Opens the file manager to pick a directory.
     */
    private void pickDirectory() {
		String fileName = mEditText.getText().toString();
		
		// Note the different intent: PICK_DIRECTORY
		Intent intent = new Intent(FileManagerIntents.ACTION_PICK_DIRECTORY);
		
		// Construct URI from file name.
		intent.setData(Uri.parse("file://" + fileName));
		
		// Set fancy title and button (optional)
		intent.putExtra(FileManagerIntents.EXTRA_TITLE, getString(R.string.pick_directory_title));
		intent.putExtra(FileManagerIntents.EXTRA_BUTTON_TEXT, getString(R.string.pick_directory_button));
		
		try {
			startActivityForResult(intent, REQUEST_CODE_PICK_FILE_OR_DIRECTORY);
		} catch (ActivityNotFoundException e) {
			// No compatible file manager was found.
			Toast.makeText(this, R.string.no_filemanager_installed, 
					Toast.LENGTH_SHORT).show();
		}
	}
    

    /**
     * This is called after the file manager finished.
     */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) {
		case REQUEST_CODE_PICK_FILE_OR_DIRECTORY:
			if (resultCode == RESULT_OK && data != null) {
				// obtain the filename
				String filename = data.getDataString();
				if (filename != null) {
					// Get rid of URI prefix:
					if (filename.startsWith("file://")) {
						filename = filename.substring(7);
					}
					
					mEditText.setText(filename);
				}				
				
			}
			break;
		}
	}
}