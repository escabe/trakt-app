package org.escabe.trakt;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.commonsware.cwac.cache.WebImageCache;

import android.app.ExpandableListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;

public class EpisodeList extends ExpandableListActivity {
	private String id;
	
	private ArrayList<Season> seasons=null;
	private ArrayList<ArrayList<Episode>> episodes = null;
	
	private EpisodeListAdapter adapter;
	private TraktAPI traktapi;
	private ProgressDialog progressdialog;
	private WebImageCache cache = null;

	/**
	 * Class for holding information about a single episode
	 * @author escabe
	 *
	 */
	private class Episode {
		private int number=0;
		private String title;
		private String poster;
		private long firstaired;
		private String overview;
		
		public void setNumber(int number) {
			this.number = number;
		}
		public int getNumber() {
			return number;
		}
		public void setTitle(String title) {
			this.title = title;
		}
		public String getTitle() {
			return title;
		}
		public void setPoster(String poster) {
			this.poster = poster;
		}
		public String getPoster() {
			return poster;
		}
		public void setFirstaired(long firstaired) {
			this.firstaired = firstaired;
		}
		public long getFirstaired() {
			return firstaired;
		}
		public void setOverview(String overview) {
			this.overview = overview;
		}
		public String getOverview() {
			return overview;
		}
		
	}
	
	/**
	 * Class for holding information about a Season
	 * @author escabe
	 *
	 */
	private class Season {
		private int number=0;
		private int year=0;
		private int episodes=0;
		private String poster;
		
		public void setNumber(int number) {
			this.number = number;
		}
		public int getNumber() {
			return number;
		}
		public void setYear(int year) {
			this.year = year;
		}
		public int getYear() {
			return year;
		}
		public void setEpisodes(int episodes) {
			this.episodes = episodes;
		}
		public int getEpisodes() {
			return episodes;
		}
		public void setPoster(String poster) {
			this.poster = poster;
		}
		public String getPoster() {
			return poster;
		}
	}
	
	private Runnable fillList;
	
	/**
	 * Off-loads retrieving data to separate Tread
	 */
	private void SwitchList() {
		fillList = new Runnable() {
			public void run() {
				ShowList();
			}};
		
		adapter.notifyDataSetInvalidated();
		
		Thread thread =  new Thread(null, fillList);
        thread.start();
        
        progressdialog = ProgressDialog.show(this,"", "Retrieving data ...", true);

	}

