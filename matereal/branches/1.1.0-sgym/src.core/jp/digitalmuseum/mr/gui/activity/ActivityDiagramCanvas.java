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
package jp.digitalmuseum.mr.gui.activity;

import java.awt.Color;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jp.digitalmuseum.mr.activity.ActivityDiagram;
import jp.digitalmuseum.mr.activity.ControlNode;
import jp.digitalmuseum.mr.activity.Edge;
import jp.digitalmuseum.mr.activity.Node;
import jp.digitalmuseum.mr.gui.DisposableComponent;
import jp.digitalmuseum.mr.gui.activity.layout.Layout;
import jp.digitalmuseum.mr.gui.activity.layout.SugiyamaLayouter;
import jp.digitalmuseum.mr.message.ActivityDiagramEvent;
import jp.digitalmuseum.mr.message.ActivityEvent;
import jp.digitalmuseum.mr.message.Event;
import jp.digitalmuseum.mr.message.EventListener;
import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PCanvas;
import edu.umd.cs.piccolo.PLayer;
import edu.umd.cs.piccolo.activities.PActivity;
import edu.umd.cs.piccolo.activities.PActivity.PActivityDelegate;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.nodes.PText;
import edu.umd.cs.piccolo.util.PBounds;

/**
 * A canvas for rendering an activity diagram as a timeline-like directed
 * acyclic graph.
 *
 * Since the diagram is usually used to represent a workflow of robots and their
 * users, it is expected to form one timeline (thus, directed graph.) It can
 * have cyclic subgraphs, but this canvas prevents making cycles by using a
 * dummy node which links to the original node of its parent graph.
 *
 * @author Jun KATO
 */
