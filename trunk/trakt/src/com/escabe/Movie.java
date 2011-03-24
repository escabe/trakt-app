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