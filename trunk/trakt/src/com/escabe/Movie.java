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

//Class to hold information about a movie
public class Movie {
	private String title;
	private String poster;
	private int watchers;
	private String id;
	private boolean watched = false;
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String t) {
		title = t;
	}
	public String getPoster() {
		return poster;
	}
	public void setPoster(String p) {
		poster = p;
	}
	public int getWatchers(){
		return watchers;
	}
	public void setWatchers(int w) {
		watchers = w;
	}
	public String getID() {
		return id;
	}
	public void setID(String i){
		id = i;
	}
	public boolean getWatched() {
		return watched;
	}
	public void setWatched(boolean w){
		watched = w;
	}
} 
