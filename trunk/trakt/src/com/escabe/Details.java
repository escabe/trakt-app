package com.escabe;

import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.text.format.Formatter;
import android.widget.ImageView;
import android.widget.TextView;

public class Details {
	private String type;
	private ImageView poster;
	private TextView title;
	private TextView details;
	private TextView overview;
	private static Details d=null;
	private trakt parentActivity;
	private DrawableManager dm;
	
	public Details(Activity parent) {
		parentActivity = (trakt) parent;
		title = (TextView) parentActivity.findViewById(R.id.textDetailTitle);
		details = (TextView) parentActivity.findViewById(R.id.textDetails);
		overview = (TextView) parentActivity.findViewById(R.id.textDetailsOverview);
		poster = (ImageView) parentActivity.findViewById(R.id.imageDetailsPoster);
		dm = new DrawableManager();
	}
	
	public static Details getInstance(Activity parent) {
		if (d==null) {
			d = new Details(parent);
		}
		return d;
	}
	
	public void showDetails (String t, String id) {
		type = t;
		parentActivity.vf.setDisplayedChild(3);
		
		if (type=="Shows") {
			try {
				JSONObject data = TraktApi.getDataObjectFromJSON("show/summary.json/%k/" + id, true);
				title.setText(data.optString("title"));
				JSONObject images = data.getJSONObject("images");
				String p = images.getString("poster");
				p = p.replace(".jpg", "-138.jpg");
				dm.fetchDrawableOnThread("http://escabe.org/resize2.php?image=" + p, poster);
				String d = String.format("First Aired: %1$tB %1$te, %1$tY\nCountry: %2$s\nRuntime: %3$d min\n",new Date(data.optLong("first_aired")*1000),
																													data.optString("country"),
																													data.optInt("runtime"));
				details.setText(d);
				overview.setText(data.optString("overview"));

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		} else {
			try {
				JSONObject data = TraktApi.getDataObjectFromJSON("movie/summary.json/%k/" + id, true);
				title.setText(data.optString("title"));
				JSONObject images = data.getJSONObject("images");
				String p = images.getString("poster");
				p = p.replace(".jpg", "-138.jpg");
				dm.fetchDrawableOnThread("http://escabe.org/resize2.php?image=" + p, poster);
				String d = String.format("Released: %1$tB %1$te, %1$tY\nRuntime: %2$d min\n",new Date(data.optLong("released")*1000),
																													data.optInt("runtime"));
				details.setText(d);
				d = String.format("\"%s\"\n\n%s",data.optString("tagline"),data.optString("overview"));
				overview.setText(d);

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
	}
}
