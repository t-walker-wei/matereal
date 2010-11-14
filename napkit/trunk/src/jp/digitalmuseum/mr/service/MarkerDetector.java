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
import java.util.Set;

import javax.swing.JComponent;

import jp.digitalmuseum.mr.Matereal;
import jp.digitalmuseum.mr.entity.Entity;
import jp.digitalmuseum.mr.message.LocationUpdateEvent;
import jp.digitalmuseum.mr.service.CoordProvider;
import jp.digitalmuseum.mr.service.ImageProvider;
import jp.digitalmuseum.mr.service.LocationProvider;
import jp.digitalmuseum.mr.service.ScreenLocationProviderAbstractImpl;
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
 * @see NapMarkerMatchingStrategy
 * @see NapMarker
 */
public class MarkerDetector extends ScreenLocationProviderAbstractImpl implements LocationProvider {
	public final static String SERVICE_NAME = "Marker Detector";
	public final static int THRESHOLD_MIN = NapMarkerDetector.THRESHOLD_MIN;
	public final static int THRESHOLD_MAX = NapMarkerDetector.THRESHOLD_MAX;
	public final static int THRESHOLD_DEFAULT = NapMarkerDetector.THRESHOLD_DEFAULT;
	private NapMarkerDetector detector;
	private HashMap<NapMarker, Entity> markerEntityMap;
	private HashMap<Entity, NapDetectionResult> entityResultMap;
	private ImageProvider imageProvider;
	private CoordProvider coordProvider;
	final private Rectangle rectangle = new Rectangle();
	final private Location location = new Location();
	final private Position position = new Position();

	public String getName() {
		return SERVICE_NAME;
	}

	public MarkerDetector() {
		detector = new NapMarkerDetectorImpl();
		markerEntityMap = new HashMap<NapMarker, Entity>();
		entityResultMap = new HashMap<Entity, NapDetectionResult>();
	}

	/**
	 * @see NapMarkerDetector#loadCameraParameter(String)
	 */
	public boolean loadCameraParameter(String fileName) {
		try {
			detector.loadCameraParameter(fileName);
		} catch (IllegalArgumentException e) {
			return false;
		}
		return true;
	}

	public synchronized void setImageProvider(ImageProvider imageProvider) {
		this.imageProvider = imageProvider;
		detector.setSize(imageProvider.getWidth(), imageProvider.getHeight());
		if (imageProvider instanceof CoordProvider) {
			setCoordsProvider((CoordProvider) imageProvider);
		}
	}

	public synchronized ImageProvider getImageProvider() {
		return imageProvider;
	}

	public void setCoordsProvider(CoordProvider coordProvider) {
		this.coordProvider = coordProvider;
	}

	public CoordProvider getCoordProvider() {
		return coordProvider;
	}

	/**
	 * @see NapMarkerDetector#setThreshold(int)
	 */
	public void setThreshold(int threshold) {
		detector.setThreshold(threshold);
	}

	/**
	 * @see NapMarkerDetector#getThreshold()
	 */
	public int getThreshold() {
		return detector.getThreshold();
	}

	/**
	 * @see NapMarkerDetector#addMarker(NapMarker)
	 */
	public synchronized void put(NapMarker marker, Entity e) {
		if (e == null) {
			throw new IllegalArgumentException("Entity parameter cannot be null.");
		}
		markerEntityMap.put(marker, e);
		detector.addMarker(marker);
	}

	public synchronized NapDetectionResult getResult(Entity e) {
		return entityResultMap.get(e);
	}

	public synchronized Map<Entity, NapDetectionResult> getResultMap() {
		return new HashMap<Entity, NapDetectionResult>(entityResultMap);
	}

	public synchronized Set<NapDetectionResult> getResults() {
		return new HashSet<NapDetectionResult>(entityResultMap.values());
	}

	public Array<ScreenRectangle> getSquares() {
		return detector.getLastSquareDetectionResult();
	}

	/**
	 * @return Returns a binarized image used for detection.
	 */
	public synchronized BufferedImage getBinarizedImage() {
		return detector.getBinarizedImage();
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
	public synchronized void start() {

		// Find an ImageProvider automatically, if not specified.
		if (imageProvider == null) {
			findImageProvider();
		}
		super.start();
	}

	public void run() {

		// Return if there's nothing to do.
		if (imageProvider == null) {
			return;
		}
		byte[] imageData = imageProvider.getImageData();
		if (imageData == null) {
			return;
		}

		// Detect markers.
		final Array<NapDetectionResult> resultHolder
				= detector.detectMarker(imageData);

		// Manage results.
		synchronized (this) {
			entityResultMap.clear();
			for (final NapDetectionResult result : resultHolder) {
				final Entity entity = markerEntityMap.get(result.getMarker());
				final NapDetectionResult previousResult =
						entityResultMap.get(entity);
				if (previousResult != null &&
						previousResult.getConfidence() >
							result.getConfidence()) {
						continue;
				}
				entityResultMap.put(entity, result);
			}
		}
		distributeEvent(new LocationUpdateEvent(this));
	}

	/**
	 * Draw markers in the specified graphics context.
	 * @param g
	 */
	public synchronized void paint(Graphics2D g) {
		for (NapDetectionResult result : entityResultMap.values()) {
			paint(g, result);
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

	public synchronized Set<Entity> getEntities() {
		return new HashSet<Entity>(markerEntityMap.values());
	}

	public synchronized boolean contains(Entity entity) {
		return markerEntityMap.values().contains(entity);
	}

	// LocationProvider

	public synchronized Location getLocation(Entity e) {
		getLocationOut(e, location);
		return new Location(location);
	}

	public synchronized void getLocationOut(Entity e, Location location) {
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
				return;
			} catch (IllegalArgumentException iae) {
				// Do nothing.
			}
		}
	}

	public synchronized Position getPosition(Entity e) {
		getPositionOut(e, position);
		return new Position(position);
	}

	public synchronized void getPositionOut(Entity e, Position position) {
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

	public synchronized double getRotation(Entity e) {
		getLocationOut(e, location);
		return location.getRotation();
	}

	public synchronized double getX(Entity e) {
		getPositionOut(e, position);
		return position.getX();
	}

	public synchronized double getY(Entity e) {
		getPositionOut(e, position);
		return position.getY();
	}

	public boolean contains(Entity e, Position position) {
		getLocationOut(e, location);
		Shape shape = e.getShape();
		return shape != null &&
				shape.contains(position.getX() - location.getX(), position.getY() - location.getY());
	}

	// ScreenLocationProvider

	public synchronized void getScreenLocationOut(Entity e, ScreenLocation screenLocation) {
		final NapDetectionResult detectionResult = getResult(e);
		if (detectionResult == null) {
			screenLocation.setNotFound(true);
		} else {
			detectionResult.getLocationOut(screenLocation);
		}
	}

	@Override
	public JComponent getConfigurationComponent() {
		return new TypicalMDCPane(this);
	}
}
