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
package jp.digitalmuseum.mr.service;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.image.BufferedImage;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.JComponent;

import jp.digitalmuseum.mr.Matereal;
import jp.digitalmuseum.mr.entity.Entity;
import jp.digitalmuseum.mr.message.LocationUpdateEvent;
import jp.digitalmuseum.mr.message.ServiceUpdateEvent;
import jp.digitalmuseum.mr.service.CoordProvider;
import jp.digitalmuseum.mr.service.ImageProvider;
import jp.digitalmuseum.mr.service.LocationProvider;
import jp.digitalmuseum.mr.service.ScreenLocationProviderAbstractImpl;
import jp.digitalmuseum.napkit.NapCameraRelation;
import jp.digitalmuseum.napkit.NapDetectionResult;
import jp.digitalmuseum.napkit.NapMarker;
import jp.digitalmuseum.napkit.NapMarkerDetector;
import jp.digitalmuseum.napkit.NapMarkerDetectorImpl;
import jp.digitalmuseum.napkit.gui.TypicalMDCPane;
import jp.digitalmuseum.utils.Location;
import jp.digitalmuseum.utils.Position;
import jp.digitalmuseum.utils.Rectangle;
import jp.digitalmuseum.utils.ScreenLocation;
import jp.digitalmuseum.utils.ScreenPosition;
import jp.digitalmuseum.utils.ScreenRectangle;
import jp.digitalmuseum.utils.Array;

/**
 * Marker detector service using NyARToolkit.
 *
 * @author Jun KATO
 * @see NapMarker
 */
public class MarkerDetector extends ScreenLocationProviderAbstractImpl implements NapMarkerDetector, LocationProvider {
	public final static String SERVICE_NAME = "Marker Detector";
	public final static int THRESHOLD_MIN = NapMarkerDetector.THRESHOLD_MIN;
	public final static int THRESHOLD_MAX = NapMarkerDetector.THRESHOLD_MAX;
	public final static int THRESHOLD_DEFAULT = NapMarkerDetector.THRESHOLD_DEFAULT;
	private NapMarkerDetector detector;
	private HashMap<NapMarker, Entity> markerEntityMap;
	private HashMap<Entity, NapDetectionResult> entityResultMap;
	private Array<NapDetectionResult> detectionResults;
	private ImageProvider imageProvider;
	private CoordProvider coordProvider;
	private ImageProvider subImageProvider;
	private NapCameraRelation cameraRelation;
	private final Rectangle rectangle = new Rectangle();
	private final Location location = new Location();
	private final Position position = new Position();

	public String getName() {
		return SERVICE_NAME;
	}

	public MarkerDetector() {
		detector = new NapMarkerDetectorImpl();
		markerEntityMap = new HashMap<NapMarker, Entity>();
		entityResultMap = new HashMap<Entity, NapDetectionResult>();
	}

	public void setImageProvider(ImageProvider imageProvider) {
		synchronized (detector) {
			this.imageProvider = imageProvider;
			detector.setSize(imageProvider.getWidth(), imageProvider.getHeight());
		}
		if (imageProvider instanceof CoordProvider) {
			setCoordsProvider((CoordProvider) imageProvider);
		}
		distributeEvent(new ServiceUpdateEvent(this, "image provider", imageProvider));
	}

	public ImageProvider getImageProvider() {
		return imageProvider;
	}

	/**
	 * Camera parameters of the primary camera and the secondary camera must be the same; typically, two cameras must be the same type.
	 * @param subImageProvider
	 * @return
	 */
	public boolean setSubCamera(ImageProvider subImageProvider) {
		if (subImageProvider.getWidth() == imageProvider.getWidth()
				&& subImageProvider.getHeight() == imageProvider.getHeight()) {
			return false;
		}
		this.subImageProvider = subImageProvider;
		distributeEvent(new ServiceUpdateEvent(this, "sub camera", subImageProvider));
		return true;
	}

	public ImageProvider getSubCamera() {
		return subImageProvider;
	}

	public boolean calcCameraRelation() {
		if (subImageProvider == null) {
			return false;
		}
		Array<NapDetectionResult> primaryResults = detectionResults;
		Array<NapDetectionResult> secondaryResults = detectMarker(subImageProvider.getImageData());
		cameraRelation = NapCameraRelation.calcCameraRelation(primaryResults, secondaryResults);
		return cameraRelation != null;
	}

	public void setCoordsProvider(CoordProvider coordProvider) {
		this.coordProvider = coordProvider;
		distributeEvent(new ServiceUpdateEvent(this, "coord provider", coordProvider));
	}

	public CoordProvider getCoordProvider() {
		return coordProvider;
	}

	public boolean addMarker(NapMarker marker) {
		return addMarker(marker, null);
	}

	public boolean addMarker(NapMarker marker, Entity e) {
		synchronized (detector) {
			if (!detector.addMarker(marker)) {
				return false;
			}
			markerEntityMap.put(marker, e);
		}
		distributeEvent(new ServiceUpdateEvent(this, "marker registration", marker));
		return true;
	}

