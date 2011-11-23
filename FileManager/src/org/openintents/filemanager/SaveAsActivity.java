package org.openintents.filemanager;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.openintents.intents.FileManagerIntents;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class SaveAsActivity extends Activity {
	protected static final int REQUEST_CODE_PICK_FILE_OR_DIRECTORY = 1;
	private Uri source;
	//Whether the scheme is file: (otherwise it's content:)
	private boolean fileScheme = false;
	
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //This activity is never shown to the user.
        setContentView(new RelativeLayout(this));
        Intent receivedIntent = getIntent();
        if(receivedIntent != null){
        	Uri uri = receivedIntent.getData();
        	source = uri;
        	if(uri.getScheme().equals("file"))
        		processFile(uri);
        	else if(uri.getScheme().equals("content"))
        		processContent(uri);
        }
        else{
			Toast.makeText(this, R.string.saveas_no_file_picked, Toast.LENGTH_SHORT).show();
        }
    }
	
	private void startPickActivity(Intent intent){
		try {
			startActivityForResult(intent, REQUEST_CODE_PICK_FILE_OR_DIRECTORY);
		} catch (ActivityNotFoundException e) {
			//Should never happen, but Java requires this catch
			Toast.makeText(this, R.string.saveas_error, Toast.LENGTH_SHORT).show();
		}
	}
	
	private Intent createPickIntent(){
		return new Intent(FileManagerIntents.ACTION_PICK_FILE);
	}
	
	private void processFile(Uri uri){
		fileScheme = true;
		Intent intent = createPickIntent();
		intent.setData(uri);
		startPickActivity(intent);
	}
	
	private void processContent(Uri uri){
		fileScheme = false;
		String name = getPath(uri);
		Intent intent = createPickIntent();
		intent.setData(Uri.parse(name));
		startPickActivity(intent);
	}
	
	/*
	 * Get the default path and filename for the saved file from content: scheme.
	 * As the directory is always used the SD storage.
	 * For GMail, the filename is the _display_name in its ContentProvider. Otherwise the file has
	 * no name.
	 * !IMPORTANT! When you add another "special" intent-filter like the one for GMail, consider,
	 * if you could add also the code for finding out the filename.
	 */
	private String getPath(Uri uri){
		Uri sd = Uri.fromFile(Environment.getExternalStorageDirectory());
		if(uri.getHost().equals("gmail-ls")){
			Cursor cur = managedQuery(uri, new String[]{"_display_name"}, null, null, null);
			int nameColumn = cur.getColumnIndex("_display_name"); 
			if(cur.moveToFirst()){
				return sd.buildUpon().appendPath(cur.getString(nameColumn)).toString();
			}
		}
		return sd.getPath();
	}
    
    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		switch (requestCode) {
		case REQUEST_CODE_PICK_FILE_OR_DIRECTORY:
			if (resultCode == RESULT_OK && data != null) {
				Uri destinationUri = data.getData();
				if (destinationUri != null && source != null) {
					String destinationPath = destinationUri.getPath();
					saveFile(new File(destinationPath));
				}
			}
			break;
		}
		finish(); //End the activity
	}
    
    private void saveFile(File destination){
		InputStream in = null;
		OutputStream out = null;
		try {
			if(fileScheme)
				in = new BufferedInputStream(new FileInputStream(source.getPath()));
			else
				in = new BufferedInputStream(getContentResolver().openInputStream(source));
			
			out = new BufferedOutputStream(new FileOutputStream(destination));
	        byte[] buffer = new byte[1024];
	        
	        while(in.read(buffer) != -1)
	        	out.write(buffer);
			Toast.makeText(this, R.string.saveas_file_saved, Toast.LENGTH_SHORT).show();
		} catch(FileNotFoundException e){
			//Should never get here
			Toast.makeText(this, R.string.saveas_error, Toast.LENGTH_SHORT).show();
		} catch(IOException e){
			Toast.makeText(this, R.string.saveas_error, Toast.LENGTH_SHORT).show();
		}
		finally{
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {}
			}
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {}
			}
			
		}
    }
}
