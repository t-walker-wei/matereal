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
import java.util.List;

import org.jbox2d.collision.shapes.CircleDef;
import org.jbox2d.collision.shapes.ShapeDef;
import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.World;

import com.phybots.Phybots;
import com.phybots.entity.ResourceAbstractImpl;
import com.phybots.entity.RobotAbstractImpl;
import com.phybots.resource.DifferentialWheelsController;
import com.phybots.resource.Wheels.WheelsStatus;
import com.phybots.utils.Location;
import com.phybots.utils.Position;


/**
 * HakoniwaRobot, a robot on the Hakoniwa simulator.
 *
 * @author Jun Kato
 * @see HakoniwaRobotWheels
 */
public class HakoniwaRobot extends RobotAbstractImpl implements HakoniwaEntity {
	private static final long serialVersionUID = 2997179665544048896L;

	/** Hakoniwa */
	private Hakoniwa hakoniwa;

	/** Radius of the robot in [m] */
	final private static float RADIUS = 0.2f;
	final private static float FRICTION = 0.3f;
	final private static float RESTITUTION = 0.1f;
	final private static float WEIGHT = 0.8f;
	final private static float DEFAULT_FORCE = 1f;
	final private static float DEFAULT_TORQUE = 0.1f;

	protected transient HakoniwaRobotWheels wheels;

	private transient Shape shape;
	private transient Body body;
	private transient ShapeDef sd;
	private transient Vec2 force;

	private Color color;

	public HakoniwaRobot() {
		this(Phybots.getInstance().lookForService(Hakoniwa.class));
	}

	public HakoniwaRobot(Hakoniwa hakoniwa) {
		this(hakoniwa.getWidth()/2.0, hakoniwa.getHeight()/2.0);
	}

	public HakoniwaRobot(Location location) {
		this(location.getX(), location.getY(), location.getRotation());
	}

	public HakoniwaRobot(Position position) {
		this(position.getX(), position.getY());
	}

	public HakoniwaRobot(Location location, Hakoniwa hakoniwa) {
		this(location.getX(), location.getY(), location.getRotation(), hakoniwa);
	}

	public HakoniwaRobot(Position position, Hakoniwa hakoniwa) {
		this(position.getX(), position.getY(), 0, hakoniwa);
	}

	public HakoniwaRobot(double x, double y) {
		this(x, y, 0);
	}

	public HakoniwaRobot(double x, double y, Hakoniwa hakoniwa) {
		this(x, y, 0, hakoniwa);
	}

	public HakoniwaRobot(double x, double y, double rotation) {
		this(x, y, rotation, Phybots.getInstance().lookForService(Hakoniwa.class));
	}

	public HakoniwaRobot(double x, double y, double rotation, Hakoniwa hakoniwa) {
		super();
		if (hakoniwa == null) {
			hakoniwa = Phybots.getInstance().lookForService(Hakoniwa.class);
		}
		this.hakoniwa = hakoniwa;
		initialize(x, y, rotation);
	}

	public HakoniwaRobot(String name) {
		this(name, Phybots.getInstance().lookForService(Hakoniwa.class));
	}

	public HakoniwaRobot(String name, Hakoniwa hakoniwa) {
		this(name, hakoniwa.getWidth()/2.0, hakoniwa.getHeight()/2.0);
	}

	public HakoniwaRobot(String name, Location location) {
		this(name, location.getX(), location.getY(), location.getRotation());
	}

	public HakoniwaRobot(String name, Position position) {
		this(name, position.getX(), position.getY());
	}

	public HakoniwaRobot(String name, Location location, Hakoniwa hakoniwa) {
		this(name, location.getX(), location.getY(), location.getRotation(), hakoniwa);
	}

	public HakoniwaRobot(String name, Position position, Hakoniwa hakoniwa) {
		this(name, position.getX(), position.getY(), 0, hakoniwa);
	}

	public HakoniwaRobot(String name, double x, double y) {
		this(name, x, y, 0);
	}

	public HakoniwaRobot(String name, double x, double y, Hakoniwa hakoniwa) {
		this(name, x, y, 0, hakoniwa);
	}

	public HakoniwaRobot(String name, double x, double y, double rotation) {
		this(name, x, y, rotation, null);
	}

	public HakoniwaRobot(String name, double x, double y, double rotation, Hakoniwa hakoniwa) {
		super(name);
		if (hakoniwa == null) {
			hakoniwa = Phybots.getInstance().lookForService(Hakoniwa.class);
		}
		this.hakoniwa = hakoniwa;
		initialize(x, y, rotation);
	}

	private void writeObject(ObjectOutputStream oos) throws IOException {
		oos.defaultWriteObject();

		Vec2 position = getBody().getPosition();
		float x = position.x * 100;
		float y = position.y * 100;
		float angle = getBody().getAngle();

		oos.writeObject(new float[] {
			x, y, angle
		});
	}

	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
		ois.defaultReadObject();

