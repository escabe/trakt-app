package org.escabe.trakt;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

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

public class TraktAPI {
	private static String apikey = "682912f6e62d666428d261544d619d7c";
	private static String baseurl = "http://api.trakt.tv/";
	private static String username;
	private static String password;
	private static Object getDataFromJSON(String url, boolean login,String type)  {
		url = baseurl + url;
		url = url.replaceAll("%k", apikey);
		url = url.replaceAll("%u", username);
		
		HttpClient httpclient = new DefaultHttpClient();
		if (login) {
	        HttpPost httppost = new HttpPost(url); 
	        JSONObject jsonpost = new JSONObject();
	        try {
				jsonpost.put("username",username);
				jsonpost.put("password", password);
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
	public static JSONArray getDataArrayFromJSON(String url) {
		return (JSONArray) getDataFromJSON(url,false,"array");
		
	}
	public static JSONArray getDataArrayFromJSON(String url, boolean login) {
    	return (JSONArray) getDataFromJSON(url,login,"array");
	}
    
	public static JSONObject getDataObjectFromJSON(String url, boolean login) {
    	return (JSONObject) getDataFromJSON(url,login,"object");
	}
}
