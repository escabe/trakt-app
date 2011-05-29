package org.escabe.trakt;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;

public class TraktWatchList extends TabActivity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	
	    TabHost host = getTabHost();
	    host.addTab(host.newTabSpec("episodes").setIndicator("Shows").setContent(new Intent(this, EpisodeWatchList.class)));
        host.addTab(host.newTabSpec("episodes").setIndicator("Episodes").setContent(new Intent(this, EpisodeWatchList.class)));
        host.addTab(host.newTabSpec("episodes").setIndicator("Movies").setContent(new Intent(this, EpisodeWatchList.class)));
	}


}
