package robot.mindstorms;
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

import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import jp.digitalmuseum.mr.Matereal;
import jp.digitalmuseum.mr.entity.MindstormsNXT;
import jp.digitalmuseum.mr.entity.MindstormsNXT.Port;
import jp.digitalmuseum.mr.message.Event;
import jp.digitalmuseum.mr.message.EventListener;
import jp.digitalmuseum.mr.message.RobotUpdateEvent;
import jp.digitalmuseum.mr.task.ManageNXTMotorState;

public class ManageMotorStates {

	public static void main(String[] args) {
		new ManageMotorStates();
	}

	public ManageMotorStates() {

		MindstormsNXT nxt = new MindstormsNXT("btspp://00165305B308");
		nxt.removeDifferentialWheels();
		nxt.addExtension("MindstormsNXTExtension", Port.B);
		nxt.addExtension("MindstormsNXTExtension", Port.C);
		nxt.connect();

		final ManageNXTMotorState monitorB = new ManageNXTMotorState();
		final ManageNXTMotorState monitorC = new ManageNXTMotorState();

		if (monitorB.assign(nxt) &&
				monitorC.assign(nxt)) {
			monitorB.setInterval(100);
			monitorB.start();
			monitorC.setInterval(100);
			monitorC.start();
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {

					JFrame frame = new JFrame();

					Container container = frame.getContentPane();
					container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
					container.add(Box.createRigidArea(new Dimension(0,5)));
					container.add(getLabelForMonitor(monitorB));
					container.add(Box.createRigidArea(new Dimension(0,5)));
					container.add(getLabelForMonitor(monitorC));
					container.add(Box.createRigidArea(new Dimension(0,5)));

					frame.addWindowListener(new WindowAdapter() {
						@Override
						public void windowClosing(WindowEvent e) {
							Matereal.getInstance().dispose();
						}
					});

					frame.pack();
					frame.setVisible(true);
				}
			});
		}
	}

	private JLabel getLabelForMonitor(final ManageNXTMotorState monitor) {

		final JLabel label = new JLabel();
		label.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
		label.setFont(Matereal.getInstance().getDefaultFont());
		label.setText("Connecting to the Mindstorms NXT bricks...");

		monitor.addEventListener(new EventListener() {
			public void eventOccurred(Event e) {
				if (e instanceof RobotUpdateEvent) {
					label.setText(String.format(
							"Rotation count: %d (%s)",
							monitor.getRotationCount(),
							monitor.isStable() ?
									"stable" : "flexible"));
				}
			}
		});

		return label;
	}
}