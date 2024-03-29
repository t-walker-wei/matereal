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

import jp.digitalmuseum.mr.activity.Action;
import jp.digitalmuseum.mr.activity.ActivityDiagram;
import jp.digitalmuseum.mr.activity.ResourceContext;
import jp.digitalmuseum.utils.Position;

public class TracePath extends LocationBasedTaskAbstractImpl {
	private double allowedDeviationAngle;
	private double allowedDistance;
	private double allowedInterimDistance;
	protected List<Position> path;
	protected Action[] actions;

	public TracePath(List<Position> path) {
		allowedDeviationAngle = VectorFieldTask.defaultAllowedDeviationAngle;
		allowedDistance = Move.defaultAllowedDistance;
		allowedInterimDistance = Move.defaultAllowedDistance*2;
		updatePath(path);
	}

	@Override
	protected void onAssigned() {
		updateSubDiagram();
	}

	public void run() {
		if (!getSubDiagram().isStarted()) {
			finish();
		}
	}

	public synchronized void setAllowedDeviationAngle(double allowedDeviationAngle) {
		this.allowedDeviationAngle = allowedDeviationAngle;
		if (isStarted()) {
			for (Action action : actions) {
				((Move) action.getTask())
						.setAllowedDistance(allowedDeviationAngle);
			}
		}
	}

	public synchronized double getAllowedDeviationAngle() {
		return allowedDeviationAngle;
	}

	public synchronized void setAllowedDistance(double allowedDistance) {
		this.allowedDistance = allowedDistance;
		if (isStarted()) {
			((Move) actions[actions.length - 1].getTask())
					.setAllowedDistance(allowedDistance);
		}
	}

	public synchronized double getAllowedDistance() {
		return allowedDistance;
	}

	public synchronized void setAllowedInterimDistance(double allowedInterimDistance) {
		this.allowedInterimDistance = allowedInterimDistance;
		if (isStarted()) {
			for (int i = 0; i < actions.length - 1; i ++) {
				((Move) actions[i].getTask())
						.setAllowedDistance(allowedInterimDistance);
			}
		}
	}

	public synchronized double getAllowedInterimDistance() {
		return allowedInterimDistance;
	}

	public synchronized void updatePath(List<Position> path) {
		this.path = new ArrayList<Position>(path);
		if (isStarted()) {
			getSubDiagram().stop();
			updateSubDiagram();
			getSubDiagram().start();
		}
	}

	public synchronized List<Position> getPath() {
		return new ArrayList<Position>(path);
	}

	protected void updateSubDiagram() {
		ActivityDiagram subDiagram = new ActivityDiagram(
				new ResourceContext(this, getResourceMap()));
		actions = new Action[path.size()];
		int i = 0;
		for (Position position : path) {
			Move move = new Move(position);
			move.setAllowedDeviationAngle(allowedDeviationAngle);
			move.setAllowedDistance(i == actions.length-1 ?
					allowedDistance : allowedInterimDistance);
			actions[i ++] = new Action(getAssignedRobot(), move);
		}
		subDiagram.addInSerial(actions);
		subDiagram.setInitialNode(actions[0]);
		setSubDiagram(subDiagram);
	}
}
