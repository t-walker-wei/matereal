package com.phybots.hakoniwa;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JComponent;


import org.jbox2d.collision.AABB;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.EdgeShape;
import org.jbox2d.collision.shapes.PointShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.MathUtils;
import org.jbox2d.common.OBBViewportTransform;
import org.jbox2d.common.Settings;
import org.jbox2d.common.Vec2;
import org.jbox2d.common.XForm;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.joints.ConstantVolumeJoint;
import org.jbox2d.dynamics.joints.Joint;
import org.jbox2d.dynamics.joints.MouseJoint;
import org.jbox2d.dynamics.joints.MouseJointDef;
import org.jbox2d.dynamics.joints.PulleyJoint;

import com.phybots.Phybots;
import com.phybots.entity.Entity;
import com.phybots.gui.ImageProviderPanel;
import com.phybots.message.ImageUpdateEvent;
import com.phybots.message.LocationUpdateEvent;
import com.phybots.service.CoordProvider;
import com.phybots.service.LocationProvider;
import com.phybots.service.ScreenLocationProvider;
import com.phybots.service.ServiceAbstractImpl;
import com.phybots.utils.Array;
import com.phybots.utils.Location;
import com.phybots.utils.Position;
import com.phybots.utils.ScreenLocation;
import com.phybots.utils.ScreenPosition;

public class Hakoniwa extends ServiceAbstractImpl implements LocationProvider, ScreenLocationProvider, CoordProvider {
	private static final long serialVersionUID = 9041637143139612791L;

	/** Default maximum world width. */
	private final static float WORLD_WIDTH = 400f;
	/** Default maximum world height. */
	private final static float WORLD_HEIGHT = 320f;

	/** Default viewport width. [px] */
	private final static int SCREEN_WIDTH = 640;
	/** Default viewport height. [px] */
	private final static int SCREEN_HEIGHT = 480;

	/** Default scaling. 640[px] = 10[m] */
	private final static float SCALE = 64;

	private final static int CIRCLE_SEGMENTS = 16;
	private final static float CIRCLE_THETA_INCREMENT = 2.0f * MathUtils.PI / CIRCLE_SEGMENTS;
	private final static int POINT_SEGMENTS = 5;
	private final static int POINT_RADIUS = 3;
	private final static float POINT_THETA_INCREMENT = 2.0f * MathUtils.PI / POINT_SEGMENTS;

	private final static float
			DEFAULT_LINEAR_DAMPING = 1.6f,
			DEFAULT_ANGULAR_DAMPING = 2f;

	private final static Color sleepingColor = new Color(0.5f, 0.5f, 0.5f);
	// private final static Color selectedColor = new Color(0.8f, 0.4f, 0.4f);
	private final static Color worldColor = new Color(0.3f, 0.9f, 0.9f);
	private final static Color jointColor = new Color(0.5f, 0.8f, 0.8f);

	private transient World world;
	private transient Vec2[] worldBound;
	private transient Array<ImageListener> listeners;

	private transient MouseJoint mouseJoint;
	private transient HakoniwaEntity mouseJointedEntity;
	private transient Vec2 mouseJointPosition;

	// TODO Implement viewport transform within this class.
	private transient OBBViewportTransform viewportTransform;
	private transient Graphics2D g;
	private transient BufferedImage image;
	private transient BufferedImage backgroundImage;
	private transient boolean isUpdated;

	// Temporal variables used in public methods for converting coordinates.
	private transient Vec2 realSrc;
	private transient Vec2 screenDest;
	private transient Vec2 realDest;
	private transient Vec2 screenSrc;

	// Temporal variables used in methods for painting figures.
	private transient Vec2 center;
	private transient Vec2 tmp;
	private transient Vec2 tmp1;
	private transient Vec2 tmp2;
	private transient Line2D.Float line;
	private transient GeneralPath path;
	private transient Location location;
	private transient Position position;
	private transient ScreenLocation screenLocation;
	private transient ScreenPosition screenPosition;

