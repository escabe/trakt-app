package org.escabe.trakt;
/**
 * Main menu Activity
 */

import org.escabe.trakt.TraktAPI.ShowMovie;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class TraktMain extends Activity {
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.trakt_main);

        //Check whether login details are correct
        checkLogin();
    
    }

    private void showRecommendations() {
        PosterView pv = (PosterView)findViewById(R.id.posterviewRecomShows);
		pv.initPosterView(this, "recommendations/shows/%k",ShowMovie.Show);
		
		pv = (PosterView)findViewById(R.id.posterviewRecomMovies);
		pv.initPosterView(this, "recommendations/movies/%k",ShowMovie.Movie);
    }
		    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // If returning from configuration
        if (requestCode==32) {
        	// Check whether login is correct now
        	checkLogin();
		}
    }

    private void checkLogin() {
        TraktAPI traktapi = new TraktAPI(this);
        if (!traktapi.LoggedIn()) {
        	Toast.makeText(this, "Authentication Failed! Please check login details.", Toast.LENGTH_SHORT).show();
        	startActivityForResult(new Intent(this, TraktPrefs.class),32);
        } else {
        	showRecommendations();
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
	    	startActivityForResult(new Intent(this, TraktPrefs.class),32);
	    	return true;	    	
	    case R.id.menuSearch:
	    	onSearchRequested();
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
    		startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("trakt://trending/movies/trending.json/%25k"),this,TraktList.class));
    		break;
    	case R.id.buttonTrendingShows:
    		startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("trakt://trending/shows/trending.json/%25k"),this,TraktList.class));
    		break;
    	case R.id.buttonUserMovies:
    		startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("trakt://self/user/library/movies/all.json/%25k/%25u"),this,TraktList.class));
    		break;
    	case R.id.buttonUserShows:
    		startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("trakt://self/user/library/shows/all.json/%25k/%25u"),this,TraktList.class));
    		break;
    	case R.id.buttonWatchlist:
    		startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("trakt://self/watchlist"),this,TraktWatchList.class));
    		break;    		
    	case R.id.buttonSearch:
    		onSearchRequested();
    		break;
    	}
    }
}