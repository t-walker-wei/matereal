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

import com.phybots.utils.Position;
import com.phybots.utils.Vector2D;
import com.phybots.vectorfield.VectorFieldAbstractImpl;


/**
 * Vector field: Move<br />
 * Move to a certain position.
 *
 * @author Jun Kato
 */
public class MoveField extends VectorFieldAbstractImpl {
	private static final long serialVersionUID = -6141693591556712915L;
	private final static String TASK_NAME_PREFIX = "Move to ";
	private final String nameString;
	private final Position destination;
	private transient double distance;

	public MoveField(double x, double y) {
		destination = new Position();
		destination.set(x, y);
		nameString = TASK_NAME_PREFIX+String.format("(%.2f, %.2f)", x, y);
	}

	public MoveField(Position destination) {
		this(destination.getX(), destination.getY());
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
	public String getName() {
		return nameString;
	}
}