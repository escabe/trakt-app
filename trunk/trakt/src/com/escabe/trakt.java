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

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.ViewFlipper;

public class trakt extends Activity {
    /** Called when the activity is first created. */
    public ViewFlipper vf;
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
    	traktlist.showList("shows/trending.json/%k",false,"Shows",true);
    }
    
    public void buttonTrendingMoviesOnClick(View view) {
    	vf.showNext();
    	TextView tv = (TextView)findViewById(R.id.textTitle);
    	tv.setText("Trending Movies");
    	traktlist.showList("movies/trending.json/%k",false,"Movies",true);
    }
    
    public void buttonWatchedOnClick(View view) {
    	vf.showNext();
    	TextView tv = (TextView)findViewById(R.id.textTitle);
    	tv.setText(Testing.username + " Watched Movies");
    	traktlist.showList("user/library/movies/all.json/%k/" + Testing.username,true,"Movies",false);
    }
    
    public void buttonSearchSeriesOnClick(View view) {
    	vf.setDisplayedChild(2);
    	search.showSearch("Shows");
    }
    

    
}
