package org.escabe.trakt;

public class LovedHatedWatched {
	private boolean loved=false;
	private boolean hated=false;
	private boolean watched=false;
	
	public LovedHatedWatched(boolean watched) {
		this.watched = watched;
	}
	
	public void setLoved(boolean loved) {
		this.loved = loved;
	}

	public boolean isLoved() {
		return loved;
	}

	public void setHated(boolean hated) {
		this.hated = hated;
	}

	public boolean isHated() {
		return hated;
	}

	public void setWatched(boolean watched) {
		this.watched = watched;
	}

	public boolean isWatched() {
		return watched;
	}
}
