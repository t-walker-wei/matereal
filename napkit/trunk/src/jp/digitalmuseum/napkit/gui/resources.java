/*
 * PROJECT: napkit at http://mr.digitalmuseum.jp/
 * ----------------------------------------------------------------------------
 *
 * This file is part of NyARToolkit Application Toolkit.
 *
 * NyARToolkit Application Toolkit, or simply "napkit",
 * is a simple wrapper library for NyARToolkit.
 *
 * ----------------------------------------------------------------------------
 *
 * License version: GPL 3.0
 *
 * napkit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * napkit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with napkit. If not, see <http://www.gnu.org/licenses/>.
 */
package jp.digitalmuseum.napkit.gui;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.PropertyResourceBundle;

public class resources extends PropertyResourceBundle {

	private final static String resourcesString =
		"MarkerDetectorPanel.descriptionHere=Description about selected image provider shown here.\n" +
		"MarkerDetectorPanel.width=width:\n" +
		"MarkerDetectorPanel.height=height:\n" +
		"MarkerDetectorPanel.fps=fps:\n" +
		"MarkerDetectorPanel.threshold=Threshold:\n" +
		"MarkerDetectorPanel.showBinarizedImage=Show binarized image\n" +
		"MarkerDetectorPanel.slideToDetectRobustlly=Slide the bar so that squares can be detected robustlly.\n" +
		"TypicalMDCPane.detector=Detector\n" +
		"TypicalMDCPane.entity=Target markers\n" +
		"TypicalMDCPane.worldCoord=World coordinate\n" +
		"MarkerEntityPanel.2=Selected marker:\n" +
		"MarkerEntityPanel.0=Directions of the marker patterns are: up, left, down, right";

	public resources() throws IOException {
		super(new ByteArrayInputStream(resourcesString.getBytes()));
	}
}
