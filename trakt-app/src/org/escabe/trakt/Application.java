package org.escabe.trakt;

import java.io.File;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.commonsware.cwac.bus.SimpleBus;
import com.commonsware.cwac.cache.AsyncCache;
import com.commonsware.cwac.cache.SimpleWebImageCache;
import com.commonsware.cwac.cache.WebImageCache;
import com.commonsware.cwac.thumbnail.ThumbnailBus;
import com.commonsware.cwac.thumbnail.ThumbnailMessage;

public class Application extends android.app.Application {
	private String TAG="TraktAPP";
	private ThumbnailBus thumbbus=new ThumbnailBus();
	private AsyncCache.DiskCachePolicy policy=new AsyncCache.DiskCachePolicy() {
		public boolean eject(File file) {
			return(System.currentTimeMillis()-file.lastModified()>1000*60*60*24*7);
		}
	};
	private SimpleWebImageCache<ThumbnailBus, ThumbnailMessage> thumbscache=null;
	// For CWAC Cache for other images
	private SimpleBus bus = new SimpleBus();
	private WebImageCache cache = null;
	private TraktAPI traktapi = null;
	
	private HashMap<String,LovedHatedWatched> lovedhatedwatched=null;
	private boolean lhwloaded = false;
	
	/**
	 * Main entry point for the Application.
	 */
	public Application() {
		super();
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		// Make sure the loved hated watched list gets filled
		traktapi = new TraktAPI(this);
		Thread thread =  new Thread(null, retrieveLovedHatedWatched);
        thread.start();
	}
	
	/**
	 * Get SimpleWebImageCache which can be used for displaying images in ListViews.
	 * @return	The Application's SimpleWebImageCache
	 */
	public SimpleWebImageCache<ThumbnailBus, ThumbnailMessage> getThumbsCache() {
		// Need to wait with creating cache until this is initialized and getCacheDir() can be called
		if (thumbscache==null)
			thumbscache = new SimpleWebImageCache<ThumbnailBus, ThumbnailMessage>(getCacheDir(), policy, 101, thumbbus);
		return thumbscache;
	}

	/**
	 * Get WebImageCache which can be used for displaying images in general ImageViews.
	 * @return	The Application's WebImageCache
	 */
	public WebImageCache getCache() {
		if (cache==null) {
			cache = new WebImageCache(getCacheDir(), bus, policy, 101,getResources().getDrawable(R.drawable.emptyposter));
		}
		return cache;
	}
	
	public HashMap<String,LovedHatedWatched> getLovedHatedWatched() {
		// Only return the list of the Thread as finished filling it.
		if (lhwloaded)
			return lovedhatedwatched;
		else
			return null;
	}


	Handler retrieveLovedHatedWatchedDone = new Handler() { 
        @Override
        public void handleMessage(Message msg) { 
           Toast.makeText(getApplicationContext(), (String) msg.obj, Toast.LENGTH_SHORT).show();
        }
    };	
	
	
	/**
	 * To be run as separate Thread. Calls trakt API multiple times to retrieve a full list of
	 * loved, hated and watched movies and shows for the user.
	 */
	private Runnable retrieveLovedHatedWatched = new Runnable() {
		public void run() {
			lovedhatedwatched = new HashMap<String,LovedHatedWatched>();
			
			JSONArray data = traktapi.getDataArrayFromJSON("user/library/movies/all.json/%k/%u", true);
			for (int i=0;i<data.length();i++) {
				try {
					JSONObject d = data.getJSONObject(i);
					LovedHatedWatched lhw = new LovedHatedWatched(d.optInt("plays")>0);
					lovedhatedwatched.put(d.optString("tmdb_id"), lhw);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					Log.e(TAG,e.toString());
				}
			}
			
			data = traktapi.getDataArrayFromJSON("user/library/movies/loved.json/%k/%u", true);
			for (int i=0;i<data.length();i++) {
				try {
					JSONObject d = data.getJSONObject(i);
					LovedHatedWatched lhw = lovedhatedwatched.get(d.optString("tmdb_id"));
					if (lhw==null) {
						lhw = new LovedHatedWatched(false);
					}
					lhw.setLoved(true);
					lovedhatedwatched.put(d.optString("tmdb_id"), lhw);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					Log.e(TAG,e.toString());
				}
			}
			
			data = traktapi.getDataArrayFromJSON("user/library/movies/hated.json/%k/%u", true);
			for (int i=0;i<data.length();i++) {
				try {
					JSONObject d = data.getJSONObject(i);
					LovedHatedWatched lhw = lovedhatedwatched.get(d.optString("tmdb_id"));
					if (lhw==null) {
						lhw = new LovedHatedWatched(false);
					}
					lhw.setHated(true);
					lovedhatedwatched.put(d.optString("tmdb_id"), lhw);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					Log.e(TAG,e.toString());
				}
			}
			
			data = traktapi.getDataArrayFromJSON("user/library/shows/all.json/%k/%u", true);
			for (int i=0;i<data.length();i++) {
				try {
					JSONObject d = data.getJSONObject(i);
					LovedHatedWatched lhw = new LovedHatedWatched(false);
					lovedhatedwatched.put(d.optString("tvdb_id"), lhw);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					Log.e(TAG,e.toString());
				}
			}
			
			data = traktapi.getDataArrayFromJSON("user/library/shows/loved.json/%k/%u", true);
			for (int i=0;i<data.length();i++) {
				try {
					JSONObject d = data.getJSONObject(i);
					LovedHatedWatched lhw = lovedhatedwatched.get(d.optString("tvdb_id"));
					if (lhw==null) {
						lhw = new LovedHatedWatched(false);
					}
					lhw.setLoved(true);
					lovedhatedwatched.put(d.optString("tvdb_id"), lhw);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					Log.e(TAG,e.toString());
				}
			}

			data = traktapi.getDataArrayFromJSON("user/library/shows/hated.json/%k/%u", true);
			for (int i=0;i<data.length();i++) {
				try {
					JSONObject d = data.getJSONObject(i);
					LovedHatedWatched lhw = lovedhatedwatched.get(d.optString("tvdb_id"));
					if (lhw==null) {
						lhw = new LovedHatedWatched(false);
					}
					lhw.setHated(true);
					lovedhatedwatched.put(d.optString("tvdb_id"), lhw);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					Log.e(TAG,e.toString());
				}
			}
			lhwloaded = true;
			Message m = new Message();
			m.obj = "Done Loading Watched/Loved/Hated information.";
			retrieveLovedHatedWatchedDone.sendMessage(m);
		}
	};
	
}
