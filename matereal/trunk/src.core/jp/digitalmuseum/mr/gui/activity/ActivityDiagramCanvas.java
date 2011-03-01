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
import java.util.Set;

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

	private Point2D s;
	private Point2D e;

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

			s = new Point2D.Float();
			e = new Point2D.Float();

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
							cameraActivity = getCamera()
									.animateViewToCenterBounds(
											event.getPickedNode()
													.getGlobalBounds(), false,
											300);
						}
					} else if (event.getClickCount() == 2) {
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
		cameraActivity = getCamera().animateViewToCenterBounds(pBounds, true,
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
		sticky.setOffset(getWidth() - sticky.getWidth() - 5, getHeight()
				- sticky.getHeight() - 5);
	}

	private void initialize() {

		// Clear the graph.
		nodeMap.clear();
		pNodeLayer.removeAllChildren();
		pLineLayer.removeAllChildren();

		// Construct a new graph.
		rootPNode = constructAcyclicGraph(ad.getInitialNode(), 0);
		rootPNode.setAsInitialNode();
		layoutNodes(rootPNode);
		layoutEdges();
	}

	/**
	 * Construct an acyclic graph with the provided root node.
	 *
	 * @param rootNode
	 * @return PNodeAbstractImpl instance corresponding to the provided root
	 *         node.
	 */
	private PNodeAbstractImpl constructAcyclicGraph(Node rootNode, int depth) {
		Set<Join> pendingNodes = constructRootedTree(rootNode, depth);
		connectPendingNodes(pendingNodes);
		return nodeMap.get(rootNode);
	}

	/**
	 * Do breadth first search and construct a <a
	 * href="http://en.wikipedia.org/wiki/Rooted_tree">rooted tree</a>.
	 *
	 * @return pending nodes which will be part of this rooted tree.
	 */
	private Set<Join> constructRootedTree(Node initialNode, int depth) {
		Set<Node> currentNodes = new HashSet<Node>();
		Set<Node> nextNodes = new HashSet<Node>();
		Set<Transition> transitions = new HashSet<Transition>();
		Set<Join> pendingNodes = new HashSet<Join>();

		PNodeAbstractImpl initialPNode = PNodeFactory
				.newNodeInstance(initialNode);
		initialPNode.setDepth(depth);
		initialPNode.setOffset(5, 5);

		nodeMap.put(initialNode, initialPNode);
		pNodeLayer.addChild(initialPNode);
		depth++;

		currentNodes.add(initialNode);
		while (!currentNodes.isEmpty()) {
			nextNodes.clear();
			for (Node parentNode : currentNodes) {
				PNodeAbstractImpl parentPNode = nodeMap.get(parentNode);

				parentNode.getTransitionsOut(transitions);
				constructLeaves(transitions, parentPNode, nextNodes);

				if (parentNode instanceof ControlNode) {
					if (parentNode instanceof Join) {
						pendingNodes.add((Join) parentNode);
					} else {
						Edge[] edges = ((ControlNode) parentNode).getEdges();
						constructLeaves(edges, parentPNode, nextNodes);
					}
				}
			}
			Set<Node> tmp = currentNodes;
			currentNodes = nextNodes;
			nextNodes = tmp;
			depth++;
		}
		return pendingNodes;
	}

	private void constructLeaves(Set<Transition> edges,
			PNodeAbstractImpl parentPNode, Set<Node> nextNodes) {
		for (Edge edge : edges) {
			constructLeaf(edge, parentPNode, nextNodes);
		}
	}

	private void constructLeaves(Edge[] edges,
			PNodeAbstractImpl parentPNode, Set<Node> nextNodes) {
		for (Edge edge : edges) {
			constructLeaf(edge, parentPNode, nextNodes);
		}
	}

	private void constructLeaf(Edge edge,
			PNodeAbstractImpl parentPNode, Set<Node> nextNodes) {
		Node node = edge.getDestination();
		if (constructPNode(node, parentPNode, edge)) {
			nextNodes.add(node);
		}
	}

	/**
	 * Construct PNode corresponding to the provided node. When it is not the
	 * first time for the node, a PLinkNode is constructed.
	 *
	 * @param node
	 * @param parentPNode
	 * @param edge
	 * @return Whether the provided node appears in the graph for the first time
	 *         or not. (If no, a PLinkNode is constructed in this method.)
	 */
	private boolean constructPNode(Node node, PNodeAbstractImpl parentPNode,
			Edge edge) {
		PNodeAbstractImpl pNode = constructPNode(node);
		parentPNode.addChild(pNode);
		pNode.setDepth(parentPNode.getDepth() + 1);
		pLineLayer.addChild(PNodeFactory.newEdgeInstance(edge, parentPNode,
				pNode));
		return !(pNode instanceof PLinkNode);
	}

	private PNodeAbstractImpl constructPNode(Node node) {
		PNodeAbstractImpl pNode;
		if (nodeMap.containsKey(node)) {
			pNode = new PLinkNode(node);
		} else {
			pNode = PNodeFactory.newNodeInstance(node);
			nodeMap.put(node, pNode);
		}
		return pNode;
	}

	/**
	 * Connect pending nodes to make a complete <a
	 * href="http://en.wikipedia.org/wiki/Directed_acyclic_graph">acyclic
	 * graph</a>.
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
					pLineLayer.addChild(new PJoinLineNode(edge, pJoiningNode,
							pJoinNode));
				} else if (depthDiff == 2) {
					// One dummy node required.
					PNodeAbstractImpl pDummyNode = new PDummyNode(source);
					pJoiningNode.addChild(pDummyNode);
					pDummyNode.setDepth(d + 1);
					pLineLayer.addChild(new PJoinLineNode(edge, pJoiningNode,
							pDummyNode));
					pLineLayer.addChild(new PJoinLineNode(edge, pDummyNode,
							pJoinNode));
				} else {
					// Two dummy node required.
					PNodeAbstractImpl pDummyNode = new PDummyNode(source);
					pJoiningNode.addChild(pDummyNode);
					pDummyNode.setDepth(d + 1);
					PNodeAbstractImpl pDummyNode2 = new PDummyNode(source);
					pDummyNode.addChild(pDummyNode2);
					pDummyNode2.setDepth(depth - 1);
					pLineLayer.addChild(new PJoinLineNode(edge, pJoiningNode,
							pDummyNode));
					pLineLayer.addChild(new PJoinLineNode(edge, pDummyNode,
							pDummyNode2));
					pLineLayer.addChild(new PJoinLineNode(edge, pDummyNode2,
							pJoinNode));
				}
			}
		}
	}

	/**
	 * Do depth first search for layouting nodes.
	 */
	private void layoutNodes(PNodeAbstractImpl rootPNode) {
		LinkedList<PNodeAbstractImpl> stack = new LinkedList<PNodeAbstractImpl>();
		stack.push(rootPNode);
		S: while (!stack.isEmpty()) {
			PNodeAbstractImpl node = stack.peek();
			Deque<PNodeAbstractImpl> children = node
					.getUnmanagedChildrenReference();
			while (!children.isEmpty()) {
				PNodeAbstractImpl child = children.peek();
				if (!child.getUnmanagedChildrenReference().isEmpty()) {
					stack.push(child);
					continue S;
				}
				child.setOffset((child.getDepth() - node.getDepth()) * 240,
						node.y);
				node.y += child.getHeight() + 5;
				node.getUnmanagedChildrenReference().poll();
			}
			stack.poll();
		}
	}

	private void layoutEdges() {
		@SuppressWarnings("unchecked")
		List<PNode> nodes = pLineLayer.getChildrenReference();
		for (PNode pNode : nodes) {
			if (pNode instanceof PLineNodeAbstractImpl) {
				updateEdgePosition((PLineNodeAbstractImpl) pNode);
			}
		}
	}

	private synchronized void updateEdgePosition(
			PLineNodeAbstractImpl pLineNodeAbstractImpl) {
		PNodeAbstractImpl sourcePNode = pLineNodeAbstractImpl.getSourcePNode();
		s.setLocation(200, 35);
		s = sourcePNode.localToGlobal(s);
		PNodeAbstractImpl destinationPNode = pLineNodeAbstractImpl
				.getDestinationPNode();
		e.setLocation(000, 35);
		e = destinationPNode.localToGlobal(e);
		pLineNodeAbstractImpl.setLine(s, e);
	}

	private void onNodeRemoved(Node node) {
		node.removeEventListener(ael);
		PNodeAbstractImpl pNode = nodeMap.get(node);
		if (pNode != null) {
			disposePNode(pNode);
		}
	}

	private void onNodeAdded(Node node) {
		node.addEventListener(ael);
	}

	private void disposePNode(PNodeAbstractImpl pNode) {
		PNode parentPNode = pNode.getParent();

		@SuppressWarnings("unchecked")
		List<PNode> nodeList = parentPNode.getChildrenReference();
		int index = -1;
		for (int i = 0; i < nodeList.size(); i++) {
			if (nodeList.get(i) == pNode) {
				index = i;
				break;
			}
		}

		double height = pNode.getHeight();
		parentPNode.removeChild(pNode);
		for (int i = index; i < nodeList.size(); i++) {
			PNode shiftingPNode = nodeList.get(i);
			Point2D offset = shiftingPNode.getOffset();
			nodeList.get(i).setOffset(offset.getX(), offset.getY() - height);
		}

		layoutEdges();
	}

	private void onTransitionAdded(Transition transition) {
		Node sourceNode = transition.getSource();
		if (!nodeMap.containsKey(sourceNode)) {
			return;
		}

	}

	private class ActivityDiagramEventListener implements EventListener {
		public void eventOccurred(Event e) {
			if (e instanceof ActivityDiagramEvent) {
				ActivityDiagramEvent ade = (ActivityDiagramEvent) e;
				switch (ade.getStatus()) {
				case NODE_ADDED:
					onNodeAdded(ade.getAffectedNode());
					break;
				case NODE_REMOVED:
					onNodeRemoved(ade.getAffectedNode());
					break;
				case TRANSITION_ADDED:
					onTransitionAdded(ade.getAffectedTransition());
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
				PNodeAbstractImpl pNodeAbstractImpl = nodeMap.get(ae
						.getSource());
				switch (ae.getStatus()) {
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
