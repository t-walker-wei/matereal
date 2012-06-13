package robot.mindstorms;
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


import com.phybots.Phybots;
import com.phybots.entity.MindstormsNXT;
import com.phybots.entity.MindstormsNXT.MindstormsNXTExtension;
import com.phybots.entity.MindstormsNXT.Port;


public class ManageMotorState {
	private static final int ROTATION_THRESHOLD = 2;
	private static final int TIME_THRESHOLD = 10;

	public static void main(String[] args) {
		new ManageMotorState();
	}

	public ManageMotorState() {

		MindstormsNXT nxt = new MindstormsNXT("btspp://00165305B308");
		nxt.removeDifferentialWheels();
		nxt.addExtension("MindstormsNXTExtension", Port.B);
		nxt.connect();

		MindstormsNXTExtension ext = nxt.requestResource(MindstormsNXTExtension.class, this);
		if (ext != null) {

			int rotationCount = ext.getOutputState().rotationCount;
			int stableCount = 0;

			makeStable(ext, true);
			boolean isStable = true;

			while (true) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					break;
				}

				int count = ext.getOutputState().rotationCount;
				if (diff(rotationCount, count) > ROTATION_THRESHOLD) {
					stableCount = 0;
				} else {
					stableCount ++;
				}

				if (isStable) {
					if (stableCount == 0) {
						makeStable(ext, false);
						isStable = false;
					}
				} else {
					if (stableCount > TIME_THRESHOLD) {
						makeStable(ext, true);
						isStable = true;
					}
				}

				rotationCount = count;
				System.out.println(rotationCount + " : " + stableCount);
			}
		}
		Phybots.getInstance().dispose();
	}

	private int diff(int a, int b) {
		int diff = a - b;
		return diff < 0 ? -diff : diff;
	}

	private boolean makeStable(MindstormsNXTExtension ext, boolean isStable) {
		System.out.println("Make stable: " + isStable);
		return ext.setOutputState(
				(byte) 0,
				isStable ? (MindstormsNXT.MOTORON | MindstormsNXT.BRAKE | MindstormsNXT.REGULATED) : 0,
				isStable ? MindstormsNXT.REGULATION_MODE_MOTOR_SPEED : 0,
				0,
				isStable ? MindstormsNXT.MOTOR_RUN_STATE_RUNNING : 0,
				0);
	}
}
