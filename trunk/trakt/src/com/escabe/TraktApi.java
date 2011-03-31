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
import java.util.HashMap;
import java.util.Map;

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

import com.escabe.trakt.MyView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.Toast;

public class TraktApi {
    private static String apikey = "682912f6e62d666428d261544d619d7c";
	private static String baseurl = "http://api.trakt.tv/";
	private static String username;
	private static String password;

	public static boolean Login(String u, String p) {
		username = u;
		password = p;
		JSONObject data = getDataObjectFromJSON("",true);
		if (data==null) {
			AlertDialog.Builder alert = new AlertDialog.Builder(trakt.instance);
			alert.setTitle("Trakt.tv");
			alert.setMessage("Login Failed!\nPlease check login details.");
			alert.setIcon(android.R.drawable.ic_dialog_alert);
			alert.setPositiveButton("OK",new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface arg0, int arg1) {
					trakt.instance.myflipper.FlipTo(MyView.SETTINGS);
				}});
			alert.show();
			return false;
		} else {
			return true;
		}
	}

	public static class UserSeries {
		private static Map<String,Movie> list=null;
		
		
		private static void init() {
			list = new HashMap<String,Movie>();
			JSONArray arr = getDataArrayFromJSON("user/library/shows/all.json/%k/%u",true);
			for (int i=0;i<arr.length();i++) {
				try {
					JSONObject data = arr.getJSONObject(i);
					Movie m = new Movie();
					m.setID(data.getString("tvdb_id"));
					list.put(m.getID(), m);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			arr = getDataArrayFromJSON("user/library/shows/loved.json/%k/%u",true);
			for (int i=0;i<arr.length();i++) {
				try {
					JSONObject data = arr.getJSONObject(i);
					String id = data.getString("tvdb_id");
					Movie m = list.get(id);
					if (m==null) {
						m = new Movie();
						m.setID(data.getString("tvdb_id"));						
					}
					m.setLoved(true);
					list.put(id, m);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			arr = getDataArrayFromJSON("user/library/shows/hated.json/%k/%u",true);
			for (int i=0;i<arr.length();i++) {
				try {
					JSONObject data = arr.getJSONObject(i);
					String id = data.getString("tvdb_id");
					Movie m = list.get(id);
					if (m==null) {
						m = new Movie();
						m.setID(data.getString("tvdb_id"));						
					}					
					m.setHated(true);
					list.put(id, m);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		public static boolean Loved(String id) {
			if (list==null) 
				init();
			
			Movie m=list.get(id);
			if (m!=null) {
				return m.getLoved();
			}
			return false;
		}
		
		public static boolean Hated(String id) {
			if (list==null) 
				init();
			
			Movie m=list.get(id);
			if (m!=null) {
				return m.getHated();
			}
			return false;
		}	

	}
	
	public static class UserMovies {
		private static Map<String,Movie> list=null;

		private static void init() {
			list = new HashMap<String,Movie>();
			// User all movies or only last 100?
			JSONArray arr = getDataArrayFromJSON("user/library/movies/all.json/%k/%u",true);
			for (int i=0;i<arr.length();i++) {
				try {
					JSONObject data = arr.getJSONObject(i);
					//data = data.getJSONObject("movie");
					Movie m = new Movie();
					m.setID(data.getString("tmdb_id"));
					m.setWatched(true);
					list.put(m.getID(), m);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			arr = getDataArrayFromJSON("user/library/movies/loved.json/%k/%u",true);
			for (int i=0;i<arr.length();i++) {
				try {
					JSONObject data = arr.getJSONObject(i);
					String id = data.getString("tmdb_id");
					Movie m = list.get(id);
					if (m==null) {
						m = new Movie();
						m.setID(data.getString("tmdb_id"));						
					}
					m.setLoved(true);
					list.put(id, m);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			arr = getDataArrayFromJSON("user/library/movies/hated.json/%k/%u",true);
			for (int i=0;i<arr.length();i++) {
				try {
					JSONObject data = arr.getJSONObject(i);
					String id = data.getString("tmdb_id");
					Movie m = list.get(id);
					if (m==null) {
						m = new Movie();
						m.setID(data.getString("tmdb_id"));						
					}					
					m.setHated(true);
					list.put(id, m);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		public static boolean Loved(String id) {
			if (list==null) 
				init();
			
			Movie m=list.get(id);
			if (m!=null) {
				return m.getLoved();
			}
			return false;
		}
		
		public static boolean Hated(String id) {
			if (list==null) 
				init();
			
			Movie m=list.get(id);
			if (m!=null) {
				return m.getHated();
			}
			return false;
		}	
		
		public static boolean Watched(String id) {
			if (list==null) 
				init();
			
			Movie m=list.get(id);
			if (m!=null) {
				return m.getWatched();
			}
			return false;
		}	
		
	}

	
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
