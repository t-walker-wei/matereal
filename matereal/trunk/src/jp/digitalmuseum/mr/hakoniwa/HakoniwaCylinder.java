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
package jp.digitalmuseum.mr.hakoniwa;

import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.jbox2d.collision.shapes.CircleDef;
import org.jbox2d.collision.shapes.ShapeDef;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.World;

public class HakoniwaCylinder extends HakoniwaEntityAbstractImpl {
	private static final long serialVersionUID = 7085381442084030550L;
	final private static float FRICTION = 0.3f;
	final private static float RESTITUTION = 0.1f;
	final private static float WEIGHT = 0.8f;
	final private static float RADIUS = 35f;

	private transient Body body;
	private transient ShapeDef sd;
	private transient java.awt.Shape shape;

	private Color color = Color.blue;

	public HakoniwaCylinder(String name, double x, double y, double angle) {
		setName(name);
		initialize((float) x, (float) y, (float) angle, RADIUS, WEIGHT);
	}

	public HakoniwaCylinder(String name, Color color, double x, double y, double angle) {
		setName(name);
		setColor(color);
		initialize((float) x, (float) y, (float) angle, RADIUS, WEIGHT);
	}

	public HakoniwaCylinder(String name, double x, double y, double radius, double angle) {
		setName(name);
		initialize((float) x, (float) y, (float) angle, (float) radius, WEIGHT);
	}

	public HakoniwaCylinder(String name, Color color, double x, double y, double radius, double angle) {
		setName(name);
		setColor(color);
		initialize((float) x, (float) y, (float) angle, (float) radius, WEIGHT);
	}

	private void writeObject(ObjectOutputStream oos) throws IOException {
		oos.defaultWriteObject();

		Vec2 position = getBody().getPosition();
		float x = position.x * 100;
		float y = position.y * 100;
		float angle = getBody().getAngle();

		float radius = ((CircleDef) sd).radius * 100;
		float weight = (float) (sd.density * radius * radius * Math.PI);

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

		initialize(x, y, angle, radius, weight);
	}

	private void initialize(float x, float y, float angle, float radius, float weight) {
		final CircleDef cd = new CircleDef();
		cd.radius = radius / 100;
		cd.restitution = RESTITUTION;
		cd.density = (float) (weight / (radius * radius * Math.PI));
		cd.friction = FRICTION;
		sd = cd;

		final BodyDef bd = new BodyDef();
		bd.position = new Vec2();
		bd.position.x = x/100;
		bd.position.y = y/100;
		bd.angle = angle;

		final Hakoniwa hakoniwa = getHakoniwa();
		synchronized (hakoniwa) {
			final World world = getHakoniwa().getWorld();
			body = world.createBody(bd);
			body.createShape(sd);
			body.setMassFromShapes();
			body.m_userData = this;
		}

		shape = new Ellipse2D.Double(-radius, -radius, radius*2, radius*2);
		getHakoniwa().registerEntity(this);
	}

	public Body getBody() {
		return body;
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
