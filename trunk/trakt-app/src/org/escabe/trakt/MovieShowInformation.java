package org.escabe.trakt;

public class MovieShowInformation {
	private String title=null;
	private String poster=null;
	private String id=null;
	private boolean viewed=false;
	
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
	
	
}
