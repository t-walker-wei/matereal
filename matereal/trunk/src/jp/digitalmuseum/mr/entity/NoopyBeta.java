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
package jp.digitalmuseum.mr.entity;

import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


import jp.digitalmuseum.connector.Connector;
import jp.digitalmuseum.mr.entity.PhysicalResourceAbstractImpl;
import jp.digitalmuseum.mr.entity.PhysicalRobotAbstractImpl;
import jp.digitalmuseum.mr.entity.ResourceAbstractImpl;
import jp.digitalmuseum.mr.resource.Pen;
import jp.digitalmuseum.mr.resource.WheelsController;

/**
 * Noopy, a prototype robot.
 *
 * @author Jun KATO
 * @see NoopyWheels
 */
public class NoopyBeta extends PhysicalRobotAbstractImpl {
	final public static double WIDTH = 12;
	final public static double HEIGHT = 16;
	private NoopyWheels wheels;
	private Set<PhysicalResourceAbstractImpl> plugins;
	private Shape shape;
	private static int instances = 0;

	public NoopyBeta(String connectionString) {
		super(connectionString);
		initialize(null);
	}

	public NoopyBeta(String connectionString, Class<Resource>... plugins) {
		super(connectionString);
		initialize(null, plugins);
	}

	public NoopyBeta(String name, String connectionString) {
		super(connectionString);
		initialize(name);
	}

	public NoopyBeta(String name, String connectionString, Class<Resource>... plugins) {
		super(connectionString);
		initialize(name, plugins);
	}

	public NoopyBeta(String name, Connector connector) {
		super(connector);
		initialize(name);
	}

	public NoopyBeta(String name, Connector connector, Class<Resource>... plugins) {
		super(connector);
		initialize(name, plugins);
	}

	@Override
	public void dispose() {
		wheels.stopWheels();
		super.dispose();
	}

	private void initialize(String name) {
		initialize(name, null);
	}

	private void initialize(String name,
			Class<Resource>[] pluginClasses) {
		setTypeName("Noopy");
		instances ++;
		if (name == null) {
			setName(getTypeName()+" ("+instances+")");
		} else {
			setName(name);
		}
		wheels = new NoopyWheels(this);
		if (pluginClasses != null) {
			plugins = new HashSet<PhysicalResourceAbstractImpl>();
			for (Class<Resource> pluginClass : pluginClasses) {
				PhysicalResourceAbstractImpl plugin;
				try {
					plugin = (PhysicalResourceAbstractImpl) pluginClass
							.getConstructor(NoopyBeta.class).newInstance(this);
				} catch (Exception e) {
					throw new RuntimeException("Plugin could't be plugged in.", e);
				}
				plugins.add(plugin);
			}
		}
		shape = new RoundRectangle2D.Double(
				-WIDTH/2 , -HEIGHT/2,
				WIDTH/2, HEIGHT/2,
				3, 3);
	}

	@Override
	protected List<ResourceAbstractImpl> getResources() {
		List<ResourceAbstractImpl> rs = super.getResources();
		rs.add(wheels);
		if (plugins != null) {
			for (PhysicalResourceAbstractImpl plugin : plugins) {
				rs.add(plugin);
			}
		}
		return rs;
	}

	public Shape getShape() {
		return shape;
	}

	/**
	 * Wheels of Noopy.
	 *
	 * @author Jun KATO
	 * @see NoopyBeta
	 */
	public static class NoopyWheels extends PhysicalResourceAbstractImpl implements WheelsController {
		private STATUS status;

		public NoopyWheels(NoopyBeta noopy) {
			super(noopy);
			initialize();
		}

		public NoopyWheels(Connector connector) {
			super(connector);
			initialize();
		}

		private void initialize() {
			status = STATUS.STOP;
		}

		@Override
		protected void onFree() {
			stopWheels();
		}

		public void goBackward() {
			if (status != STATUS.GO_BACKWARD) {
				getConnector().write("i");
				status = STATUS.GO_BACKWARD;
			}
		}

		public void goForward() {
			if (status != STATUS.GO_FORWARD) {
				getConnector().write("d");
				status = STATUS.GO_FORWARD;
			}
		}

		public void spin(SPIN direction) {
			if (direction.equals(SPIN.LEFT)) {
				if (status != STATUS.SPIN_LEFT) {
					getConnector().write("p");
					status = STATUS.SPIN_LEFT;
				}
			} else {
				if (status != STATUS.SPIN_RIGHT) {
					getConnector().write("w");
					status = STATUS.SPIN_RIGHT;
				}
			}
		}

		public void spinLeft() {
			if (status != STATUS.SPIN_LEFT) {
				getConnector().write("p");
				status = STATUS.SPIN_LEFT;
			}
		}

		public void spinRight() {
			if (status != STATUS.SPIN_RIGHT) {
				getConnector().write("w");
				status = STATUS.SPIN_RIGHT;
			}
		}

		public void stopWheels() {
			if (status != STATUS.STOP) {
				getConnector().write("y");
				status = STATUS.STOP;
			}
		}

		public STATUS getStatus() {
			return status;
		}
	}

	/**
	 * Pen of Noopy.
	 *
	 * @author Jun KATO
	 * @see NoopyBeta
	 */
	public static class NoopyPen extends PhysicalResourceAbstractImpl implements Pen {
		private STATUS penStatus;

		public NoopyPen(NoopyBeta noopy) {
			super(noopy);
			initialize();
		}

		public NoopyPen(Connector connector) {
			super(connector);
			initialize();
		}

		private void initialize() {
			penStatus = STATUS.UP;
		}

		@Override
		protected void onFree() {
			// endPen();
		}

		public void putPen() {
			if (penStatus == STATUS.UP) {
				getConnector().write("u");
				penStatus = Pen.STATUS.DOWN;
			}
		}

		public void endPen() {
			if (penStatus == STATUS.DOWN) {
				getConnector().write("v");
				penStatus = Pen.STATUS.UP;
			}
		}

		public STATUS getStatus() {
			return penStatus;
		}
	}
}
