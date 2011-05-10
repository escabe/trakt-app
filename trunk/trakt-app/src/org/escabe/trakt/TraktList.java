package org.escabe.trakt;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.escabe.trakt.TraktAPI.ShowMovie;
import org.json.JSONArray;
import org.json.JSONException;
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
import android.widget.ArrayAdapter;
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
	private ArrayList<MovieShowInformation> data=null;

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
	    data=(ArrayList<MovieShowInformation>)getLastNonConfigurationInstance();
		if (data==null) {
			// Create new list if none was saved
			data = new ArrayList<MovieShowInformation>();
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
			JSONArray arr = null;
			//Get list
			arr = traktapi.getDataArrayFromJSON(params[0],true);
			// Clear current data Array
			data.clear();
			// Parse the data
			ParseRespone(arr);

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
		    	data.clear();
		    	//Search Shows
		   		arr = traktapi.getDataArrayFromJSON("search/shows.json/%k/" + params[0],true);
		    	ParseRespone(arr);

		    	//Search Movies
		    	arr = traktapi.getDataArrayFromJSON("search/movies.json/%k/" + params[0],true);
		    	ParseRespone(arr);

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
    
    /**
	 * Parses JSONArray into MovieShowInformation data array.
	 * @param arr
	 */
	private void ParseRespone(JSONArray arr) {
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

	    		// Check whether show or movie based on type of ID given
	    		String id = obj.optString("tmdb_id");
	    		if(id.length()>0) {
	    			info.setShowmovie(ShowMovie.Movie);
	    		} else {
	    			info.setShowmovie(ShowMovie.Show);
	    			id =obj.optString("tvdb_id"); 
	    		}
	    		
    			info.setId(id);

    			data.add(info);
	    	}
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

	}
	
	@Override
	public Object onRetainNonConfigurationInstance() {
		return(data);
	}
	
	@Override
	public void onListItemClick (ListView l, View v, int position, long id) {
		// Determine which item is selected then call TraktDetails Activity to show the details for this Show/Movie.
		MovieShowInformation info = data.get(position);
		if (info.getShowmovie() == ShowMovie.Movie) {
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
            	poster.setTag("http://escabe.org/resize.php?image=" + info.getPoster());
            	
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
