/*
 * PROJECT: napkit at http://digitalmuseum.jp/en/software/
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
package com.phybots.service;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.JComponent;

import com.phybots.Phybots;
import com.phybots.entity.Entity;
import com.phybots.message.LocationUpdateEvent;
import com.phybots.message.ServiceUpdateEvent;
import com.phybots.service.CoordProvider;
import com.phybots.service.ImageProvider;
import com.phybots.service.LocationProvider;
import com.phybots.service.ScreenLocationProviderAbstractImpl;
import com.phybots.service.ServiceGroup;
import com.phybots.utils.Array;
import com.phybots.utils.Location;
import com.phybots.utils.Position;
import com.phybots.utils.Rectangle;
import com.phybots.utils.ScreenLocation;
import com.phybots.utils.ScreenPosition;
import com.phybots.utils.ScreenRectangle;

import jp.digitalmuseum.napkit.NapCameraRelation;
import jp.digitalmuseum.napkit.NapDetectionResult;
import jp.digitalmuseum.napkit.NapMarker;
import jp.digitalmuseum.napkit.NapMarkerDetector;
import jp.digitalmuseum.napkit.NapMarkerDetectorImpl;
import jp.digitalmuseum.napkit.gui.TypicalMDCPane;

/**
 * Marker detector service using NyARToolkit.
 *
 * @author Jun Kato
 * @see NapMarker
 */
public class MarkerDetector extends ScreenLocationProviderAbstractImpl implements NapMarkerDetector, LocationProvider {
	private static final long serialVersionUID = 958747719464907174L;
	public final static String SERVICE_NAME = "Marker Detector";
	public final static int THRESHOLD_MIN = NapMarkerDetector.THRESHOLD_MIN;
	public final static int THRESHOLD_MAX = NapMarkerDetector.THRESHOLD_MAX;
	public final static int THRESHOLD_DEFAULT = NapMarkerDetector.THRESHOLD_DEFAULT;

	private transient NapMarkerDetector detector;
	private transient HashMap<Entity, NapDetectionResult> entityResultMap;
	private transient Array<NapDetectionResult> detectionResults;
	private transient Rectangle rectangle;
	private transient Location location;
	private transient Position position;

	private HashMap<NapMarker, Entity> markerEntityMap;
	private ImageProvider imageProvider;
	private CoordProvider coordProvider;
	private ImageProvider subImageProvider;
	private NapCameraRelation cameraRelation;

	public MarkerDetector() {
		super();
		markerEntityMap = new HashMap<NapMarker, Entity>();
	}

	public String getName() {
		return SERVICE_NAME;
	}

	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
		ois.defaultReadObject();
		initialize();
	}

	protected void initialize() {
		super.initialize();

		detector = new NapMarkerDetectorImpl();
		entityResultMap = new HashMap<Entity, NapDetectionResult>();

		rectangle = new Rectangle();
		location = new Location();
		position = new Position();
	}

	public void setImageProvider(ImageProvider imageProvider) {
		synchronized (detector) {
			this.imageProvider = imageProvider;
			detector.setSize(imageProvider.getWidth(), imageProvider.getHeight());
		}
		if (imageProvider instanceof CoordProvider) {
			setCoordsProvider((CoordProvider) imageProvider);
		}
		distributeEvent(new ServiceUpdateEvent(this, "imageProvider", imageProvider));
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
		distributeEvent(new ServiceUpdateEvent(this, "subCamera", subImageProvider));
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
		Set<ImageProvider> imageProviders = Phybots.getInstance()
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
							result.getConfidence()) {
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

	public boolean setPixelReader(String readerName) {
		return detector.setPixelReader(readerName);
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

	public Array<NapDetectionResult> detectMarker(Object imageData) {
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
