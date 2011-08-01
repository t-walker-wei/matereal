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

import javax.swing.JTabbedPane;

import jp.digitalmuseum.mr.gui.CoordProviderPanel;
import jp.digitalmuseum.mr.gui.DisposableComponent;
import jp.digitalmuseum.mr.message.Event;
import jp.digitalmuseum.mr.message.EventListener;
import jp.digitalmuseum.mr.message.ServiceEvent;
import jp.digitalmuseum.mr.message.ServiceStatus;
import jp.digitalmuseum.mr.message.ServiceUpdateEvent;
import jp.digitalmuseum.mr.service.CoordProvider;
import jp.digitalmuseum.mr.service.ImageProvider;
import jp.digitalmuseum.mr.service.MarkerDetector;

/**
 * Typical marker detector and world coordinate provider configuration pane.
 *
 * @author Jun KATO
 * @see MarkerDetectorPanel
 * @see CoordProviderPanel
 */
public class TypicalMDCPane extends JTabbedPane implements DisposableComponent {
	private static final long serialVersionUID = 1L;
	private MarkerDetector markerDetector;
	private CoordProvider coordProvider;
	private MarkerDetectorPanel markerDetectorPanel;
	private MarkerEntityPanel markerEntityPanel;
	private CoordProviderPanel coordProviderPanel;
	private transient EventListener eventListener;

	/**
	 * This is the default constructor
	 */
	public TypicalMDCPane(final MarkerDetector detector) {
		super();
		this.markerDetector = detector;
		this.coordProvider = null;
		markerDetectorPanel = new MarkerDetectorPanel(detector);
		markerEntityPanel = new MarkerEntityPanel(detector);

		initialize();

		eventListener = new EventListener() {

			@Override
			public void eventOccurred(Event e) {
				if (e instanceof ServiceUpdateEvent) {
					ServiceUpdateEvent sue = (ServiceUpdateEvent) e;
					if("imageProvider".equals(sue.getParameter())) {
						updateCoordsTab();
					}
				} else if (e instanceof ServiceEvent
						&& ((ServiceEvent) e).getStatus() == ServiceStatus.DISPOSED) {
					dispose();
				}
			}
		};
		detector.addEventListener(eventListener);
	}

	/**
	 * This method initializes this
	 *
	 * @return void
	 */
	private void initialize() {
		this.setSize(720, 640);
		addTab(Messages.getString("TypicalMDCPane.detector"), markerDetectorPanel);
		addTab(Messages.getString("TypicalMDCPane.entity"), markerEntityPanel);
	}

	private void updateCoordsTab() {
		final ImageProvider imageProvider = markerDetector.getImageProvider();
		if (!(imageProvider instanceof CoordProvider)) { return; }
		final CoordProvider coordinate = (CoordProvider) imageProvider;
		coordProviderPanel = new CoordProviderPanel(coordinate);
		if (coordProvider != null) {
			removeTabAt(2);
			coordProviderPanel.dispose();
		}
		addTab(Messages.getString("TypicalMDCPane.worldCoord"), coordProviderPanel);
		coordProvider = coordinate;
	}

	public void dispose() {
		setEnabled(false);
		if (coordProviderPanel != null) {
			coordProviderPanel.dispose();
		}
		markerDetectorPanel.dispose();
		markerDetector.removeEventListener(eventListener);
		markerDetector = null;
		coordProvider = null;
	}
}
