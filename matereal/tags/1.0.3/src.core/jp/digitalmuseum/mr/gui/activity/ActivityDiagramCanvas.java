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
import java.awt.geom.Point2D;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import jp.digitalmuseum.mr.activity.ActivityDiagram;
import jp.digitalmuseum.mr.activity.ControlNode;
import jp.digitalmuseum.mr.activity.Edge;
import jp.digitalmuseum.mr.activity.Join;
import jp.digitalmuseum.mr.activity.Node;
import jp.digitalmuseum.mr.activity.Transition;
import jp.digitalmuseum.mr.gui.DisposableComponent;
import jp.digitalmuseum.mr.message.ActivityDiagramEvent;
import jp.digitalmuseum.mr.message.ActivityEvent;
import jp.digitalmuseum.mr.message.Event;
import jp.digitalmuseum.mr.message.EventListener;
import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PCanvas;
import edu.umd.cs.piccolo.PLayer;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.activities.PTransformActivity;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.nodes.PText;
import edu.umd.cs.piccolo.util.PBounds;

/**
 * A canvas for rendering an activity diagram as a timeline-like directed
 * acyclic graph.
 *
 * Since the diagram is usually used to represent a workflow of robots and
 * their users, it is expected to form one timeline (thus, directed graph.)
 * It can have cyclic subgraphs, but this canvas prevents making cycles by
 * using a dummy node which links to the original node of its parent graph.
 *
 * @author Jun KATO
 */
public class ActivityDiagramCanvas extends PCanvas implements DisposableComponent {
	private static final long serialVersionUID = -6783170737987111980L;
	private ActivityDiagram ad;
	private ActivityDiagramEventListener adel;
	private ActivityEventListener ael;

	private HashMap<Node, PNodeAbstractImpl> nodeMap;
	private PNodeAbstractImpl rootPNode;

	private PLayer pNodeLayer;
	private PLayer pLineLayer;
	private PPath sticky;
	private PText stickyText;
	private PTransformActivity cameraActivity;

	public ActivityDiagramCanvas(final ActivityDiagram ad) {
		this.ad = ad;
		synchronized (ad) {

			nodeMap = new HashMap<Node, PNodeAbstractImpl>();
			pLineLayer = new PLayer();
			getCamera().addLayer(pLineLayer);
			pNodeLayer = new PLayer();
			getCamera().addLayer(pNodeLayer);
			setBackground(new Color(240, 240, 240));

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

			if (ad.getInitialNode() != null) {
				initialize();
			}

			adel = new ActivityDiagramEventListener();
			ad.addEventListener(adel);

			ael = new ActivityEventListener();
			for (Node node : ad.getNodes()) {
				node.addEventListener(ael);
				if (node.isEntered()) {
					PNodeAbstractImpl pNodeAbstractImpl = nodeMap.get(node);
					if (pNodeAbstractImpl != null) {
						pNodeAbstractImpl.onEnter();
					}
				}
			}

			addInputEventListener(new PBasicInputEventHandler() {

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
							cameraActivity = getCamera().animateViewToCenterBounds(
									event.getPickedNode().getGlobalBounds(),
									false,
									300);
						}
					} else if (event.getClickCount() == 2){
						if (event.isLeftMouseButton()) {
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
			});

			addComponentListener(new ComponentAdapter() {
				@Override
				public void componentResized(ComponentEvent e) {
					updateSticky();
				}
			});
		}
	}

	public void animateViewToCenterDiagram() {
		if (cameraActivity != null) {
			cameraActivity.terminate();
		}
		PBounds pBounds = rootPNode.getGlobalFullBounds();
		pBounds.x -= 10;
		pBounds.y -= 10;
		pBounds.width += 20;
		pBounds.height += 20;
		cameraActivity = getCamera().animateViewToCenterBounds(
				pBounds,
				true,
				500);
	}

	public void dispose() {
		synchronized (ad) {
			ad.removeEventListener(adel);
			for (Node node : ad.getNodes()) {
				node.removeEventListener(ael);
			}
		}
	}

	private void updateSticky() {
		sticky.setOffset(
				getWidth() - sticky.getWidth() - 5,
				getHeight() - sticky.getHeight() - 5);
	}

	private void initialize() {

		// Clear graph.
		nodeMap.clear();
		pNodeLayer.removeAllChildren();
		pLineLayer.removeAllChildren();

		Set<Join> pendingNodes = constructRootedTree();
		connectPendingNodes(pendingNodes);
		layoutNodes();
		layoutArrows();
	}

