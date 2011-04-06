package org.escabe.trakt;

import java.io.File;

import android.content.SharedPreferences;

import com.commonsware.cwac.cache.AsyncCache;
import com.commonsware.cwac.cache.SimpleWebImageCache;
import com.commonsware.cwac.thumbnail.ThumbnailBus;
import com.commonsware.cwac.thumbnail.ThumbnailMessage;

public class Application extends android.app.Application {
	
	private ThumbnailBus bus=new ThumbnailBus();
	private AsyncCache.DiskCachePolicy policy=new AsyncCache.DiskCachePolicy() {
		public boolean eject(File file) {
			return(System.currentTimeMillis()-file.lastModified()>1000*60*60*24*7);
		}
	};
	private SimpleWebImageCache<ThumbnailBus, ThumbnailMessage> cache=null;
							

	public Application() {
		super();
	}

	public SimpleWebImageCache<ThumbnailBus, ThumbnailMessage> getCache() {
		// Need to wait with creating cache until this is initialized and getCacheDir() can be called
		if (cache==null)
			cache = new SimpleWebImageCache<ThumbnailBus, ThumbnailMessage>(getCacheDir(), policy, 101, bus);
		return cache;
	}

	
}
