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

import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.commonsware.cwac.cache.WebImageCache;
import com.commonsware.cwac.thumbnail.ThumbnailAdapter;

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;
/**
 * Activity to Display lists of Shows or Movies
 * @author escabe
 *
 */
public class TraktList extends ListActivity {
	private static final String TAG="ListActivity";
	// Main ArrayList holding the currently displayed data
	private JSONArray data=null;
	private JSONObject userdata=null;

	// For CWAC Thumbnail
	private static final int[] IMAGE_IDS={R.id.imagePoster};
	private ThumbnailAdapter thumbs=null;
	private WebImageCache cache = null;
	
	private static boolean initial;
	private Spinner sp;
	private TraktAPI traktapi=null;

	private Boolean bare;
	
	// To hold information about what we are currently looking at
	
	private enum UserTrending { User, Trending, Search}
	private UserTrending usertrending;
	
	// Strings for the spinner
	private String[] strings = {"Trending Shows","Trending Movies",
								"All Your Shows","All Your Movies",
								"Last 100 Watched",
								"Movies Watchlist","Shows Watchlist","Episode Watchlist",
								"Search"};
	
	private String[] uu = new String[] {"trakt://trending/shows/trending.json/%25k","trakt://trending/movies/trending.json/%25k",
			"trakt://self/user/library/shows/all.json/%25k/%25u","trakt://self/user/library/movies/all.json/%25k/%25u",
			"trakt://self/user/watched.json/%25k/%25u",
			"trakt://self/user/watchlist/movies.json/%25k/%25u","trakt://self/user/watchlist/shows.json/%25k/%25u","trakt://self/user/watchlist/episodes.json/%25k/%25u",
			"SEARCH"};
	
	private List<String> urls; 			

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    bare = getIntent().getBooleanExtra("bare", false);
	    if (bare)
	    	setContentView(R.layout.trakt_list_bare);
	    else
	    	setContentView(R.layout.trakt_list);
	    // Initialize API
	    
	    traktapi = new TraktAPI(this);
	    urls = Arrays.asList(uu);
	    // Do the following here instead of in onStart such that this is not repeated when returning from Details Activity
	    

		// Initialize CWAC Thumbnail for posters
		thumbs = new ThumbnailAdapter(this, new MovieShowAdapter(), ((Application)getApplication()).getThumbsCache(),IMAGE_IDS);
		// Assign the adaptor to the list
		setListAdapter(thumbs);

