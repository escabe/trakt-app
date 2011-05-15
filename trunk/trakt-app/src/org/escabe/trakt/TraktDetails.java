package org.escabe.trakt;

import java.util.Date;
import java.util.HashMap;

import org.escabe.trakt.TraktAPI.MarkMode;
import org.json.JSONObject;

import com.commonsware.cwac.cache.WebImageCache;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
/**
 * Activity to show details about a Movie
 * @author escabe
 *
 */
public class TraktDetails extends ActivityWithUpdate {
	private TraktAPI traktapi;
	
	private String id;
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
		// Consider implementing when called incorrectly, but is currently only accessible internally anyway
    
    }
    
	/**
	 * Parse Intent URI to determine for which Movie/Show display details
	 * @param uri	URI from Intent 
	 */
    private void HandleUri(Uri uri) {
		if (uri.getScheme().equals("tmdb")) {
			id = uri.getSchemeSpecificPart();
			GetData("movie/summary.json/%k/" + id);
		}
    }

   /**
    * (Un)mark movie as watched/loved/hated
    * @param view
    */
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
    	traktapi.MarkMovieAsWatched(this, mm, id, data.optString("title"));
    }
    
    private class DataGrabber extends AsyncTask<String,Void,Boolean> {
		private ProgressDialog progressdialog;
		private Context parent;
		
		public DataGrabber(Context parent) {
			this.parent = parent;
		}

		@Override
		protected void onPreExecute() {
		    progressdialog = ProgressDialog.show(parent,"", "Retrieving data ...", true);
		}
    	
		@Override
		protected Boolean doInBackground(String... params) {
			data = traktapi.getDataObjectFromJSON(params[0], true); 
			return true;
		}
		@Override
	    protected void onPostExecute(Boolean result) {
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

			String d = String.format("Released: %1$tB %1$te, %1$tY\nRuntime: %2$d min",new Date(data.optLong("released")*1000),data.optInt("runtime"));
			details.setText(d);
			d = String.format("\"%s\"\n\n%s",data.optString("tagline"),data.optString("overview"));
			overview.setText(d);
			
			// Marked Watched/Loved/Hated
        	ImageView watched = (ImageView) findViewById(R.id.imageDetailsWatched);        	
			ImageView loved = (ImageView) findViewById(R.id.imageDetailsLoved);
        	ImageView hated = (ImageView) findViewById(R.id.imageDetailsHated);

        	if (data.optBoolean("watched")) watched.setBackgroundResource(R.drawable.watchedactive); 
        	
        	String rating = data.optString("rating");
        	if (rating.equals("love")) loved.setBackgroundResource(R.drawable.ic_item_loved_active);
        	if (rating.equals("hate")) hated.setBackgroundResource(R.drawable.ic_item_hated_active);

        	
			// Close the progress dialog
	        progressdialog.dismiss();
		}
    }
    
    /**
     * Actually retrieve details information from Server. Will start a separate Thread for this.
     * @param url
     */
	private void GetData(String url) {
		DataGrabber dg = new DataGrabber(this);
		dg.execute(url);
	}
	
	@Override
	public void DoUpdate() {
		GetData("movie/summary.json/%k/" + id);
	}
	
}
