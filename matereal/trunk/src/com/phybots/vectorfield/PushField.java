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
package com.phybots.vectorfield;

import com.phybots.entity.Entity;
import com.phybots.utils.Position;
import com.phybots.utils.Vector2D;
import com.phybots.vectorfield.VectorFieldAbstractImpl;


/**
 * Vector field: Push<br />
 * Push an object to a certain position.
 * (<a href="http://designinterface.jp/projects/push/">A Dipole Field for Object Delivery by Pushing on a Flat Surface</a>)
 *
 * @author Jun Kato
 */
public class PushField extends VectorFieldAbstractImpl {
	private static final long serialVersionUID = 619630171424670817L;
	private final String nameString;
	private final Entity entity;
	private final Position destination;
	private final Position entityPosition;
	private transient double distance;

	public PushField(Entity entity, double x, double y) {
		this.entity = entity;
		entityPosition = new Position();
		destination = new Position(x, y);
		nameString = "Push "+entity.getName()+" to "+String.format("(%.2f, %.2f)", x, y);;
	}

	public PushField(Entity entity, Position destination) {
		this(entity, destination.getX(), destination.getY());
	}

	public Position getDestination() {
		return new Position(destination);
	}

	public void getDestinationOut(Position position) {
		position.set(destination);
	}

	public synchronized void getVectorOut(Position position, Vector2D vector) {
		getLocationProvider().getPositionOut(entity, entityPosition);
		computeDipoleVector(entityPosition, position, destination, vector);
		distance = entityPosition.distance(destination);
		vector.normalize();
	}

	public synchronized double getLastDistance() {
		return distance;
	}

	private final Vector2D robotToGoal = new Vector2D();
	private final Vector2D objectToGoal = new Vector2D();
	private final Vector2D goalToObject = new Vector2D();
	private final Vector2D objectToRobot = new Vector2D();
	private final Vector2D robotToObject = new Vector2D();
	private final Vector2D x = new Vector2D();
	private final Vector2D y = new Vector2D();
	public void computeDipoleVector(Position object, Position robot, Position goal, Vector2D vec) {

		Vector2D.subOut(goal, robot, robotToGoal);
		Vector2D.subOut(goal, object, objectToGoal);
		Vector2D.subOut(robot, object, objectToRobot);
		goalToObject.set(-objectToGoal.getX(), -objectToGoal.getY());
		robotToObject.set(-objectToRobot.getX(), -objectToRobot.getY());

		final double angle =
				object.getRelativeDirection(robot)
				-object.getRelativeDirection(goal);
		x.set(objectToGoal); x.normalize();
		y.set(-x.getY(), x.getX()); // y = rotate(x, pi/2);

		// computing "a" (move cautiously when the object is near the goal)
		final double
				angleROG = object.getRelativeDirection(robot)
							-object.getRelativeDirection(goal),
				angleORG = robot.getRelativeDirection(object)
							-robot.getRelativeDirection(goal);
		double a;
		if (angleORG == 0 || angleROG == 0) {
			a = 1;
		} else {
			a = Math.abs(angleROG / angleORG);
			if (a > 10) {
				a = 10;
			}
		}

		final double cos = Math.cos(angle);
		final double sin = Math.sin(angle);
		x.mul(cos*cos - a*sin*sin);	// x * cos(2 * angle)
		y.mul(sin*cos + a*sin*cos);	// y * sin(2 * angle)
		Vector2D.addOut(x, y, vec);

		// orbit when the robot is in front of the object
		objectToRobot.normalize();
		if (Vector2D.dot(vec, objectToRobot) > 0) {
			if (Vector2D.cross(vec, objectToRobot) == 0) {
				vec.set(-vec.getY(), vec.getX()); // vec = rotate(vec, pi/2);
			} else {
				objectToRobot.mul(Vector2D.dot(vec, objectToRobot));
				vec.sub(objectToRobot);
			}
		}
	}

	@Override
	public String getName() {
		return nameString;
	}
}