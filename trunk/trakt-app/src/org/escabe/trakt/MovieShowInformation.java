package org.escabe.trakt;

import org.escabe.trakt.TraktAPI.ShowMovie;

/**
 * Class to hold information about a Movie or Show
 * @author escabe
 *
 */
public class MovieShowInformation {
	private String title=null;
	private String poster=null;
	private String id=null;
	private boolean viewed=false;
	private int watchers = 0;
	private ShowMovie showmovie; 
	
	public void setTitle(String title) {
		this.title = title;
	}

	public String getTitle() {
		return title;
	}

	public void setPoster(String poster) {
		this.poster = poster;
	}

	public String getPoster() {
		return poster;
	}

	public void setViewed(boolean viewed) {
		this.viewed = viewed;
	}

	public boolean isViewed() {
		return viewed;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void setWatchers(int watchers) {
		this.watchers = watchers;
	}

	public int getWatchers() {
		return watchers;
	}

	public void setShowmovie(ShowMovie showmovie) {
		this.showmovie = showmovie;
	}

	public ShowMovie getShowmovie() {
		return showmovie;
	}
	
	
}