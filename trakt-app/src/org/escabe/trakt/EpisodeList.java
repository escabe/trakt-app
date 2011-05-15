package org.escabe.trakt;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
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

public class EpisodeList extends ExpandableListActivity {
	private String TAG="EpisodeList";
	private String id;
	private String showname;

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
			showname = data.optString("title");
			return true;
		}
		
		@Override
	    protected void onPostExecute(Boolean result) {
			TextView title = (TextView) findViewById(R.id.textEpisodeDetailsTitle);
			title.setText(data.optString("title"));
			try {
				JSONObject images = data.getJSONObject("images");
				String p = images.optString("poster");
				p = p.replace(".jpg", "-138.jpg");
				ImageView poster = (ImageView) findViewById(R.id.imageEpisodeDetailsPoster);
				// Use CWAC Cache to retrieve the poster. Poster currently are pulled through a PHP script to resize
				cache.handleImageView(poster,"http://escabe.org/resize2.php?image=" + p , "myposter");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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
    		ImageView loved = (ImageView) findViewById(R.id.imageEpisodeDetailsLoved);
        	ImageView hated = (ImageView) findViewById(R.id.imageEpisodeDetailsHated);
        	String rating = data.optString("rating");
        	if (rating.equals("love")) loved.setBackgroundResource(R.drawable.ic_item_loved_active);
        	if (rating.equals("hate")) hated.setBackgroundResource(R.drawable.ic_item_hated_active);
			
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
		
		intent.putExtra("showname", showname);
		intent.putExtra("title",d.optString("title"));
		intent.putExtra("overview", d.optString("overview"));
		String p = d.optString("screen");
        p = p.replace(".jpg", "-218.jpg");
		intent.putExtra("poster", p);
		intent.putExtra("firstaired", d.optLong("first_aired"));
		intent.putExtra("watched", d.optBoolean("watched"));
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
				String p = d.optString("screen");
		        p = p.replace(".jpg", "-218.jpg");
				String url = "http://escabe.org/resize3.php?image=" + p;
				try {
					cache.handleImageView(poster, url, url);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				((ImageView)row.findViewById(R.id.imageEpisodeWatched)).setVisibility(d.optBoolean("watched") ? View.VISIBLE:View.GONE);
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
				title.setText(String.format("Season %d",s.optInt("season")));
				
				TextView details = (TextView)row.findViewById(R.id.textEpisodeListSeasonDetails);
				details.setText(String.format("Episodes: %d",s.optJSONArray("episodes").length()));
				
				ImageView poster = (ImageView) row.findViewById(R.id.imageEpisodeListSeasonPoster);
				String url = "http://escabe.org/resize.php?image=" + s.optString("poster");
				try {
					cache.handleImageView(poster, url, url);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
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
    
    
}