		if (!bare) {
			// For user avatar
			Application app = (Application)getApplication();
			cache = new WebImageCache(getCacheDir(), app.bus, app.policy, 101,getResources().getDrawable(R.drawable.ic_item_avatar));
			
			// Add Adapter to the spinner (to allow switching between lists)
			initial = true;
			sp = (Spinner)findViewById(R.id.spinnerTraktList);
			sp.setAdapter(new SpinAdapter());
			sp.setOnItemSelectedListener(new OnItemSelectedListener() {
				public void onItemSelected(AdapterView<?> parent, View v,int position, long id) {
					if(!initial)
						if(urls.get(position).equals("SEARCH") ) {
							onSearchRequested();
						} else {
							HandleUri(Uri.parse(urls.get(position)));
						}
					else
						initial = false;
				}
				public void onNothingSelected(AdapterView<?> parent) {
					// TODO Auto-generated method stub
				}
			});			
		}
		// Determine with which intent the Activity was started.
		HandleIntent(getIntent());
	}
	
	/**
	 * Adapter for the Spinner
	 * @author escabe
	 *
	 */
	class SpinAdapter implements SpinnerAdapter {
		public int getCount() {
			return strings.length;
		}
		public Object getItem(int position) {
			return strings[position];
		}
		public long getItemId(int position) {
			return position;
		}
		public View getView(int position, View convertView, ViewGroup parent) {
			View row = convertView;
			if(row==null) {
                LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                row = vi.inflate(android.R.layout.simple_spinner_item, null);
			}
			TextView t = (TextView) row.findViewById(android.R.id.text1);
			t.setText(strings[position]);
			return row;
		}
		public View getDropDownView(int position, View convertView,
				ViewGroup parent) {
			View row = convertView;
			if(row==null) {
                LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                row = vi.inflate(android.R.layout.simple_spinner_dropdown_item, null);
			}
			TextView t = (TextView) row.findViewById(android.R.id.text1);
			t.setText(strings[position]);
			return row;
		}
		public int getItemViewType(int position) {
			return 0;
		}
		public int getViewTypeCount() {
			return 1;
		}
		public boolean hasStableIds() {
			return true;
		}
		public boolean isEmpty() {
			return false;
		}
		public void registerDataSetObserver(DataSetObserver observer) {
			// TODO Auto-generated method stub
		}
		public void unregisterDataSetObserver(DataSetObserver observer) {
			// TODO Auto-generated method stub
		}
	}
	
	/**
	 * Switch data in the current list. Will be done in an AsyncTask
	 * @param url
	 * @param login
	 */
	private void SwitchList(final String url,final boolean login) {
		DataGrabber dg = new DataGrabber(this);
		dg.execute(url);
	}
	
	private void SwitchUser(String username) {
		if (bare) return;
		if (username==null) {
			username = PreferenceManager.getDefaultSharedPreferences(this).getString("user","");
		}
		if (userdata!=null) {
			if (userdata.optString("username").equals(username))
				return;
		}
		UserGrabber ug = new UserGrabber(this);
		ug.execute("user/profile.json/%k/%u");
	}
	
	private class UserGrabber extends AsyncTask<String,Void,Boolean> {
		private TraktList parent;
		
		public UserGrabber(TraktList parent) {
			this.parent = parent;
		}
		
		@Override
		protected void onPreExecute() {
			ProgressBar pb = (ProgressBar) parent.findViewById(R.id.progbarListUser);
			pb.setVisibility(View.VISIBLE);
		}
		
		@Override
		protected Boolean doInBackground(String... params) {
			//Get list
			userdata = traktapi.getDataObjectFromJSON(params[0],true);
			if (userdata==null) {
				return false;
			}
			return true;
		}
		@Override
	    protected void onPostExecute(Boolean result) {
			if (!result) {
				ProgressBar pb = (ProgressBar) parent.findViewById(R.id.progbarListUser);
				pb.setVisibility(View.INVISIBLE);
				Toast.makeText(parent, "Failed retrieving user information.", Toast.LENGTH_SHORT).show();
				return;
			}
			TextView ut = (TextView) parent.findViewById(R.id.textListUserInfo);
			JSONObject stats = userdata.optJSONObject("stats");
			String lawa = "";
			JSONObject lw = userdata.optJSONArray("watched").optJSONObject(0);
			if (lw!=null) {
				if (lw.optString("type").equals("episode")) {
					lawa = String.format("%s %02dx%02d",lw.optJSONObject("show").optString("title"),
														lw.optJSONObject("episode").optInt("season"),
														lw.optJSONObject("episode").optInt("number"));
				} else {
					lawa = lw.optJSONObject("movie").optString("title");
				}
			}
			
			String d = String.format("<b>%s</b> - %s<br>" +
									 "<b>Location:</b><i>%s</i><br>" +
									 "<b>Watched:</b> Episodes: <i>%d</i> Movies: <i>%d</i><br>" +
									 "<b>Last Watched:</b><i>%s</i>",
					userdata.optString("username"),userdata.optString("full_name"),
					userdata.optString("location"),
					stats.optJSONObject("episodes").optInt("watched"),stats.optJSONObject("movies").optInt("watched"),
					lawa);
			
			ut.setText(Html.fromHtml(d));

			ImageView iv = (ImageView)parent.findViewById(R.id.imageListUser);
			try {
				cache.handleImageView(iv, traktapi.ResizeAvatar(userdata.optString("avatar"),3), "avatar");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				Log.w(TAG,"Failed to retrieve avatar",e);
			}
			
			// Hide progress dialog
			ProgressBar pb = (ProgressBar) parent.findViewById(R.id.progbarListUser);
			pb.setVisibility(View.INVISIBLE);
			
	    }

		
	}
	
	private class DataGrabber extends AsyncTask<String,Void,Boolean> {
		private TraktList parent;
		
		public DataGrabber(TraktList parent) {
			this.parent = parent;
		}
		
		@Override
		protected void onPreExecute() {
			thumbs.notifyDataSetInvalidated();

			ProgressBar pb;
			if (bare)
				pb = (ProgressBar) parent.findViewById(R.id.progbarListBare);
			else
				pb = (ProgressBar) parent.findViewById(R.id.progbarListList);
			pb.setVisibility(View.VISIBLE);
		}
		
		@Override
		protected Boolean doInBackground(String... params) {
			//Get list
			data = traktapi.getDataArrayFromJSON(params[0],true);
			if (data==null) {
				return false;
			}
			return true;
		}
		@Override
	    protected void onPostExecute(Boolean result) {
			if (!result) {
				Toast.makeText(parent, "Failed loading list data.",Toast.LENGTH_SHORT).show();
			}
			thumbs.notifyDataSetChanged();
	        // Scroll to first item
			setSelection(0);
	        // Hide progress dialog
			ProgressBar pb;
			if (bare)
				pb = (ProgressBar) parent.findViewById(R.id.progbarListBare);
			else
				pb = (ProgressBar) parent.findViewById(R.id.progbarListList);
			pb.setVisibility(View.GONE);

	    }
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
	    setIntent(intent);
	    HandleIntent(intent);
	}


	@Override
	public void onStart() {
		super.onStart();
	}
	/**
	 * Determine what to do based on intent
	 * @param intent
	 */
    private void HandleIntent(Intent intent) {
    	if (intent.getAction().equals(Intent.ACTION_VIEW)) {
    		HandleUri(intent.getData());
    	} else if (intent.getAction().equals(Intent.ACTION_SEARCH)) {
    	      String query = intent.getStringExtra(SearchManager.QUERY);
    	      DoSearch(query);
    	}
    }
    
    /**
	 * Decode the URI to determine what to do.
	 * @param uri
	 */
	private void HandleUri(Uri uri) {
	
		if (!bare) {
			// Set the Spinner correctly
			sp.setSelection(urls.indexOf(uri.toString()));
			TextView listitle = (TextView) findViewById(R.id.textTraktListTitle);
			listitle.setText(strings[urls.indexOf(uri.toString())]);
		}
		String t = uri.getHost();
		if (t.equals("trending")) {
			usertrending = UserTrending.Trending;
		} else {
			usertrending = UserTrending.User;
		}
		
		SwitchList(uri.getPath(),true);
		SwitchUser(getIntent().getStringExtra("user"));
		    	
	}


	/**
     * Instantiate a search
     * @param q
     * @param sm
     */
    private void DoSearch(String q) {
		usertrending = UserTrending.Search;
		sp.setSelection(urls.indexOf("SEARCH"));
		TextView listitle = (TextView) findViewById(R.id.textTraktListTitle);
		listitle.setText("Search: " + q);

		DataGrabber dg = new DataGrabber(this) {
			@Override
			protected Boolean doInBackground(String... params) {
		    	JSONArray arr = null;
				//Search Shows
		   		data = traktapi.getDataArrayFromJSON("search/shows.json/%k/" + params[0],true);
		    	if (data==null) {
		    		return false;
		    	}
		    	//Search Movies
		   		arr = traktapi.getDataArrayFromJSON("search/movies.json/%k/" + params[0],true);
		    	if (arr==null) {
		    		return true;
		    	}
		   		for (int i=0;i<arr.length();i++) {
		    		data.put(arr.optJSONObject(i));
		    	}

				return true;
			}
		};
		
		dg.execute(URLEncoder.encode(q));
    }
    
    @Override
	public void onListItemClick (ListView l, View v, int position, long pos) {
		// Determine which item is selected then call TraktDetails Activity to show the details for this Show/Movie.
		JSONObject info = data.optJSONObject(position);

		// If mixed list with type parameter:
    	String t = info.optString("type");
    	if (t.equals("movie")) {
    		info = info.optJSONObject("movie");
    	} else if (t.equals("episode")) {
    		info = info.optJSONObject("show");
    	}

		String id = info.optString("tmdb_id");
		if(id.length()>0) { // Movie
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("tmdb:" + id),this,TraktDetails.class));
		} else { // Show
			// Changed to display episode list instead of Details view
			//startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("tvdb:" + info.getId()),this,TraktDetails.class));
			id = info.optString("tvdb_id");
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("tvdb:" + id),this,EpisodeList.class));
		}
	}
    
    private String AddWatchers(JSONObject info) {
    	String d;
		if (usertrending==UserTrending.Trending) {
        	d = String.format("Watchers: %d", info.optInt("watchers"));
    	} else {
    		d = "";
    	}
		return d;
    }

	private void MovieDetails(TextView title, TextView details,JSONObject info) {
    	title.setText(info.optString("title"));

		
		String d = "";
		
		d += AddWatchers(info);
		details.setText(d);
	}
    
	private void ShowDetails(TextView title, TextView details,JSONObject info) {
    	title.setText(info.optString("title"));

		
		String d = "";
    	
    	d += AddWatchers(info);
    	details.setText(d);
	}
	
	private void EpisodeDetails(TextView title,TextView details,JSONObject show, JSONObject episode, Long watchdate) {
		title.setText(String.format("%2$s - %1$tB %1$te, %1$tY",watchdate*1000,show.optString("title")));
		
		String d = String.format("%02dx%02d %s",episode.optInt("season"),episode.optInt("number"),episode.optString("title"));
		
		details.setText(d);
	}
	
	/**
	 * Adaptor for the List    	
	 * @author escabe
	 *
	 */
	class MovieShowAdapter extends BaseAdapter {
        public View getView(int position, View convertView, ViewGroup parent) {
			View row = convertView;
            if (row == null) { //Create new row when needed
                LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                row = vi.inflate(R.layout.trak_list_row, null);
            }
            JSONObject info = (JSONObject) getItem(position);
            if (info != null) {
            	JSONObject episode=null;
            	Long watchdate = null;
            	// If mixed list with type parameter:
            	String t = info.optString("type");
            	if (t.equals("movie")) {
            		watchdate = info.optLong("watched");
            		info = info.optJSONObject("movie");
            	} else if (t.equals("episode")) {
            		watchdate = info.optLong("watched");
            		episode = info.optJSONObject("episode");
            		info = info.optJSONObject("show");
            	}
            	
            	// Fill in information based on type
            	TextView title = (TextView)row.findViewById(R.id.textTitle);
            	TextView details = (TextView)row.findViewById(R.id.textDetails);

            	String id = info.optString("tmdb_id");
        		if(id.length()>0) { // Movie
        			MovieDetails(title,details,info);
        		} else { // Show
        			id = info.optString("tvdb_id");
        			if (episode!=null) { // Episode
        				EpisodeDetails(title,details,info,episode,watchdate);
        			} else { // Show
        				ShowDetails(title,details,info);
        			}
        		}
            	
            	// Poster
	    		JSONObject picts = info.optJSONObject("images");
            	ImageView poster = (ImageView)row.findViewById(R.id.imagePoster);
            	// Posters are retrieved through CWAC Thumbnail so set image URL as Tag
            	poster.setImageResource(R.drawable.emptyposter);
            	poster.setTag(traktapi.ResizePoster(picts.optString("poster"),1));

            	
            	// Display Loved, Hated and Watched icons
            	ImageView loved = (ImageView) row.findViewById(R.id.imageLoved);
            	ImageView hated = (ImageView) row.findViewById(R.id.imageHated);
            	ImageView watched = (ImageView) row.findViewById(R.id.imageWatched);
            	ImageView watchlist = (ImageView) row.findViewById(R.id.imageWatchlist);
            	
        		String rating = info.optString("rating");
    			loved.setVisibility( rating.equals("love") ? View.VISIBLE:View.GONE );
    			hated.setVisibility( rating.equals("hate") ? View.VISIBLE:View.GONE );
    			if (usertrending==UserTrending.Trending) {
    				watched.setVisibility( info.optBoolean("watched") ? View.VISIBLE:View.GONE );
    			} else if (usertrending==UserTrending.User) {
    				watched.setVisibility( info.optInt("plays") > 0 ? View.VISIBLE:View.GONE );
    			} else {
    				watched.setVisibility(View.GONE);
    			}
    			watchlist.setVisibility( info.optBoolean("in_watchlist") ? View.VISIBLE:View.GONE );
            }
            return(row);
		}

		public int getCount() {
			if (data==null)
				return 0;
			return data.length();
		}

		public Object getItem(int position) {
			if (data==null)
				return null;
			return data.optJSONObject(position);
		}

		public long getItemId(int position) {
			return position;
		}
	}
	
}