	public boolean addMarkers(Set<NapMarker> markers) {
		boolean result = true;
		for (NapMarker marker : markers) {
			result &= addMarker(marker);
			if (!result) {
				break;
			}
		}
		return result;
	}

	public boolean addMarkers(Map<NapMarker, Entity> markersMap) {
		boolean result = true;
		for (Entry<NapMarker, Entity> entry : markersMap.entrySet()) {
			result &= addMarker(entry.getKey(), entry.getValue());
			if (!result) {
				break;
			}
		}
		return result;
	}
	public void removeMarker(NapMarker marker) {
		synchronized (detector) {
			markerEntityMap.remove(marker);
			detector.removeMarker(marker);
		}
		distributeEvent(new ServiceUpdateEvent(this, "marker unregistration", marker));
	}

	public void removeMarkers(Set<NapMarker> markers) {
		for (NapMarker marker : markers) {
			removeMarker(marker);
		}
	}

	public Entity getEntity(NapMarker marker) {
		synchronized (detector) {
			return markerEntityMap.get(marker);
		}
	}

	public NapDetectionResult getResult(Entity e) {
		synchronized (detector) {
			return entityResultMap.get(e);
		}
	}

	public Map<Entity, NapDetectionResult> getResultMap() {
		synchronized (detector) {
			return new HashMap<Entity, NapDetectionResult>(entityResultMap);
		}
	}

	private void findImageProvider() {
		Set<ImageProvider> imageProviders = Matereal.getInstance()
				.lookForServices(ImageProvider.class);
		if (imageProviders.size() > 0) {
			setImageProvider(imageProviders.iterator().next());
		}
	}

	private void checkCoordProvider() {
		if (coordProvider == null) {
			throw new IllegalStateException("No CoordsProvider specified.");
		}
	}

	@Override
	public synchronized void start(ServiceGroup serviceGroup) {

		// Find an ImageProvider automatically, if not specified.
		if (imageProvider == null) {
			findImageProvider();
		}
		super.start(serviceGroup);
	}

	public void run() {

		// Return if there's nothing to do.
		if (imageProvider == null) {
			return;
		}

		// Grab image data.
		byte[] imageData = imageProvider.getImageData();
		if (imageData == null) {
			return;
		}

		synchronized (detector) {

			// Detect markers.
			detectMarker(imageData);

			Set<NapMarker> lostMarkers = new HashSet<NapMarker>();
			if (subImageProvider != null) {
				lostMarkers.addAll(markerEntityMap.keySet());
			}

			// Manage results.
			entityResultMap.clear();
			for (final NapDetectionResult result : detectionResults) {
				final Entity entity = markerEntityMap.get(result.getMarker());
				final NapDetectionResult previousResult = entityResultMap.get(entity);

				// Ignore results with low confidence.
				if (previousResult != null &&
						previousResult.getConfidence() >
							result.getConfidence() * 2) {
						continue;
				}
				entityResultMap.put(entity, result);

				if (subImageProvider != null) {
					lostMarkers.remove(result.getMarker());
				}
			}

			// Try to find lost markers in the sub camera image.
			if (subImageProvider != null
					&& !lostMarkers.isEmpty()) {
				for (NapDetectionResult result : detector.detectMarker(subImageProvider.getImageData())) {
					NapMarker marker = result.getMarker();
					if (lostMarkers.contains(marker)) {
						NapDetectionResult assumedResult =
								cameraRelation.assumeDetectionResult(detector, result);
						if (assumedResult != null) {
							entityResultMap.put(markerEntityMap.get(marker), assumedResult);
						}
					}
				}
			}
		}
		distributeEvent(new LocationUpdateEvent(this));
	}

	public JComponent getConfigurationComponent() {
		return new TypicalMDCPane(this);
	}

	/**
	 * Draw markers in the specified graphics context.
	 * @param g
	 */
	public void paint(Graphics2D g) {
		synchronized (detector) {
			for (NapDetectionResult result : entityResultMap.values()) {
				paint(g, result);
			}
		}
	}

	/**
	 * Draw a detection result (marker) in the specified graphics context.
	 * @param g
	 * @param result
	 */
	public void paint(Graphics2D g, NapDetectionResult result) {
		if (result != null) {
			final ScreenRectangle square = result.getSquare();
			final ScreenLocation location = result.getLocation();
			final int sx = location.getX(), sy = location.getY();
			final ScreenPosition
				lt = square.getLeftTop(),
				rt = square.getRightTop(),
				b = new ScreenPosition(lt.getX()-sx,lt.getY()-sy),
				c = new ScreenPosition(rt.getX()-sx,rt.getY()-sy);
			final double
				d = lt.distance(rt),
				a = Math.abs(b.getX()*c.getY()-c.getX()*b.getY()),
				l = d == 0 ? 0 : a/d;
			final double theta = location.getRotation();
			square.draw(g, true);
			g.drawLine(sx, sy,
					sx+(int)(Math.cos(theta)*l),
					sy+(int)(Math.sin(theta)*l));
		}
	}

	// EntityInformationProvider

