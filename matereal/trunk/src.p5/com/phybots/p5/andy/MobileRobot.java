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

import jp.digitalmuseum.connector.Connector;
import jp.digitalmuseum.connector.ConnectorFactory;

import com.phybots.entity.PhysicalRobot;
import com.phybots.entity.Robot;
import com.phybots.task.FollowVectorField;
import com.phybots.task.GoBackward;
import com.phybots.task.GoForward;
import com.phybots.task.Move;
import com.phybots.task.Push;
import com.phybots.task.SpinLeft;
import com.phybots.task.SpinRight;
import com.phybots.task.Stop;
import com.phybots.task.Task;
import com.phybots.utils.VectorField;

public class MobileRobot extends Entity {
	private static final String PREFIX_PACKAGE_NAME = "com.phybots.entity.";
	private Robot robot;
	private Task task;
	private Position goal;
	private Entity entity;

	public static Robot getRobotInstance(
			String robotClassName) {
		Robot robot = null;
		if (!robotClassName.startsWith(PREFIX_PACKAGE_NAME) &&
				!robotClassName.contains(".")) {
			robotClassName = PREFIX_PACKAGE_NAME + robotClassName;
		}
		try {
			robot = (Robot) Class.forName(robotClassName).newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return robot;
	}
	
	public static PhysicalRobot getRobotInstance(
			String robotClassName, String connectorName) {
		PhysicalRobot robot = (PhysicalRobot) getRobotInstance(robotClassName);
		Connector connector = ConnectorFactory.makeConnector(connectorName);
		robot.setConnector(connector);
		return robot;
	}

	public MobileRobot(String robotClassName) {
		this(getRobotInstance(robotClassName));
	}

	public MobileRobot(String robotClassName, String connectorName) {
		this(getRobotInstance(robotClassName, connectorName));
	}

	public MobileRobot(Robot robot) {
		super(robot);
		goal = new Position();
		this.robot = robot;
	}

	public String getStatus() {
		if (task == null) {
			return null;
		}
		return task.toString();
	}

	public void setName(String name) {
		robot.setName(name);
	}

	public String getName() {
		return robot.getName();
	}

	public Position getGoal() {
		if (task == null) {
			return null;
		}
		return new Position(goal);
	}

	public boolean getGoalOut(Position p) {
		if (task == null) {
			return false;
		}
		p.set(goal);
		return true;
	}

	public boolean startTask(Task task) {
		if (task == null) {
			return false;
		}
		if (this.task != null) {
			stopTask();
		}
		if (task.assign(robot)) {
			task.start();
			this.task = task;
			return true;
		}
		return false;
	}

	public void stopTask() {
		task.stop();
	}

	public Task getTask() {
		return task;
	}

	public Entity getSubject() {
		return entity;
	}

	public boolean forward() {
		return startTask(new GoForward());
	}

	public boolean backward() {
		return startTask(new GoBackward());
	}

	public boolean spinLeft() {
		return startTask(new SpinLeft());
	}

	public synchronized boolean spinRight() {
		return startTask(new SpinRight());
	}

	public synchronized boolean stop() {
		return startTask(new Stop());
	}

	private boolean move() {
		Task move = new Move(goal.getX(), goal.getY());
		return startTask(move);
	}

	public synchronized boolean moveTo(Position p) {
		goal.set(p);
		return move();
	}

	public synchronized boolean moveTo(double x, double y) {
		goal.set(x, y);
		return move();
	}

	public synchronized boolean moveTo(int screenX, int screenY) {
		goal.setScreen(screenX, screenY);
		return move();
	}

	private boolean push() {
		Task push = new Push(
				entity.getEntityCore(), goal.getX(), goal.getY());
		return startTask(push);
	}

	public synchronized boolean pushTo(Entity e, Position p) {
		entity = e;
		goal.set(p);
		return push();
	}

	public synchronized boolean pushTo(Entity e, double x, double y) {
		entity = e;
		goal.set(x, y);
		return push();
	}

	public synchronized boolean pushTo(Entity e, int screenX, int screenY) {
		entity = e;
		goal.setScreen(screenX, screenY);
		return push();
	}

	public boolean followVectorField(VectorField vf) {
		return startTask(new FollowVectorField(vf));
	}

	public Robot getEntityCore() {
		return robot;
	}
}
