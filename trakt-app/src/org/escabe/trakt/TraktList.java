package org.escabe.trakt;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.commonsware.cwac.thumbnail.ThumbnailAdapter;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class TraktList extends ListActivity {

	private ArrayList<MovieShowInformation> data=null;
	private static final int[] IMAGE_IDS={R.id.imagePoster};
	private ThumbnailAdapter thumbs=null;
	private Runnable fillList;
	private ProgressDialog progressdialog;
	private TraktAPI traktapi=null;
	private enum ShowMovie {Show, Movie}
	private ShowMovie showmovie;
	
	private enum UserTrending { User, Trending}
	private UserTrending usertrending;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.trakt_list);

	    traktapi = new TraktAPI(this);
		data=(ArrayList<MovieShowInformation>)getLastNonConfigurationInstance();
		if (data==null) {
			data = new ArrayList<MovieShowInformation>();
			thumbs = new ThumbnailAdapter(this, new MovieShowAdapter(), ((Application)getApplication()).getCache(),IMAGE_IDS);
			setListAdapter(thumbs);
		}
		HandleIntent(getIntent());

	
	}

	private Runnable UpdateComplete = new Runnable() {

		public void run() {
			thumbs.notifyDataSetChanged();
	        setSelection(0);
	        progressdialog.dismiss();
		}
	};
	
	@Override
	public void onStart() {
		super.onStart();
	}

    private void HandleIntent(Intent intent) {
    	if (intent.getAction().equals(Intent.ACTION_VIEW)) {
    		HandleUri(intent.getDataString());
    	}
    
    }
    
    private void HandleUri(String uri) {
		if (uri.equals("trakt://movies/trending")) {
			showmovie = ShowMovie.Movie;
			usertrending = UserTrending.Trending;
			SwitchList("movies/trending.json/%k",false);
		} else if (uri.equals("trakt://shows/trending")) {
			showmovie = ShowMovie.Show;
			usertrending = UserTrending.Trending;
			SwitchList("shows/trending.json/%k",false);
		} else if (uri.equals("trakt://user/library/shows/all")) {
			showmovie = ShowMovie.Show;
			usertrending = UserTrending.User;
			SwitchList("user/library/shows/all.json/%k/%u",true);
		}else if (uri.equals("trakt://user/library/movies/all")) {
			showmovie = ShowMovie.Movie;
			usertrending = UserTrending.User;
			SwitchList("user/library/movies/all.json/%k/%u",true);
		}
    }
    
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
	
	@Override
	public Object onRetainNonConfigurationInstance() {
		return(data);
	}
	
	@Override
	public void onListItemClick (ListView l, View v, int position, long id) {
		MovieShowInformation info = data.get(position);
		if (showmovie == ShowMovie.Movie) {
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("tmdb:" + info.getId()),this,TraktDetails.class));
		} else {
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("tvdb:" + info.getId()),this,TraktDetails.class));
		}
	}

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
	    	
	class MovieShowAdapter extends ArrayAdapter<MovieShowInformation> {
		MovieShowAdapter() {
			super(TraktList.this,R.layout.trak_list_row,data);
		}
		
		@Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            if (row == null) {
                LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                row = vi.inflate(R.layout.trak_list_row, null);
            }
            MovieShowInformation info = getItem(position);
            if (info != null) {
            	TextView title = (TextView)row.findViewById(R.id.textTitle);
            	title.setText(info.getTitle());

            	ImageView poster = (ImageView)row.findViewById(R.id.imagePoster);
            	poster.setImageResource(R.drawable.emptyposter);
            	poster.setTag(info.getPoster());
            	
            	TextView details = (TextView)row.findViewById(R.id.textDetails);
            	String d = String.format("Watchers: %d", info.getWatchers());
            	details.setText(d);
            }
            return(row);
		}
	}
	
}
