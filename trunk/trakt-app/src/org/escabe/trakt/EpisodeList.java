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

import android.app.ExpandableListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class EpisodeList extends ExpandableListActivity implements ActivityWithUpdate {
	private static final String TAG = "EpisodeList";
	private String id;
	private JSONObject data;
	
	private EpisodeListAdapter adapter;
	private TraktAPI traktapi;

	private WebImageCache cache = null;

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
			data = traktapi.getDataObjectFromJSON("show/summary.json/%k/" + id + "/extended",true);
			if (data==null) {
				return false;
			}
			return true;
		}
		
		@Override
	    protected void onPostExecute(Boolean result) {
			if (!result) {
				progressdialog.dismiss();
				Toast.makeText(parent, "Failed loading Epsiode list.",Toast.LENGTH_SHORT).show();
				return;
			}
			
			// Configure shouts
			ShoutView sv = (ShoutView)findViewById(R.id.shoutEpisodeList);
			sv.setViewurl("show/shouts.json/%k/" + data.optString("tvdb_id"));
			
			TextView title = (TextView) findViewById(R.id.textEpisodeDetailsTitle);
			title.setText(data.optString("title"));
			try {
				JSONObject images = data.getJSONObject("images");
	
				ImageView poster = (ImageView) findViewById(R.id.imageEpisodeDetailsPoster);
				// Use CWAC Cache to retrieve the poster. Poster currently are pulled through a PHP script to resize
				cache.handleImageView(poster,traktapi.ResizePoster(images.optString("poster"),2) , "myposter");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				Log.w(TAG,"Failed to retrieve poster",e);
			}
			TextView details = (TextView) findViewById(R.id.textEpisodeDetailsDetails);
			TextView overview = (TextView) findViewById(R.id.textEpisodeDetailsSummary);

			String d = String.format("First Aired: %1$tB %1$te, %1$tY\nCountry: %2$s\nRuntime: %3$d min",
					new Date(data.optLong("first_aired")*1000),
					data.optString("country"),
					data.optInt("runtime"));
			details.setText(d);
			overview.setText(data.optString("overview"));

			
			// Marked Watched/Loved/Hated
			ImageView watchlist = (ImageView) findViewById(R.id.imageEpisodeListWatchlist);
			ImageView loved = (ImageView) findViewById(R.id.imageEpisodeListLoved);
        	ImageView hated = (ImageView) findViewById(R.id.imageEpisodeListHated);

        	if (data.optBoolean("in_watchlist")) watchlist.setBackgroundResource(R.drawable.ic_item_watchlist_icon_active);
    		else watchlist.setBackgroundColor(android.R.color.transparent); 
        	String rating = data.optString("rating");
        	if (rating.equals("love")) loved.setBackgroundResource(R.drawable.ic_item_loved_active);
        		else loved.setBackgroundColor(android.R.color.transparent);
        	if (rating.equals("hate")) hated.setBackgroundResource(R.drawable.ic_item_hated_active);
        		else hated.setBackgroundColor(android.R.color.transparent);
			

        	
			// Notify list that data has been retrieved
			adapter.notifyDataSetChanged();
			
	        // Hide progress dialog
			progressdialog.dismiss();
	    }
		
	}
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.trakt_episode_list);
		traktapi = new TraktAPI(this);
	    
		cache = ((Application)getApplication()).getCache();
		
		adapter = new EpisodeListAdapter(); 
		
		setListAdapter(adapter);
		
	    HandleIntent(getIntent());
		
	}
	
	@Override
	public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
		JSONObject d = data.optJSONArray("seasons").optJSONObject(groupPosition).optJSONArray("episodes").optJSONObject(childPosition);
		
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("episode://tvdb/" + this.id +"/" + d.optInt("season") + "/" + d.optInt("episode") ),this,TraktEpisodeDetails.class);

		startActivity(intent);
		return false;
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
    
	private void SwitchList() {
		DataGrabber dg = new DataGrabber(this);
		dg.execute();
	}
	
	/**
	 * Parse Intent URI to determine for which Show seasons/episodes will be shown
	 * @param uri	URI from Intent 
	 */
    private void HandleUri(Uri uri) {
		if (uri.getScheme().equals("tvdb")) {
			id = uri.getSchemeSpecificPart();
			SwitchList();
		}
    }
    
    public void imageEpisodeListOnClick(View view) {
    	switch (view.getId()) {
    		case R.id.imageEpisodeListWatchlist:
    			if (data.optBoolean("in_watchlist")) { // Unwatchlist
    				traktapi.Mark(this, "show","unwatchlist",data.optString("imdb_id"));
    			} else { // Watchlist
    				traktapi.Mark(this, "show","watchlist",data.optString("imdb_id"));
    			}
    		break;
    		case R.id.imageEpisodeListLoved:
	    		if (data.optString("rating").equals("love")) { // Unrate
	    			traktapi.Mark(this, "show","unrate",data.optString("imdb_id"));
	    		} else { // Rate as loved
	    			traktapi.Mark(this, "show","love",data.optString("imdb_id"));
	    		}
	    		break;
	    	case R.id.imageEpisodeListHated:
	    		if (data.optString("rating").equals("hate")) { // Unrate
	    			traktapi.Mark(this, "show","unrate",data.optString("imdb_id"));
	    		} else { // Rate as hated
	    			traktapi.Mark(this, "show","hate",data.optString("imdb_id"));
	    		}
	    		break;
    	}
    }

    
	/**
	 * Class for displaying information in the ExpandableList
	 * @author escabe
	 *
	 */
	class EpisodeListAdapter extends BaseExpandableListAdapter {
    
		/**
		 * Display an Episode row
		 */
		public View getChildView(int groupPosition, int childPosition,
				boolean isLastChild, View row, ViewGroup parent) {
			if (row==null) {
                LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                row = vi.inflate(R.layout.trakt_episode_episode, null);
			}
			JSONObject d = data.optJSONArray("seasons").optJSONObject(groupPosition).optJSONArray("episodes").optJSONObject(childPosition);
			if (d!=null) {
				TextView title = (TextView) row.findViewById(R.id.textEpisodeListEpisodeTitle);
				title.setText(String.format("%02dx%02d: %s",d.optInt("season"),d.optInt("episode"),d.optString("title")));
	
				TextView details = (TextView) row.findViewById(R.id.textEpisodeListEpisodeDetails);
				details.setText(String.format("First Aired: %1$tB %1$te, %1$tY",d.optLong("first_aired")*1000));
				
				ImageView poster = (ImageView) row.findViewById(R.id.imageEpisodeListEpisodePoster);
				String url = traktapi.ResizeScreen(d.optString("screen"),1);
				try {
					cache.handleImageView(poster, url, url);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					Log.w(TAG,"Failed to retrieve poster",e1);
				}
				
				((ImageView)row.findViewById(R.id.imageEpisodeWatched)).setVisibility(d.optBoolean("watched") ? View.VISIBLE:View.GONE);
        		
				String rating = d.optString("rating");
				((ImageView)row.findViewById(R.id.imageEpisodeLoved)).setVisibility( rating.equals("love") ? View.VISIBLE:View.GONE );
				((ImageView)row.findViewById(R.id.imageEpisodeHated)).setVisibility( rating.equals("hate") ? View.VISIBLE:View.GONE );
			}
			return row;
		}

		/**
		 * Display a Season row
		 */
		public View getGroupView(int groupPosition, boolean isExpanded,
				View row, ViewGroup parent) {
			if (row==null) {
                LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                row = vi.inflate(R.layout.trakt_episode_season, null);
			}
			JSONObject s = data.optJSONArray("seasons").optJSONObject(groupPosition);
			if (s!=null) {
				TextView title = (TextView)row.findViewById(R.id.textEpisodeListSeasonTitle);
				if (s.optInt("season")==0) {
					title.setText("Specials");
				} else {
					title.setText(String.format("Season %d",s.optInt("season")));
				}
				
				TextView details = (TextView)row.findViewById(R.id.textEpisodeListSeasonDetails);
				details.setText(String.format("Episodes: %d",s.optJSONArray("episodes").length()));
				
				ImageView poster = (ImageView) row.findViewById(R.id.imageEpisodeListSeasonPoster);
				String url = traktapi.ResizePoster(s.optString("poster"),1);
				try {
					cache.handleImageView(poster, url, url);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					Log.w(TAG,"Failed to retrieve poster",e1);
				}
			}
			
			return row;
		}	   
		
		public Object getChild(int groupPosition, int childPosition) {
			if(data==null)
				return null;
			return data.optJSONArray("seasons").optJSONObject(groupPosition).optJSONArray("episodes").optJSONObject(childPosition);
		}

		public long getChildId(int groupPosition, int childPosition) {
			return childPosition;
		}

		
		public int getChildrenCount(int groupPosition) {
			if(data==null)
				return 0;
			return data.optJSONArray("seasons").optJSONObject(groupPosition).optJSONArray("episodes").length();
		}

		public Object getGroup(int groupPosition) {
			if(data==null)
				return null;
			return data.optJSONArray("seasons").optJSONObject(groupPosition);
		}

		public int getGroupCount() {
			if(data==null)
				return 0;
			return data.optJSONArray("seasons").length();
		}

		public long getGroupId(int groupPosition) {
			return groupPosition;
		}

		public boolean hasStableIds() {
			return false;
		}

		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return true;
		}
		
	}

	public void DoUpdate() {
		HandleIntent(getIntent());
		
	}
    
    
}
