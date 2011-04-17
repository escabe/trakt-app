package org.escabe.trakt;

import java.util.ArrayList;
import java.util.HashMap;

import org.escabe.trakt.TraktAPI.ShowMovie;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.commonsware.cwac.thumbnail.ThumbnailAdapter;

import android.app.Activity;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
/**
 * Activity to Display lists of Shows or Movies
 * @author escabe
 *
 */
public class TraktList extends ListActivity {
	// Main ArrayList holding the currently displayed data
	private ArrayList<MovieShowInformation> data=null;
	// For CWAC Thumbnail
	private static final int[] IMAGE_IDS={R.id.imagePoster};
	private ThumbnailAdapter thumbs=null;

	private Runnable fillList;
	private ProgressDialog progressdialog;

	private TraktAPI traktapi=null;

	// To hold information about what we are currently looking at
	
	private ShowMovie showmovie;
	private enum UserTrending { User, Trending, Search}
	private UserTrending usertrending;
	
	private HashMap<String,LovedHatedWatched> lovedhatedwatched=null;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.trakt_list);
	    // Initialize API
	    traktapi = new TraktAPI(this);

	    // Do the following here instead of in onStart such that this is not repeated when returning from Details Activity
	    
	    // Retrieve current list if was suspended
	    data=(ArrayList<MovieShowInformation>)getLastNonConfigurationInstance();
		if (data==null) {
			// Create new list if none was saved
			data = new ArrayList<MovieShowInformation>();
			// Initialize CWAC Thumbnail for posters
			thumbs = new ThumbnailAdapter(this, new MovieShowAdapter(), ((Application)getApplication()).getThumbsCache(),IMAGE_IDS);
			// Assign the adaptor to the list
			setListAdapter(thumbs);
		}
		// Determine with which intent the Activity was started.
		HandleIntent(getIntent());

	
	}
	/**
	 * Called by SwitchList on UiThread when data has been retrieved
	 */
	private Runnable UpdateComplete = new Runnable() {

		public void run() {
			// Notify list that data has been retrieved
			thumbs.notifyDataSetChanged();
	        // Scroll to first item
			setSelection(0);
	        // Hide progress dialog
			progressdialog.dismiss();
		}
	};
	


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
    		HandleUri(intent.getDataString());
    	}
    }
    /**
     * Decode the URI to determine what to do.
     * @param uri
     */
    private void HandleUri(String uri) {
		// Consider to make this more a parser which will automatically call SwitchList with correct URL
    	if (uri.equals("trakt://movies/trending")) { //Trending Movies
			showmovie = ShowMovie.Movie;
			usertrending = UserTrending.Trending;
			SwitchList("movies/trending.json/%k",true);
		} else if (uri.equals("trakt://shows/trending")) { //Trending Shows
			showmovie = ShowMovie.Show;
			usertrending = UserTrending.Trending;
			SwitchList("shows/trending.json/%k",true);
		} else if (uri.equals("trakt://user/library/shows/all")) { //User Shows Library
			showmovie = ShowMovie.Show;
			usertrending = UserTrending.User;
			SwitchList("user/library/shows/all.json/%k/%u",true);
		}else if (uri.equals("trakt://user/library/movies/all")) { //User Movies Library
			showmovie = ShowMovie.Movie;
			usertrending = UserTrending.User;
			SwitchList("user/library/movies/all.json/%k/%u",true);
		}else if (uri.startsWith("trakt://search/movies")) {
			showmovie = ShowMovie.Movie;
			usertrending = UserTrending.Search;
			String[] s = uri.split("/");
			SwitchList("search/movies.json/%k/" + s[s.length-1],true);
		}else if (uri.startsWith("trakt://search/shows")) {
			showmovie = ShowMovie.Show;
			usertrending = UserTrending.Search;
			String[] s = uri.split("/");
			SwitchList("search/shows.json/%k/" + s[s.length-1],true);
		}
    }
    /**
     * Switch data in the current list. Will be done on separate Thread.
     * @param url
     * @param login
     */
	private void SwitchList(final String url,final boolean login) {
		fillList = new Runnable() {
			public void run() {
				ShowList(url,login);
			}};
		
		thumbs.notifyDataSetInvalidated();
		
		Thread thread =  new Thread(null, fillList);
        thread.start();
        
        progressdialog = ProgressDialog.show(this,"", "Retrieving data ...", true);

	}

	/**
	 * This should be run on a separate Thread. Retrieves the actual data from the server.
	 * @param url
	 * @param login
	 */
	private void ShowList(String url,boolean login) {
    	JSONArray arr = null;
    	//Get list
   		arr = traktapi.getDataArrayFromJSON(url,login);
    	// Clear current data Array
    	data.clear();

    	//Re-Fill the data Array
    	try {
    		//For all items
	    	for (int i=0;i<arr.length();i++) {
	    		MovieShowInformation info = new MovieShowInformation();
	    		// Get item from array
	    		JSONObject obj = arr.getJSONObject(i);
	    		// Get poster
	    		JSONObject picts = obj.getJSONObject("images");
	    		String p = picts.optString("poster");
	    		p = p.replace(".jpg", "-138.jpg");
	    		info.setPoster(p);
	    		// Get title
	    		info.setTitle(obj.optString("title"));
	    		// Get number of watchers
	    		if (usertrending==UserTrending.Trending) {
	    			info.setWatchers(obj.optInt("watchers"));
	    		}
	    		// Save ID
	    		if (showmovie == ShowMovie.Movie) {
	    			info.setId(obj.optString("tmdb_id"));
	    		} else {
	    			info.setId(obj.optString("tvdb_id"));
	    		}
	    		data.add(info);
	    	}
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        runOnUiThread(UpdateComplete);
	}
	
	@Override
	public Object onRetainNonConfigurationInstance() {
		return(data);
	}
	
	@Override
	public void onListItemClick (ListView l, View v, int position, long id) {
		// Determine which item is selected then call TraktDetails Activity to show the details for this Show/Movie.
		MovieShowInformation info = data.get(position);
		if (showmovie == ShowMovie.Movie) {
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("tmdb:" + info.getId()),this,TraktDetails.class));
		} else {
			// Changed to display episode list instead of Details view
			//startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("tvdb:" + info.getId()),this,TraktDetails.class));
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("tvdb:" + info.getId()),this,EpisodeList.class));
		}
	}

	
	/**
	 * Adaptor for the List    	
	 * @author escabe
	 *
	 */
	class MovieShowAdapter extends ArrayAdapter<MovieShowInformation> {
		MovieShowAdapter() {
			super(TraktList.this,R.layout.trak_list_row,data);
		}
		
		@Override
        public View getView(int position, View convertView, ViewGroup parent) {
			View row = convertView;
            if (row == null) { //Create new row when needed
                LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                row = vi.inflate(R.layout.trak_list_row, null);
            }
            MovieShowInformation info = getItem(position);
            if (info != null) {
            	// Fill in basic information
            	TextView title = (TextView)row.findViewById(R.id.textTitle);
            	title.setText(info.getTitle());

            	ImageView poster = (ImageView)row.findViewById(R.id.imagePoster);
            	
            	// Posters are retrieved through CWAC Thumbnail so set image URL as Tag
            	poster.setImageResource(R.drawable.emptyposter);
            	poster.setTag(info.getPoster());
            	
            	TextView details = (TextView)row.findViewById(R.id.textDetails);
            	// Only show number of watchers when this information is available
            	if (usertrending==UserTrending.Trending) {
	            	String d = String.format("Watchers: %d", info.getWatchers());
	            	details.setText(d);
            	} else {
            		details.setText("");
            	}
            	// Display Loved, Hated and Watched icons
            	if (lovedhatedwatched==null)
            		lovedhatedwatched=((Application)getApplication()).getLovedHatedWatched();
            	
            	ImageView loved = (ImageView) row.findViewById(R.id.imageLoved);
            	ImageView hated = (ImageView) row.findViewById(R.id.imageHated);
            	ImageView watched = (ImageView) row.findViewById(R.id.imageWatched);
            	
            	// Check if Thread has already retrieved all info
            	if(lovedhatedwatched!=null) {
            		LovedHatedWatched lhw = lovedhatedwatched.get(info.getId());
            		if (lhw!=null) {
            			loved.setVisibility( lhw.isLoved() ? View.VISIBLE:View.GONE );
            			hated.setVisibility( lhw.isHated() ? View.VISIBLE:View.GONE );
            			watched.setVisibility( lhw.isWatched() ? View.VISIBLE:View.GONE );
            		} else {
            			loved.setVisibility(View.GONE);
            			hated.setVisibility(View.GONE);
            			watched.setVisibility(View.GONE);
            		}
            	} else {
        			loved.setVisibility(View.GONE);
        			hated.setVisibility(View.GONE);
        			watched.setVisibility(View.GONE);
            	}
            }
            return(row);
		}
	}
	
}
