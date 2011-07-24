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