	/**
	 * Actual method in which the data is retrieved
	 */
	private void ShowList() {
		// Get overview of Seasons
		JSONArray data = traktapi.getDataArrayFromJSON("show/seasons.json/%k/" + id);
		for (int i=0;i<data.length();i++) { // For each Season
			try {
				// Season info
				JSONObject d = data.getJSONObject(i);
				Season s = new Season();
				s.setEpisodes(d.optInt("episodes"));
				s.setNumber(d.optInt("season"));

				JSONObject images = d.getJSONObject("images");
	    		String p = images.optString("poster");
	    		p = p.replace(".jpg", "-138.jpg");
	    		s.setPoster(p);

	    		seasons.add(s);

	    		// Episode info per season
				ArrayList<Episode> el = new ArrayList<Episode>();
				JSONArray sdata = traktapi.getDataArrayFromJSON("show/season.json/%k/" + id + "/" + s.getNumber());
				for (int j=0;j<sdata.length();j++) { // For each episode in a season
					JSONObject sd = sdata.getJSONObject(j);
					Episode e = new Episode();
					e.setNumber(sd.optInt("episode"));
					e.setTitle(sd.optString("title"));
					e.setFirstaired(sd.optLong("first_aired"));
					e.setOverview(sd.optString("overview"));
					
					images = sd.getJSONObject("images");
		    		p = images.optString("screen");
		    		p = p.replace(".jpg", "-218.jpg");
		    		e.setPoster(p);

		    		el.add(e);
				}
				episodes.add(el);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		runOnUiThread(UpdateComplete);
	}
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.trakt_episode_list);
		seasons = new ArrayList<Season>();
		episodes = new ArrayList<ArrayList<Episode>>();
		traktapi = new TraktAPI(this);
	    
		cache = ((Application)getApplication()).getCache();
		
		adapter = new EpisodeListAdapter(this,seasons,episodes); 
		
		setListAdapter(adapter);
		
	    HandleIntent(getIntent());
		
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
    
	
	/**
	 * Parse Intent URI to determine for which Show seasons/episodes will be shown
	 * @param uri	URI from Intent 
	 */
    private void HandleUri(Uri uri) {
		if (uri.getScheme().equals("tmdb")) {
			id = uri.getSchemeSpecificPart();
			SwitchList();
		}
    }
    
	/**
	 * Called by SwitchList on UiThread when data has been retrieved
	 */
	private Runnable UpdateComplete = new Runnable() {

		public void run() {
			// Notify list that data has been retrieved
			adapter.notifyDataSetChanged();
	        // Scroll to first item
	        // Hide progress dialog
			progressdialog.dismiss();
		}
	};
	
	/**
	 * Class for displaying information in the ExpandableList
	 * @author escabe
	 *
	 */
	class EpisodeListAdapter extends BaseExpandableListAdapter {
		private Context context;
	
		private ArrayList<Season> seasons;
		private ArrayList<ArrayList<Episode>> episodes;
	
	    public EpisodeListAdapter(Context context, ArrayList<Season> seasons, ArrayList<ArrayList<Episode>> episodes) {
	        this.context = context;
	        this.seasons = seasons;
	        this.episodes = episodes;
	    }
		    
		/**
		 * Display an Episode row
		 */
		public View getChildView(int groupPosition, int childPosition,
				boolean isLastChild, View row, ViewGroup parent) {
			Episode e = (Episode) getChild(groupPosition,childPosition);
			Season s = (Season) getGroup(groupPosition);
			if (row==null) {
                LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                row = vi.inflate(R.layout.trakt_episode_episode, null);
			}
			TextView title = (TextView) row.findViewById(R.id.textEpisodeListEpisodeTitle);
			title.setText(String.format("%02dx%02d: %s",s.getNumber(),e.getNumber(),e.getTitle()));

			TextView details = (TextView) row.findViewById(R.id.textEpisodeListEpisodeDetails);
			details.setText(String.format("First Aired: %1$tB %1$te, %1$tY",e.getFirstaired()*1000));
			
			ImageView poster = (ImageView) row.findViewById(R.id.imageEpisodeListEpisodePoster);
			String url = "http://escabe.org/resize3.php?image=" + e.getPoster();
			try {
				cache.handleImageView(poster, url, url);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			return row;
		}

		/**
		 * Display a Season row
		 */
		public View getGroupView(int groupPosition, boolean isExpanded,
				View row, ViewGroup parent) {
			Season s = (Season)getGroup(groupPosition);
			if (row==null) {
                LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                row = vi.inflate(R.layout.trakt_episode_season, null);
			}
			TextView title = (TextView)row.findViewById(R.id.textEpisodeListSeasonTitle);
			title.setText(String.format("Season %d",s.getNumber()));
			
			TextView details = (TextView)row.findViewById(R.id.textEpisodeListSeasonDetails);
			details.setText(String.format("Episodes: %d",s.getEpisodes()));

			
			ImageView poster = (ImageView) row.findViewById(R.id.imageEpisodeListSeasonPoster);
			String url = "http://escabe.org/resize.php?image=" + s.getPoster();
			try {
				cache.handleImageView(poster, url, url);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			
			return row;
		}	   
		
		public Object getChild(int groupPosition, int childPosition) {
			return episodes.get(groupPosition).get(childPosition);
		}

		public long getChildId(int groupPosition, int childPosition) {
			return childPosition;
		}

		
		public int getChildrenCount(int groupPosition) {
			return episodes.get(groupPosition).size();
		}

		public Object getGroup(int groupPosition) {
			return seasons.get(groupPosition);
		}

		public int getGroupCount() {
			return seasons.size();
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
