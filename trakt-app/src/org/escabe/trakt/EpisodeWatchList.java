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

import org.json.JSONArray;
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

public class EpisodeWatchList extends ExpandableListActivity {
	private static final String TAG="EpisodeWatchList";
	private JSONArray data;
	private WebImageCache cache = null;
	private TraktAPI traktapi;
	private EpisodeWatchListAdapter adapter;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.trakt_episode_watchlist);
	    
	    traktapi = new TraktAPI(this);
	    
		cache = ((Application)getApplication()).getCache();
		
		adapter = new EpisodeWatchListAdapter(); 
		
		setListAdapter(adapter);

		HandleIntent(getIntent());
		
	}
	
	private void HandleIntent(Intent intent) {
   		HandleUri(intent.getData());
    }
    private void HandleUri(Uri uri) {
			UpdateList();			

    }
    
    private void UpdateList() {
    	DataGrabber dg = new DataGrabber(this);
    	dg.execute();
    	
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
			data = traktapi.getDataArrayFromJSON("user/watchlist/episodes.json/%k/%u",true);
			if (data==null) {
				return false;
			}
			return true;
		}
		
		@Override
	    protected void onPostExecute(Boolean result) {
			if(!result) {
				Toast.makeText(parent, "Failed loading Epsiode watchlist.",Toast.LENGTH_SHORT).show();
			}
			
			// Notify list that data has been retrieved
			adapter.notifyDataSetChanged();
			
	        // Hide progress dialog
			progressdialog.dismiss();
	    }
		
	}
	
	
	@Override
	public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
		JSONObject s = (JSONObject) adapter.getGroup(groupPosition);
		JSONObject d = (JSONObject) adapter.getChild(groupPosition, childPosition);
		
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("episode://tvdb/" + s.optString("tvdb_id") +"/" + d.optInt("season") + "/" + d.optInt("number") ),this,TraktEpisodeDetails.class);

		startActivity(intent);
		return false;
	}
	
	class EpisodeWatchListAdapter extends BaseExpandableListAdapter {

		public Object getChild(int groupPosition, int childPosition) {
			if (data==null) 
				return null;
			return data.optJSONObject(groupPosition).optJSONArray("episodes").optJSONObject(childPosition);
		}

		public long getChildId(int groupPosition, int childPosition) {
			return childPosition;
		}

		public int getChildrenCount(int groupPosition) {
			if (data==null) 
				return 0;
			return data.optJSONObject(groupPosition).optJSONArray("episodes").length();
		}

		public Object getGroup(int groupPosition) {
			if (data==null) 
				return null;
			return data.optJSONObject(groupPosition);
		}

		public int getGroupCount() {
			if (data==null) 
				return 0;
			return data.length();
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

		public View getGroupView(int groupPosition, boolean isExpanded,	View row, ViewGroup parent) {
			if (row==null) {
                LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                row = vi.inflate(R.layout.trakt_episode_season, null);
			}
			JSONObject s = (JSONObject) getGroup(groupPosition);
			if (s!=null) {
				TextView title = (TextView)row.findViewById(R.id.textEpisodeListSeasonTitle);
				title.setText(s.optString("title"));
				
				TextView details = (TextView)row.findViewById(R.id.textEpisodeListSeasonDetails);
				details.setText(String.format("Episodes: %d",s.optJSONArray("episodes").length()));
				
				ImageView poster = (ImageView) row.findViewById(R.id.imageEpisodeListSeasonPoster);
				String url = traktapi.ResizePoster(s.optJSONObject("images").optString("poster"),1);
				try {
					cache.handleImageView(poster, url, url);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					Log.w(TAG,"Failed to retrieve poster",e1);
				}
			}

			return row;
		}

		public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View row, ViewGroup parent) {
			if (row==null) {
                LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                row = vi.inflate(R.layout.trakt_episode_episode, null);
			}
			JSONObject d = (JSONObject) getChild(groupPosition,childPosition);
			if (d!=null) {
				TextView title = (TextView) row.findViewById(R.id.textEpisodeListEpisodeTitle);
				title.setText(String.format("%02dx%02d: %s",d.optInt("season"),d.optInt("number"),d.optString("title")));
	
				TextView details = (TextView) row.findViewById(R.id.textEpisodeListEpisodeDetails);
				details.setText(String.format("Watchlist: %1$tB %1$te, %1$tY",d.optLong("inserted")*1000));
				
				ImageView poster = (ImageView) row.findViewById(R.id.imageEpisodeListEpisodePoster);
				String url = traktapi.ResizeScreen(d.optJSONObject("images").optString("screen"),1);
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

	}

}
