/*
 * PROJECT: Phybots at http://phybots.com/
 * ----------------------------------------------------------------------------
 *
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Phybots.
 *
 * The Initial Developer of the Original Code is Jun Kato.
 * Portions created by the Initial Developer are
 * Copyright (C) 2009 Jun Kato. All Rights Reserved.
 *
 * Contributor(s): Jun Kato
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either of the GNU General Public License Version 2 or later (the "GPL"),
 * or the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 */
package com.phybots.p5.andy;

import java.util.HashSet;

import com.phybots.message.Event;
import com.phybots.message.EventListener;
import com.phybots.message.LocationUpdateEvent;


public class Entity {

	private HashSet<LocationListener> listeners;
	private com.phybots.utils.Location location;
	private com.phybots.utils.Position position;
	private EventListener listener;
	private com.phybots.entity.Entity entityCore;

	public Entity(com.phybots.entity.Entity entity) {
		Andy.getInstance();
		this.entityCore = entity;
		listeners = new HashSet<LocationListener>();
		location = new com.phybots.utils.Location();
		position = new com.phybots.utils.Position();
		listener = new EventListener() {
			private final com.phybots.utils.Location location =
					new com.phybots.utils.Location();

			public void eventOccurred(Event e) {
				if (e instanceof LocationUpdateEvent) {
					synchronized (listeners) {
						((LocationUpdateEvent) e).getSource().getLocationOut(getEntityCore(), location);
						if (!location.isNotFound()) {
							final Location location = new Location();
							location.setLocation(location);
							for (LocationListener locationListener : listeners) {
								locationListener.locationUpdated(
										Entity.this, location);
							}
						}
					}
				}
			}
		};
	}

	public void addLocationListener(LocationListener locationListener) {
		synchronized (listeners) {
			listeners.add(locationListener);
			if (listeners.size() == 1) {
				registerLocationListener();
			}
		}
	}

	public LocationListener removeLocationListener(
			LocationListener locationListener) {
		synchronized (listeners) {
			if (listeners.remove(locationListener)) {
				if (listeners.size() == 0) {
					unregisterLocationListener();
				}
				return locationListener;
			}
			return null;
		}
	}

	private void registerLocationListener() {
		Andy.getInstance().getLocationProvider().addEventListener(listener);
	}

	private void unregisterLocationListener() {
		Andy.getInstance().getLocationProvider().removeEventListener(listener);
	}

	public Location getLocation() {
		Location location = new Location();
		getLocationOut(location);
		return location;
	}

	public synchronized void getLocationOut(Location location) {
		Andy.getInstance().getLocationProvider().getLocationOut(
				getEntityCore(), this.location);
		location.setLocation(this.location);
	}

	public Position getPosition() {
		Position position = new Position();
		getPositionOut(position);
		return position;
	}

	public void getPositionOut(Position position) {
		Andy.getInstance().getLocationProvider().getPositionOut(
				getEntityCore(), this.position);
		position.set(this.position);
	}

	public com.phybots.entity.Entity getEntityCore() {
		return entityCore;
	}
}
