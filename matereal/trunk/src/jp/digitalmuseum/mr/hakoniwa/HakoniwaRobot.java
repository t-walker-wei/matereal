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
import java.util.List;

import org.jbox2d.collision.shapes.CircleDef;
import org.jbox2d.collision.shapes.ShapeDef;
import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.World;

import jp.digitalmuseum.mr.Matereal;
import jp.digitalmuseum.mr.entity.ResourceAbstractImpl;
import jp.digitalmuseum.mr.entity.RobotAbstractImpl;
import jp.digitalmuseum.mr.resource.DifferentialWheelsController;
import jp.digitalmuseum.mr.resource.Wheels.STATUS;
import jp.digitalmuseum.mr.task.Task;
import jp.digitalmuseum.utils.Location;
import jp.digitalmuseum.utils.Position;

/**
 * HakoniwaRobot, a robot on the Hakoniwa simulator.
 *
 * @author Jun KATO
 * @see HakoniwaRobotWheels
 */
public class HakoniwaRobot extends RobotAbstractImpl implements HakoniwaEntity {

	/** Hakoniwa */
	private Hakoniwa hakoniwa;

	/** Radius of the robot in [m] */
	final private static float RADIUS = 0.2f;
	final private static float FRICTION = 0.3f;
	final private static float RESTITUTION = 0.1f;
	final private static float WEIGHT = 0.8f;
	final private static float DEFAULT_FORCE = 1f;
	final private static float DEFAULT_TORQUE = 0.1f;

	protected HakoniwaRobotWheels wheels;

	private Shape shape;
	private Body body;
	private ShapeDef sd;
	private Color color;

	public HakoniwaRobot(String name) {
		this(name, Matereal.getInstance().lookForService(Hakoniwa.class));
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
		this(name, x, y, rotation, Matereal.getInstance().lookForService(Hakoniwa.class));
	}

	public HakoniwaRobot(String name, double x, double y, double rotation, Hakoniwa hakoniwa) {
		this.hakoniwa = hakoniwa;
		setName(name);
		initialize(x, y, rotation);
		hakoniwa.registerEntity(this);
	}

	@Override
	public void dispose() {
		super.dispose();
		hakoniwa.unregisterEntity(this);
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
	}

	@Override
	protected List<ResourceAbstractImpl> getResources() {
		List<ResourceAbstractImpl> rs = super.getResources();
		rs.add(wheels);
		return rs;
	}

	private final Vec2 force = new Vec2();
	public void preStep() {
		final float angle = body.getAngle();
		final float ex = MathUtils.cos(angle);
		final float ey = MathUtils.sin(angle);
		if (wheels.status == STATUS.CURVE_LEFT ||
				wheels.status == STATUS.CURVE_RIGHT) {
			final float f = DEFAULT_FORCE * wheels.rotationSpeed * (100+wheels.innerSpeed) / 20000;
			force.x = f * ex;
			force.y = f * ey;
			body.applyForce(force, body.getPosition());

			final float torque = (wheels.status == STATUS.CURVE_LEFT ?
							DEFAULT_TORQUE : -DEFAULT_TORQUE) *
					wheels.rotationSpeed * (100-wheels.innerSpeed) / 20000;
			body.applyTorque(torque);
		}
		else if (wheels.status == STATUS.GO_FORWARD ||
				wheels.status == STATUS.GO_BACKWARD) {
			// Acceleration in [m/s^2] and force in [N].
			/*
				final float a = 1f;
				final float f = body.getMass() * (status.equals(STATUS.GO_FORWARD) ? a : -a);
			 */
			final float f = (wheels.status == STATUS.GO_FORWARD ?
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
		else if (wheels.status == STATUS.SPIN_LEFT ||
				wheels.status == STATUS.SPIN_RIGHT) {

			// Apply motor power. [Nm]
			final float torque = (wheels.status == STATUS.SPIN_LEFT ?
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

	/**
	 * Wheels of HakoniwaRobot.
	 *
	 * @author Jun KATO
	 * @see HakoniwaRobot
	 */
	protected static class HakoniwaRobotWheels extends ResourceAbstractImpl implements DifferentialWheelsController {
		private final static int DEFAULT_SPEED = 50;
		private final static int DEFAULT_ROTATION_SPEED = 50;
		protected Task task;
		private STATUS status;
		private int speed;
		private int rotationSpeed;
		private int innerSpeed;

		private HakoniwaRobotWheels(HakoniwaRobot hakoniwaRobot) {
			super(hakoniwaRobot);
			speed = DEFAULT_SPEED;
			rotationSpeed = DEFAULT_ROTATION_SPEED;
			status = STATUS.STOP;
		}

		@Override
		protected void onFree() {
			stopWheels();
		}

		public STATUS getStatus() {
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
			status = STATUS.GO_BACKWARD;
		}

		public void goForward() {
			status = STATUS.GO_FORWARD;
		}

		public void spin(SPIN direction) {
			if (direction == SPIN.LEFT) {
				spinLeft();
			} else {
				spinRight();
			}
		}

		public void spinLeft() {
			status = STATUS.SPIN_LEFT;
			innerSpeed = -100;
		}

		public void spinRight() {
			status = STATUS.SPIN_RIGHT;
			innerSpeed = -100;
		}

		public void curve(SPIN direction, int innerSpeed) {
			if (direction == SPIN.LEFT) {
				curveLeft(innerSpeed);
			} else {
				curveRight(innerSpeed);
			}
		}

		public void curveLeft(int innerSpeed) {
			status = STATUS.CURVE_LEFT;
			this.innerSpeed = innerSpeed;
		}

		public void curveRight(int innerSpeed) {
			status = STATUS.CURVE_RIGHT;
			this.innerSpeed = innerSpeed;
		}

		public void stopWheels() {
			status = STATUS.STOP;
		}

		public boolean drive(int leftPower, int rightPower) {
			if (leftPower < -100 || rightPower < -100 ||
					leftPower > 100 || rightPower > 100) {
				return false;
			}
			if (leftPower == rightPower) {
				if (leftPower == 0) {
					status = STATUS.STOP;
				} else if (leftPower > 0) {
					speed = leftPower;
					status = STATUS.GO_FORWARD;
				} else {
					speed = rightPower;
					status = STATUS.GO_BACKWARD;
				}
			} else if (leftPower == -rightPower) {
				if (leftPower > 0) {
					rotationSpeed = leftPower;
					status = STATUS.SPIN_RIGHT;
				} else {
					rotationSpeed = rightPower;
					status = STATUS.SPIN_LEFT;
				}
			} else {
				if (leftPower > rightPower) {
					rotationSpeed = leftPower;
					innerSpeed = 100*rightPower/leftPower;
					status = STATUS.CURVE_RIGHT;
				} else {
					rotationSpeed = rightPower;
					innerSpeed = 100*leftPower/rightPower;
					status = STATUS.CURVE_LEFT;
				}
			}
			return true;
		}

	}

}
