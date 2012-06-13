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

import org.jbox2d.collision.shapes.ShapeDef;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.World;

import com.phybots.Phybots;
import com.phybots.entity.EntityImpl;
import com.phybots.utils.Location;
import com.phybots.utils.Position;


/**
 * Abstract implementation of HakoniwaEntity.<br />
 * HakoniwaEntity implementation classes must extend this abstract class.
 *
 * @author Jun Kato
 */
public abstract class HakoniwaEntityAbstractImpl extends EntityImpl implements HakoniwaEntity {
	private static final long serialVersionUID = 8048508309803789117L;

	/** Hakoniwa */
	private Hakoniwa hakoniwa;

	/** Shape definition for Box2D */
	private ShapeDef sd;

	private Body body;

	public HakoniwaEntityAbstractImpl() {
		initialize(null);
	}

	public HakoniwaEntityAbstractImpl(Hakoniwa hakoniwa) {
		initialize(hakoniwa);
	}

	public HakoniwaEntityAbstractImpl(String name) {
		super(name);
		initialize(null);
	}

	public HakoniwaEntityAbstractImpl(String name, Hakoniwa hakoniwa) {
		super(name);
		initialize(hakoniwa);
	}

	private void initialize(Hakoniwa hakoniwa) {
		this.hakoniwa = hakoniwa;
	}

	/**
	 * Subclasses must call this method at the end of its initialization.
	 *
	 * @param x x-coordinate
	 * @param y y-coordinate
	 * @param rotation rotation
	 * @param sd Shape definition
	 */
	protected void initialize(double x, double y, double rotation, ShapeDef sd) {

		if (hakoniwa == null) {
			hakoniwa = Phybots.getInstance().lookForService(Hakoniwa.class);
		}

		final BodyDef bd = new BodyDef();
		bd.position = new Vec2();
		bd.position.x = (float) (x/100);
		bd.position.y = (float) (y/100);
		bd.angle = (float) rotation;

		this.sd = sd;

		synchronized (hakoniwa) {
			final World world = hakoniwa.getWorld();
			body = world.createBody(bd);
			body.createShape(sd);
			body.setMassFromShapes();
			body.m_userData = this;
		}

		hakoniwa.registerEntity(this);
	}

	@Override
	public void dispose() {
		super.dispose();
		hakoniwa.unregisterEntity(this);
		hakoniwa.getWorld().destroyBody(getBody());
	}

	public void setHakoniwa(Hakoniwa hakoniwa) {
		Location location = getLocation();
		this.hakoniwa.unregisterEntity(this);
		this.hakoniwa.getWorld().destroyBody(getBody());
		this.hakoniwa = hakoniwa;
		initialize(location.getX(), location.getY(), location.getRotation(), sd);
	}

	public Hakoniwa getHakoniwa() {
		return hakoniwa;
	}

	public Body getBody() {
		return body;
	}

	protected ShapeDef getShapeDef() {
		return sd;
	}

	public Location getLocation() {
		Location location = new Location();
		getLocationOut(location);
		return location;
	}

	public void getLocationOut(Location location) {
		final Vec2 position = getBody().getPosition();
		location.setLocation(
				(double) (position.x*100),
				(double) (position.y*100),
				(double) getBody().getAngle());
	}

	public Position getPosition() {
		Position position = new Position();
		getPositionOut(position);
		return position;
	}

	public void getPositionOut(Position position) {
		final Vec2 vec2 = getBody().getPosition();
		position.set(
				(double) (vec2.x*100),
				(double) (vec2.y*100));
	}

	public double getX() {
		return getBody().getPosition().x;
	}

	public double getY() {
		return getBody().getPosition().y;
	}

	public double getRotation() {
		return getBody().getAngle();
	}

	public void setPosition(Position position) {
		setPosition(position.getX(), position.getY());
	}

	public void setPosition(double x, double y) {
		setLocation(x, y, getRotation());
	}

	public void setLocation(Location location) {
		setLocation(location.getX(), location.getY(), location.getRotation());
	}

	public void setLocation(double x, double y, double rotation) {
		Vec2 position = new Vec2((float) (x/100), (float) (y/100));
		getBody().setXForm(position, (float) rotation);
	}

	public void setX(double x) {
		setPosition(x, getY());
	}

	public void setY(double y) {
		setPosition(getX(), y);
	}

	public void setRotation(double rotation) {
		setLocation(getX(), getY(), rotation);
	}
}