	/**
	 * Do breadth first search for constructing a <a href="http://en.wikipedia.org/wiki/Rooted_tree">rooted tree</a>.
	 *
	 * @return pending nodes which will be  of this rooted tree.
	 */
	private Set<Join> constructRootedTree() {
		Map<Node, PNodeAbstractImpl> currentNodeMap = new HashMap<Node, PNodeAbstractImpl>();
		Map<Node, PNodeAbstractImpl> nextNodeMap = new HashMap<Node, PNodeAbstractImpl>();
		Set<Transition> transitions = new HashSet<Transition>();

		Node initialNode = ad.getInitialNode();
		rootPNode = PNodeFactory.newInstance(initialNode);
		rootPNode.setOffset(5, 5);
		rootPNode.setAsInitialNode();

		nodeMap.put(initialNode, rootPNode);
		currentNodeMap.put(initialNode, rootPNode);

		Set<Node> visitedNodes = nodeMap.keySet();
		Set<Join> pendingNodes = new HashSet<Join>();

		pNodeLayer.addChild(rootPNode);

		int depth = 1;

		while (!currentNodeMap.isEmpty()) {
			nextNodeMap.clear();
			for (Entry<Node, PNodeAbstractImpl> entry : currentNodeMap.entrySet()) {
				Node node = entry.getKey();
				PNodeAbstractImpl parentPNode = entry.getValue();
				node.getTransitionsOut(transitions);
				for (Transition transition : transitions) {

					// New node
					Node n = transition.getDestination();
					PNodeAbstractImpl pNode;
					if (visitedNodes.contains(n)) {
						pNode = new PLinkNode(n);
					} else {
						pNode = PNodeFactory.newInstance(n);
						nextNodeMap.put(n, pNode);
						nodeMap.put(n, pNode); // visitedNodes.add(n);
					}
					parentPNode.addChild(pNode);
					pNode.setDepth(depth);
					pLineLayer.addChild(new PTransitionLineNode(transition, parentPNode, pNode));
				}

				if (node instanceof ControlNode) {
					boolean join = node instanceof Join;
					if (join) {
						pendingNodes.add((Join) node);
					} else {
						for (Edge edge : ((ControlNode)node).getEdges()) {

							// New node
							Node n = edge.getDestination();
							PNodeAbstractImpl pNode;
							if (visitedNodes.contains(n)) {
								pNode = new PLinkNode(n);
							} else {
								pNode = PNodeFactory.newInstance(n);
								nextNodeMap.put(n, pNode);
								nodeMap.put(n, pNode); // visitedNodes.add(n);
							}
							parentPNode.addChild(pNode);
							pNode.setDepth(depth);
							pLineLayer.addChild(new PForkLineNode(edge, parentPNode, pNode));
						}
					}
				}
			}
			Map<Node, PNodeAbstractImpl> tmp = currentNodeMap;
			currentNodeMap = nextNodeMap;
			nextNodeMap = tmp;
			depth ++;
		}
		return pendingNodes;
	}

