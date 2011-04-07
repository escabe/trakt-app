package org.escabe.trakt;

import java.io.File;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import com.commonsware.cwac.bus.SimpleBus;
import com.commonsware.cwac.cache.AsyncCache;
import com.commonsware.cwac.cache.WebImageCache;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
/**
 * Activity to show details about a Movie
 * @author escabe
 *
 */
public class TraktDetails extends Activity {
	/* Currently displays info for a Movie or Show, instead of Show might want to use this only for Episode and create
	 * an ListActivity which displays episode list when selecting a Show
	 */
	
	private ProgressDialog progressdialog;
	private Runnable getdata;
	private TraktAPI traktapi;
	private enum ShowMovie {Show, Movie} //Consider making this (more) public as more classes may need this
	private ShowMovie showmovie;
	
	private WebImageCache cache = null;

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
	
	/**
	 * Check with which Intent the Activity was started
	 * @param intent
	 */
	private void HandleIntent(Intent intent) {
    	// Will only support viewing information
		if (intent.getAction().equals(Intent.ACTION_VIEW)) {
    		HandleUri(intent.getData());
    	} 
		// Consider implementing when called incorrecly, but is currently only accessible internally anyway
    
    }
    
	/**
	 * Parse Intent URI to determine for which Movie/Show display details
	 * @param uri	URI from Intent 
	 */
    private void HandleUri(Uri uri) {
		if (uri.getScheme().equals("tmdb")) {
			showmovie = ShowMovie.Movie;
			String id = uri.getSchemeSpecificPart();
			GetData("movie/summary.json/%k/" + id);
		} else if (uri.getScheme().equals("tvdb")) {
			showmovie = ShowMovie.Show;
			String id = uri.getSchemeSpecificPart();
			GetData("show/summary.json/%k/" + id);
		}
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
				String d = String.format("Released: %1$tB %1$te, %1$tY\nRuntime: %2$d min\n",new Date(data.optLong("released")*1000),data.optInt("runtime"));
				details.setText(d);
				d = String.format("\"%s\"\n\n%s",data.optString("tagline"),data.optString("overview"));
				overview.setText(d);
			} else {
				String d = String.format("First Aired: %1$tB %1$te, %1$tY\nCountry: %2$s\nRuntime: %3$d min\n",
						new Date(data.optLong("first_aired")*1000),
						data.optString("country"),
						data.optInt("runtime"));
				details.setText(d);
				overview.setText(data.optString("overview"));
			}
			// Close the progess dialog
	        progressdialog.dismiss();
		}
	};
	
}
