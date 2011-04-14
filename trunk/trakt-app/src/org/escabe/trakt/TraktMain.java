package org.escabe.trakt;
/**
 * Main menu Activity
 */

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

public class TraktMain extends Activity {
    private SharedPreferences prefs=null;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.trakt_main);
        
        prefs=PreferenceManager.getDefaultSharedPreferences(this);
		
		String user=prefs.getString("user", null);
		String password=prefs.getString("password", null);

		// If username and passsword are not defined show preferences screen 
		if (user==null || password==null) {
			startActivity(new Intent(this, TraktPrefs.class));
		}
		// TODO Check if login details are actually correct
    }
    

    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.menu_main, menu);
	    return true;
	}
    
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	    case R.id.menuSettings:
	    	startActivity(new Intent(this, TraktPrefs.class));
	    	return true;	    	
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}

	/**
	 * Handles all buttons
	 * @param view
	 */
    public void buttonTrendingClick(View view) {
    	//Based on which button was pressed call other acitivities
    	switch (view.getId()) {
    	// The following buttons lead to showing the TraktList activity
    	case R.id.buttonTrendingMovies:
    		startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("trakt://movies/trending"),this,TraktList.class));
    		break;
    	case R.id.buttonTrendingShows:
    		startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("trakt://shows/trending"),this,TraktList.class));
    		break;
    	case R.id.buttonUserMovies:
    		startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("trakt://user/library/movies/all"),this,TraktList.class));
    		break;
    	case R.id.buttonUserShows:
    		startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("trakt://user/library/shows/all"),this,TraktList.class));
    		break;

    	}
    }
}