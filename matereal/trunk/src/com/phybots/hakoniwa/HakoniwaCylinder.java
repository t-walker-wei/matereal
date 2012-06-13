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
package com.phybots.hakoniwa;

import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;


import org.jbox2d.collision.shapes.CircleDef;
import org.jbox2d.common.Vec2;

import com.phybots.Phybots;
import com.phybots.utils.Location;
import com.phybots.utils.Position;

public class HakoniwaCylinder extends HakoniwaEntityAbstractImpl {
	private static final long serialVersionUID = 7085381442084030550L;
	final private static float FRICTION = 0.3f;
	final private static float RESTITUTION = 0.1f;
	final private static float WEIGHT = 0.8f;
	final private static float RADIUS = 35f;

	private transient Shape shape;

	private Color color = Color.blue;

	public HakoniwaCylinder() {
		this(Phybots.getInstance().lookForService(Hakoniwa.class));
	}

	public HakoniwaCylinder(Hakoniwa hakoniwa) {
		this(hakoniwa.getWidth()/2.0, hakoniwa.getHeight()/2.0, hakoniwa);
	}

	public HakoniwaCylinder(Location location) {
		this(location.getX(), location.getY(), location.getRotation());
	}

	public HakoniwaCylinder(Location location, Hakoniwa hakoniwa) {
		this(location.getX(), location.getY(), location.getRotation(), hakoniwa);
	}

	public HakoniwaCylinder(Position position) {
		this(position.getX(), position.getY());
	}

	public HakoniwaCylinder(Position position, Hakoniwa hakoniwa) {
		this(position.getX(), position.getY(), 0, hakoniwa);
	}

	public HakoniwaCylinder(double x, double y) {
		this(x, y, 0);
	}

	public HakoniwaCylinder(double x, double y, Hakoniwa hakoniwa) {
		this(x, y, 0, hakoniwa);
	}

	public HakoniwaCylinder(double x, double y, double rotation) {
		this(x, y, rotation, Phybots.getInstance().lookForService(Hakoniwa.class));
	}

	public HakoniwaCylinder(double x, double y, double rotation, Hakoniwa hakoniwa) {
		this(x, y, rotation, null, hakoniwa);
	}

	public HakoniwaCylinder(double x, double y, double rotation, Color color) {
		this(x, y, rotation, color, null);
	}

	public HakoniwaCylinder(double x, double y, double rotation, Color color, Hakoniwa hakoniwa) {
		this(x, y, rotation, RADIUS, color, hakoniwa);
	}

	public HakoniwaCylinder(double x, double y, double rotation, double radius) {
		this(x, y, rotation, radius, null, null);
	}

	public HakoniwaCylinder(double x, double y, double rotation, double radius, Hakoniwa hakoniwa) {
		this(x, y, rotation, radius, null, hakoniwa);
	}

	public HakoniwaCylinder(double x, double y, double rotation, double radius, Color color) {
		this(x, y, rotation, radius, color, null);
	}

	public HakoniwaCylinder(double x, double y, double rotation, double radius, Color color, Hakoniwa hakoniwa) {
		this(x, y, rotation, radius, WEIGHT, color, hakoniwa);
	}

	public HakoniwaCylinder(double x, double y, double rotation, double radius, double weight, Color color) {
		this(x, y, rotation, radius, weight, color, null);
	}

	public HakoniwaCylinder(double x, double y, double rotation, double radius, double weight, Color color, Hakoniwa hakoniwa) {
		super(hakoniwa);
		initialize(color, x, y, rotation, radius, weight);
	}

	public HakoniwaCylinder(String name) {
		this(name, Phybots.getInstance().lookForService(Hakoniwa.class));
	}

	public HakoniwaCylinder(String name, Hakoniwa hakoniwa) {
		this(name, hakoniwa.getWidth()/2.0, hakoniwa.getHeight()/2.0, hakoniwa);
	}

	public HakoniwaCylinder(String name, Location location) {
		this(name, location.getX(), location.getY(), location.getRotation());
	}

