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

import jp.digitalmuseum.mr.entity.Robot;
import jp.digitalmuseum.mr.task.FollowVectorField;
import jp.digitalmuseum.mr.task.GoBackward;
import jp.digitalmuseum.mr.task.GoForward;
import jp.digitalmuseum.mr.task.Move;
import jp.digitalmuseum.mr.task.Push;
import jp.digitalmuseum.mr.task.SpinLeft;
import jp.digitalmuseum.mr.task.SpinRight;
import jp.digitalmuseum.mr.task.Stop;
import jp.digitalmuseum.mr.task.Task;
import jp.digitalmuseum.utils.VectorField;

public class MobileRobot extends Entity {
	private Robot robot;
	private Task task;
	private Position position;
	private Entity entity;

	public MobileRobot(Robot robot) {
		Andy.getInstance();
		position = new Position();
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
		return new Position(position);
	}

	public boolean getGoalOut(Position p) {
		if (task == null) {
			return false;
		}
		p.set(position);
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
		Task move = new Move(position.getX(), position.getY());
		return startTask(move);
	}

	public synchronized boolean moveTo(Position p) {
		position.set(p);
		return move();
	}

	public synchronized boolean moveTo(double x, double y) {
		position.set(x, y);
		return move();
	}

	public synchronized boolean moveTo(int screenX, int screenY) {
		position.setScreen(screenX, screenY);
		return move();
	}

	private boolean push() {
		Task push = new Push(
				entity.getEntityCore(), position.getX(), position.getY());
		return startTask(push);
	}

	public synchronized boolean pushTo(Entity e, Position p) {
		entity = e;
		position.set(p);
		return push();
	}

	public synchronized boolean pushTo(Entity e, double x, double y) {
		entity = e;
		position.set(x, y);
		return push();
	}

	public synchronized boolean pushTo(Entity e, int screenX, int screenY) {
		entity = e;
		position.setScreen(screenX, screenY);
		return push();
	}

	public boolean followVectorField(VectorField vf) {
		return startTask(new FollowVectorField(vf));
	}

	public jp.digitalmuseum.mr.entity.Entity getEntityCore() {
		return robot;
	}
}
