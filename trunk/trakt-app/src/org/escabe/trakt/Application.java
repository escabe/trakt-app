/* TODO:
 *  - Use JSON Data directly (no MovieShowInformation class anymore)
 *  - Use AsyncTask instead of Thread for retrieving data
 *  - Error handling
 *  - Loading indicator for PosterView
 */

package org.escabe.trakt;

import java.io.File;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
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
	public AsyncCache.DiskCachePolicy policy=new AsyncCache.DiskCachePolicy() {
		public boolean eject(File file) {
			return(System.currentTimeMillis()-file.lastModified()>1000*60*60*24*7);
		}
	};
	private SimpleWebImageCache<ThumbnailBus, ThumbnailMessage> thumbscache=null;
	// For CWAC Cache for other images
	public SimpleBus bus = new SimpleBus();
	private WebImageCache cache = null;
	private TraktAPI traktapi = null;
	

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
		
		
		traktapi = new TraktAPI(this);
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
	
    public boolean ValidateLogin() {
    	return true;
    }
	
	
}
