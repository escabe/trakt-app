/*******************************************************************************
 * Copyright 2011 EscAbe
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.escabe.trakt;

import java.util.Date;
import org.json.JSONObject;

import com.commonsware.cwac.cache.WebImageCache;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
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
public class TraktDetails extends Activity implements ActivityWithUpdate {
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
    	switch(view.getId()) {
	    	case R.id.imageDetailsWatched:
	    		if (data.optBoolean("watched")) { // Mark as unwatched
	    			traktapi.Mark(this, "movie","unseen",data.optString("imdb_id"));
	    		} else { // Mark as watched
	    			traktapi.Mark(this, "movie","seen",data.optString("imdb_id"));
	    		}
	    		break;
	    	case R.id.imageDetailsWatchlist:
	    		if (data.optBoolean("in_watchlist")) { // Remove from watchlist
	    			traktapi.Mark(this, "movie","unwatchlist",data.optString("imdb_id"));
	    		} else { // Add to watchlist
	    			traktapi.Mark(this, "movie","watchlist",data.optString("imdb_id"));
	    		}
    		break;
	    	case R.id.imageDetailsLoved:
	    		if (data.optString("rating").equals("love")) { // Unrate
	    			traktapi.Mark(this, "movie","unrate",data.optString("imdb_id"));
	    		} else { // Rate as loved
	    			traktapi.Mark(this, "movie","love",data.optString("imdb_id"));
	    		}
	    		break;
	    	case R.id.imageDetailsHated:
	    		if (data.optString("rating").equals("hate")) { // Unrate
	    			traktapi.Mark(this, "movie","unrate",data.optString("imdb_id"));
	    		} else { // Rate as hated
	    			traktapi.Mark(this, "movie","hate",data.optString("imdb_id"));
	    		}
	    		break;
	    		
    	}

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
				String p = traktapi.ResizePoster(images.optString("poster"),2);
				ImageView poster = (ImageView) findViewById(R.id.imageDetailsPoster);
				// Use CWAC Cache to retrieve the poster. Poster currently are pulled through a PHP script to resize
				cache.handleImageView(poster,p , "myposter");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			TextView details = (TextView) findViewById(R.id.textDetailsDetails);
			TextView overview = (TextView) findViewById(R.id.textDetailsSummary);

			// Details vary depending on whether displaying Movie or Show

			String d = String.format("Released: %1$tB %1$te, %1$tY\nRuntime: %2$d min",new Date(data.optLong("released")*1000),data.optInt("runtime"));
			details.setText(d);
			if (!data.optString("tagline").equals("")) {
				d = String.format("\"%s\"\n\n", data.optString("tagline"));
			} else {
				d = "";
			}
			d += data.optString("overview");
			overview.setText(d);
			
			// Marked Watched/Loved/Hated
        	ImageView watched = (ImageView) findViewById(R.id.imageDetailsWatchedBanner);        	
			ImageView loved = (ImageView) findViewById(R.id.imageDetailsLoved);
        	ImageView hated = (ImageView) findViewById(R.id.imageDetailsHated);
        	ImageView watchlist = (ImageView) findViewById(R.id.imageDetailsWatchlist);

        	watched.setVisibility( data.optBoolean("watched") ? View.VISIBLE:View.GONE );

        	if (data.optBoolean("in_watchlist")) watchlist.setBackgroundResource(R.drawable.ic_item_watchlist_icon_active);
    		else watchlist.setBackgroundColor(android.R.color.black); 
        	
        	String rating = data.optString("rating");
        	if (rating.equals("love")) loved.setBackgroundResource(R.drawable.ic_item_loved_active);
        		else loved.setBackgroundColor(android.R.color.black);
        	if (rating.equals("hate")) hated.setBackgroundResource(R.drawable.ic_item_hated_active);
        		else hated.setBackgroundColor(android.R.color.black);	

        	
			// Close the progress dialog
	        progressdialog.dismiss();
		}
    }
    
    public void GetData(String url) {
		DataGrabber dg = new DataGrabber(this);
		dg.execute(url);
    }
	
	public void DoUpdate() {
		GetData("movie/summary.json/%k/" + id);
	}
	
}
