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

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Stack;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.ViewFlipper;

public class trakt extends Activity {
    /** Called when the activity is first created. */
//    public ViewFlipper vf;
    private TraktList traktlist;
    private Search search;
    public static trakt instance;
    public MyFlipper myflipper;
    
    private SharedPreferences settings;
    
	public enum MyView {
		MAIN_MENU(0), TRAKTLIST(1), SEARCH(2), DETAILS(3), SETTINGS(4);
		private int id;
		MyView(int i) {
			id = i;
		}
		public int id() {
			return id;
		}
	}
	
    public class MyFlipper {
    	private ViewFlipper viewflipper;
    	private Stack<Integer> viewstack;
    	public MyFlipper(ViewFlipper vf, Activity parent) {
    		viewflipper = vf;
    		viewstack = new Stack<Integer>();
    		viewflipper.setOutAnimation(AnimationUtils.makeOutAnimation(parent, false));
    		viewflipper.setInAnimation(AnimationUtils.makeInAnimation(parent, false));

    	}
    	public void FlipTo(MyView v) {
    		if (viewflipper.getDisplayedChild()==v.id())
    			return;
    		viewstack.push(viewflipper.getDisplayedChild());
    		viewflipper.setDisplayedChild(v.id());
    		
    	}
    	public boolean FlipBack() {
    		
    		if (viewstack.isEmpty()){
    			return false;
    		}
    		int p = viewstack.pop();
			viewflipper.setDisplayedChild(p);
    		return true;
    	}
    }
    
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        instance = this;
        
        setContentView(R.layout.main);

        // Read preferences
        settings = getPreferences(MODE_PRIVATE);
        TraktApi.Login(settings.getString("username",""),settings.getString("password",""));
    	TextView un = (TextView)findViewById(R.id.editUsername);
    	un.setText(settings.getString("username", ""));
        
        // Save some "handles" to important GUI elements
        ViewFlipper vf = (ViewFlipper) findViewById(R.id.viewFlipper1);
        myflipper = new MyFlipper(vf,this);
        
        traktlist = new TraktList(this);
        search = new Search(this);
    }
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.main_menu, menu);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	    case R.id.menu_settings:
	    	myflipper.FlipTo(MyView.SETTINGS);
	    	return true;	    	
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
	
	// Override the back button
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if (keyCode == KeyEvent.KEYCODE_BACK) {
	        if(myflipper.FlipBack()){
	           return true;
	        }
	    }
	    return super.onKeyDown(keyCode, event);
	}

	public void buttonLoginOnClick(View view) {
		TextView un = (TextView)findViewById(R.id.editUsername);
		TextView pa = (TextView)findViewById(R.id.editPassword);
		String username = un.getText().toString();
		String password = pa.getText().toString();
		try {
			MessageDigest sha = MessageDigest.getInstance("SHA-1");
			sha.update(password.getBytes("iso-8859-1"));
			byte[] hash = sha.digest();
			password = "";
			for (int i=0;i<hash.length;i++) {
				password += Integer.toHexString(hash[i] & 0xFF);
			}
		} catch (NoSuchAlgorithmException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (TraktApi.Login(username,password)) {
			SharedPreferences.Editor ed = settings.edit();
			ed.putString("username", username);
			ed.putString("password", password);
			ed.commit();
			AlertDialog.Builder alert = new AlertDialog.Builder(trakt.instance);
			alert.setTitle("Trakt.tv");
			alert.setMessage("Login Succesfull!");
			alert.setIcon(android.R.drawable.ic_dialog_info);
			alert.setPositiveButton("OK",new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface arg0, int arg1) {
					trakt.instance.myflipper.FlipBack();
				}});
			alert.show();
		}
	}
	
    public void buttonTrendingTVOnClick(View view) {
    	traktlist.ShowTrending("Shows");
    }
    
    public void buttonTrendingMoviesOnClick(View view) {
    	traktlist.ShowTrending("Movies");
    }
    
    public void buttonWatchedOnClick(View view) {
    	/*myflipper.FlipTo(MyView.TRAKTLIST);;
    	TextView tv = (TextView)findViewById(R.id.textTitle);
    	tv.setText(settings.getString("username","") + " Watched Movies");
    	traktlist.ShowList("user/library/movies/all.json/%k/" + settings.getString("username",""),true);*/
    	traktlist.ShowUserList("Movies","all");
    }
    
    public void buttonSearchSeriesOnClick(View view) {
    	myflipper.FlipTo(MyView.SEARCH);
    	search.showSearch("Shows");
    }
    

    
}
