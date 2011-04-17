package org.escabe.trakt;

import java.util.Date;
import java.util.HashMap;

import org.escabe.trakt.TraktAPI.MarkMode;
import org.escabe.trakt.TraktAPI.ShowMovie;
import org.json.JSONObject;

import com.commonsware.cwac.cache.WebImageCache;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
/**
 * Activity to show details about a Movie
 * @author escabe
 *
 */
public class TraktDetails extends ActivityWithUpdate {
	/* Currently displays info for a Movie or Show, instead of Show might want to use this only for Episode and create
	 * an ListActivity which displays episode list when selecting a Show
	 */
	
	private ProgressDialog progressdialog;
	private Runnable getdata;
	private TraktAPI traktapi;
	
	private ShowMovie showmovie;
	private String id;
	private WebImageCache cache = null;

	private HashMap<String,LovedHatedWatched> lovedhatedwatched=null;
	
	// Holds the data for current Movie/Show in JSON form
	JSONObject data;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.trakt_details);

	    // Initialize variables
		cache = ((Application)getApplication()).getCache();
		traktapi = new TraktAPI(this);
		// Check what to do
		HandleIntent(getIntent());
	}
	
	@Override
	public void onStart() {
		super.onStart();
	}
	
	public BroadcastReceiver broadcastreceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			
		}
	};
	
	/**
	 * Check with which Intent the Activity was started
	 * @param intent
	 */
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
		if (uri.getScheme().equals("tmdb")) {
			showmovie = ShowMovie.Movie;
			id = uri.getSchemeSpecificPart();
			GetData("movie/summary.json/%k/" + id);
		} else if (uri.getScheme().equals("tvdb")) {
			showmovie = ShowMovie.Show;
			id = uri.getSchemeSpecificPart();
			GetData("show/summary.json/%k/" + id);
		}
    }

   
    public void imageDetailsOnClick(View view) {
    	MarkMode mm = null;
    	switch(view.getId()) {
	    	case R.id.imageDetailsWatched:
	    		if (data.optBoolean("watched")) { // Mark as unwatched
	    			mm = MarkMode.Unwatched;
	    		} else { // Mark as watched
	    			mm = MarkMode.Watched;
	    		}
	    		break;
    	}
    	traktapi.MarkAs(this, mm, showmovie, id, data.optString("title"));
    }
    
    /**
     * Actually retrieve details information from Server. Will start a separate Thread for this.
     * @param url
     */
	private void GetData(final String url) {
		getdata = new Runnable() {
			public void run() {
				data = traktapi.getDataObjectFromJSON(url, true); 
				runOnUiThread(UpdateComplete);
			}};
		Thread thread =  new Thread(null, getdata);
        thread.start();
        progressdialog = ProgressDialog.show(this,"", "Retrieving data ...", true);
	}
	
	/**
	 * Called by GetData Thread on UiThread when data has been retrieved
	 */
	private Runnable UpdateComplete = new Runnable() {
		public void run() {
			// Fill in all the information
			TextView title = (TextView) findViewById(R.id.textDetailsTitle);
			title.setText(data.optString("title"));
			try {
				JSONObject images = data.getJSONObject("images");
				String p = images.optString("poster");
				p = p.replace(".jpg", "-138.jpg");
				ImageView poster = (ImageView) findViewById(R.id.imageDetailsPoster);
				// Use CWAC Cache to retrieve the poster. Poster currently are pulled through a PHP script to resize
				cache.handleImageView(poster,"http://escabe.org/resize2.php?image=" + p , "myposter");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			TextView details = (TextView) findViewById(R.id.textDetailsDetails);
			TextView overview = (TextView) findViewById(R.id.textDetailsSummary);

			// Details vary depending on whether displaying Movie or Show
			if (showmovie==ShowMovie.Movie) {
				String d = String.format("Released: %1$tB %1$te, %1$tY\nRuntime: %2$d min",new Date(data.optLong("released")*1000),data.optInt("runtime"));
				details.setText(d);
				d = String.format("\"%s\"\n\n%s",data.optString("tagline"),data.optString("overview"));
				overview.setText(d);
			} else {
				String d = String.format("First Aired: %1$tB %1$te, %1$tY\nCountry: %2$s\nRuntime: %3$d min",
						new Date(data.optLong("first_aired")*1000),
						data.optString("country"),
						data.optInt("runtime"));
				details.setText(d);
				overview.setText(data.optString("overview"));
			}
			
			// Marked Watched/Loved/Hated
        	if (lovedhatedwatched==null)
        		lovedhatedwatched=((Application)getApplication()).getLovedHatedWatched();
        	
        	// Check if Thread has already retrieved all info
        	if(lovedhatedwatched!=null) {
        		LovedHatedWatched lhw = lovedhatedwatched.get(id);
        		if (lhw!=null) {
		    		ImageView loved = (ImageView) findViewById(R.id.imageDetailsLoved);
		        	ImageView hated = (ImageView) findViewById(R.id.imageDetailsHated);
		        	if (lhw.isLoved()) loved.setBackgroundResource(R.drawable.lovedactive);
		        	if (lhw.isHated()) hated.setBackgroundResource(R.drawable.hatedactive);
        		}
        	}

        	// Moved watched here as info is available in data directly
        	ImageView watched = (ImageView) findViewById(R.id.imageDetailsWatched);        	
        	if (data.optBoolean("watched")) 
        		watched.setBackgroundResource(R.drawable.watchedactive);
        	else
        		watched.setBackgroundColor(Color.BLACK);
        	
			// Close the progress dialog
	        progressdialog.dismiss();
		}
	};
	
	public void imageDetailsPosterOnClick(View view) {
		if (showmovie==ShowMovie.Show) {
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("tvdb:" + id),this,EpisodeList.class));
		}
	}

	@Override
	public void DoUpdate() {
    	if (showmovie == ShowMovie.Movie) {
    		GetData("movie/summary.json/%k/" + id);
    	} else {
    		GetData("show/summary.json/%k/" + id);	
    	}
	}
	
}
