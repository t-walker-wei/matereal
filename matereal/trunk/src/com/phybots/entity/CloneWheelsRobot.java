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
package com.phybots.entity;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.phybots.entity.ProxyResourceAbstractImpl;
import com.phybots.entity.ProxyRobotAbstractImpl;
import com.phybots.entity.Resource;
import com.phybots.entity.ResourceAbstractImpl;
import com.phybots.entity.Robot;
import com.phybots.resource.WheelsController;


/**
 * ProxyRobot: CloneWheelsRobot<br />
 * Proxy robot class that copies moving functions to multiple robots.
 * TODO Implement PairRobot which represents two robots.
 *
 * @author Jun Kato
 */
public class CloneWheelsRobot extends ProxyRobotAbstractImpl {
	private static final long serialVersionUID = 4996307031791909067L;
	final private Set<Robot> robots;
	final private Set<WheelsController> wheels;
	final private CloneWheels w;

	public CloneWheelsRobot(Set<Robot> robots) {
		this.robots = new HashSet<Robot>(robots);
		wheels = new HashSet<WheelsController>();
		w = new CloneWheels(this);
	}

	@Override
	protected List<ResourceAbstractImpl> getResources() {
		List<ResourceAbstractImpl> rs = super.getResources();
		rs.add(w);
		return rs;
	}

	public Set<Robot> getRelatedRobots() {
		return new HashSet<Robot>(robots);
	}

	@Override
	public synchronized <T extends Resource> T requestResource(
			Class<T> resourceType, Object owner) {
		final T virtualResource =
				super.requestResource(resourceType, owner);
		if (virtualResource != null) {
			for (Robot robot : robots) {
				final WheelsController w =
						robot.requestResource(WheelsController.class, owner);
				if (w == null) {

					// Free all resources when failed to acquire all resources.
					for (WheelsController w_ : wheels) {
						robot.freeResource(w_, owner);
					}
					return null;
				}
				wheels.add(w);
			}
		}
		return virtualResource;
	}

	private class CloneWheels extends ProxyResourceAbstractImpl implements WheelsController {
		private static final long serialVersionUID = -4046377720816462415L;
		private WheelsStatus status;

		public CloneWheels(ProxyRobotAbstractImpl robot) {
			super(robot);
			status = WheelsStatus.STOP;
		}

		public WheelsStatus getStatus() {
			return status;
		}
		public void goBackward() {
			status = WheelsStatus.GO_BACKWARD;
			for (WheelsController wh : wheels) { wh.goBackward(); }
		}
		public void goForward() {
			status = WheelsStatus.GO_FORWARD;
			for (WheelsController wh : wheels) { wh.goForward(); }
		}
		public void spin(Spin direction) {
			if (direction == Spin.LEFT) {
				spinLeft();
			} else {
				spinRight();
			}
		}
		public void spinLeft() {
			status = WheelsStatus.SPIN_LEFT;
			for (WheelsController wh : wheels) { wh.spinLeft(); }
		}
		public void spinRight() {
			status = WheelsStatus.SPIN_RIGHT;
			for (WheelsController wh : wheels) { wh.spinRight(); }
		}
		public void stopWheels() {
			status = WheelsStatus.STOP;
			for (WheelsController wh : wheels) { wh.stopWheels(); }
		}

		@Override
		public void onFree() {
			stopWheels();
		}
	}
}