	private HashSet<HakoniwaEntity> entities;

	/**
	 * ODE: dv/dt + c * v = 0
	 *
	 * Solution: v(t) = v0 * exp(-c * t)
	 * Time step: v(t + dt) = v0 * exp(-c * (t + dt)) = v0 * exp(-c * t) * exp(-c * dt) = v * exp(-c * dt)
	 * v2 = exp(-c * dt) * v1
	 * Taylor expansion:
	 * v2 = (1.0f - c * dt) * v1
	 * @see org.jbox2d.dynamics.Island#solve(org.jbox2d.dynamics.TimeStep, Vec2, boolean, boolean)
	 */
	private float
			linearDamping = DEFAULT_LINEAR_DAMPING,
			angularDamping = DEFAULT_ANGULAR_DAMPING;

	private float scale;
	private boolean isAntialiased;
	private boolean backgroundTransparent;

	public Hakoniwa() {
		this(SCREEN_WIDTH, SCREEN_HEIGHT);
	}

	public Hakoniwa(int width, int height) {
		super();
		initialize(width, height);
	}

	private void writeObject(ObjectOutputStream oos) throws IOException {
		oos.defaultWriteObject();
		oos.writeInt(getWidth());
		oos.writeInt(getHeight());
	}

	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
		ois.defaultReadObject();
		int width = ois.readInt();
		int height = ois.readInt();
		initialize(width, height);
	}

	private void initialize(int width, int height) {

		mouseJointPosition = new Vec2();

		backgroundTransparent = true;
		isUpdated = false;

		// Temporal variables used in public methods for converting coordinates.
		realSrc = new Vec2();
		screenDest = new Vec2();
		realDest = new Vec2();
		screenSrc = new Vec2();

		// Temporal variables used in methods for painting figures.
		center = new Vec2();
		tmp = new Vec2();
		tmp1 = new Vec2();
		tmp2 = new Vec2();
		line = new Line2D.Float();
		path = new GeneralPath();
		location = new Location();
		position = new Position();
		screenLocation = new ScreenLocation();
		screenPosition = new ScreenPosition();

		// Instantiate the viewport.
		viewportTransform = new OBBViewportTransform();
		viewportTransform.setYFlip(true);

		// Set world bound.
		final AABB worldAABB = new AABB();
		final Vec2 worldLower = worldAABB.lowerBound;
		final Vec2 worldUpper = worldAABB.upperBound;
		worldLower.set(-WORLD_WIDTH/2, -WORLD_HEIGHT/2);
		worldUpper.set(WORLD_WIDTH/2, WORLD_HEIGHT/2);
		worldBound = new Vec2[] {
				new Vec2(worldLower.x, worldLower.y),
				new Vec2(worldUpper.x, worldLower.y),
				new Vec2(worldUpper.x, worldUpper.y),
				new Vec2(worldLower.x, worldUpper.y)
		};

		// Set gravity.
		final Vec2 gravity = new Vec2(0f, 0f);

		// Set whether to allow neglecting calculation of static objects.
		final boolean doSleep = true;

		// Instantiate the world.
		world = new World(worldAABB, gravity, doSleep);
		world.setAutoDebugDraw(false);
		world.setWarmStarting(true);
		world.setPositionCorrection(true);
		world.setContinuousPhysics(true);

		//
		setViewportSize(width, height);
		setViewportScale(SCALE);
		entities = new HashSet<HakoniwaEntity>();
		listeners = new Array<ImageListener>();
	}

	public synchronized void run() {
		final boolean isDamping =
				linearDamping != 0f ||
				angularDamping != 0f;
		for (HakoniwaEntity entity : entities) {
			entity.preStep();
			if (isDamping) {
				final Body body = entity.getBody();
				body.setLinearDamping(linearDamping);
				body.setAngularDamping(angularDamping);
			}
		}
		world.step((float)getInterval()/1000, 7);
		distributeEvent(new LocationUpdateEvent(this));
		distributeEvent(new ImageUpdateEvent(this));
		if (listeners.size() > 0) {
			for (ImageListener listener : listeners) {
				listener.imageUpdated(getImage());
			}
		}
		isUpdated = true;
	}

	public static Hakoniwa getHakoniwaFor(HakoniwaEntity e) {
		for (Hakoniwa hakoniwa : Phybots.getInstance().lookForServices(Hakoniwa.class)) {
			if (hakoniwa.contains(e)) {
				return hakoniwa;
			}
		}
		return null;
	}

	synchronized void registerEntity(HakoniwaEntity e) {
		entities.add(e);
	}

	synchronized boolean unregisterEntity(HakoniwaEntity e) {
		return entities.remove(e);
	}

	public float getLinearDamping() {
		return linearDamping;
	}

	public void setLinearDamping(float linearDamping) {
		this.linearDamping = linearDamping;
	}

	public float getAngularDamping() {
		return angularDamping;
	}

	public void setAngularDamping(float angularDamping) {
		this.angularDamping = angularDamping;
	}

	public synchronized void updateMouseJoint(Position p, HakoniwaEntity e) {
		if (mouseJoint != null && !mouseJointedEntity.equals(e)) {
			destroyMouseJoint();
		}
		mouseJointPosition.x = (float) p.getX()/100;
		mouseJointPosition.y = (float) p.getY()/100;
		if (mouseJoint == null) {
			final Body body = e.getBody();
			MouseJointDef md = new MouseJointDef();
			md.body1 = world.getGroundBody();
			md.body2 = body;
			md.target.set(mouseJointPosition);
			md.maxForce = 50f*body.m_mass;
			md.frequencyHz = 1000f/getInterval();
			md.dampingRatio = 0.9f;
			mouseJoint = (MouseJoint) world.createJoint(md);
			mouseJointedEntity = e;
		}
		mouseJoint.setTarget(mouseJointPosition);
	}

	public synchronized void destroyMouseJoint() {
		if (mouseJoint != null) {
			world.destroyJoint(mouseJoint);
			mouseJoint = null;
			mouseJointedEntity = null;
		}
	}

	public synchronized boolean hasMouseJoint() {
		return mouseJoint != null;
	}

	public synchronized HakoniwaEntity getMouseJointedEntity() {
		return mouseJointedEntity;
	}

	public boolean isBackgroundTransparent() {
		return backgroundTransparent;
	}

	public void setBackgroundTransparent(boolean backgroundTransparent) {
		this.backgroundTransparent = backgroundTransparent;
	}

	public synchronized boolean isAntialiased() {
		return isAntialiased;
	}

	public synchronized void setAntialiased(boolean isAntialiased) {
		this.isAntialiased = isAntialiased;
		if (g != null) {
			g.setRenderingHint(
					RenderingHints.KEY_ANTIALIASING,
					isAntialiased ?
							RenderingHints.VALUE_ANTIALIAS_ON :
							RenderingHints.VALUE_ANTIALIAS_OFF);
		}
	}

	public World getWorld() {
		return world;
	}

	/**
	 * Do not dispose this graphics context.
	 * @return
	 */
	public Graphics2D createBackgroundGraphics() {
		return backgroundImage.createGraphics();
	}

	@Override
	public String getName() {
		return "Hakoniwa";
	}

	/**
	 * Set size of the viewport.
	 * @param screenWidth Width of the viewport in [px].
	 * @param screenHeight Height of the viewport in [px].
	 */
	public synchronized void setViewportSize(int screenWidth, int screenHeight) {
		viewportTransform.setExtents((float)screenWidth/2, (float)screenHeight/2);
		setViewportScale(scale);
		image = new BufferedImage(
				screenWidth,
				screenHeight,
				BufferedImage.TYPE_INT_ARGB);
		g = image.createGraphics();
		g.setRenderingHint(
				RenderingHints.KEY_ANTIALIASING,
				isAntialiased ?
						RenderingHints.VALUE_ANTIALIAS_ON :
						RenderingHints.VALUE_ANTIALIAS_OFF);
		backgroundImage = new BufferedImage(
				screenWidth,
				screenHeight,
				BufferedImage.TYPE_INT_ARGB);
	}

	/**
	 * Set scale of the viewport.
	 * @param scale [pixels/cm]
	 */
	public synchronized void setViewportScale(float scale) {
		this.scale = scale;
		viewportTransform.setCamera((float) getRealWidth()/200, (float) getRealHeight()/200, scale);
	}

	// CoordProvider

	public int getWidth() {
		return (int) viewportTransform.getExtents().x*2;
	}

	public int getHeight() {
		return (int) viewportTransform.getExtents().y*2;
	}

	public double getRealWidth() {
		return getWidth()/scale*100;
	}

	public double getRealHeight() {
		return getHeight()/scale*100;
	}

	public ScreenLocation realToScreen(Location realLocation) {
		final ScreenLocation screenLocation = new ScreenLocation();
		realToScreenOut(realLocation, screenLocation);
		return screenLocation;
	}

	public ScreenPosition realToScreen(Position realPosition) {
		final ScreenPosition screenPosition = new ScreenPosition();
		realToScreenOut(realPosition, screenPosition);
		return screenPosition;
	}

	public synchronized void realToScreenOut(Location realLocation, ScreenLocation screenLocation) {
		realLocation.getPositionOut(position);
		realToScreenOut(position, screenPosition);
		screenLocation.setPosition(screenPosition);
		screenLocation.setRotation(realLocation.getRotation());
	}

	public synchronized void realToScreenOut(Position realPosition, ScreenPosition screenPosition) {
		realSrc.x = (float) realPosition.getX()/100;
		realSrc.y = (float) realPosition.getY()/100;
		viewportTransform.getWorldToScreen(realSrc, screenDest);
		screenPosition.set((int) screenDest.x, (int) screenDest.y);
	}

	public Location screenToReal(ScreenLocation screenLocation)
			throws IllegalArgumentException {
		final Location realLocation = new Location();
		screenToRealOut(screenLocation, realLocation);
		return realLocation;
	}

	public Position screenToReal(ScreenPosition screenPosition)
			throws IllegalArgumentException {
		final Position realPosition = new Position();
		screenToRealOut(screenPosition, realPosition);
		return realPosition;
	}

	public synchronized void screenToRealOut(ScreenLocation screenLocation,
			Location location) throws IllegalArgumentException {
		screenLocation.getPositionOut(screenPosition);
		screenToRealOut(screenPosition, position);
		location.setPosition(position);
		location.setRotation(screenLocation.getRotation());
	}

	public synchronized void screenToRealOut(ScreenPosition screenPosition,
			Position realPosition) throws IllegalArgumentException {
		screenSrc.x = (float) screenPosition.getX();
		screenSrc.y = (float) screenPosition.getY();
		viewportTransform.getScreenToWorld(screenSrc, realDest);
		realPosition.set(realDest.x*100, realDest.y*100);
	}

	public void drawImage(Graphics g) {
		drawImage(g, 0, 0);
	}

	public void drawImage(Graphics g, int x, int y) {
		synchronized (image) {
			updateImage();
			g.drawImage(image, x, y, null);
		}
	}

	public BufferedImage getImage() {
		synchronized (image) {
			updateImage();
			final BufferedImage newImage = new BufferedImage(
					image.getWidth(), image.getHeight(), image.getType());
			final Graphics2D g2 = newImage.createGraphics();
			g2.drawImage(image, 0, 0, null);
			g2.dispose();
			return newImage;
		}
	}

	public byte[] getImageData() {
		synchronized (image) {
			updateImage();
			final BufferedImage newImage = new BufferedImage(
					image.getWidth(), image.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
			final Graphics2D g2 = newImage.createGraphics();
			g2.setColor(Color.white);
			g2.fillRect(0, 0, getWidth(), getHeight());
			g2.drawImage(image, 0, 0, null);
			g2.dispose();
			return ((DataBufferByte)newImage.getRaster().getDataBuffer()).getData();
		}
	}

	public synchronized void addImageListener(ImageListener listener) {
		listeners.push(listener);
	}

	public synchronized boolean removeImageListener(ImageListener listener) {
		return listeners.remove(listener);
	}

	private void updateImage() {
		if (!isUpdated) {
			return;
		}
		synchronized (image){

			// Clear the image.
			if (isBackgroundTransparent()) {
				g.setComposite(AlphaComposite.Clear);
				g.fillRect(0, 0, image.getWidth(), image.getHeight());
			} else {
				g.setColor(Color.white);
				g.fillRect(0, 0, image.getWidth(), image.getHeight());
			}
			g.setComposite(AlphaComposite.SrcOver);

			// Draw background.
			g.drawImage(backgroundImage, 0, 0, null);

			// Draw shapes.
			g.setStroke(new BasicStroke(2f));
			for (Body b = world.getBodyList(); b != null; b = b.getNext()) {
				final XForm xf = b.getMemberXForm();
				Color c = sleepingColor;
				if (b.m_userData instanceof HakoniwaEntity)
				{
					final HakoniwaEntity e = (HakoniwaEntity)b.m_userData;
					c = e.getColor();
				}
				for (Shape s = b.getShapeList(); s != null; s = s.getNext()) {
					drawShape(s, xf, c);
				}
			}

			// Draw joints.
			for (Joint j = world.getJointList(); j != null; j = j.getNext()) {
				drawJoint(j);
			}

			// Draw world bounds.
			setColor(worldColor);
			drawPolygon(worldBound, 4);
		}
		isUpdated = false;
	}

	// EntityInformationProvider

	public synchronized Set<Entity> getEntities() {
		return new HashSet<Entity>(entities);
	}

	public synchronized boolean contains(Entity entity) {
		return entities.contains(entity);
	}

	// LocationProvider

	public CoordProvider getCoordProvider() {
		return this;
	}

	public Location getLocation(Entity e) {
		getLocationOut(e, location);
		return new Location(location);
	}

	public void getLocationOut(Entity e, Location location) {
		if (e instanceof HakoniwaEntity &&
				((HakoniwaEntity) e).getHakoniwa() == this) {
			((HakoniwaEntity) e).getLocationOut(location);
		} else {
			location.setNotFound(true);
		}
	}

	public Position getPosition(Entity e) {
		getLocationOut(e, location);
		return location.getPosition();
	}

	public void getPositionOut(Entity e, Position position) {
		getLocationOut(e, location);
		if (!location.isNotFound()) {
			position.set(location.getX(), location.getY());
		} else {
			position.setNotFound(true);
		}
	}

	public double getX(Entity e) {
		getLocationOut(e, location);
		return location.getX();
	}

	public double getY(Entity e) {
		getLocationOut(e, location);
		return location.getY();
	}

	public double getRotation(Entity e) {
		getLocationOut(e, location);
		return location.getRotation();
	}

	public boolean contains(Entity e, Position position) {
		getLocationOut(e, location);
		double x = position.getX() - location.getX();
		double y = position.getY() - location.getY();
		double c = Math.cos(-location.getRotation());
		double s = Math.sin(-location.getRotation());
		java.awt.Shape shape = e.getShape();
		return shape != null &&
				shape.contains(x*c+y*s, y*c-s*x);
	}

	// ScreenLocationProvider

	public ScreenLocation getScreenLocation(Entity e) {
		getScreenLocationOut(e, screenLocation);
		return new ScreenLocation(screenLocation);
	}

	public void getScreenLocationOut(Entity e, ScreenLocation screenLocation) {
		getLocationOut(e, location);
		position.set(location.getX(), location.getY());
		realToScreenOut(position, screenPosition);
		screenLocation.setLocation(
				screenPosition,
				-location.getRotation());
	}

	public ScreenPosition getScreenPosition(Entity e) {
		getScreenLocationOut(e, screenLocation);
		return screenLocation.getPosition();
	}

	public void getScreenPositionOut(Entity e, ScreenPosition screenPosition) {
		getScreenLocationOut(e, screenLocation);
		screenPosition.set(screenLocation.getX(), screenLocation.getY());
	}

	public int getScreenX(Entity e) {
		getScreenLocationOut(e, screenLocation);
		return screenLocation.getX();
	}

	public int getScreenY(Entity e) {
		getScreenLocationOut(e, screenLocation);
		return screenLocation.getY();
	}

	public double getScreenRotation(Entity e) {
		getScreenLocationOut(e, screenLocation);
		return screenLocation.getRotation();
	}

	// Painting features.

	private void setColor(Color color) {
		g.setColor(color);
	}

	private void drawShape(final Shape shape, final XForm xf, final Color color) {
		// This method uses center, tmp, tmp1 and tmp2 temporarily.
		setColor(color);
		switch (shape.getType())
		{

		case CIRCLE_SHAPE: {
			final CircleShape circle = (CircleShape) shape;
			XForm.mulToOut(xf, circle.getMemberLocalPosition(), center);
			viewportTransform.getWorldToScreen(center, tmp2);
			final float radius = circle.getRadius();
			final Vec2 axis = xf.R.col1;

			// Draw and fill a circle.
			float theta = 0.0f;
			path.reset();
			boolean isFirst = true;
			Vec2 first = tmp, current = tmp1;
			for (int i = 0; i < CIRCLE_SEGMENTS; ++i) {
				current.set(
						radius * MathUtils.cos(theta),
						radius * MathUtils.sin(theta));
				viewportTransform.vectorTransform(current, current);
				current.addLocal(tmp2);
				if (isFirst) {
					first.set(current);
					path.moveTo(current.x, current.y);
					isFirst = false;
				}
				else {
					path.lineTo(current.x, current.y);
				}
				theta += CIRCLE_THETA_INCREMENT;
			}
			path.lineTo(first.x, first.y);
			path.closePath();
			g.draw(path);
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.2f));
			g.fill(path);
			g.setComposite(AlphaComposite.SrcOver);

			// Draw an axis.
			tmp1.set(center.x + radius * axis.x, center.y + radius * axis.y);
			viewportTransform.getWorldToScreen(tmp1, tmp1);
			drawLineOnScreen(tmp1, tmp2);
			break;
		}

		case POINT_SHAPE: {
			final PointShape point = (PointShape) shape;
			XForm.mulToOut(xf, point.getMemberLocalPosition(), center);
			viewportTransform.getWorldToScreen(center, tmp2);

			// Draw a circle.
			float theta = 0.0f;
			Vec2 first = tmp, previous = null, current = tmp1;
			for (int i = 0; i < POINT_SEGMENTS; ++ i) {
				current.set(
						tmp2.x + MathUtils.cos(theta) * POINT_RADIUS,
						tmp2.y + MathUtils.sin(theta) * POINT_RADIUS);
				if (previous == null) {
					first.set(current);
					previous = tmp2;
				}
				else {
					drawLineOnScreen(previous, current);
				}
				previous.set(current);
				theta += POINT_THETA_INCREMENT;
			}
			drawLineOnScreen(current, first);
			break;
		}

		case POLYGON_SHAPE: {
			final PolygonShape poly = (PolygonShape) shape;
			final int vertexCount = poly.getVertexCount();
			final Vec2[] localVertices = poly.getVertices();
			if (vertexCount > Settings.maxPolygonVertices ||
					vertexCount <= 0) {
				break;
			}

			// Draw and fill a polygon.
			path.reset();
			boolean isFirst = true;
			Vec2 first = tmp, current = tmp1;
			for (int i = 0; i < vertexCount; ++ i) {
				current = XForm.mul(xf, localVertices[i]);
				viewportTransform.getWorldToScreen(current, current);
				if (isFirst) {
					first.set(current);
					path.moveTo(current.x, current.y);
					isFirst = false;
				}
				else {
					path.lineTo(current.x, current.y);
				}
			}
			path.lineTo(first.x, first.y);
			path.closePath();
			g.draw(path);
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.2f));
			g.fill(path);
			g.setComposite(AlphaComposite.SrcOver);
			break;
		}

		case EDGE_SHAPE:
			final EdgeShape edge = (EdgeShape) shape;
			XForm.mulToOut(xf, edge.getVertex1(), tmp1);
			XForm.mulToOut(xf, edge.getVertex2(), tmp2);
			drawLine(tmp1, tmp2);
			break;
		}
	}

	private void drawJoint(final Joint joint) {
		final Body b1 = joint.getBody1();
		final Body b2 = joint.getBody2();
		final XForm xf1 = b1.getMemberXForm();
		final XForm xf2 = b2.getMemberXForm();
		final Vec2 x1 = xf1.position;
		final Vec2 x2 = xf2.position;
		final Vec2 p1 = joint.getAnchor1();
		final Vec2 p2 = joint.getAnchor2();
		setColor(jointColor);
		switch (joint.getType())
		{

		case DISTANCE_JOINT:
			drawLine(p1, p2);
			break;

		case PULLEY_JOINT:
			final PulleyJoint pulley = (PulleyJoint) joint;
			final Vec2 s1 = pulley.getGroundAnchor1();
			final Vec2 s2 = pulley.getGroundAnchor2();
			drawLine(s1, p1);
			drawLine(s2, p2);
			drawLine(s1, s2);
			break;

		case MOUSE_JOINT:
			// Don't draw mouse joint
			break;

		case CONSTANT_VOLUME_JOINT:
			final ConstantVolumeJoint cvj = (ConstantVolumeJoint) joint;
			final Body[] bodies = cvj.getBodies();
			Vec2 first = null, previous = null, current = null;
			for (int i = 0; i < bodies.length; ++ i) {
				current = bodies[i].getMemberWorldCenter();
				viewportTransform.getWorldToScreen(current, current);
				if (previous == null) {
					first = current;
				}
				else {
					drawLineOnScreen(previous, current);
				}
				previous = current;
			}
			drawLineOnScreen(current, first);
			break;

		default:
			drawLine(x1, p1);
			drawLine(p1, p2);
			drawLine(x2, p2);
			break;
		}
	}

	private void drawPolygon(Vec2[] vertices, int vertexCount) {
		Vec2 first = null, previous = null, current = null;
		for (int i = 0; i < vertexCount; ++ i) {
			current = vertices[i];
			if (previous == null) {
				first = current;
			}
			else {
				drawLine(previous, current);
			}
			previous = current;
		}
		drawLine(current, first);
	}

	private void drawLine(Vec2 argP1, Vec2 argP2) {
		viewportTransform.getWorldToScreen(argP1, tmp1);
		viewportTransform.getWorldToScreen(argP2, tmp2);
		drawLineOnScreen(tmp1, tmp2);
	}

	private void drawLineOnScreen(Vec2 p1, Vec2 p2) {
		line.setLine(p1.x, p1.y, p2.x, p2.y);
		g.draw(line);
	}

	@Override
	public JComponent getConfigurationComponent() {
		return new ImageProviderPanel(this);
	}
}
