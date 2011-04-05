package org.escabe.trakt;

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

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class TraktList extends ListActivity {

	private ArrayList<MovieShowInformation> data=null;
	private static final int[] IMAGE_IDS={R.id.imagePoster};
	private ThumbnailAdapter thumbs=null;
	private Runnable fillList;
	private ProgressDialog progressdialog;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.trakt_list);
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
		
		data=(ArrayList<MovieShowInformation>)getLastNonConfigurationInstance();
		
		if (data==null) {
			data = new ArrayList<MovieShowInformation>();
			thumbs = new ThumbnailAdapter(this, new MovieShowAdapter(), ((Application)getApplication()).getCache(),IMAGE_IDS);
			setListAdapter(thumbs);
			
			fillList = new Runnable() {
				public void run() {
					ShowList("movies/trending.json/%k",false);
				}};
			
			thumbs.notifyDataSetInvalidated();
			
			Thread thread =  new Thread(null, fillList);
	        thread.start();
	        
	        progressdialog = ProgressDialog.show(this,    
	              "Please wait...", "Retrieving data ...", true);
		}
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		return(data);
	}
	
	    	

	private void ShowList(String url,boolean login) {
    	JSONArray arr = null;
    	//Get list
   		arr = TraktAPI.getDataArrayFromJSON(url,login);
    	// Clear current Movie Array
    	data.clear();
    	// Notify ListView adapter of change
    	

    	//Re-Fill the Movie Array
    	try {
    		//For all items
	    	for (int i=0;i<arr.length();i++) {
	    		MovieShowInformation info = new MovieShowInformation();
	    		// Get item from array
	    		JSONObject obj = arr.getJSONObject(i);
	    		// Get poster
	    		JSONObject picts = obj.getJSONObject("images");
	    		String p = picts.getString("poster");
	    		p = p.replace(".jpg", "-138.jpg");
	    		info.setPoster(p);
	    		// Get title
	    		info.setTitle(obj.getString("title"));
	    		// Get number of watchers

	    		// Save ID
    			info.setId(obj.getString("tmdb_id"));
	    		
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
            	poster.setTag(info.getPoster());
            }
            return(row);
		}
	}
	
}