		float[] parameters = (float[]) ois.readObject();
		float x = parameters[0];
		float y = parameters[1];
		float angle = parameters[2];

		initialize(x, y, angle);
	}

	@Override
	public void dispose() {
		super.dispose();
		hakoniwa.unregisterEntity(this);
		hakoniwa.getWorld().destroyBody(getBody());
	}

	private void initialize(double x, double y, double rotation) {
		shape = new Ellipse2D.Float(
				-RADIUS*100, -RADIUS*100,
				RADIUS*200, RADIUS*200);

		sd = new CircleDef();
		((CircleDef) sd).radius = RADIUS;
		sd.restitution = RESTITUTION;
		sd.density = WEIGHT / (RADIUS * RADIUS * MathUtils.PI);
		sd.friction = FRICTION;

		final BodyDef bd = new BodyDef();
		bd.position = new Vec2();
		bd.position.x = (float) x/100;
		bd.position.y = (float) y/100;
		bd.angle = (float) rotation;

		synchronized (hakoniwa) {
			final World world = hakoniwa.getWorld();
			body = world.createBody(bd);
			body.createShape(sd);
			body.setMassFromShapes();
			body.m_userData = this;
		}

		wheels = new HakoniwaRobotWheels(this);
		force = new Vec2();

		hakoniwa.registerEntity(this);
	}

	@Override
	protected List<ResourceAbstractImpl> getResources() {
		List<ResourceAbstractImpl> rs = super.getResources();
		rs.add(wheels);
		return rs;
	}

	public void preStep() {
		final float angle = body.getAngle();
		final float ex = MathUtils.cos(angle);
		final float ey = MathUtils.sin(angle);
		if (wheels.status == WheelsStatus.CURVE_LEFT ||
				wheels.status == WheelsStatus.CURVE_RIGHT) {
			final float f = DEFAULT_FORCE * wheels.rotationSpeed * (100+wheels.innerSpeed) / 20000;
			force.x = f * ex;
			force.y = f * ey;
			body.applyForce(force, body.getPosition());

			final float torque = (wheels.status == WheelsStatus.CURVE_LEFT ?
							DEFAULT_TORQUE : -DEFAULT_TORQUE) *
					wheels.rotationSpeed * (100-wheels.innerSpeed) / 20000;
			body.applyTorque(torque);
		}
		else if (wheels.status == WheelsStatus.GO_FORWARD ||
				wheels.status == WheelsStatus.GO_BACKWARD) {
			// Acceleration in [m/s^2] and force in [N].
			/*
				final float a = 1f;
				final float f = body.getMass() * (status.equals(STATUS.GO_FORWARD) ? a : -a);
			 */
			final float f = (wheels.status == WheelsStatus.GO_FORWARD ?
							DEFAULT_FORCE : -DEFAULT_FORCE) *
					wheels.speed / 100;

			// Apply motor power. [N]
			force.x = f * ex;
			force.y = f * ey;
			body.applyForce(force, body.getPosition());

			// Apply frictional force.
			// This can also be simulated by setting linear damping like body.setLinearDamping(0.01f);
			/*
				final Vec2 v = body.getLinearVelocity();
				final float velocity = v.length();
				final float k = 0.5f;
				final float N = k * body.getMass() * 9.8f;
				final float tmp = N / velocity;
				force.x -= tmp * v.x;
				force.y -= tmp * v.y;
			 */
		}
		else if (wheels.status == WheelsStatus.SPIN_LEFT ||
				wheels.status == WheelsStatus.SPIN_RIGHT) {

			// Apply motor power. [Nm]
			final float torque = (wheels.status == WheelsStatus.SPIN_LEFT ?
							DEFAULT_TORQUE : -DEFAULT_TORQUE) *
					wheels.rotationSpeed * (100-wheels.innerSpeed) / 20000;
			body.applyTorque(torque);
		}
		/*
		else {
			Vec2 v = body.getLinearVelocity();
			v.x = v.x*0.6f;
			v.y = v.y*0.6f;
			body.setAngularVelocity(body.getAngularVelocity()*0.6f);
		}
		*/
	}

	public void setHakoniwa(Hakoniwa hakoniwa) {
		this.hakoniwa.unregisterEntity(this);
		this.hakoniwa.getWorld().destroyBody(getBody());

		// TODO write re-initialization code.

		hakoniwa.registerEntity(this);
		this.hakoniwa = hakoniwa;
	}

	public Hakoniwa getHakoniwa() {
		return hakoniwa;
	}

	public float getRadius() {
		return RADIUS*100;
	}

	public Shape getShape() {
		return shape;
	}

	public Color getColor() {
		return color == null ? Color.orange : color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public Body getBody() {
		return body;
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

	/**
	 * Wheels of HakoniwaRobot.
	 *
	 * @author Jun Kato
	 * @see HakoniwaRobot
	 */
	protected static class HakoniwaRobotWheels extends ResourceAbstractImpl implements DifferentialWheelsController {
		private static final long serialVersionUID = 4609377279740403081L;
		private final static int DEFAULT_SPEED = 50;
		private final static int DEFAULT_ROTATION_SPEED = 50;
		private WheelsStatus status;
		private int speed;
		private int rotationSpeed;
		private int innerSpeed;

		private HakoniwaRobotWheels(HakoniwaRobot hakoniwaRobot) {
			super(hakoniwaRobot);
			initialize();
		}

		private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
			initialize();
			ois.defaultReadObject();
		}

		private void initialize() {
			speed = DEFAULT_SPEED;
			rotationSpeed = DEFAULT_ROTATION_SPEED;
			status = WheelsStatus.STOP;
		}

		@Override
		protected void onFree() {
			stopWheels();
		}

		public WheelsStatus getStatus() {
			return status;
		}

		public int getRecommendedSpeed() {
			return DEFAULT_SPEED;
		}

		public int getRecommendedRotationSpeed() {
			return DEFAULT_ROTATION_SPEED;
		}

		public int getSpeed() {
			return speed;
		}

		public boolean setSpeed(int speed) {
			if (speed >= 0 && speed <= 100) {
				this.speed = speed;
				return true;
			}
			return false;
		}

		public int getRotationSpeed() {
			return rotationSpeed;
		}

		public boolean setRotationSpeed(int rotationSpeed) {
			if (rotationSpeed >= 0 && speed <= 100) {
				this.rotationSpeed = rotationSpeed;
				return true;
			}
			return false;
		}

		public int getLeftWheelPower() {
			switch (status) {
			case GO_FORWARD:
				return speed;
			case GO_BACKWARD:
				return -speed;
			case SPIN_RIGHT:
			case CURVE_RIGHT:
				return rotationSpeed;
			case SPIN_LEFT:
				return - rotationSpeed;
			case CURVE_LEFT:
				return rotationSpeed*innerSpeed/100;
			default:
				return 0;
			}
		}

		public int getRightWheelPower() {
			switch (status) {
			case GO_FORWARD:
				return speed;
			case GO_BACKWARD:
				return -speed;
			case SPIN_RIGHT:
				return - rotationSpeed;
			case CURVE_RIGHT:
				return rotationSpeed*innerSpeed/100;
			case SPIN_LEFT:
			case CURVE_LEFT:
				return rotationSpeed;
			default:
				return 0;
			}
		}

		public void goBackward() {
			status = WheelsStatus.GO_BACKWARD;
		}

		public void goForward() {
			status = WheelsStatus.GO_FORWARD;
		}

		public void spin(Spin direction) {
			if (direction == Spin.LEFT) {
				spinLeft();
			} else {
				spinRight();
			}
		}

		public void spinLeft() {
			status = WheelsStatus.SPIN_LEFT;
			innerSpeed = -100;
		}

		public void spinRight() {
			status = WheelsStatus.SPIN_RIGHT;
			innerSpeed = -100;
		}

		public void curve(Spin direction, int innerSpeed) {
			if (direction == Spin.LEFT) {
				curveLeft(innerSpeed);
			} else {
				curveRight(innerSpeed);
			}
		}

		public void curveLeft(int innerSpeed) {
			status = WheelsStatus.CURVE_LEFT;
			this.innerSpeed = innerSpeed;
		}

		public void curveRight(int innerSpeed) {
			status = WheelsStatus.CURVE_RIGHT;
			this.innerSpeed = innerSpeed;
		}

		public void stopWheels() {
			status = WheelsStatus.STOP;
		}

		public boolean drive(int leftPower, int rightPower) {
			if (leftPower < -100 || rightPower < -100 ||
					leftPower > 100 || rightPower > 100) {
				return false;
			}
			if (leftPower == rightPower) {
				if (leftPower == 0) {
					status = WheelsStatus.STOP;
				} else if (leftPower > 0) {
					speed = leftPower;
					status = WheelsStatus.GO_FORWARD;
				} else {
					speed = rightPower;
					status = WheelsStatus.GO_BACKWARD;
				}
			} else if (leftPower == -rightPower) {
				if (leftPower > 0) {
					rotationSpeed = leftPower;
					status = WheelsStatus.SPIN_RIGHT;
				} else {
					rotationSpeed = rightPower;
					status = WheelsStatus.SPIN_LEFT;
				}
			} else {
				if (leftPower > rightPower) {
					rotationSpeed = leftPower;
					innerSpeed = 100*rightPower/leftPower;
					status = WheelsStatus.CURVE_RIGHT;
				} else {
					rotationSpeed = rightPower;
					innerSpeed = 100*leftPower/rightPower;
					status = WheelsStatus.CURVE_LEFT;
				}
			}
			return true;
		}
	}

}
