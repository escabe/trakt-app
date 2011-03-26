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

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

public class Search {
	private Activity parentActivity;
	private String type;
	private EditText et;
	private ArrayList<Movie> MovieList;
	private ListView lv;
	private MovieAdapter ma;
	private Spinner sp;
	
	public Search(Activity activity) {
		parentActivity = activity;
		et = (EditText)parentActivity.findViewById(R.id.editSearch);
		lv = (ListView)parentActivity.findViewById(R.id.listSearch);
		MovieList = new ArrayList<Movie>();
		ma = new MovieAdapter(parentActivity,R.layout.searchrow,MovieList);
		lv.setAdapter(ma);

		Button b = (Button)parentActivity.findViewById(R.id.buttonSearch);
		
		b.setOnClickListener(new buttonSearchOnClick());
		
		sp = (Spinner)parentActivity.findViewById(R.id.spinSearch);
		sp.setOnItemSelectedListener(new spinOnItemSelected()) ;		
	}
	
	private class spinOnItemSelected implements AdapterView.OnItemSelectedListener {
		public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
			if (pos==0) {
				showSearch("Movies");
			} else {
				showSearch("Shows");
			}
		}
		public void onNothingSelected(AdapterView<?> parent) {
			// Do nothing
		}
	}
	
	private class buttonSearchOnClick implements View.OnClickListener {
		public void onClick(View v) {
			doSearch();
		}
	}
	
	
	public void showSearch(String t) {
		type = t;
		et.setHint("Search " + t);
		if (type=="Shows"){
			sp.setSelection(1);
		} else {
			sp.setSelection(0);
		}
	}

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
                    v = vi.inflate(R.layout.searchrow, null);
                }
                Movie o = items.get(position);
                if (o != null) {
                        TextView tt = (TextView) v.findViewById(R.id.textSearchRowTitle);
                        if (tt != null) {
                              tt.setText(o.getTitle());                            
                        }
                        TextView ti = (TextView) v.findViewById(R.id.textSearchRowInfo);
                        if (ti!=null) {
                        	ti.setText("" + o.getWatchers() + (o.getWatched()?" watched":""));
                        }

                }
                return v;
        }

	}
	
	public void doSearch() {
		MovieList.clear();
		ma.notifyDataSetInvalidated();
		if (type == "Shows") {
			MovieList.addAll(TvdbUtils.Search(et.getText().toString()));
		}
		
		ma.notifyDataSetChanged();
	
	}

}
