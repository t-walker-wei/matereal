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
package com.phybots.workflow;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import com.phybots.entity.Resource;
import com.phybots.entity.Robot;
import com.phybots.message.Event;
import com.phybots.message.EventListener;
import com.phybots.message.ServiceEvent;
import com.phybots.message.ServiceStatus;
import com.phybots.task.Task;


public class Action extends Node implements EventListener, Serializable {
	private static final long serialVersionUID = -7946448386025919578L;
	private Robot robot;
	private Task task;
	private transient Set<Class<? extends Resource>> transferringResourceTypes;
	private transient boolean isDone;

	public Action(Robot robot, Task task) {
		this.robot = robot;
		this.task = task;
		transferringResourceTypes = new HashSet<Class<? extends Resource>>();
	}

	public Robot getRobot() {
		return robot;
	}

	public Task getTask() {
		return task;
	}

	@Override
	protected boolean isAllowedEntry() {
		synchronized (robot) {
			boolean isAllowedEntry;
			ResourceContext resourceContext = getWorkflow().getResourceContext();
			if (resourceContext != null &&
					resourceContext.getTask().getAssignedRobot() == robot) {
				freeRequiredResources();
				isAllowedEntry = task.isAssignable(robot);
				rollbackRequiredResources();
			} else {
				isAllowedEntry = task.isAssignable(robot);
			}
			return isAllowedEntry;
		}
	}

	@Override
	protected void onEnter() {
		ResourceContext resourceContext = getWorkflow().getResourceContext();
		if (resourceContext != null &&
				resourceContext.getTask().getAssignedRobot() == robot) {
			freeRequiredResources();
		}
		if (task.assign(robot)) {
			isDone = false;
			task.addEventListener(this);
			task.start();
		} else {
			rollbackRequiredResources();
		}
	}

	/**
	 * @see #eventOccurred(Event)
	 */
	@Override
	protected synchronized boolean isDone() {
		return isDone;
	}

	@Override
	protected void onLeave() {
		task.removeEventListener(this);
		task.stop();
		rollbackRequiredResources();
	}

	private void freeRequiredResources() {
		ResourceContext resourceContext = getWorkflow().getResourceContext();
		List<Class<? extends Resource>> requiredResourceTypes = task.getRequirements();

		// Look for the desired resources and free them.
		for (Class<? extends Resource> requiredResourceType : requiredResourceTypes) {
			for (Entry<Class<? extends Resource>, Resource> entry : resourceContext.getResources()) {
				Class<? extends Resource> resourceType = entry.getKey();
				if (requiredResourceType.isAssignableFrom(resourceType)) {
					robot.freeResource(entry.getValue(), resourceContext.getTask());
					transferringResourceTypes.add(resourceType);
				}
			}
		}
	}

	private void rollbackRequiredResources() {
		ResourceContext resourceContext = getWorkflow().getResourceContext();
		if (resourceContext != null &&
				!transferringResourceTypes.isEmpty()) {
			robot.requestResources(transferringResourceTypes, resourceContext.getTask());
			transferringResourceTypes.clear();
		}
	}

	public void eventOccurred(Event e) {
		if (e.getSource() == task) {
			if (e instanceof ServiceEvent) {
				ServiceEvent se = (ServiceEvent) e;
				if (se.getStatus() == ServiceStatus.STOPPED) {
					isDone = true;
				}
			}
		}
	}

	@Override
	public String toString() {
		return robot.getName()+": "+task.getClass().getSimpleName() /*.toString()*/;
	}
}
