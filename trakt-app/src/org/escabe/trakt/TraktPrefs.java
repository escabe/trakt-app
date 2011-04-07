package org.escabe.trakt;
import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Show preferences based on preferences XML-file.
 * @author escabe
 *
 */
public class TraktPrefs extends PreferenceActivity {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    
		addPreferencesFromResource(R.xml.preferences);
	    
	}
}
