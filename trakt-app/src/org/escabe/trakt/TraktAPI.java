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

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Class for interacting with the trakt.tv API
 * @author escabe
 *
 */
public class TraktAPI {
	// "Constants"
	private String apikey = "682912f6e62d666428d261544d619d7c";
	private String baseurl = "http://api.trakt.tv/";

	private String username;
	private String password;
	private SharedPreferences prefs;

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
	
	/**
	 * Actually retrieve data from the server and encode as either JSONObject or JSONArray
	 * @param url	API URL (baseurl part needs to be omitted).
	 * @param login	send user login details to API.
	 * @param type	"array" or "object" specifying return type.
	 * @return		JSONObject or JSONArray which was returned by the server.
	 */
	private Object getDataFromJSON(String url, boolean login,String type)  {
		// Build URL. URLS may contain certain tags which will be replaced
		url = baseurl + url;
		url = url.replaceAll("%k", apikey);
		url = url.replaceAll("%u", username);

		// Construct HttpClient
		HttpClient httpclient = new DefaultHttpClient();
		if (login) {
			// If login add login information to a JSONObject
	        HttpPost httppost = new HttpPost(url); 
	        JSONObject jsonpost = new JSONObject();
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
}