	/**
	 * Connect pending nodes to make a completed <a href="http://en.wikipedia.org/wiki/Directed_acyclic_graph">acyclic graph</a>.
	 *
	 * @param pendingNodes
	 */
	private void connectPendingNodes(Set<Join> pendingNodes) {
		Set<Edge> edges = new HashSet<Edge>();
		for (Join joinNode : pendingNodes) {
			edges.clear();

			// Look for the deepest joining node.
			int depth = 0;
			for (Edge edge : joinNode.getEdges()) {
				Node source = edge.getSource();
				if (nodeMap.containsKey(source)) {
					PNodeAbstractImpl pNode = nodeMap.get(source);
					int d = pNode.getDepth();
					if (d > depth) {
						depth = d;
					}
					edges.add(edge);
				} else {
					// Unreachable node.
				}
			}

			// Set depth of the join node.
			PNodeAbstractImpl pJoinNode = nodeMap.get(joinNode);
			pJoinNode.setDepthOffset(depth + 1 - pJoinNode.getDepth());

			// Add dummy nodes to parents of the join node.
			for (Edge edge : edges) {
				Node source = edge.getSource();
				PNodeAbstractImpl pJoiningNode = nodeMap.get(source);
				int d = pJoiningNode.getDepth();
				int depthDiff = depth - d;
				if (depthDiff <= 1) {
					// No dummy nodes required.
					pLineLayer.addChild(new PJoinLineNode(edge, pJoiningNode,	pJoinNode));
				} else if (depthDiff == 2) {
					// One dummy node required.
					PNodeAbstractImpl pDummyNode = new PDummyNode(source);
					pJoiningNode.addChild(pDummyNode);
					pDummyNode.setDepth(d + 1);
					pLineLayer.addChild(new PJoinLineNode(edge, pJoiningNode,	pDummyNode));
					pLineLayer.addChild(new PJoinLineNode(edge, pDummyNode,		pJoinNode));
				} else {
					// Two dummy node required.
					PNodeAbstractImpl pDummyNode = new PDummyNode(source);
					pJoiningNode.addChild(pDummyNode);
					pDummyNode.setDepth(d + 1);
					PNodeAbstractImpl pDummyNode2 = new PDummyNode(source);
					pDummyNode.addChild(pDummyNode2);
					pDummyNode2.setDepth(depth - 1);
					pLineLayer.addChild(new PJoinLineNode(edge, pJoiningNode,	pDummyNode));
					pLineLayer.addChild(new PJoinLineNode(edge, pDummyNode,		pDummyNode2));
					pLineLayer.addChild(new PJoinLineNode(edge, pDummyNode2,	pJoinNode));
				}
			}
		}
	}

	/**
	 * Do depth first serach for layouting nodes.
	 */
	private void layoutNodes() {
		LinkedList<PNodeAbstractImpl> stack = new LinkedList<PNodeAbstractImpl>();
		stack.push(rootPNode);
		S : while (!stack.isEmpty()) {
			PNodeAbstractImpl node = stack.peek();
			Deque<PNodeAbstractImpl> children = node.getUnmanagedChildrenReference();
			while (!children.isEmpty()) {
				PNodeAbstractImpl child = children.peek();
				if (!child.getUnmanagedChildrenReference().isEmpty()) {
					stack.push(child);
					continue S;
				}
				child.setOffset(
						(child.getDepth() - node.getDepth()) * 240,
						node.y);
				node.y += child.getHeight() + 5;
				node.getUnmanagedChildrenReference().poll();
			}
			stack.poll();
		}
	}

	private void layoutArrows() {
		@SuppressWarnings("unchecked")
		List<PNode> nodes = pLineLayer.getChildrenReference();
		Point2D s = new Point2D.Float();
		Point2D e = new Point2D.Float();
		for (PNode pNode : nodes) {
			if (pNode instanceof PLineNodeAbstractImpl) {
				PLineNodeAbstractImpl pLineNodeAbstractImpl = (PLineNodeAbstractImpl) pNode;
				PNodeAbstractImpl sourcePNode = pLineNodeAbstractImpl.getSourcePNode();
				s.setLocation(200, 35);
				s = sourcePNode.localToGlobal(s);
				PNodeAbstractImpl destinationPNode = pLineNodeAbstractImpl.getDestinationPNode();
				e.setLocation(000, 35);
				e = destinationPNode.localToGlobal(e);
				pLineNodeAbstractImpl.setLine(s, e);
			}
		}
	}

	private class ActivityDiagramEventListener implements EventListener {
		public void eventOccurred(Event e) {
			if (e instanceof ActivityDiagramEvent) {
				ActivityDiagramEvent ade = (ActivityDiagramEvent) e;
				switch(ade.getStatus()) {
				case NODE_ADDED:
					ade.getAffectedNode().addEventListener(ael);
					break;
				case NODE_REMOVED:
					ade.getAffectedNode().removeEventListener(ael);
					break;
				case TRANSITION_ADDED:
					break;
				case TRANSITION_REMOVED:
					break;
				case INITIAL_NODE_SET:
					initialize();
					break;
				}
			}
		}
	}

	private class ActivityEventListener implements EventListener {
		public void eventOccurred(Event e) {
			if (e instanceof ActivityEvent) {
				ActivityEvent ae = (ActivityEvent) e;
				PNodeAbstractImpl pNodeAbstractImpl = nodeMap.get(ae.getSource());
				switch(ae.getStatus()) {
				case ENTERED:
					pNodeAbstractImpl.onEnter();
					break;
				case LEFT:
					pNodeAbstractImpl.onLeave();
					break;
				}
			}
		}
	}
}
