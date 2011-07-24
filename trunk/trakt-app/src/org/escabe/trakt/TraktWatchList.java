package org.escabe.trakt;

import android.app.TabActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TabHost;

public class TraktWatchList extends TabActivity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    TabHost host = getTabHost();
	    
	    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("trakt://self/user/watchlist/shows.json/%25k/%25u"),this,TraktList.class);
	    i.putExtra("bare", true);
	    host.addTab(host.newTabSpec("episodes").setIndicator("Shows").setContent(i));
	    
        
	    host.addTab(host.newTabSpec("episodes").setIndicator("Episodes").setContent(new Intent(this, EpisodeWatchList.class)));

	    i = new Intent(Intent.ACTION_VIEW, Uri.parse("trakt://self/user/watchlist/movies.json/%25k/%25u"),this,TraktList.class);
	    i.putExtra("bare", true);
	    host.addTab(host.newTabSpec("episodes").setIndicator("Movies").setContent(i));
	}

}
