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
package jp.digitalmuseum.mr.task;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import jp.digitalmuseum.mr.activity.ActivityDiagram;
import jp.digitalmuseum.mr.entity.Resource;
import jp.digitalmuseum.mr.entity.ResourceMap;
import jp.digitalmuseum.mr.entity.Robot;
import jp.digitalmuseum.mr.message.Event;
import jp.digitalmuseum.mr.message.EventListener;
import jp.digitalmuseum.mr.message.RobotEvent;
import jp.digitalmuseum.mr.message.ServiceEvent;
import jp.digitalmuseum.mr.message.ServiceEvent.STATUS;
import jp.digitalmuseum.mr.service.ServiceAbstractImpl;
import jp.digitalmuseum.mr.service.ServiceGroup;

/**
 * Abstract implementation of Task interface.<br />
 * All Task implementation classes must extend this abstract class.
 *
 * @author Jun KATO
 */
public abstract class TaskAbstractImpl extends ServiceAbstractImpl implements Task {
	private Robot robot;
	private Queue<RobotEvent> robotEventQueue;
	private ResourceMap resourceMap;
	private RobotEventListener robotEventListener;
	private ActivityDiagram subDiagram;
	private boolean isStopping;

	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}

	public TaskAbstractImpl() {
		super();
		robotEventQueue = new LinkedList<RobotEvent>();
		robotEventListener = new RobotEventListener();
	}

	/**
	 * @return Returns a set of required interfaces for this task.
	 */
	public List<Class<? extends Resource>> getRequirements() {
		return new ArrayList<Class<? extends Resource>>();
	}

	public synchronized boolean assign(Robot robot) {
		ResourceMap resourceMap = robot.requestResources(getRequirements(), this);
		if (resourceMap != null) {
			this.robot = robot;
			this.resourceMap = resourceMap;
			onAssigned();
			return true;
		}
		return false;
	}

	public boolean isAssignable(Robot robot) {
		synchronized (robot) {
			return requirementsSuppliable(
					getRequirements(),
					robot.getAvailableExclusiveResourceTypes());
		}
	}

	public synchronized boolean isAssigned() {
		return robot != null;
	}

	public Robot getAssignedRobot() {
		return robot;
	}

	@Override
	final public synchronized void start() {
		start(null);
	}

	@Override
	final public synchronized void start(ServiceGroup serviceGroup) {
		if (robot == null) {
			throw new IllegalStateException("This task ("+getName()+") is not assigned to a robot.");
		}
		super.start(serviceGroup);
		if (hasSubDiagram()) {
			getSubDiagram().start();
		}
	}

	/**
	 * Free resources and stop this task.
	 */
	@Override
	final synchronized public void stop() {
		if (!isStarted()) {
			return;
		}
		isStopping = true;
		if (hasSubDiagram()) {
			getSubDiagram().stop();
		}
		receiveRobotEvent(false);
		robot.freeResources(resourceMap.resources(), this);
		robot = null;
		resourceMap = null;
		super.stop();
		isStopping = false;
	}


	protected void setSubDiagram(ActivityDiagram subDiagram) {
		this.subDiagram = subDiagram;
	}

	public ActivityDiagram getSubDiagram() {
		return subDiagram;
	}

	public boolean hasSubDiagram() {
		return subDiagram != null;
	}

	/**
	 * @return Returns the current resource map.
	 */
	protected ResourceMap getResourceMap() {
		return resourceMap;
	}

	/**
	 * Called when this task is assigned to a robot.
	 * This method is usually used for getting a resource object and saving it to a instance field.
	 */
	protected void onAssigned() {
		// Do nothing unless overrode by subclasses.
	}

	protected synchronized void finish() {
		if (!isStopping) {
			stop();
		}
		distributeEvent(new ServiceEvent(this, STATUS.FINISHED));
	}

	/**
	 * Listen to/Ignore RobotEvent brought by the assigned robot.
	 * @see #hasNextRobotEvent()
	 * @see #pollRobotEvent()
	 */
	final protected void receiveRobotEvent(boolean flag) {
		if (flag) {
			robot.addEventListener(robotEventListener);
		} else {
			robot.removeEventListener(robotEventListener);
		}
	}

	/**
	 * @return Returns true if at least one RobotEvent is available in the event queue.
	 */
	final protected boolean hasNextRobotEvent() {
		return !robotEventQueue.isEmpty();
	}

	/**
	 * @return Returns RobotEvent if available in the event queue. Otherwise, returns null.
	 */
	final protected RobotEvent pollRobotEvent() {
		return robotEventQueue.poll();
	}

	private class RobotEventListener implements EventListener {

		/**
		 * <b>Note:</b> This method here is thread-safe by natural, because only one robot calls this method.
		 */
		public void eventOccurred(Event event) {
			if (event instanceof RobotEvent) {
				robotEventQueue.add((RobotEvent) event);
			}
		}
	}

	/**
	 * @param list
	 * @param list2
	 * @return Returns if the requirements can be satisfied with the supply by means of class types.
	 */
	public static boolean requirementsSuppliable(
			List<Class<? extends Resource>> list,
			List<Class<? extends Resource>> list2) {
		final Set<Class<? extends Resource>> reqs
				= new HashSet<Class<? extends Resource>>(list);
		for (Class<? extends Resource> cls : list2) {
			for (Iterator<Class<? extends Resource>> it = reqs.iterator(); it.hasNext();) {
				final Class<? extends Resource> req = it.next();
				if (req.isAssignableFrom(cls)) {
					it.remove();
				}
			}
		}
		return reqs.isEmpty();
	}
}
