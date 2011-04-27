package org.escabe.trakt;

import java.util.ArrayList;

import org.escabe.trakt.TraktAPI.ShowMovie;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.commonsware.cwac.thumbnail.ThumbnailAdapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.AdapterView.OnItemClickListener;

/**
 * Reusable Component to display trakt information in the form of a horizontal scrolling poster strip.
 * @author escabe (idea Sebastian)
 *
 */
public class PosterView extends Gallery {
	private String TAG = "PosterView";
	private String url;
	private ArrayList<MovieShowInformation> data;
	private ThumbnailAdapter thumbs=null;
	private static final int[] IMAGE_IDS={R.id.imagePosterViewPoster};
	private TraktAPI traktapi;
	private ShowMovie showmovie;
	private Activity parent;
	
	public PosterView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		traktapi = new TraktAPI(context);
		data = new ArrayList<MovieShowInformation>();

		// When clicked on poster display details
		setOnItemClickListener(new OnItemClickListener(){
			public void onItemClick(AdapterView<?> p, View v, int position, long id) {
				// Determine which item is selected then call TraktDetails Activity to show the details for this Show/Movie.
				MovieShowInformation info = data.get(position);
				if (showmovie == ShowMovie.Movie) {
					parent.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("tmdb:" + info.getId()),parent,TraktDetails.class));
				} else {
					// Changed to display episode list instead of Details view
					//startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("tvdb:" + info.getId()),this,TraktDetails.class));
					parent.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("tvdb:" + info.getId()),parent,EpisodeList.class));
				}		
			}}
		);
	}
	
	/**
	 * Retrieves data from trakt and fills data array. 
	 */
	private Runnable getData = new Runnable() {
		public void run() {
			JSONArray arr = traktapi.getDataArrayFromJSON(url,true);
			for (int i=0;i<arr.length();i++) {
				try {
					JSONObject d = arr.getJSONObject(i);
					MovieShowInformation info = new MovieShowInformation();
					info.setTitle(d.optString("title"));
					JSONObject picts = d.getJSONObject("images");
		    		String p = picts.optString("poster");
		    		p = p.replace(".jpg", "-138.jpg");
		    		info.setPoster(p);
		    		if (showmovie==ShowMovie.Movie) {
		    			info.setId(d.optString("tmdb_id"));
		    		} else {
		    			info.setId(d.optString("tvdb_id"));
		    		}
					data.add(info);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					Log.e(TAG,e.toString());
				}
			}
			Message m = new Message();
			gotData.sendMessage(m);
		}
	};
	/**
	 * Called upon completion of retrieving data
	 */
	Handler gotData = new Handler() { 
        @Override
        public void handleMessage(Message msg) {
        	thumbs.notifyDataSetChanged();
        }
	};
	/**
	 * To be called by Activity containing this Component in order to start filling it with data
	 * @param parent Parent Activity
	 * @param url Trakt API url to retrieve data from
	 * @param sm Show or Movie
	 */
	public void initPosterView(Activity parent,String url, ShowMovie sm) {
		thumbs = new ThumbnailAdapter(parent, new MovieShowAdapter(parent), ((Application)parent.getApplication()).getThumbsCache(),IMAGE_IDS);
		setAdapter(thumbs);
		this.url = url;
		this.parent = parent;
		showmovie = sm;
		Update();
		
	}
	/**
	 * Starts retrieving data on new thread
	 */
	public void Update() {
		thumbs.notifyDataSetInvalidated();
		data.clear();
		Thread thread =  new Thread(null, getData);
        thread.start();
	}
	
	public void setUrl(String url) {
		this.url = url;
	}

	public String getUrl() {
		return url;
	}

	/**
	 * Adapter for the underlying Gallery
	 * @author escabe
	 *
	 */
	class MovieShowAdapter extends ArrayAdapter<MovieShowInformation> {
		private Context context;
		MovieShowAdapter(Context context) {
			super(context,R.layout.trakt_posterview_item,data);
			this.context = context;
		}
		
		@Override
        public View getView(int position, View convertView, ViewGroup parent) {
			View item = convertView;
			if (item==null) {
                LayoutInflater vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                item = vi.inflate(R.layout.trakt_posterview_item, null);
			}
			MovieShowInformation info = getItem(position);
			ImageView poster = (ImageView)item.findViewById(R.id.imagePosterViewPoster);
			poster.setImageResource(R.drawable.emptyposter);
			poster.setTag("http://escabe.org/resize2.php?image=" + info.getPoster());
			return item;
		}
	}


}
