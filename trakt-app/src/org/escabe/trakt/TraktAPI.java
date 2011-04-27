package org.escabe.trakt;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

/**
 * Class for interacting with the trakt.tv API
 * @author escabe
 *
 */
public class TraktAPI {
	private String TAG = "TraktAPI";
	public enum ShowMovie {Show, Movie};
	public enum MarkMode {Watched, Unwatched, Loved, Unloved, Hated, Unhated};
	// "Constants"
	private String apikey = "682912f6e62d666428d261544d619d7c";
	private String baseurl = "http://api.trakt.tv/";

	private String username;
	private String password;
	private SharedPreferences prefs;

	private ProgressDialog progressdialog;
	
	/**
	 * Constructor.
	 * @param c	Context which is needed to be able to retrieve the Preferences.
	 */
	public TraktAPI(Context c) {
		// Get preferences object and retrieve username and password
		prefs = PreferenceManager.getDefaultSharedPreferences(c);
		username = prefs.getString("user", "");
		password = EncodePassword(prefs.getString("password", ""));
	}

	Handler MarkComplete = new Handler() { 
        @Override
        public void handleMessage(Message msg) { 
           Object[] o =  (Object[]) msg.obj;
           ActivityWithUpdate parent = (ActivityWithUpdate) o[0];
           progressdialog.dismiss();
           Toast.makeText(parent.getApplicationContext(), (String) o[1], Toast.LENGTH_SHORT).show();
           parent.DoUpdate();           
        }
    };	
	
    
	public void MarkEpisodeAsWatched(final Activity context, final MarkMode markmode , final String id, final int season, final int episode, String title) {
		progressdialog = ProgressDialog.show(context,"", String.format("Marking %s %02dx%02d as watched...",title,season,episode), true);
		Runnable Marker = new Runnable() {
			public void run() {
				JSONObject data=null;
				try {
					String url = "show/episode/";
					JSONObject post = new JSONObject();
					// Episodes Array
					JSONArray es = new JSONArray();
					// Episode Element
					JSONObject e = new JSONObject();
					e.put("season",season);
					e.put("episode",episode);
					es.put(e);
					post.put("episodes",es);
					post.put("tvdb_id", id);
					url += (markmode==MarkMode.Watched?"seen/":"unseen/");
					url += "%k";
					data = getDataObjectFromJSON(url,true,post);
				} catch (Exception e) {
					Log.e(TAG,e.toString());
				}
				Object[] d = new Object[2];
				d[0] = context;
				if (data!=null) {
					d[1] = "Marking succeeded";
				} else {
					d[1] = "Marking Failed";
				}
					
				Message m = new Message();
				m.obj = d;
				MarkComplete.sendMessage(m);

			}
		};
		
		Thread t = new Thread(null,Marker);
		t.run();
	}

    
	public void MarkMovieAsWatched(final Activity context, final MarkMode markmode , final String id, String title) {
		progressdialog = ProgressDialog.show(context,"", String.format("Marking %s (%s) as watched...",title,id), true);
		Runnable Marker = new Runnable() {
			public void run() {
				JSONObject data=null;
				try {
					String url = "movie/";
					JSONObject post = new JSONObject();
					// Movies Array
					JSONArray ms = new JSONArray();
					// Movie Element
					JSONObject m = new JSONObject();
					m.put("tmdb_id", id);
					ms.put(m);
					post.put("movies",ms);
					url += (markmode==MarkMode.Watched?"seen/":"unseen/");
					url += "%k";
					data = getDataObjectFromJSON(url,true,post);
				} catch (Exception e) {
					Log.e(TAG,e.toString());
				}
				Object[] d = new Object[2];
				d[0] = context;
				if (data!=null) {
					d[1] = "Marking succeeded";
				} else {
					d[1] = "Marking Failed";
				}
					
				Message m = new Message();
				m.obj = d;
				MarkComplete.sendMessage(m);

			}
		};
		
		Thread t = new Thread(null,Marker);
		t.run();
	}
	
	/**
	 * Encodes p as SHA1 Hash.
	 * @param p	Password.
	 * @return	SHA1 encoded password.
	 */
	private String EncodePassword(String p) {
		MessageDigest sha;
		try {
			sha = MessageDigest.getInstance("SHA-1");
			sha.update(p.getBytes("iso-8859-1"));
			byte[] hash = sha.digest();
			p = "";
			for (int i=0;i<hash.length;i++) {
				p += Integer.toHexString(hash[i] & 0xFF);
			}
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return p;
	}
	
	
	
	private Object getDataFromJSON(String url, boolean login,String type) {
		return getDataFromJSON(url, login,type,null);	
	}
	
	/**
	 * Actually retrieve data from the server and encode as either JSONObject or JSONArray
	 * @param url	API URL (baseurl part needs to be omitted).
	 * @param login	send user login details to API.
	 * @param type	"array" or "object" specifying return type.
	 * @return		JSONObject or JSONArray which was returned by the server.
	 */
	private Object getDataFromJSON(String url, boolean login,String type,JSONObject postdata)  {
		// Build URL. URLS may contain certain tags which will be replaced
		url = baseurl + url;
		url = url.replaceAll("%k", apikey);
		url = url.replaceAll("%u", username);

		// Construct HttpClient
		HttpClient httpclient = new DefaultHttpClient();
		if (login) {
			// If login add login information to a JSONObject
	        HttpPost httppost = new HttpPost(url); 
	        JSONObject jsonpost;
	        if (postdata==null) {
	        	jsonpost = new JSONObject();
	        } else {
	        	jsonpost = postdata;
	        }
	        try {
				jsonpost.put("username",username);
				jsonpost.put("password", password);
				httppost.setEntity(new StringEntity(jsonpost.toString()));
		        // Perform POST
				String response = httpclient.execute(httppost, new BasicResponseHandler());
		        // Return the data in the requested format
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
	        // Simply perform a GET
			HttpGet httpget = new HttpGet(url); 
	        try {
		        String response = httpclient.execute(httpget, new BasicResponseHandler());
		        // Return the data in the requested format
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
	/**
	 * Retrieve an array of data without logging in.
	 * @param url	API URL.
	 * @return		JSONArray containing data returned by server.
	 */
	public JSONArray getDataArrayFromJSON(String url) {
		return (JSONArray) getDataFromJSON(url,false,"array");
		
	}
	/**
	 * Retrieve an array of data allows logging in.
	 * @param url	API URL.
	 * @param login	Send login to server
	 * @return		JSONArray containing data returned by server.
	 */
	public JSONArray getDataArrayFromJSON(String url, boolean login) {
    	return (JSONArray) getDataFromJSON(url,login,"array");
	}
    /**
     * Retrieve a single object of data.
     * @param url	API URL.
     * @param login	Send login to server
     * @return		JSONObject containing data returned by server.
     */
	public JSONObject getDataObjectFromJSON(String url, boolean login) {
    	return (JSONObject) getDataFromJSON(url,login,"object");
	}
	
	public JSONObject getDataObjectFromJSON(String url, boolean login,JSONObject post) {
    	return (JSONObject) getDataFromJSON(url,login,"object",post);
	}

}
