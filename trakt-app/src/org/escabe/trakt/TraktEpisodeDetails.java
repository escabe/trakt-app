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

import java.util.List;

import org.json.JSONObject;

import com.commonsware.cwac.cache.WebImageCache;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class TraktEpisodeDetails extends Activity implements ActivityWithUpdate {
	private String TAG="EpisodeDetails";
	private JSONObject data;
	private JSONObject show;
	private JSONObject episode;
	
	private WebImageCache cache = null;
	private TraktAPI traktapi = null;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.trakt_episode_details);
	    
	    cache = ((Application)getApplication()).getCache();
	
	    traktapi = new TraktAPI(this);
	    
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
			ShowEpisodeDetails(uri.getPathSegments());			
		}
    }
    
    public void imageEpisodeDetailsOnClick(View view) {
    	switch(view.getId()) {
	    	case R.id.imageEpisodeDetailsWatched:
	    		if (episode.optBoolean("watched")) { // Mark as unwatched
	    			traktapi.Mark(this,"episode","unseen",show.optString("tvdb_id"), episode.optInt("season"), episode.optInt("number") );
	    		} else { // Mark as watched
	    			traktapi.Mark(this,"episode","seen",show.optString("tvdb_id"), episode.optInt("season"), episode.optInt("number") );
	    		}
	    		break;
	    	case R.id.imageEpisodeDetailsWatchlist:
	    		if (episode.optBoolean("in_watchlist")) { // Remove from watchlist
	    			traktapi.Mark(this,"episode","unwatchlist",show.optString("tvdb_id"), episode.optInt("season"), episode.optInt("number") );
	    		} else { // Add to watchlist
	    			traktapi.Mark(this,"episode","watchlist",show.optString("tvdb_id"), episode.optInt("season"), episode.optInt("number") );
	    		}
	    		break;	    		
	    	case R.id.imageEpisodeDetailsLoved:
	    		if (episode.optString("rating").equals("love")) { // Unrate
	    			traktapi.Mark(this,"episode","unrate",show.optString("tvdb_id"), episode.optInt("season"), episode.optInt("number") );
	    		} else { // Rate as loved
	    			traktapi.Mark(this,"episode","love",show.optString("tvdb_id"), episode.optInt("season"), episode.optInt("number") );
	    		}
	    		break;
	    	case R.id.imageEpisodeDetailsHated:
	    		if (episode.optString("rating").equals("hate")) { // Unrate
	    			traktapi.Mark(this,"episode","unrate",show.optString("tvdb_id"), episode.optInt("season"), episode.optInt("number") );
	    		} else { // Rate as hated
	    			traktapi.Mark(this,"episode","hate",show.optString("tvdb_id"), episode.optInt("season"), episode.optInt("number") );
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
			//Get list
			data = traktapi.getDataObjectFromJSON(params[0],true);
			show = data.optJSONObject("show");
			episode = data.optJSONObject("episode");
			return true;
		}
		@Override
	    protected void onPostExecute(Boolean result) {
			((TextView)findViewById(R.id.textEpisodeDetailsShowName)).setText(show.optString("title"));
			String d = String.format("%02dx%02d %s",episode.optInt("season"),episode.optInt("number"),episode.optString("title"));
			((TextView)findViewById(R.id.textEpisodeDetailsTitle)).setText(d);
			((TextView)findViewById(R.id.textEpisodeDetailsOverview)).setText(episode.optString("overview"));
			
			String p = traktapi.ResizeScreen(episode.optJSONObject("images").optString("screen"),3);
			ImageView poster = (ImageView) findViewById(R.id.imageEpisodeDetailsPoster);
			
			try {
				cache.handleImageView(poster, p, p);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				Log.e(TAG,e.toString());
			}
			
			d = String.format("First Aired: %1$tB %1$te, %1$tY",episode.optLong("first_aired")*1000);
			((TextView)findViewById(R.id.textEpisodeDetailsDetails)).setText(d);
			
			
			ImageView watched = (ImageView) findViewById(R.id.imageEpisodeDetailsWatchedBanner);
			ImageView watchedlist = (ImageView) findViewById(R.id.imageEpisodeDetailsWatchlist);
			ImageView loved = (ImageView) findViewById(R.id.imageEpisodeDetailsLoved);
			ImageView hated = (ImageView) findViewById(R.id.imageEpisodeDetailsHated);

			watched.setVisibility( episode.optBoolean("watched") ? View.VISIBLE:View.GONE );
			
			if (episode.optBoolean("in_watchlist")) watchedlist.setBackgroundResource(R.drawable.ic_item_watchlist_icon_active);
			else watchedlist.setBackgroundColor(Color.TRANSPARENT);
			
			if (episode.optString("rating").equals("love")) loved.setBackgroundResource(R.drawable.ic_item_loved_active);
				else loved.setBackgroundColor(Color.TRANSPARENT);

			if (episode.optString("rating").equals("hate")) hated.setBackgroundResource(R.drawable.ic_item_hated_active);
				else hated.setBackgroundColor(Color.TRANSPARENT);

			
	        // Hide progress dialog
			progressdialog.dismiss();

	    }
	}
    
    private void ShowEpisodeDetails(List<String> info) {
    	String id = info.get(0);
    	String season = info.get(1);
    	String episode = info.get(2);
    	
    	DataGrabber dg = new DataGrabber(this);
    	dg.execute("show/episode/summary.json/%k/" + id + "/" + season + "/" + episode);
    
    }
    
    
    public void DoUpdate() {
		HandleIntent(getIntent());
		
	}
	
}
