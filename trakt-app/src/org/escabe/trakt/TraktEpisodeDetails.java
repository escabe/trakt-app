package org.escabe.trakt;

import java.util.List;

import org.escabe.trakt.TraktAPI.MarkMode;
import org.json.JSONException;
import org.json.JSONObject;

import com.commonsware.cwac.cache.WebImageCache;

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

public class TraktEpisodeDetails extends ActivityWithUpdate {
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
    	MarkMode mm = null;
    	switch(view.getId()) {
	    	case R.id.imageEpisodeDetailsWatched:
	    		if (episode.optBoolean("watched")) { // Mark as unwatched
	    			mm = MarkMode.Unwatched;
	    		} else { // Mark as watched
	    			mm = MarkMode.Watched;
	    		}
	    		break;
    	}

    	try {
			episode.put("watched", !episode.optBoolean("watched"));
		} catch (JSONException e) {
			e.printStackTrace();
		}
    	
    	traktapi.MarkEpisodeAsWatched(this,mm, show.optString("tvdb_id"), episode.optInt("season"), episode.optInt("number"), episode.optString("title"));
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
			DoUpdate();
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
    
    

	@Override
	public void DoUpdate() {
		((TextView)findViewById(R.id.textEpisodeDetailsShowName)).setText(show.optString("title"));
		String d = String.format("%02dx%02d %s",episode.optInt("season"),episode.optInt("number"),episode.optString("title"));
		((TextView)findViewById(R.id.textEpisodeDetailsTitle)).setText(d);
		((TextView)findViewById(R.id.textEpisodeDetailsOverview)).setText(episode.optString("overview"));
		
		String p = episode.optJSONObject("images").optString("screen");
		p = p.replace(".jpg", "-218.jpg");
		ImageView poster = (ImageView) findViewById(R.id.imageEpisodeDetailsPoster);
		
		try {
			cache.handleImageView(poster, p, p);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Log.e(TAG,e.toString());
		}
		
		d = String.format("First Aired:\n%1$tB %1$te, %1$tY",episode.optLong("first_aired")*1000);
		((TextView)findViewById(R.id.textEpisodeDetailsDetails)).setText(d);
		
		
		ImageView w = (ImageView) findViewById(R.id.imageEpisodeDetailsWatched);
		if (episode.optBoolean("watched"))
			w.setBackgroundResource(R.drawable.watchedactive);
		else
			w.setBackgroundColor(Color.BLACK);
				
		
	}
	
}
