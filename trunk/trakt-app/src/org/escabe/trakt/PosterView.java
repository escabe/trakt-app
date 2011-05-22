package org.escabe.trakt;

import org.escabe.trakt.TraktAPI.ShowMovie;
import org.json.JSONArray;
import org.json.JSONObject;

import com.commonsware.cwac.thumbnail.ThumbnailAdapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;

/**
 * Reusable Component to display trakt information in the form of a horizontal scrolling poster strip.
 * @author escabe (idea Sebastian)
 *
 */
public class PosterView extends Gallery {
	private String TAG = "PosterView";
	private String url;
	private JSONArray data=null;
	private ThumbnailAdapter thumbs=null;
	private static final int[] IMAGE_IDS={R.id.imagePosterViewPoster};
	private TraktAPI traktapi;
	private ShowMovie showmovie;
	private Activity parent;
	
	public PosterView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		traktapi = new TraktAPI(context);
		// When clicked on poster display details
		setOnItemClickListener(new OnItemClickListener(){
			public void onItemClick(AdapterView<?> p, View v, int position, long id) {
				// Determine which item is selected then call TraktDetails Activity to show the details for this Show/Movie.
				JSONObject info = data.optJSONObject(position);
				if (info==null) return;
				if (showmovie == ShowMovie.Movie) {
					parent.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("tmdb:" + info.optString("tmdb_id")),parent,TraktDetails.class));
				} else {
					// Changed to display episode list instead of Details view
					//startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("tvdb:" + info.getId()),this,TraktDetails.class));
					parent.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("tvdb:" + info.optString("tvdb_id")),parent,EpisodeList.class));
				}		
			}}
		);
	}
	
	private class DataGrabber extends AsyncTask<String,Void,Boolean> {
		@Override
		protected void onPreExecute() {
		}
		@Override
		protected Boolean doInBackground(String... params) {
			thumbs.notifyDataSetInvalidated();
			data = traktapi.getDataArrayFromJSON(url,true);
			return true;
		}
		@Override
	    protected void onPostExecute(Boolean result) {
        	thumbs.notifyDataSetChanged();
		}

	}
	
	/**
	 * To be called by Activity containing this Component in order to start filling it with data
	 * @param parent Parent Activity
	 * @param url Trakt API url to retrieve data from
	 * @param sm Show or Movie
	 */
	public void initPosterView(Activity parent,String url, ShowMovie sm) {
		/*MovieShowInformation info = null;
		data.add(info);*/
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
		DataGrabber dg = new DataGrabber();
	
		dg.execute();
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
	class MovieShowAdapter extends BaseAdapter {
		private Context context;
		public MovieShowAdapter(Context context) {
			this.context = context;
		}
		
        public View getView(int position, View convertView, ViewGroup parent) {
			View item = convertView;
			if (item==null) {
                LayoutInflater vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                item = vi.inflate(R.layout.trakt_posterview_item, null);
			}
			JSONObject info = (JSONObject) getItem(position);
			ImageView poster = (ImageView)item.findViewById(R.id.imagePosterViewPoster);
			if (info==null) {
				poster.setImageResource(R.drawable.emptyposter);
			} else {
				poster.setImageResource(R.drawable.emptyposter);

				JSONObject picts = info.optJSONObject("images");
				poster.setTag(traktapi.ResizePoster(picts.optString("poster"),2));
			}
			return item;
		}

		public int getCount() {
			if (data==null)
				return 1;
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
