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
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewFlipper;

public class trakt extends Activity {
    private String apikey = "682912f6e62d666428d261544d619d7c";
	private String baseurl = "http://api.trakt.tv/";
	
    /** Called when the activity is first created. */
    private ViewFlipper vf;
    private TraktList traktlist;
    private Search search;
    
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // Save some "handles" to important GUI elements
        vf = (ViewFlipper) findViewById(R.id.viewFlipper1);
        vf.setOutAnimation(AnimationUtils.makeOutAnimation(this, false));
        vf.setInAnimation(AnimationUtils.makeInAnimation(this, false));
        traktlist = new TraktList(this);
        search = new Search(this);
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
	

	
    public void buttonTrendingTVOnClick(View view) {
    	vf.showNext();
    	TextView tv = (TextView)findViewById(R.id.textTitle);
    	tv.setText("Trending Shows");
    	traktlist.showList(baseurl + "shows/trending.json/" + apikey,false,"trending");
    }
    
    public void buttonTrendingMoviesOnClick(View view) {
    	vf.showNext();
    	TextView tv = (TextView)findViewById(R.id.textTitle);
    	tv.setText("Trending Movies");
    	traktlist.showList(baseurl + "movies/trending.json/" + apikey,false,"trending");
    }
    
    public void buttonWatchedOnClick(View view) {
    	vf.showNext();
    	TextView tv = (TextView)findViewById(R.id.textTitle);
    	tv.setText("All Watched Movies By" + Testing.username);
    	traktlist.showList(baseurl + "user/library/movies/all.json/" + apikey + "/" + Testing.username,true,"user");
    }
    
    public void buttonSearchSeriesOnClick(View view) {
    	vf.setDisplayedChild(2);
    	search.showSearch("Shows");
    }
    

    
}
