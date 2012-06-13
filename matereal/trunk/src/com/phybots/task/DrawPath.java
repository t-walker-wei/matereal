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
package com.phybots.task;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

import com.phybots.entity.Resource;
import com.phybots.resource.PenController;
import com.phybots.task.VectorFieldTask;
import com.phybots.utils.Position;
import com.phybots.utils.Vector2D;


public class DrawPath extends VectorFieldTask {
	private static final long serialVersionUID = 5151082669273478700L;
	final private static double DUMMY_LENGTH = 20.0;
	private List<Position> path;

	private transient PenController pen;

	private transient Position currentGoal;
	private transient int currentGoalIndex;

	private transient Position nextGoal;
	private transient Position realGoal;
	private transient Vector2D v;

	public DrawPath(List<Position> path) {
		initialize(new ArrayList<Position>(path));
	}

	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
		ois.defaultReadObject();
		initialize(path);
	}

	private void initialize(List<Position> path) {
		this.path = path;
		currentGoalIndex = 0;
		currentGoal = path.get(currentGoalIndex ++);
		nextGoal = new Position();
		realGoal = new Position();
		v = new Vector2D();
	}

	@Override
	public List<Class<? extends Resource>> getRequirements() {
		List<Class<? extends Resource>> resourceTypes = super.getRequirements();
		resourceTypes.add(PenController.class);
		return resourceTypes;
	}

	@Override
	protected void onAssigned() {
		super.onAssigned();
		pen = getResourceMap().get(PenController.class);
	}

	@Override
	protected void onStart() {
		super.onStart();
		pen.putPen();
		getNextGoal();
	}

	private boolean getNextGoal() {
		if (currentGoalIndex >= path.size()) {
			return false;
		}
		nextGoal = path.get(currentGoalIndex);

		if (currentGoalIndex < path.size()) {
			// Get unit vector from departure to destination.
			v.set(nextGoal);
			v.sub(currentGoal);
			v.div(v.getNorm());
		}

		// Calculate dummy destination.
		realGoal.set(nextGoal);
		v.mul(DUMMY_LENGTH);
		realGoal.add(v);
		v.div(v.getNorm());

		currentGoal.set(nextGoal);
		currentGoalIndex ++;
		return true;
	}

	Position p = new Position();
	@Override
	public void run() {
		super.run();
		getPositionOut(p);
		p.sub(nextGoal);
		if (p.dot(v) > 0) {
			if (!getNextGoal()) {
				pen.endPen();
				finish();
			}
		}
	}

	@Override
	public void getUniqueVectorOut(Position position, Vector2D vector) {
		vector.set(
				realGoal.getX() - position.getX(),
				realGoal.getY() - position.getY());
	}
}
