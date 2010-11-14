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
package jp.digitalmuseum.napkit.gui;

import javax.swing.JTabbedPane;

import jp.digitalmuseum.mr.gui.CoordProviderPanel;
import jp.digitalmuseum.mr.gui.DisposableComponent;
import jp.digitalmuseum.mr.service.CoordProvider;
import jp.digitalmuseum.mr.service.ImageProvider;
import jp.digitalmuseum.mr.service.MarkerDetector;
import jp.digitalmuseum.mr.service.ServiceAbstractImpl;

/**
 * Typical marker detector and world coordinate provider configuration pane.
 *
 * @author Jun KATO
 * @see MarkerDetectorPanel
 * @see CoordProviderPanel
 */
public class TypicalMDCPane extends JTabbedPane implements DisposableComponent {
	private static final long serialVersionUID = 1L;
	private WatcherService watcher;
	private MarkerDetector markerDetector;
	private CoordProvider coordProvider;
	private MarkerDetectorPanel markerDetectorPanel;
	private CoordProviderPanel coordProviderPanel;

	/**
	 * This is the default constructor
	 */
	public TypicalMDCPane(final MarkerDetector detector) {
		super();
		this.markerDetector = detector;
		this.coordProvider = null;
		markerDetectorPanel = new MarkerDetectorPanel(detector);
		initialize();

		// Start watcher service.
		watcher = new WatcherService();
		watcher.start(detector.getServiceGroup());
	}

	/**
	 * This method initializes this
	 *
	 * @return void
	 */
	private void initialize() {
		this.setSize(720, 640);
		addTab(Messages.getString("TypicalMDCPane.detector"), markerDetectorPanel);
	}

	private void updateCoordsTab() {
		final ImageProvider imageProvider = markerDetector.getImageProvider();
		if (!(imageProvider instanceof CoordProvider)) { return; }
		final CoordProvider coordinate = (CoordProvider) imageProvider;
		coordProviderPanel = new CoordProviderPanel(coordinate);
		if (coordProvider != null) {
			removeTabAt(1);
			coordProviderPanel.dispose();
		}
		addTab(Messages.getString("TypicalMDCPane.worldCoord"), coordProviderPanel);
		coordProvider = coordinate;
	}

	public void dispose() {
		coordProviderPanel.dispose();
		markerDetectorPanel.dispose();
		watcher.stop();
	}

	private class WatcherService extends ServiceAbstractImpl {
		public String getName() { return "Marker Detector Source Watcher"; }
		public void run() {
			final ImageProvider currentImageProvider = markerDetector.getImageProvider();
			if (!currentImageProvider.equals(coordProvider)) {
				updateCoordsTab();
			}
		}
	}
}