	public Set<Entity> getEntities() {
		synchronized (detector) {
			return new HashSet<Entity>(markerEntityMap.values());
		}
	}

	public Set<NapMarker> getMarkers() {
		synchronized (detector) {
			return new HashSet<NapMarker>(markerEntityMap.keySet());
		}
	}

	public boolean contains(Entity entity) {
		synchronized (detector) {
			return markerEntityMap.values().contains(entity);
		}
	}

	// LocationProvider

	public Location getLocation(Entity e) {
		Location location = new Location();
		getLocationOut(e, location);
		return location;
	}

	public void getLocationOut(Entity e, Location location) {
		checkCoordProvider();
		final NapDetectionResult detectionResult = getResult(e);
		if (detectionResult == null) {
			location.setNotFound(true);
		} else {
			final ScreenRectangle screenRectangle = detectionResult.getSquare();
			try {
				final Iterator<Position> i = rectangle.iterator();
				for (ScreenPosition screenCorner : screenRectangle) {
					final Position corner = i.next();
					coordProvider.screenToRealOut(screenCorner, corner);
				}
				coordProvider.screenToRealOut(
						detectionResult.getPosition(),
						position);
				location.setLocation(
						position,
						rectangle.getRotation());
			} catch (IllegalArgumentException iae) {
				// Do nothing.
			}
		}
	}

	public Position getPosition(Entity e) {
		Position position = new Position();
		getPositionOut(e, position);
		return position;
	}

	public void getPositionOut(Entity e, Position position) {
		checkCoordProvider();
		final NapDetectionResult detectionResult = getResult(e);
		if (detectionResult == null) {
			position.setNotFound(true);
		} else {
			coordProvider.screenToRealOut(
					detectionResult.getPosition(),
					position);
		}
	}

	public double getRotation(Entity e) {
		synchronized (location) {
			getLocationOut(e, location);
			return location.getRotation();
		}
	}

	public double getX(Entity e) {
		synchronized (position) {
			getPositionOut(e, position);
			return position.getX();
		}
	}

	public double getY(Entity e) {
		synchronized (position) {
			getPositionOut(e, position);
			return position.getY();
		}
	}

	public boolean contains(Entity e, Position position) {
		getLocationOut(e, location);
		Shape shape = e.getShape();
		return shape != null &&
				shape.contains(position.getX() - location.getX(), position.getY() - location.getY());
	}

	// ScreenLocationProvider

	public void getScreenLocationOut(Entity e, ScreenLocation screenLocation) {
		final NapDetectionResult detectionResult = getResult(e);
		if (detectionResult == null) {
			screenLocation.setNotFound(true);
		} else {
			detectionResult.getLocationOut(screenLocation);
		}
	}

	// NapMarkerDetector

	public boolean loadCameraParameter(String fileName) {
		return detector.loadCameraParameter(fileName);
	}

	public boolean loadCameraParameter(String fileName, int width, int height) {
		return detector.loadCameraParameter(fileName, width, height);
	}

	public double[] getCameraProjectionMatrix() {
		return detector.getCameraProjectionMatrix();
	}

	public void getCameraProjectionMatrixOut(double[] cameraProjectionMatrix) {
		detector.getCameraProjectionMatrixOut(cameraProjectionMatrix);
	}

	public void setThreshold(int threshold) {
		detector.setThreshold(threshold);
		distributeEvent(new ServiceUpdateEvent(this, "threshold", threshold));
	}

	public int getThreshold() {
		return detector.getThreshold();
	}

	public BufferedImage getBinarizedImage() {
		return detector.getBinarizedImage();
	}

	public boolean isTransMatEnabled() {
		return detector.isTransMatEnabled();
	}

	public void setTransMatEnabled(boolean isTransMatEnabled) {
		detector.setTransMatEnabled(isTransMatEnabled);
	}

	public int getWidth() {
		return detector.getWidth();
	}

	public int getHeight() {
		return detector.getHeight();
	}

	public void setSize(int width, int height) {
		detector.setSize(width, height);
	}

	public Array<NapDetectionResult> detectMarker(byte[] imageData) {
		detectionResults = detector.detectMarker(imageData);
		return detectionResults;
	}

	public NapDetectionResult getResult(NapMarker marker) {
		return detector.getResult(marker);
	}

	public Array<NapDetectionResult> getResults() {
		return new Array<NapDetectionResult>(detectionResults);
	}

	public Array<ScreenRectangle> getSquares() {
		return detector.getSquares();
	}

	/**
	 * Use {@link #addMarker(NapMarker, Entity)} instead.
	 */
	@Deprecated
	public boolean put(NapMarker marker, Entity e) {
		return addMarker(marker, e);
	}

	/**
	 * Use {@link #removeMarker(NapMarker)} instead.
	 */
	@Deprecated
	public void remove(NapMarker marker) {
		removeMarker(marker);
	}

	@Deprecated
	public Array<NapDetectionResult> getLastMarkerDetectionResult() {
		return getResults();
	}

	@Deprecated
	public Array<ScreenRectangle> getLastSquareDetectionResult() {
		return getSquares();
	}
}