public class ActivityDiagramCanvas extends PCanvas implements
		DisposableComponent {
	private static final long serialVersionUID = -6783170737987111980L;
	private static final Color backgroundColor = new Color(240, 240, 240);
	private static final int padding = 10;
	private static final int marginX = 50;
	private static final int marginY = 10;

	private ActivityDiagram ad;
	private ActivityDiagramEventListener adel;
	private ActivityEventListener ael;

	private Map<Node, PNodeAbstractImpl> nodeMap;
	private Map<Edge, PLineNodeAbstractImpl> edgeMap;
	private SugiyamaLayouter layouter;

	private PLayer pNodeLayer;
	private PLayer pLineLayer;

	private PPath sticky;
	private PText stickyText;

	private transient PActivity cameraActivity;
	private transient PActivity graphLayoutActivity;
	boolean isGraphUpdated = false;

	public ActivityDiagramCanvas(final ActivityDiagram ad) {
		this.ad = ad;
		initialize();
	}

	public static Color getBackgroundColor() {
		return backgroundColor;
	}

	public static int getPadding() {
		return padding;
	}

	public static int getMarginX() {
		return marginX;
	}

	public static int getMarginY() {
		return marginY;
	}

	public void animateViewToCenterDiagram() {
		if (cameraActivity != null) {
			cameraActivity.terminate();
		}
		PBounds pBounds = pNodeLayer.getFullBounds();
		pBounds.x -= padding;
		pBounds.y -= padding;
		pBounds.width += padding * 2;
		pBounds.height += padding * 2;
		cameraActivity = getCamera().animateViewToCenterBounds(pBounds, true, 500);
	}

	public void dispose() {
		synchronized (ad) {
			ad.removeEventListener(adel);
			for (Node node : ad.getNodes()) {
				node.removeEventListener(ael);
			}
		}
	}

	private void initialize() {

		nodeMap = new HashMap<Node, PNodeAbstractImpl>();
		edgeMap = new HashMap<Edge, PLineNodeAbstractImpl>();
		layouter = new SugiyamaLayouter();

		pLineLayer = new PLayer();
		getCamera().addLayer(pLineLayer);
		pNodeLayer = new PLayer();
		getCamera().addLayer(pNodeLayer);

		setBackground(backgroundColor);

		initializeSticky();

		addInputEventListener(new InputEventHandler());

		synchronized (ad) {
			if (ad.getInitialNode() != null) {
				initializeGraph();
			}
			adel = new ActivityDiagramEventListener();
			ad.addEventListener(adel);
			ael = new ActivityEventListener();
			for (Node node : ad.getNodes()) {
				node.addEventListener(ael);
				if (node.isEntered()) {
					onEnter(node);
				}
			}
		}
	}

	private void initializeSticky() {
		sticky = PPath.createRectangle(0, 0, 240, 25);
		sticky.setPaint(Color.black);
		sticky.setStroke(null);
		stickyText = new PText("Activity Diagram Viewer");
		stickyText.setWidth(230);
		stickyText.setHeight(15);
		stickyText.setOffset(5, 5);
		stickyText.setTextPaint(Color.white);
		sticky.addChild(stickyText);
		getCamera().addChild(sticky);

		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				updateSticky();
			}
		});
	}

	private void updateSticky() {
		sticky.setOffset(getWidth() - sticky.getWidth() - 5, getHeight()
				- sticky.getHeight() - 5);
	}

	private void initializeGraph() {
		if (graphLayoutActivity != null &&
				graphLayoutActivity.isStepping()) {
			isGraphUpdated = true;
			return;
		}

		// Clear the graph.
		nodeMap.clear();
		edgeMap.clear();
		pNodeLayer.removeAllChildren();
		pLineLayer.removeAllChildren();

		// Layout a new graph.
		layoutGraph();
	}

	private void layoutGraph() {
		if (graphLayoutActivity != null &&
				graphLayoutActivity.isStepping()) {
			isGraphUpdated = true;
			return;
		}
		graphLayoutActivity = null;

		Layout layout = layouter.doLayout(ad.getInitialNode());
		Set<Edge> edges = new HashSet<Edge>();
		for (Node node : ad.getNodes()) {
			if (layout.contains(node)) {

				// Layout a node.
				PNodeAbstractImpl pNode;
				if (nodeMap.containsKey(node)) {
					pNode = nodeMap.get(node);
				} else {
					pNode = PNodeFactory.newNodeInstance(node);
					pNodeLayer.addChild(pNode);
					nodeMap.put(node, pNode);
				}
				PActivity a = pNode.setPosition(layout.getNodeCoord(node));
				if (graphLayoutActivity == null) {
					graphLayoutActivity = a;
				}
				getRoot().addActivity(a);

				// Count up edges from the node.
				edges.clear();
				if (node instanceof ControlNode) {
					edges.addAll(Arrays.asList(((ControlNode) node).getEdges()));
				}
				edges.addAll(node.getTransitions());

				// Layout edges.
				for (Edge edge : edges) {
					PLineNodeAbstractImpl pLineNode;
					if (edgeMap.containsKey(edge)) {
						pLineNode = edgeMap.get(edge);
					} else {
						pLineNode = PNodeFactory.newEdgeInstance(edge);
						edgeMap.put(edge, pLineNode);
					}
					pLineLayer.addChild(pLineNode);
					getRoot().addActivity(pLineNode.setLine(layout.getEdgeCoord(edge)));
				}
			}
		}

		nodeMap.get(ad.getInitialNode()).setAsInitialNode();

		if (graphLayoutActivity != null) {
			graphLayoutActivity.setDelegate(new PActivityDelegate() {
				public void activityStepped(PActivity pactivity) {
					// Do nothing.
				}
				public void activityStarted(PActivity pactivity) {
					// Do nothing.
				}
				public void activityFinished(PActivity pactivity) {
					graphLayoutActivity = null;
					if (isGraphUpdated) {
						isGraphUpdated = false;
						initializeGraph();
					}
				}
			});
		}
	}

	private void onEnter(Node node) {
		PNodeAbstractImpl pNodeAbstractImpl = nodeMap.get(node);
		if (pNodeAbstractImpl != null) {
			pNodeAbstractImpl.onEnter();
		}
	}

	private void onLeave(Node node) {
		PNodeAbstractImpl pNodeAbstractImpl = nodeMap.get(node);
		if (pNodeAbstractImpl != null) {
			pNodeAbstractImpl.onLeave();
		}
	}

	private class InputEventHandler extends PBasicInputEventHandler {

		@Override
		public void mousePressed(PInputEvent event) {
			if (cameraActivity != null) {
				cameraActivity.terminate();
			}
		}

		@Override
		public void mouseClicked(PInputEvent event) {
			if (!(event.getPickedNode() instanceof PCamera)) {
				if (!event.isLeftMouseButton()) {
					cameraActivity = getCamera()
							.animateViewToCenterBounds(
									event.getPickedNode()
											.getGlobalBounds(), false,
									300);
				}
			} else if (event.getClickCount() == 2) {
				if (event.isLeftMouseButton()) {
					ActivityDiagram ad = ActivityDiagramCanvas.this.ad;
					if (ad.isPaused()) {
						ad.resume();
					} else {
						ad.pause();
					}
				} else {
					animateViewToCenterDiagram();
				}
			}
		}
	}

	private class ActivityDiagramEventListener implements EventListener {
		public void eventOccurred(Event e) {
			if (e instanceof ActivityDiagramEvent) {
				ActivityDiagramEvent ade = (ActivityDiagramEvent) e;
				switch (ade.getStatus()) {
				case NODE_ADDED:
				case TRANSITION_ADDED:
					layoutGraph();
					break;
				case NODE_REMOVED:
				case TRANSITION_REMOVED:
				case INITIAL_NODE_SET:
					initializeGraph();
					break;
				}
			}
		}
	}

	private class ActivityEventListener implements EventListener {
		public void eventOccurred(Event e) {
			if (e instanceof ActivityEvent) {
				ActivityEvent ae = (ActivityEvent) e;
				switch (ae.getStatus()) {
				case ENTERED:
					onEnter(ae.getSource());
					break;
				case LEFT:
					onLeave(ae.getSource());
					break;
				}
			}
		}
	}

}
