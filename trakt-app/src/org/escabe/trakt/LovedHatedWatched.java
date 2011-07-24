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
