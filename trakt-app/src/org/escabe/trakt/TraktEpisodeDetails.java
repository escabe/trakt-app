package org.escabe.trakt;

import java.util.List;

import org.escabe.trakt.TraktAPI.MarkMode;

import com.commonsware.cwac.cache.WebImageCache;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class TraktEpisodeDetails extends ActivityWithUpdate {
	private String TAG="EpisodeDetails";
	private String id;
	private int season;
	private int episode;
	private boolean watched;
	private String title;
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
	    		if (watched) { // Mark as unwatched
	    			mm = MarkMode.Unwatched;
	    		} else { // Mark as watched
	    			mm = MarkMode.Watched;
	    		}
	    		break;
    	}
    	watched = !watched;
    	traktapi.MarkEpisodeAsWatched(this,mm, id, season, episode, title);
    }
    
    private void ShowEpisodeDetails(List<String> info) {
    	id = info.get(0);
    	season = Integer.parseInt(info.get(1));
    	episode = Integer.parseInt(info.get(2));
    	
    	Intent intent = getIntent();
    	
    	watched = intent.getBooleanExtra("watched",false);
    	title = intent.getStringExtra("title");
		
    	DoUpdate();
    }

	@Override
	public void DoUpdate() {
		Intent intent = getIntent();
		((TextView)findViewById(R.id.textEpisodeDetailsShowName)).setText(intent.getStringExtra("showname"));
		String d = String.format("%02dx%02d %s",season,episode,title);
		((TextView)findViewById(R.id.textEpisodeDetailsTitle)).setText(d);
		((TextView)findViewById(R.id.textEpisodeDetailsOverview)).setText(intent.getStringExtra("overview"));
		
		String p = intent.getStringExtra("poster");
		ImageView poster = (ImageView) findViewById(R.id.imageEpisodeDetailsPoster);
		try {
			cache.handleImageView(poster, p, p);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Log.e(TAG,e.toString());
		}
		
		d = String.format("First Aired:\n%1$tB %1$te, %1$tY",intent.getLongExtra("firstaired", 0)*1000);
		((TextView)findViewById(R.id.textEpisodeDetailsDetails)).setText(d);
		
		
		ImageView w = (ImageView) findViewById(R.id.imageEpisodeDetailsWatched);
		if (watched)
			w.setBackgroundResource(R.drawable.watchedactive);
		else
			w.setBackgroundColor(Color.BLACK);
				
		
	}
	
}
