package org.openintents.distribution;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Displays the Eula for the first time, reading it from a raw resource.
 * 
 * @version 2009-01-17
 * @author Peli
 *
 */
public class EulaActivity extends Activity {

	/** TAG for log messages. */
	private static final String TAG = "EulaActivity";
	
	static final String PREFERENCES_EULA_ACCEPTED = "eula_accepted";
	
	/**
	 * Extra for main intent.
	 * Specifies activity that should be launched after Eula has been accepted.
	 */
	private static final String EXTRA_LAUNCH_ACTIVITY_PACKAGE = "org.openintents.extra.launch_activity_package";
	private static final String EXTRA_LAUNCH_ACTIVITY_CLASS = "org.openintents.extra.launch_activity_class";
	
	private Button mAgree;
	private Button mDisagree;
	
	private String mLaunchPackage;
	private String mLaunchClass;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		
		setContentView(RD.layout.eula);
		
		// Extras are provided by checkEula() below.
		Intent i = getIntent();
		Bundle b = i.getExtras();
		mLaunchPackage = b.getString(EXTRA_LAUNCH_ACTIVITY_PACKAGE);
		mLaunchClass = b.getString(EXTRA_LAUNCH_ACTIVITY_CLASS);
		
		//mIntroContinue = (Button) findViewById(R.id.intro_continue);
		mAgree = (Button) findViewById(RD.id.button1);
		mAgree.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				acceptEula();
			}
		});
		
		mDisagree = (Button) findViewById(RD.id.button2);
		mDisagree.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				refuseEula();
			}
		});
		
		TextView text = (TextView) findViewById(RD.id.text);
		text.setText(readLicenseFromRawResource(RD.raw.license_short));
		
	}
	
	/**
	 * Accept EULA and proceed with main application.
	 */
	public void acceptEula() {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor e = sp.edit();
		e.putBoolean(PREFERENCES_EULA_ACCEPTED, true);
		e.commit();
		
		// Call the activity that originally called checkEula()
		Intent i = new Intent();
		i.setClassName(mLaunchPackage, mLaunchClass);
		startActivity(i);
		finish();
	}
	
	/**
	 * Refuse EULA.
	 */
	public void refuseEula() {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor e = sp.edit();
		e.putBoolean(PREFERENCES_EULA_ACCEPTED, false);
		e.commit();
		
		finish();
	}

	/**
	 * Test whether EULA has been accepted. Otherwise display EULA.
	 * 
	 * @return True if Eula has been accepted.
	 */
	public static boolean checkEula(Activity activity) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(activity);
		boolean accepted = sp.getBoolean(PREFERENCES_EULA_ACCEPTED, false);
		
		if (accepted) {
			Log.i(TAG, "Eula has been accepted.");
			return true;
		} else {
			Log.i(TAG, "Eula has not been accepted yet.");
			
			// Launch Eula activity
			Intent i = new Intent(activity, EulaActivity.class);
			ComponentName ci = activity.getComponentName();
			
			// Specify in intent extras which activity should be called
			// after Eula has been accepted.
			Log.d(TAG, "Local package name: " + ci.getPackageName());
			Log.d(TAG, "Local class name: " + ci.getClassName());
			i.putExtra(EXTRA_LAUNCH_ACTIVITY_PACKAGE, ci.getPackageName());
			i.putExtra(EXTRA_LAUNCH_ACTIVITY_CLASS, ci.getClassName());
			activity.startActivity(i);
			activity.finish();
			return false;
		}
	}
	
	/**
	 * Read license from raw resource.
	 * @param resourceid ID of the raw resource.
	 * @return
	 */
	private String readLicenseFromRawResource(int resourceid) {

		// Retrieve license from resource:
		String license = "";
		Resources resources = getResources();
    		
		//Read in the license file as a big String
		BufferedReader in
		   = new BufferedReader(new InputStreamReader(
				resources.openRawResource(resourceid)));
		String line;
		StringBuilder sb = new StringBuilder();
		try {
			while ((line = in.readLine()) != null) { // Read line per line.
				if (TextUtils.isEmpty(line)) {
					// Empty line: Leave line break
					sb.append("\n\n");
				} else {
					sb.append(line);
					sb.append(" ");
				}
			}
			license = sb.toString();
		} catch (IOException e) {
			//Should not happen.
			e.printStackTrace();
		}
		
    	
    	return license;
	}
}
