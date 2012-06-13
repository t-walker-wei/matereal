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
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;



import org.jbox2d.collision.shapes.PolygonDef;
import org.jbox2d.common.Vec2;

import com.phybots.Phybots;
import com.phybots.utils.Location;
import com.phybots.utils.Position;

public class HakoniwaBox extends HakoniwaEntityAbstractImpl {
	private static final long serialVersionUID = -1083652973569308258L;
	final private static float FRICTION = 0.3f;
	final private static float RESTITUTION = 0.1f;
	final private static double WEIGHT = 0.8;
	final private static double WIDTH = 40;
	final private static double HEIGHT = 30;

	private transient Shape shape;

	private Color color = Color.blue;

	public HakoniwaBox() {
		this(Phybots.getInstance().lookForService(Hakoniwa.class));
	}

	public HakoniwaBox(Hakoniwa hakoniwa) {
		this(hakoniwa.getWidth()/2.0, hakoniwa.getHeight()/2.0, hakoniwa);
	}

	public HakoniwaBox(Location location) {
		this(location.getX(), location.getY(), location.getRotation());
	}

	public HakoniwaBox(Location location, Hakoniwa hakoniwa) {
		this(location.getX(), location.getY(), location.getRotation(), hakoniwa);
	}

	public HakoniwaBox(Position position) {
		this(position.getX(), position.getY());
	}

	public HakoniwaBox(Position position, Hakoniwa hakoniwa) {
		this(position.getX(), position.getY(), 0, hakoniwa);
	}

	public HakoniwaBox(double x, double y) {
		this(x, y, 0);
	}

	public HakoniwaBox(double x, double y, Hakoniwa hakoniwa) {
		this(x, y, 0, hakoniwa);
	}

	public HakoniwaBox(double x, double y, double rotation) {
		this(x, y, rotation, Phybots.getInstance().lookForService(Hakoniwa.class));
	}

	public HakoniwaBox(double x, double y, double rotation, Hakoniwa hakoniwa) {
		this(x, y, rotation, null, hakoniwa);
	}

	public HakoniwaBox(double x, double y, double rotation, Color color) {
		this(x, y, rotation, color, null);
	}

	public HakoniwaBox(double x, double y, double rotation, Color color, Hakoniwa hakoniwa) {
		this(x, y, rotation, WIDTH, HEIGHT, color, hakoniwa);
	}

	public HakoniwaBox(double x, double y, double rotation, double width, double height) {
		this(x, y, rotation, width, height, null, null);
	}

	public HakoniwaBox(double x, double y, double rotation, double width, double height, Hakoniwa hakoniwa) {
		this(x, y, rotation, width, height, null, hakoniwa);
	}

	public HakoniwaBox(double x, double y, double rotation, double width, double height, Color color) {
		this(x, y, rotation, width, height, color, null);
	}

	public HakoniwaBox(double x, double y, double rotation, double width, double height, Color color, Hakoniwa hakoniwa) {
		this(x, y, rotation, width, height, WEIGHT, color, hakoniwa);
	}

	public HakoniwaBox(double x, double y, double rotation, double width, double height, double weight, Color color) {
		this(x, y, rotation, width, height, weight, color, null);
	}

	public HakoniwaBox(double x, double y, double rotation, double width, double height, double weight, Color color, Hakoniwa hakoniwa) {
		super(hakoniwa);
		initialize(color, x, y, rotation, width, height, weight);
	}

	public HakoniwaBox(String name) {
		this(name, Phybots.getInstance().lookForService(Hakoniwa.class));
	}

	public HakoniwaBox(String name, Hakoniwa hakoniwa) {
		this(name, hakoniwa.getWidth()/2.0, hakoniwa.getHeight()/2.0, hakoniwa);
	}

	public HakoniwaBox(String name, Location location) {
		this(name, location.getX(), location.getY(), location.getRotation());
	}

