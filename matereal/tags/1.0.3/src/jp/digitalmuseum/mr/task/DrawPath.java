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
import java.util.List;

import jp.digitalmuseum.mr.entity.Resource;
import jp.digitalmuseum.mr.resource.PenController;
import jp.digitalmuseum.utils.Position;
import jp.digitalmuseum.utils.Vector2D;

public class DrawPath extends VectorFieldTask {
	final private static double DUMMY_LENGTH = 20.0;
	private List<Position> path;
	private int currentGoalIndex;
	private Position currentGoal;
	private Position nextGoal = new Position();
	private Position realGoal = new Position();
	private Vector2D v = new Vector2D();

	public DrawPath(List<Position> path) {
		this.path = new ArrayList<Position>(path);
		currentGoal = path.get(currentGoalIndex ++);
	}

	@Override
	public List<Class<? extends Resource>> getRequirements() {
		List<Class<? extends Resource>> resourceTypes = super.getRequirements();
		resourceTypes.add(PenController.class);
		return resourceTypes;
	}

	@Override
	protected void onStart() {
		super.onStart();
		getResourceMap().get(PenController.class).putPen();
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
				getResourceMap().get(PenController.class).endPen();
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
