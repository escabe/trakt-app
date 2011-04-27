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
	
	private HashMap<String,LovedHatedWatched> lovedhatedwatched=null;
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
		private boolean watched;
		
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
		public void setWatched(boolean watched) {
			this.watched = watched;
		}
		public boolean isWatched() {
			return watched;
		}
		
	}
	
	/**
	 * Class for holding information about a Season
	 * @author escabe
	 *
	 */
	private class Season {
		private int number=0;
		private int episodes=0;
		private String poster;
		
		public void setNumber(int number) {
			this.number = number;
		}
		public int getNumber() {
			return number;
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
		data = traktapi.getDataObjectFromJSON("show/summary.json/%k/" + id + "/extended",true);
		showname = data.optString("title");
		try {
			JSONArray d = data.getJSONArray("seasons");
			for (int i=0;i<d.length();i++) {
				JSONObject dd = d.getJSONObject(i);
				Season s = new Season();
				s.setEpisodes(dd.optInt("episodes"));
				s.setNumber(dd.optInt("season"));
	
				JSONObject images = dd.getJSONObject("images");
	    		String p = images.optString("poster");
	    		p = p.replace(".jpg", "-138.jpg");
	    		s.setPoster(p);
	
	    		JSONArray ddd = dd.getJSONArray("episodes");

	    		s.setEpisodes(ddd.length());
	    		
	    		ArrayList<Episode> el = new ArrayList<Episode>();
	    		for (int j=0;j<ddd.length();j++) {
					JSONObject dddd = ddd.getJSONObject(j);
					Episode e = new Episode();
					e.setNumber(dddd.optInt("episode"));
					e.setTitle(dddd.optString("title"));
					e.setFirstaired(dddd.optLong("first_aired"));
					e.setOverview(dddd.optString("overview"));
					e.setWatched(dddd.optBoolean("watched"));
					images = dddd.getJSONObject("images");
		    		p = images.optString("screen");
		    		p = p.replace(".jpg", "-218.jpg");
		    		e.setPoster(p);
	
		    		el.add(e);    			
	    		}
	    		seasons.add(s);
	    		episodes.add(el);
			}
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			Log.e(TAG,e1.toString());
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
	
	@Override
	public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
		Episode e = episodes.get(groupPosition).get(childPosition);
		Season s = seasons.get(groupPosition);
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("episode://tvdb/" + this.id +"/" + s.getNumber() + "/" + e.getNumber() ),this,TraktEpisodeDetails.class);
		
		intent.putExtra("showname", showname);
		intent.putExtra("title",e.getTitle());
		intent.putExtra("overview", e.getOverview());
		intent.putExtra("poster", e.getPoster());
		intent.putExtra("firstaired", e.getFirstaired());
		intent.putExtra("watched", e.isWatched());
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
    
    private void FillInDetails() {
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
    	if (lovedhatedwatched==null)
    		lovedhatedwatched=((Application)getApplication()).getLovedHatedWatched();
    	
    	// Check if Thread has already retrieved all info
    	if(lovedhatedwatched!=null) {
    		LovedHatedWatched lhw = lovedhatedwatched.get(id);
    		if (lhw!=null) {
	    		ImageView loved = (ImageView) findViewById(R.id.imageEpisodeDetailsLoved);
	        	ImageView hated = (ImageView) findViewById(R.id.imageEpisodeDetailsHated);
	        	if (lhw.isLoved()) loved.setBackgroundResource(R.drawable.lovedactive);
	        	if (lhw.isHated()) hated.setBackgroundResource(R.drawable.hatedactive);
    		}
    	}

    }
    
	/**
	 * Called by SwitchList on UiThread when data has been retrieved
	 */
	private Runnable UpdateComplete = new Runnable() {

		public void run() {
			// Fill in details
			FillInDetails();
			
			// Notify list that data has been retrieved
			adapter.notifyDataSetChanged();

			
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
		private ArrayList<Season> seasons;
		private ArrayList<ArrayList<Episode>> episodes;
	
	    public EpisodeListAdapter(Context context, ArrayList<Season> seasons, ArrayList<ArrayList<Episode>> episodes) {
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
			
			((ImageView)row.findViewById(R.id.imageEpisodeWatched)).setVisibility(e.isWatched() ? View.VISIBLE:View.GONE);
			
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
