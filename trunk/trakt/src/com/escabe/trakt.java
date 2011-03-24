package com.escabe;

import java.io.*;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.*;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewFlipper;

public class trakt extends Activity {
    /** Called when the activity is first created. */
    private ViewFlipper vf;
	private ListView lv;

    private String apikey = "682912f6e62d666428d261544d619d7c";
	private String baseurl = "http://api.trakt.tv/";
	
	private ArrayList<Movie> MovieList = new ArrayList<Movie>();
	private MovieAdapter ma;
	private DrawableManager dm = new DrawableManager();
	

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // Save some "handles" to important GUI elements
        vf = (ViewFlipper) findViewById(R.id.viewFlipper1);
        // Configure the ListView
        lv = (ListView)findViewById(R.id.listView1);
        ma = new MovieAdapter(this,R.layout.row,MovieList);
        lv.setAdapter(ma);
    }
	
	// Override the back button
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if (keyCode == KeyEvent.KEYCODE_BACK) {
	        if(vf.getDisplayedChild() != 0){
	           vf.showPrevious();
	           return true;
	        }
	    }
	    return super.onKeyDown(keyCode, event);
	}
	
	// Class to hold information about a movie

		
	// Adapter to show current List of Movies in the ListView
	private class MovieAdapter extends ArrayAdapter<Movie> {
		private ArrayList<Movie> items;
		public MovieAdapter(Context context, int textViewResourceId, ArrayList<Movie> items) {
			super(context, textViewResourceId, items);
			this.items = items;
		}
		
		// Fill in a Row
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
                View v = convertView;
                if (v == null) {
                    LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    v = vi.inflate(R.layout.row, null);
                }
                Movie o = items.get(position);
                if (o != null) {
                        TextView tt = (TextView) v.findViewById(R.id.textRowTitle);
                        if (tt != null) {
                              tt.setText(o.getTitle());                            
                        }
                        TextView ti = (TextView) v.findViewById(R.id.textRowInfo);
                        if (ti!=null) {
                        	ti.setText("Watchers: " + o.getWatchers() + (o.getWatched()?" watched":""));
                        }
                        ImageView iv = (ImageView) v.findViewById(R.id.imageRow);
                        if (iv != null) {
                        	dm.fetchDrawableOnThread("http://escabe.org/resize.php?image=" + o.getPoster(), iv);
                        }

                }
                return v;
        }

	}
		
	public class DrawableManager {
	    private final Map<String, Drawable> drawableMap;

	    public DrawableManager() {
	        drawableMap = new HashMap<String, Drawable>();
	    }

	    public Drawable fetchDrawable(String urlString) {
	        if (drawableMap.containsKey(urlString)) {
	            return drawableMap.get(urlString);
	        }

	        Log.d(this.getClass().getSimpleName(), "image url:" + urlString);
	        try {
	            InputStream is = fetch(urlString);
	            Drawable drawable = Drawable.createFromStream(is, "src");
	            drawableMap.put(urlString, drawable);
	            return drawable;
	        } catch (MalformedURLException e) {
	            Log.e(this.getClass().getSimpleName(), "fetchDrawable failed", e);
	            return null;
	        } catch (IOException e) {
	            Log.e(this.getClass().getSimpleName(), "fetchDrawable failed", e);
	            return null;
	        }
	    }

	    public void fetchDrawableOnThread(final String urlString, final ImageView imageView) {
	        if (drawableMap.containsKey(urlString)) {
	            imageView.setImageDrawable(drawableMap.get(urlString));
	            return;
	        }
	        imageView.setImageResource(R.drawable.emptyposter);

	        final Handler handler = new Handler() {
	            @Override
	            public void handleMessage(Message message) {
	                imageView.setImageDrawable((Drawable) message.obj);
	            }
	        };

	        Thread thread = new Thread() {
	            @Override
	            public void run() {
	                Drawable drawable = fetchDrawable(urlString);
	                Message message = handler.obtainMessage(1, drawable);
	                handler.sendMessage(message);
	            }
	        };
	        thread.start();
	    }

	    private InputStream fetch(String urlString) throws MalformedURLException, IOException {
	        DefaultHttpClient httpClient = new DefaultHttpClient();
	        HttpGet request = new HttpGet(urlString);
	        HttpResponse response = httpClient.execute(request);
	        return response.getEntity().getContent();
	    }

	}
	
	private boolean watchedStatus(String type, String id) {
		JSONObject arr = getDataObjectFromJSON(baseurl + "/" + type + "/summary.json/" + apikey + "/" + id,true);
		return arr.optBoolean("watched");
	}
	
	
	private Object getDataFromJSON(String url, boolean login,String type)  {
		HttpClient httpclient = new DefaultHttpClient();
		if (login) {
	        HttpPost httppost = new HttpPost(url); 
	        JSONObject jsonpost = new JSONObject();
	        try {
				jsonpost.put("username",Testing.username);
				jsonpost.put("password", Testing.password);
				httppost.setEntity(new StringEntity(jsonpost.toString()));
		        String response = httpclient.execute(httppost, new BasicResponseHandler());
		        if (type=="array") {
		        	return new JSONArray(response);
		        } else {
		        	return new JSONObject(response);		        	
		        }
	        } catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClientProtocolException e) {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
	        } catch (IOException e) {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
	        }
		} else { // No login
	        HttpGet httpget = new HttpGet(url); 
	        try {
		        String response = httpclient.execute(httpget, new BasicResponseHandler());
		        if (type=="array") {
		        	return new JSONArray(response);
		        } else {
		        	return new JSONObject(response);		        	
		        }
	        } catch (ClientProtocolException e) {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
	        } catch (IOException e) {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
	        } catch (JSONException e) {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
	        }
		}
        return null;
	
	}
	private JSONArray getDataArrayFromJSON(String url) {
		return (JSONArray) getDataFromJSON(url,false,"array");
		
	}
    private JSONArray getDataArrayFromJSON(String url, boolean login) {
    	return (JSONArray) getDataFromJSON(url,login,"array");
	}
    
    private JSONObject getDataObjectFromJSON(String url, boolean login) {
    	return (JSONObject) getDataFromJSON(url,login,"object");
	}
    

    private void showList(String url,boolean login) {
    	JSONArray arr = null;
    	//Get list
   		arr = getDataArrayFromJSON(url,login);
    	// Clear current Movie Array
    	MovieList.clear();
    	// Notify ListView adapter of change
    	ma.notifyDataSetChanged();

    	//Re-Fill the Movie Array
    	try {
    		//For all items
	    	for (int i=0;i<arr.length();i++) {
	    		Movie m = new Movie();
	    		// Get item from array
	    		JSONObject obj = arr.getJSONObject(i);
	    		// Get poster
	    		JSONObject picts = obj.getJSONObject("images");
	    		String p = picts.getString("poster");
	    		p.replace(".jpg", "-138.jpg");
	    		m.setPoster(p);
	    		// Get title
	    		m.setTitle(obj.getString("title"));
	    		// Get number of watchers
	    		m.setWatchers(obj.optInt("watchers"));
	    		// Save ID
	    		m.setID(obj.getString("imdb_id"));
	    		MovieList.add(m);
	    		ma.notifyDataSetChanged();
	    	}
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } 
	}
	
    public void buttonTrendingTVOnClick(View view) {
    	vf.showNext();
    	TextView tv = (TextView)findViewById(R.id.textTitle);
    	tv.setText("Trending Shows");
    	showList(baseurl + "shows/trending.json/" + apikey,false);
    }
    
    public void buttonTrendingMoviesOnClick(View view) {
    	vf.showNext();
    	TextView tv = (TextView)findViewById(R.id.textTitle);
    	tv.setText("Trending Movies");
    	showList(baseurl + "movies/trending.json/" + apikey,false);

    }
    
    public void buttonWatchedOnClick(View view) {
    	vf.showNext();
    	TextView tv = (TextView)findViewById(R.id.textTitle);
    	tv.setText("All Watched Movies By" + Testing.username);
    	showList(baseurl + "user/library/movies/all.json/" + apikey + "/" + Testing.username,true);

    }
    
}