	public HakoniwaCylinder(String name, Location location, Hakoniwa hakoniwa) {
		this(name, location.getX(), location.getY(), location.getRotation(), hakoniwa);
	}

	public HakoniwaCylinder(String name, Position position) {
		this(name, position.getX(), position.getY());
	}

	public HakoniwaCylinder(String name, Position position, Hakoniwa hakoniwa) {
		this(name, position.getX(), position.getY(), 0, hakoniwa);
	}

	public HakoniwaCylinder(String name, double x, double y) {
		this(name, x, y, 0);
	}

	public HakoniwaCylinder(String name, double x, double y, Hakoniwa hakoniwa) {
		this(name, x, y, 0, hakoniwa);
	}

	public HakoniwaCylinder(String name, double x, double y, double rotation) {
		this(name, x, y, rotation, Phybots.getInstance().lookForService(Hakoniwa.class));
	}

	public HakoniwaCylinder(String name, double x, double y, double rotation, Hakoniwa hakoniwa) {
		this(name, x, y, rotation, null, hakoniwa);
	}

	public HakoniwaCylinder(String name, double x, double y, double rotation, Color color) {
		this(name, x, y, rotation, color, null);
	}

	public HakoniwaCylinder(String name, double x, double y, double rotation, Color color, Hakoniwa hakoniwa) {
		this(name, x, y, rotation, RADIUS, color, hakoniwa);
	}

	public HakoniwaCylinder(String name, double x, double y, double rotation, double radius) {
		this(name, x, y, rotation, radius, null, null);
	}

	public HakoniwaCylinder(String name, double x, double y, double rotation, double radius, Hakoniwa hakoniwa) {
		this(name, x, y, rotation, radius, null, hakoniwa);
	}

	public HakoniwaCylinder(String name, double x, double y, double rotation, double radius, Color color) {
		this(name, x, y, rotation, radius, color, null);
	}

	public HakoniwaCylinder(String name, double x, double y, double rotation, double radius, Color color, Hakoniwa hakoniwa) {
		this(name, x, y, rotation, radius, WEIGHT, color, hakoniwa);
	}

	public HakoniwaCylinder(String name, double x, double y, double rotation, double radius, double weight, Color color) {
		this(name, x, y, rotation, radius, weight, color, null);
	}

	public HakoniwaCylinder(String name, double x, double y, double rotation, double radius, double weight, Color color, Hakoniwa hakoniwa) {
		super(name, hakoniwa);
		initialize(color, x, y, rotation, radius, weight);
	}

	private void writeObject(ObjectOutputStream oos) throws IOException {
		oos.defaultWriteObject();

		Vec2 position = getBody().getPosition();
		float x = position.x * 100;
		float y = position.y * 100;
		float angle = getBody().getAngle();

		CircleDef cd = (CircleDef) getShapeDef();
		float radius = cd.radius * 100;
		float weight = (float) (cd.density * radius * radius * Math.PI);

		oos.writeObject(new float[] {
			x, y, angle, radius, weight
		});
	}

	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
		ois.defaultReadObject();

		float[] parameters = (float[]) ois.readObject();
		float x = parameters[0];
		float y = parameters[1];
		float angle = parameters[2];
		float radius = parameters[3];
		float weight = parameters[4];

		initialize(null, x, y, angle, radius, weight);
	}

	private void initialize(Color color, double x, double y, double rotation, double radius, double weight) {

		if (color != null) {
			setColor(color);
		}

		final CircleDef cd = new CircleDef();
		cd.radius = (float) (radius / 100);
		cd.restitution = RESTITUTION;
		cd.density = (float) (weight / (radius * radius * Math.PI));
		cd.friction = FRICTION;

		shape = new Ellipse2D.Double(-radius, -radius, radius*2, radius*2);
		initialize(x, y, rotation, cd);
	}

	public void setColor(Color color) {
		this.color  = color;
	}

	public Color getColor() {
		return color;
	}

	public void preStep() {
		// Do nothing.
	}

	public Shape getShape() {
		return shape;
	}

}
