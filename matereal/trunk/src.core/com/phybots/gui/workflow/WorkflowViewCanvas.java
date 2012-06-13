/*
 * PROJECT: Phybots at http://phybots.com/
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
 * The Original Code is Phybots.
 *
 * The Initial Developer of the Original Code is Jun Kato.
 * Portions created by the Initial Developer are
 * Copyright (C) 2009 Jun Kato. All Rights Reserved.
 *
 * Contributor(s): Jun Kato
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
package com.phybots.gui.workflow;

import java.awt.Color;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.phybots.gui.DisposableComponent;
import com.phybots.gui.workflow.layout.Layout;
import com.phybots.gui.workflow.layout.SugiyamaLayouter;
import com.phybots.message.Event;
import com.phybots.message.EventListener;
import com.phybots.message.WorkflowNodeEvent;
import com.phybots.message.WorkflowUpdateEvent;
import com.phybots.workflow.ControlNode;
import com.phybots.workflow.Edge;
import com.phybots.workflow.Node;
import com.phybots.workflow.Transition;
import com.phybots.workflow.Workflow;

import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PCanvas;
import edu.umd.cs.piccolo.PLayer;
import edu.umd.cs.piccolo.activities.PActivity;
import edu.umd.cs.piccolo.activities.PActivity.PActivityDelegate;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.util.PBounds;

/**
 * A canvas for rendering a workflow graph as a timeline-like directed
 * acyclic graph.
 *
 * Since the graph is usually used to represent a workflow of robots and their
 * users, it is expected to form one timeline (thus, directed graph.) It can
 * have cyclic subflows, but this canvas prevents making cycles by using a
 * dummy node which links to the original node of its parent graph.
 *
 * @author Jun Kato
 */
public class WorkflowViewCanvas extends PCanvas implements
		DisposableComponent {
	private static final long serialVersionUID = -6783170737987111980L;
	private static final Color backgroundColor = new Color(240, 240, 240);
	private static final int padding = 10;
	private static final int marginX = 50;
	private static final int marginY = 10;

	private Workflow workflow;
	private WorkflowEventListener wel;
	private WorkflowNodeEventListener wnel;

	private Map<Node, PNodeAbstractImpl> nodeMap;
	private Map<Edge, PLineNodeAbstractImpl> edgeMap;
	private SugiyamaLayouter layouter;

	private PLayer pNodeLayer;
	private PLayer pLineLayer;

	private transient PActivity cameraActivity;
	private transient PActivity graphLayoutActivity = null;
	boolean isGraphUpdated = false;

	public WorkflowViewCanvas(final Workflow workflow) {
		this.workflow = workflow;
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

	public void animateViewToCenterGraph() {
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
		synchronized (workflow) {
			workflow.removeEventListener(wel);
			for (Node node : workflow.getNodes()) {
				onNodeRemoved(node);
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

		addInputEventListener(new InputEventHandler());

		synchronized (workflow) {
			if (workflow.getInitialNode() != null) {
				initializeGraph();
			}
			wel = new WorkflowEventListener();
			workflow.addEventListener(wel);
			wnel = new WorkflowNodeEventListener();
			for (Node node : workflow.getNodes()) {
				onNodeAdded(node);
				if (node.isEntered()) {
					onEnter(node);
				}
			}
		}
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
		synchronized (workflow) {
			if (graphLayoutActivity != null &&
					graphLayoutActivity.isStepping()) {
				isGraphUpdated = true;
				return;
			}
			graphLayoutActivity = null;

			Node initialNode = workflow.getInitialNode();
			if (initialNode == null) {
				return;
			}

			Layout layout = layouter.doLayout(initialNode);
			Set<Edge> edges = new HashSet<Edge>();
			for (Node node : workflow.getNodes()) {
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

			nodeMap.get(workflow.getInitialNode()).setAsInitialNode();

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
							layoutGraph();
						}
						animateViewToCenterGraph();
					}
				});
			}
		}
	}

	private void onNodeAdded(Node node) {
		node.addEventListener(wnel);
	}

	private void onNodeRemoved(Node node) {
		PNodeAbstractImpl pNodeAbstractImpl = nodeMap.get(node);
		pNodeLayer.removeChild(pNodeAbstractImpl);
		node.removeEventListener(wnel);
	}

	private void onTransitionRemoved(Transition transition) {
		PLineNodeAbstractImpl pLineNodeAbstractImpl = edgeMap.get(transition);
		pLineLayer.removeChild(pLineNodeAbstractImpl);
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
					Workflow ad = WorkflowViewCanvas.this.workflow;
					if (ad.isPaused()) {
						ad.resume();
					} else {
						ad.pause();
					}
				} else {
					animateViewToCenterGraph();
				}
			}
		}
	}

	private class WorkflowEventListener implements EventListener {
		public void eventOccurred(Event e) {
			if (e instanceof WorkflowUpdateEvent) {
				WorkflowUpdateEvent wue = (WorkflowUpdateEvent) e;
				switch (wue.getStatus()) {
				case NODE_ADDED:
					onNodeAdded(wue.getAffectedNode());
					break;
				case TRANSITION_ADDED:
					break;
				case NODE_REMOVED:
					onNodeRemoved(wue.getAffectedNode());
					break;
				case TRANSITION_REMOVED:
					onTransitionRemoved(wue.getAffectedTransition());
					break;
				case INITIAL_NODE_SET:
					break;
				}
				layoutGraph();
			}
		}
	}

	private class WorkflowNodeEventListener implements EventListener {
		public void eventOccurred(Event e) {
			if (e instanceof WorkflowNodeEvent) {
				WorkflowNodeEvent wne = (WorkflowNodeEvent) e;
				switch (wne.getStatus()) {
				case ENTERED:
					onEnter(wne.getSource());
					break;
				case LEFT:
					onLeave(wne.getSource());
					break;
				}
			}
		}
	}

}
