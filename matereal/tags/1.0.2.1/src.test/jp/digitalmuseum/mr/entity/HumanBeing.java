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
import java.util.List;

import jp.digitalmuseum.mr.entity.ResourceAbstractImpl;
import jp.digitalmuseum.mr.entity.RobotAbstractImpl;
import jp.digitalmuseum.mr.resource.WheelsController;

/**
 * Human being.
 * TODO Implement head-mounted camera, broom, etc.
 *
 * @author Jun KATO
 */
public class HumanBeing extends RobotAbstractImpl {
	private HumanLegs humanLegs;

	public HumanBeing() {
		humanLegs = new HumanLegs(this);
	}

	@Override
	protected List<ResourceAbstractImpl> getResources() {
		List<ResourceAbstractImpl> rs = super.getResources();
		rs.add(humanLegs);
		return rs;
	}

	public WheelsController.STATUS getStatusOfLegs() {
		return humanLegs.getStatus();
	}

	public Shape getShape() {
		return null;
	}

	public static class HumanLegs extends ResourceAbstractImpl implements WheelsController {
		STATUS status;

		protected HumanLegs(HumanBeing robot) {
			super(robot);
			status = STATUS.STOP;
		}

		protected void dispose() {
			stopWheels();
		}

		public STATUS getStatus() {
			return status;
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
		}

		public void spinRight() {
			status = STATUS.SPIN_RIGHT;
		}

		public void stopWheels() {
			status = STATUS.STOP;
		}
	}

	/*
	public static class HumanEyes extends ResourceAbstractImpl implements HeadmountedCamera {

		protected HumanEyes(HumanBeing robot) {
			super(robot);
		}

		public void startCapture() {
		}

		public void endCapture() {
		}

		public BufferedImage getImage() {
			return null;
		}

		protected void dispose() {
			endCapture();
		}
	}

	public static class HumanBroom extends ResourceAbstractImpl implements CleanerBrush {

		protected HumanBroom(HumanBeing robot) {
			super(robot);
		}

		public void startCleaning() {
		}

		public void endCleaning() {
		}

		protected void dispose() {
		}
	}
	*/
}
