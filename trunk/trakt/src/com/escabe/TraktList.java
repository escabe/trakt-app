/*******************************************************************************
 * Copyright 2011 EscAbe
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.escabe;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class TraktList {
	private ListView lv;

    private String apikey = "682912f6e62d666428d261544d619d7c";
	private String baseurl = "http://api.trakt.tv/";
	
	private ArrayList<Movie> MovieList = new ArrayList<Movie>();
	private MovieAdapter ma;
	private DrawableManager dm = new DrawableManager();
	private Activity parentActivity;
	
	private String type;
	
	public TraktList(Activity activity) {
		parentActivity = activity;
        lv = (ListView)parentActivity.findViewById(R.id.listMainList);
        ma = new MovieAdapter(parentActivity,R.layout.row,MovieList);
        lv.setAdapter(ma);
	}
	
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
                    LayoutInflater vi = (LayoutInflater)parentActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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

    public void showList(String url,boolean login,String t) {
    	type = t;
    	JSONArray arr = null;
    	//Get list
   		arr = getDataArrayFromJSON(url,login);
    	// Clear current Movie Array
    	MovieList.clear();
    	// Notify ListView adapter of change
    	ma.notifyDataSetInvalidated();

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
	    	}
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        ma.notifyDataSetChanged();
	}
}
