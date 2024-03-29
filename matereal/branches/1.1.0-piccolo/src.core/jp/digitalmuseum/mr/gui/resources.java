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
package jp.digitalmuseum.mr.gui;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.PropertyResourceBundle;

public class resources extends PropertyResourceBundle {

	private final static String resourcesString =
		"CoordProviderPanel.centimeter=[cm]\n" +
		"CoordProviderPanel.apply=Apply change of the area size\n" +
		"CoordProviderPanel.errorDescWidth=Please input numbers for the width.\n" +
		"CoordProviderPanel.errorTitle=Parse Error\n" +
		"CoordProviderPanel.errorDescHeight=Please input numbers for the height.\n" +
		"CoordProviderPanel.width=Width of the rectangle area:\n" +
		"CoordProviderPanel.height=Height of the rectangle area:\n" +
		"CoordProviderPanel.reset=Reset the rectangle\n" +
		"MonitorPanel.interval=Interval:\n" +
		"MonitorPanel.selectedService=Selected Service\n" +
		"MonitorPanel.nameOfSelectedService=Name of the selected service.\n" +
		"MonitorPanel.selectedServiceGroup=Selected ServiceGroup\n" +
		"MonitorPanel.nameOfSelectedServiceGroup=Name of the selected service group.\n" +
		"MonitorPanel.millisecond=[ms]";

	public resources() throws IOException {
		super(new ByteArrayInputStream(resourcesString.getBytes()));
	}
}
