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

import java.util.HashMap;
import java.util.HashSet;
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
import jp.digitalmuseum.mr.message.Event;
import jp.digitalmuseum.mr.message.EventListener;
import edu.umd.cs.piccolo.PCanvas;
import edu.umd.cs.piccolo.PLayer;

public class ActivityDiagramCanvas extends PCanvas implements DisposableComponent {
	private static final long serialVersionUID = -6783170737987111980L;
	private ActivityDiagram ad;
	private ActivityDiagramEventListener adel;
	private HashMap<Node, PNodeAbstractImpl> nodeMap;
	private PLayer pNodeLayer;
	private PLayer pLineLayer;

	public ActivityDiagramCanvas(ActivityDiagram ad) {
		this.ad = ad;
		adel = new ActivityDiagramEventListener();
		ad.addEventListener(adel);
		nodeMap = new HashMap<Node, PNodeAbstractImpl>();
		pNodeLayer = new PLayer();
		pLineLayer = new PLayer();
		if (ad.getInitialNode() != null) {
			initialize();
		}
		getCamera().addLayer(pLineLayer);
		getCamera().addLayer(pNodeLayer);
	}

	public void dispose() {
		ad.removeEventListener(adel);
	}

	private void initialize() {
		synchronized (ad) {

			// Breadth first search
			makePNodes();

			// Depth first search

		}
	}

	private void makePNodes() {
		Map<Node, PNodeAbstractImpl> currentNodeMap = new HashMap<Node, PNodeAbstractImpl>();
		Map<Node, PNodeAbstractImpl> nextNodeMap = new HashMap<Node, PNodeAbstractImpl>();
		Set<Transition> transitions = new HashSet<Transition>();

		int depth = 0;

		Node initialNode = ad.getInitialNode();
		PNodeAbstractImpl initialPNode = PNodeFactory.newInstance(initialNode, depth);

		nodeMap.clear();
		currentNodeMap.put(initialNode, initialPNode);

		Set<Node> visitedNodes = nodeMap.keySet();
		Set<Join> pendingNodes = new HashSet<Join>();

		pNodeLayer.removeAllChildren();
		pLineLayer.removeAllChildren();
		pNodeLayer.addChild(initialPNode);

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
						pNode = PNodeFactory.newInstance(n, depth);
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
								pNode = PNodeFactory.newInstance(n, depth);
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

		Set<Edge> edges = new HashSet<Edge>();
		for (Join joinNode : pendingNodes) {
			edges.clear();

			// Look for the deepest joining node.
			depth = 0;
			for (Edge edge : joinNode.getEdges()) {
				Node source = edge.getSource();
				if (nodeMap.containsKey(source)) {
					PNodeAbstractImpl pNode = nodeMap.get(source);
					int d = pNode.getDepth();
					if (d > depth) {
						d = depth;
					}
					edges.add(edge);
				} else {
					// Unreachable node.
				}
			}

			// Set depth of the join node.
			PNodeAbstractImpl pJoinNode = nodeMap.get(joinNode);
			pJoinNode.setDepth(depth + 1);

			// Add dummy nodes
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
					PNodeAbstractImpl pDummyNode = new PDummyNode();
					pJoiningNode.addChild(pDummyNode);
					pLineLayer.addChild(new PJoinLineNode(edge, pJoiningNode,	pDummyNode));
					pLineLayer.addChild(new PJoinLineNode(edge, pDummyNode,		pJoinNode));
				} else {
					// Two dummy node required.
					PNodeAbstractImpl pDummyNode = new PDummyNode();
					PNodeAbstractImpl pDummyNode2 = new PDummyNode();
					pJoiningNode.addChild(pDummyNode);
					pJoiningNode.addChild(pDummyNode2);
					pLineLayer.addChild(new PJoinLineNode(edge, pJoiningNode,	pDummyNode));
					pLineLayer.addChild(new PJoinLineNode(edge, pDummyNode,		pDummyNode2));
					pLineLayer.addChild(new PJoinLineNode(edge, pDummyNode2,	pJoinNode));
				}
			}
		}
	}

	private void addNode(Node node) {

	}

	private void removeNode(Node node) {

	}

	private void addTransition(Transition transition) {

	}

	private void removeTransition(Transition transition) {

	}

	private class ActivityDiagramEventListener implements EventListener {
		public void eventOccurred(Event e) {
			if (e instanceof ActivityDiagramEvent) {
				ActivityDiagramEvent ade = (ActivityDiagramEvent) e;
				switch(ade.getStatus()) {
				case NODE_ADDED:
					addNode(ade.getAffectedNode());
					break;
				case NODE_REMOVED:
					removeNode(ade.getAffectedNode());
					break;
				case TRANSITION_ADDED:
					addTransition(ade.getAffectedTransition());
					break;
				case TRANSITION_REMOVED:
					removeTransition(ade.getAffectedTransition());
					break;
				}
			}
		}
	}
}
