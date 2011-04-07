package org.escabe.trakt;

import java.io.File;

import com.commonsware.cwac.bus.SimpleBus;
import com.commonsware.cwac.cache.AsyncCache;
import com.commonsware.cwac.cache.SimpleWebImageCache;
import com.commonsware.cwac.cache.WebImageCache;
import com.commonsware.cwac.thumbnail.ThumbnailBus;
import com.commonsware.cwac.thumbnail.ThumbnailMessage;

public class Application extends android.app.Application {
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
							
	/**
	 * Main entry point for the Application.
	 */
	public Application() {
		super();
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
	
	

	
}
