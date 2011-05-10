package org.escabe.trakt;

import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.commonsware.cwac.thumbnail.ThumbnailAdapter;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;
/**
 * Activity to Display lists of Shows or Movies
 * @author escabe
 *
 */
public class TraktList extends ListActivity {
	// Main ArrayList holding the currently displayed data
	private JSONArray data=null;

	// For CWAC Thumbnail
	private static final int[] IMAGE_IDS={R.id.imagePoster};
	private ThumbnailAdapter thumbs=null;
	private static boolean initial;
	private Spinner sp;
	private TraktAPI traktapi=null;

	// To hold information about what we are currently looking at
	
	private enum UserTrending { User, Trending, Search}
	private UserTrending usertrending;
	
	private HashMap<String,LovedHatedWatched> lovedhatedwatched=null;
	
	// Strings for the spinner
	private String[] strings = {"Trending Shows","Trending Movies",
			"All Your Shows","All Your Movies","Search"};
	
	private String[] uu = new String[] {"trakt://shows/trending","trakt://movies/trending",
			"trakt://user/library/shows/all","trakt://user/library/movies/all","SEARCH"};
	
	private List<String> urls; 			

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.trakt_list);
	    // Initialize API
	    
	    traktapi = new TraktAPI(this);
	    urls = Arrays.asList(uu);
	    // Do the following here instead of in onStart such that this is not repeated when returning from Details Activity
	    
	    // Retrieve current list if was suspended
	    data=(JSONArray)getLastNonConfigurationInstance();

	    if (data==null) {
			// Initialize CWAC Thumbnail for posters
			thumbs = new ThumbnailAdapter(this, new MovieShowAdapter(), ((Application)getApplication()).getThumbsCache(),IMAGE_IDS);
			// Assign the adaptor to the list
			setListAdapter(thumbs);
		}
		
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
						HandleUri(urls.get(position));
					}
				else
					initial = false;
			}
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub
			}
		});			

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
	
	private class DataGrabber extends AsyncTask<String,Void,Boolean> {
		private ProgressDialog progressdialog;
		private Context parent;
		
		public DataGrabber(Context parent) {
			this.parent = parent;
		}
		
		@Override
		protected void onPreExecute() {
			thumbs.notifyDataSetInvalidated();
		    progressdialog = ProgressDialog.show(parent,"", "Retrieving data ...", true);
		}
		
		@Override
		protected Boolean doInBackground(String... params) {
			//Get list
			data = traktapi.getDataArrayFromJSON(params[0],true);
			return true;
		}
		@Override
	    protected void onPostExecute(Boolean result) {
			thumbs.notifyDataSetChanged();
	        // Scroll to first item
			setSelection(0);
	        // Hide progress dialog
			progressdialog.dismiss();

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
    		HandleUri(intent.getDataString());
    	} else if (intent.getAction().equals(Intent.ACTION_SEARCH)) {
    	      String query = intent.getStringExtra(SearchManager.QUERY);
    	      DoSearch(query);
    	}
    }
    
    /**
     * Instantiate a search
     * @param q
     * @param sm
     */
    private void DoSearch(String q) {
		usertrending = UserTrending.Search;
		sp.setSelection(urls.indexOf("SEARCH"));
		
		DataGrabber dg = new DataGrabber(this) {
			@Override
			protected Boolean doInBackground(String... params) {
		    	JSONArray arr = null;
				//Search Shows
		   		data = traktapi.getDataArrayFromJSON("search/shows.json/%k/" + params[0],true);
		    	
		    	//Search Movies
		   		arr = traktapi.getDataArrayFromJSON("search/movies.json/%k/" + params[0],true);
		    	for (int i=0;i<arr.length();i++) {
		    		data.put(arr.optJSONObject(i));
		    	}

				return true;
			}
		};
		
		dg.execute(URLEncoder.encode(q));
    }
    
    /**
     * Decode the URI to determine what to do.
     * @param uri
     */
    private void HandleUri(String uri) {

    	// Set the Spinner correctly
    	sp.setSelection(urls.indexOf(uri));
    	
    	// Consider to make this more a parser which will automatically call SwitchList with correct URL
    	if (uri.equals("trakt://movies/trending")) { //Trending Movies

			usertrending = UserTrending.Trending;
			SwitchList("movies/trending.json/%k",true);
		} else if (uri.equals("trakt://shows/trending")) { //Trending Shows

			usertrending = UserTrending.Trending;
			SwitchList("shows/trending.json/%k",true);
		} else if (uri.equals("trakt://user/library/shows/all")) { //User Shows Library

			usertrending = UserTrending.User;
			SwitchList("user/library/shows/all.json/%k/%u",true);
		}else if (uri.equals("trakt://user/library/movies/all")) { //User Movies Library

			usertrending = UserTrending.User;
			SwitchList("user/library/movies/all.json/%k/%u",true);
		}else if (uri.startsWith("trakt://search/movies")) {
			String[] s = uri.split("/");
			DoSearch(s[s.length-1]);
		}else if (uri.startsWith("trakt://search/shows")) {
			String[] s = uri.split("/");
			DoSearch(s[s.length-1]);
		}
    	    	
    }
    
	@Override
	public Object onRetainNonConfigurationInstance() {
		return(data);
	}
	
	@Override
	public void onListItemClick (ListView l, View v, int position, long pos) {
		// Determine which item is selected then call TraktDetails Activity to show the details for this Show/Movie.
		JSONObject info = data.optJSONObject(position);
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
            	// Fill in basic information
            	TextView title = (TextView)row.findViewById(R.id.textTitle);
            	title.setText(info.optString("title"));

            	// Poster
	    		JSONObject picts = info.optJSONObject("images");
	    		String p = picts.optString("poster");
	    		p = p.replace(".jpg", "-138.jpg");
            	ImageView poster = (ImageView)row.findViewById(R.id.imagePoster);
            	// Posters are retrieved through CWAC Thumbnail so set image URL as Tag
            	poster.setImageResource(R.drawable.emptyposter);
            	poster.setTag("http://escabe.org/resize.php?image=" + p);
            	
            	TextView details = (TextView)row.findViewById(R.id.textDetails);
            	// Only show number of watchers when this information is available
            	if (usertrending==UserTrending.Trending) {
	            	String d = String.format("Watchers: %d", info.optInt("watchers"));
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

        		String id = info.optString("tmdb_id");
        		if(id.length()>0) { // Movie
        		} else { // Show
        			id = info.optString("tvdb_id");
        		}
            	
            	// Check if Thread has already retrieved all info
            	if(lovedhatedwatched!=null) {
            		LovedHatedWatched lhw = lovedhatedwatched.get(id);
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
