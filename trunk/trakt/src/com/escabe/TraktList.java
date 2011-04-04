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
package com.escabe;

import java.io.File;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.commonsware.cwac.cache.AsyncCache;
import com.commonsware.cwac.cache.SimpleWebImageCache;
import com.commonsware.cwac.thumbnail.ThumbnailAdapter;
import com.commonsware.cwac.thumbnail.ThumbnailBus;
import com.commonsware.cwac.thumbnail.ThumbnailMessage;
import com.escabe.trakt.MyView;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

public class TraktList {
	private ListView lv;

	private ArrayList<Movie> MovieList = new ArrayList<Movie>();
	private MovieAdapter ma;
	private ThumbnailAdapter ta;
	private Details details;
	private boolean showPosters = true;
	private Spinner spinner;
	private boolean trending;
	private String type;
	
	private AsyncCache.DiskCachePolicy policy=new AsyncCache.DiskCachePolicy() {
		public boolean eject(File file) {
			return(System.currentTimeMillis()-file.lastModified()>1000*60*60*24*7);
		}
	};
	
	private ThumbnailBus bus=new ThumbnailBus();
	private SimpleWebImageCache<ThumbnailBus, ThumbnailMessage> cache=
							new SimpleWebImageCache<ThumbnailBus, ThumbnailMessage>(trakt.instance.getCacheDir(), policy, 101, bus);

	private static final int[] IMAGE_IDS={R.id.imageRow};
	
	
  	public TraktList(Activity activity) {
		//parentActivity = activity;
        details = Details.getInstance(activity);
        
		lv = (ListView)trakt.instance.findViewById(R.id.listMainList);
        ma = new MovieAdapter(trakt.instance,R.layout.row,MovieList);
        
        ta = new ThumbnailAdapter(trakt.instance, ma,cache,IMAGE_IDS);
        lv.setAdapter(ta);
        
        lv.setOnItemClickListener(new lvOnItemClick());
        spinner = (Spinner) trakt.instance.findViewById(R.id.spinTrakt);
	}
		
	private class lvOnItemClick implements AdapterView.OnItemClickListener {

