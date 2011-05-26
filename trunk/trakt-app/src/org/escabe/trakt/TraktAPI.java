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

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
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
	private Context context;
	
	// "Constants"
	private String apikey = "682912f6e62d666428d261544d619d7c";
	private String baseurl = "http://api-trakt.apigee.com/";

	private String username;
	private String password;
	private SharedPreferences prefs;

	/**
	 * Constructor.
	 * @param c	Context which is needed to be able to retrieve the Preferences.
	 */
	public TraktAPI(Context c) {
		// Get preferences object and retrieve username and password
		context = c;
		prefs = PreferenceManager.getDefaultSharedPreferences(c);
		username = prefs.getString("user", "");
		password = EncodePassword(prefs.getString("password", ""));
	}
	
    public String ResizePoster(String image, int size) {
    	String p = image.replace(".jpg", "-138.jpg");
    	switch (size) {
    	case 1:
    		return "http://escabe.org/resize.php?image=" + p + 
    			"&h=" +	context.getResources().getDimensionPixelSize(R.dimen.PosterSmallHeight) +
    			"&w=" + context.getResources().getDimensionPixelSize(R.dimen.PosterSmallWidth);
    	case 2:
    		return "http://escabe.org/resize.php?image=" + p + 
			"&h=" +	context.getResources().getDimensionPixelSize(R.dimen.PosterMediumHeight) +
			"&w=" + context.getResources().getDimensionPixelSize(R.dimen.PosterMediumWidth);
    	default:
    		return p;
    	}
    }
    
    public String ResizeAvatar(String image, int size) {
    	switch (size) {
    	case 1:
    		return "http://escabe.org/resize.php?image=" + image + 
    			"&h=" +	context.getResources().getDimensionPixelSize(R.dimen.AvatarSmallHeight) +
    			"&w=" + context.getResources().getDimensionPixelSize(R.dimen.AvatarSmallWidth);
    	case 2:
    		return "http://escabe.org/resize.php?image=" + image + 
			"&h=" +	context.getResources().getDimensionPixelSize(R.dimen.AvatarMediumHeight) +
			"&w=" + context.getResources().getDimensionPixelSize(R.dimen.AvatarMediumWidth);
    	default:
    		return image;
    	}
    }
    
    public String ResizeScreen(String image, int size) {
    	String s = image.replace(".jpg", "-218.jpg");
    	switch (size) {
    	case 1:
    		return "http://escabe.org/resize.php?image=" + s + 
			"&h=" +	context.getResources().getDimensionPixelSize(R.dimen.ScreenSmallHeight) +
			"&w=" + context.getResources().getDimensionPixelSize(R.dimen.ScreenSmallWidth);
    	case 2:
    		
    	default:
    			return s;
    	}
    }

    public void Mark(ActivityWithUpdate parent, Object... params) {
    	Marker m = new Marker(parent);
    	m.execute(params);
    }
    
    public class Marker extends AsyncTask<Object,Void,Boolean> {
		ActivityWithUpdate parent;
		ProgressDialog progressdialog;
		
		public Marker(ActivityWithUpdate parent) {
			this.parent = parent;
		}
		@Override
		protected void onPreExecute() {
			progressdialog = ProgressDialog.show(context,"", String.format("Marking..."), true);
		}
		
		@Override
		protected Boolean doInBackground(Object... params) {
			String type = (String) params[0];
			String status = (String) params[1];
			if (status.equals("seen") || status.equals("unseen")) {
				if (type.equals("movie")) {
					try {
						String url = "movie/";
						JSONObject post = new JSONObject();
						// Movies Array
						JSONArray ms = new JSONArray();
						// Movie Element
						JSONObject m = new JSONObject();
						
						m.put("tmdb_id", (String) params[2]);
						ms.put(m);
						post.put("movies",ms);
						url += status + "/%k";
						JSONObject data = getDataObjectFromJSON(url,true,post);
						return data!=null;
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else if(type.equals("episode")) {
					try {
						String url = "show/episode/";
						JSONObject post = new JSONObject();
						// Episodes Array
						JSONArray es = new JSONArray();
						// Episode Element
						JSONObject e = new JSONObject();
						e.put("season",(Integer) params[3]);
						e.put("episode",(Integer) params[4]);
						es.put(e);
						post.put("episodes",es);
						post.put("tvdb_id", (String) params[2]);
						url += status + "/%k";
						JSONObject data = getDataObjectFromJSON(url,true,post);
						return data!=null;
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
			} else {
				
			}
			return null;
		}
		
		@Override
	    protected void onPostExecute(Boolean result) {
			String message;
			if (result) {
				message = "Marking succeeded";
			} else {
				message = "Marking failed";
			}
			progressdialog.dismiss();
			Toast.makeText(parent.getApplicationContext(), message, Toast.LENGTH_SHORT).show();
			parent.DoUpdate();    
	    }
    
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
