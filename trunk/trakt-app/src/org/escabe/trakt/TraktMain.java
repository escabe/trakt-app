package org.escabe.trakt;

import java.io.File;

import com.commonsware.cwac.cache.AsyncCache;
import com.commonsware.cwac.cache.SimpleWebImageCache;
import com.commonsware.cwac.thumbnail.ThumbnailBus;
import com.commonsware.cwac.thumbnail.ThumbnailMessage;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
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
		
		if (user==null || password==null) {
			startActivity(new Intent(this, TraktPrefs.class));
		}
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
	
    public void buttonTrendingMoviesOnClick(View view) {
    	startActivity(new Intent(this,TraktList.class));
    }
}