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
package jp.digitalmuseum.mr.vectorfield;

import jp.digitalmuseum.mr.entity.Robot;
import jp.digitalmuseum.mr.service.LocationProvider;
import jp.digitalmuseum.utils.Position;
import jp.digitalmuseum.utils.Vector2D;

public class MoveField extends VectorFieldAbstractImpl {
	private final static String TASK_NAME_PREFIX = "Move to ";
	private final String nameString;
	private Robot robot;
	private final Position destination;
	private double distance;

	public MoveField(Robot robot, double x, double y) {
		this.robot = robot;
		destination = new Position();
		destination.set(x, y);
		nameString = TASK_NAME_PREFIX+String.format("(%.2f, %.2f)", x, y);
	}

	public MoveField(double x, double y) {
		this(null, x, y);
	}

	public MoveField(Robot robot, Position destination) {
		this(destination.getX(), destination.getY());
	}

	public MoveField(Position destination) {
		this(null, destination);
	}

	public void setRobot(Robot robot) {
		this.robot = robot;
		updateLocationProvider();
	}

	public Robot getRobot() {
		return robot;
	}

	public Position getDestination() {
		return new Position(destination);
	}

	public void getDestinationOut(Position position) {
		position.set(destination);
	}

	public synchronized void getVectorOut(Position position, Vector2D vector) {
		vector.set(
				destination.getX()-position.getX(),
				destination.getY()-position.getY());
		distance = vector.getNorm();
		vector.normalize();
	}

	public synchronized double getLastDistance() {
		return distance;
	}

	@Override
	protected boolean checkLocationProvider(LocationProvider locationProvider) {
		return robot == null ? true : locationProvider.contains(robot);
	}

	@Override
	public String getName() {
		return nameString;
	}
}
