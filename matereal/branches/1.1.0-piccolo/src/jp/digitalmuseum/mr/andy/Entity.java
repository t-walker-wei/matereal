/*
 * PROJECT: matereal at http://mr.digitalmuseum.jp/
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
 * The Original Code is matereal.
 *
 * The Initial Developer of the Original Code is Jun KATO.
 * Portions created by the Initial Developer are
 * Copyright (C) 2009 Jun KATO. All Rights Reserved.
 *
 * Contributor(s): Jun KATO
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
package jp.digitalmuseum.mr.andy;

import java.util.HashSet;

import jp.digitalmuseum.mr.message.Event;
import jp.digitalmuseum.mr.message.EventListener;
import jp.digitalmuseum.mr.message.LocationUpdateEvent;

public class Entity {

	private HashSet<LocationListener> listeners;
	private jp.digitalmuseum.utils.Location location;
	private jp.digitalmuseum.utils.Position position;
	private EventListener listener;
	private jp.digitalmuseum.mr.entity.Entity entityCore;

	public Entity(jp.digitalmuseum.mr.entity.Entity entity) {
		Andy.getInstance();
		this.entityCore = entity;
		listeners = new HashSet<LocationListener>();
		location = new jp.digitalmuseum.utils.Location();
		position = new jp.digitalmuseum.utils.Position();
		listener = new EventListener() {
			private final jp.digitalmuseum.utils.Location location =
					new jp.digitalmuseum.utils.Location();

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

	public jp.digitalmuseum.mr.entity.Entity getEntityCore() {
		return entityCore;
	}
}
