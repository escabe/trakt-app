package org.escabe.trakt;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;

public class TraktEpisodeDetails extends Activity {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.trakt_episode_details);
	    
	    HandleIntent(getIntent());
	}

	private void HandleIntent(Intent intent) {
    	// Will only support viewing information
		if (intent.getAction().equals(Intent.ACTION_VIEW)) {
    		HandleUri(intent.getData());
    	} 
		// Consider implementing when called incorrectly, but is currently only accessible internally anyway
    
    }
    
	/**
	 * Parse Intent URI to determine for which Movie/Show display details
	 * @param uri	URI from Intent 
	 */
    private void HandleUri(Uri uri) {
		if (uri.getScheme().equals("episode")) {
			Intent intent = getIntent();
			((TextView)findViewById(R.id.textEpisodeDetailsTitle)).setText(intent.getStringExtra("title"));
			((TextView)findViewById(R.id.textEpisodeDetailsOverview)).setText(intent.getStringExtra("overview"));
			
		}
    }
	
}