	public HakoniwaBox(String name, Location location, Hakoniwa hakoniwa) {
		this(name, location.getX(), location.getY(), location.getRotation(), hakoniwa);
	}

	public HakoniwaBox(String name, Position position) {
		this(name, position.getX(), position.getY());
	}

	public HakoniwaBox(String name, Position position, Hakoniwa hakoniwa) {
		this(name, position.getX(), position.getY(), 0, hakoniwa);
	}

	public HakoniwaBox(String name, double x, double y) {
		this(name, x, y, 0);
	}

	public HakoniwaBox(String name, double x, double y, Hakoniwa hakoniwa) {
		this(name, x, y, 0, hakoniwa);
	}

	public HakoniwaBox(String name, double x, double y, double rotation) {
		this(name, x, y, rotation, Phybots.getInstance().lookForService(Hakoniwa.class));
	}

	public HakoniwaBox(String name, double x, double y, double rotation, Hakoniwa hakoniwa) {
		this(name, x, y, rotation, null, hakoniwa);
	}

	public HakoniwaBox(String name, double x, double y, double rotation, Color color) {
		this(name, x, y, rotation, color, null);
	}

	public HakoniwaBox(String name, double x, double y, double rotation, Color color, Hakoniwa hakoniwa) {
		this(name, x, y, rotation, WIDTH, HEIGHT, color, hakoniwa);
	}

	public HakoniwaBox(String name, double x, double y, double rotation, double width, double height) {
		this(name, x, y, rotation, width, height, null, null);
	}

	public HakoniwaBox(String name, double x, double y, double rotation, double width, double height, Hakoniwa hakoniwa) {
		this(name, x, y, rotation, width, height, null, hakoniwa);
	}

	public HakoniwaBox(String name, double x, double y, double rotation, double width, double height, Color color) {
		this(name, x, y, rotation, width, height, color, null);
	}

	public HakoniwaBox(String name, double x, double y, double rotation, double width, double height, Color color, Hakoniwa hakoniwa) {
		this(name, x, y, rotation, width, height, WEIGHT, color, hakoniwa);
	}

	public HakoniwaBox(String name, double x, double y, double rotation, double width, double height, double weight, Color color) {
		this(name, x, y, rotation, width, height, weight, color, null);
	}

	public HakoniwaBox(String name, double x, double y, double rotation, double width, double height, double weight, Color color, Hakoniwa hakoniwa) {
		super(name, hakoniwa);
		initialize(color, x, y, rotation, width, height, weight);
	}

	private void writeObject(ObjectOutputStream oos) throws IOException {
		oos.defaultWriteObject();

		Vec2 position = getBody().getPosition();
		float x = position.x * 100;
		float y = position.y * 100;
		float angle = getBody().getAngle();

		Rectangle2D bounds = shape.getBounds2D();
		float width = (float) bounds.getWidth();
		float height = (float) bounds.getHeight();
		float weight = getShapeDef().density * width * height;

		oos.writeObject(new float[] {
			x, y, angle, width, height, weight
		});
	}

	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
		ois.defaultReadObject();

		float[] parameters = (float[]) ois.readObject();
		float x = parameters[0];
		float y = parameters[1];
		float angle = parameters[2];
		float width = parameters[3];
		float height = parameters[4];
		float weight = parameters[5];

		initialize(null, x, y, angle, width, height, weight);
	}

	private void initialize(Color color, double x, double y, double rotation, double width, double height, double weight) {

		if (color != null) {
			setColor(color);
		}

		final PolygonDef pd = new PolygonDef();
		pd.setAsBox((float) width / 200, (float) height / 200);
		pd.restitution = RESTITUTION;
		pd.density = (float) (weight / (width * height));
		pd.friction = FRICTION;

		shape = new Rectangle2D.Double(-width/2, -height/2, width, height);
		initialize(x, y, rotation, pd);
	}

	public Shape getShape() {
		return shape;
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
}