		public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
			Movie m = MovieList.get(pos);
			details.showDetails(type, m.getID());
		}
	}
	
	// Adapter to show current List of Movies in the ListView
	private class MovieAdapter extends ArrayAdapter<Movie> {
		private ArrayList<Movie> items;
		public MovieAdapter(Context context, int textViewResourceId, ArrayList<Movie> items) {
			super(context, textViewResourceId, items);
			this.items = items;
		}
		
		// Fill in a Row
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
                View v = convertView;
                if (v == null) {
                    LayoutInflater vi = (LayoutInflater)trakt.instance.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    v = vi.inflate(R.layout.row, null);
                }
                Movie o = items.get(position);
                if (o != null) {
                        TextView tt = (TextView) v.findViewById(R.id.textRowTitle);
                        tt.setText(o.getTitle());                            

                        
                        TextView ti = (TextView) v.findViewById(R.id.textRowInfo);
                    	if (trending) {
	                        String d = String.format("Watchers: %d", o.getWatchers());
	                    	ti.setText(d);
                    	} else {
                    		ti.setText("");
                    	}

                    	ImageView iv = (ImageView) v.findViewById(R.id.imageRow);
                    	iv.setImageResource(R.drawable.emptyposter);
                    	if (o.getPoster()!="") {
                    		iv.setTag("http://escabe.org/resize.php?image=" + o.getPoster());
                    	}	
                    	
                    	if (type=="Shows") {
                    		// No Watched
                    		((ImageView)v.findViewById(R.id.imageRowWatched)).setVisibility(View.GONE);
                    		// Loved
                    		if (TraktApi.UserSeries.Loved(o.getID())) {
                    			((ImageView)v.findViewById(R.id.imageRowLoved)).setVisibility(View.VISIBLE);
                    		} else {
                    			((ImageView)v.findViewById(R.id.imageRowLoved)).setVisibility(View.GONE);
                    		}
                    		// Hated
                    		if (TraktApi.UserSeries.Hated(o.getID())) {
                    			((ImageView)v.findViewById(R.id.imageRowHated)).setVisibility(View.VISIBLE);
                    		} else {
                    			((ImageView)v.findViewById(R.id.imageRowHated)).setVisibility(View.GONE);
                    		}
                    	} else { // Movies
                    		// Watched
                    		if (TraktApi.UserMovies.Watched(o.getID())) {
                    			((ImageView)v.findViewById(R.id.imageRowWatched)).setVisibility(View.VISIBLE);
                    		} else {
                    			((ImageView)v.findViewById(R.id.imageRowWatched)).setVisibility(View.GONE);
                    		}                    		
                    		// Loved
                    		if (TraktApi.UserMovies.Loved(o.getID())) {
                    			((ImageView)v.findViewById(R.id.imageRowLoved)).setVisibility(View.VISIBLE);
                    		} else {
                    			((ImageView)v.findViewById(R.id.imageRowLoved)).setVisibility(View.GONE);
                    		}
                    		// Hated
                    		if (TraktApi.UserMovies.Hated(o.getID())) {
                    			((ImageView)v.findViewById(R.id.imageRowHated)).setVisibility(View.VISIBLE);
                    		} else {
                    			((ImageView)v.findViewById(R.id.imageRowHated)).setVisibility(View.GONE);
                    		}
                    	}
                }
                return v;
        }

	}
	
	public void ShowTrending(String t) {
		type = t;
		trending = true;
		
		if (t=="Movies") {
	    	trakt.instance.myflipper.FlipTo(MyView.TRAKTLIST);
	    	TextView tv = (TextView)trakt.instance.findViewById(R.id.textTitle);
	    	tv.setText("Trending Movies");
	    	ShowList("movies/trending.json/%k",false);
		} else {
	    	trakt.instance.myflipper.FlipTo(MyView.TRAKTLIST);
	    	TextView tv = (TextView)trakt.instance.findViewById(R.id.textTitle);
	    	tv.setText("Trending Shows");
	    	ShowList("shows/trending.json/%k",false);
		}
	}
	
	public void ShowUserList(String t,String l) {
		type = t;
		trending = false;
		if (t=="Movies") {
	    	trakt.instance.myflipper.FlipTo(MyView.TRAKTLIST);
	    	TextView tv = (TextView)trakt.instance.findViewById(R.id.textTitle);
	    	tv.setText(l + "/" + t);
	    	ShowList("user/library/movies/" + l + ".json/%k/%u",true);
		} else {
	    	trakt.instance.myflipper.FlipTo(MyView.TRAKTLIST);
	    	TextView tv = (TextView)trakt.instance.findViewById(R.id.textTitle);
	    	tv.setText(l + "/" + t);
	    	ShowList("user/library/shows/" + l + ".json/%k/%u",true);
		}		
	
	}
	
    public void ShowList(String url,boolean login) {
    	JSONArray arr = null;
    	//Get list
   		arr = TraktApi.getDataArrayFromJSON(url,login);
    	// Clear current Movie Array
    	MovieList.clear();
    	// Notify ListView adapter of change
    	ta.notifyDataSetInvalidated();

    	//Re-Fill the Movie Array
    	try {
    		//For all items
	    	for (int i=0;i<arr.length();i++) {
	    		Movie m = new Movie();
	    		// Get item from array
	    		JSONObject obj = arr.getJSONObject(i);
	    		// Get poster
	    		JSONObject picts = obj.getJSONObject("images");
	    		String p = picts.getString("poster");
	    		p = p.replace(".jpg", "-138.jpg");
	    		m.setPoster(p);
	    		// Get title
	    		m.setTitle(obj.getString("title"));
	    		// Get number of watchers
	    		m.setWatchers(obj.optInt("watchers"));
	    		// Save ID
	    		
	    		if (type=="Shows") {
	    			m.setID(obj.getString("tvdb_id"));
	    		} else {
	    			m.setID(obj.getString("tmdb_id"));	
	    		}
	    		
	    		MovieList.add(m);
	    	}
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        ta.notifyDataSetChanged();
        lv.setSelection(0);
	}
}
