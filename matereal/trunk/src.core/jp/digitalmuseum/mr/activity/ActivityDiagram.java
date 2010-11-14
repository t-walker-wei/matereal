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
package jp.digitalmuseum.mr.activity;

import java.awt.Color;
import java.awt.Paint;
import java.awt.Shape;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.functors.ConstantTransformer;

import edu.uci.ics.jung.algorithms.layout.*;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.decorators.AbstractVertexShapeTransformer;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.picking.PickedInfo;
import edu.uci.ics.jung.visualization.renderers.Renderer;

import jp.digitalmuseum.mr.gui.DisposableComponent;
import jp.digitalmuseum.mr.service.ServiceAbstractImpl;
import jp.digitalmuseum.utils.Array;

public class ActivityDiagram extends Node {
	private DirectedGraph<Node, Edge> graph;
	private Node initialNode;
	private Set<Node> nodes;
	private Array<Node> currentNodes;
	private TransitionMonitor monitor;
	private Array<ActivityViewer> panels;
	private boolean compiled;

	public ActivityDiagram() {
		graph = new DirectedSparseGraph<Node, Edge>();
		nodes = new HashSet<Node>();
		currentNodes = new Array<Node>();
		monitor = new TransitionMonitor();
		panels = new Array<ActivityViewer>();
	}

	public void setInitialNode(Node node) {
		stop();
		this.initialNode = node;
	}

	public Node getInitialNode() {
		return initialNode;
	}

	public synchronized void add(Node node) {
		nodes.add(node);
		graph.addVertex(node);
		if (node instanceof ControlNode) {
			for (Edge edge : ((ControlNode) node).getEdges()) {
				graph.addEdge(edge, edge.getSource(), edge.getDestination());
			}
		}
		node.setActivityDiagram(this);
		compiled = false;
	}

	public synchronized void add(Node... nodes) {
		for (Node node : nodes) {
			add(node);
		}
	}

	public synchronized void addInSerial(Node[] nodes) {
		Node b = null;
		for (Node a : nodes) {
			if (b != null) {
				addTransition(new Transition(b, a));
			}
			add(a);
			b = a;
		}
	}

	public synchronized boolean remove(Node node) {
		if (node != null && nodes.remove(node)) {
			if (currentNodes.contains(node)) {
				stop(node, false);
			}
			if (node instanceof ControlNode) {
				for (Edge edge : ((ControlNode) node).getEdges()) {
					graph.removeEdge(edge);
				}
			}
			graph.removeVertex(node);
			node.setActivityDiagram(null);
			removeRelatedTransitions(node);
			compiled = false;
			repaintViewers();
			return true;
		}
		return false;
	}

	public void addTransition(Transition transition) {
		Node source = transition.getSource();
		source.addTransition(transition);
		graph.addEdge(transition, transition.getSource(), transition
				.getDestination());
		compiled = false;
	}

	public boolean removeTransition(Transition transition) {
		Node source = transition.getSource();
		graph.removeEdge(transition);
		if (source.removeTransition(transition)) {
			compiled = false;
			return true;
		}
		return false;
	}

	public void removeRelatedTransitions(Node node) {
		for (Node n : nodes) {
			if (n == node) {
				n.clearTransitions();
			} else {
				n.removeRelatedTransitions(node);
			}
		}
	}

	/**
	 * @throws IllegalStateException
	 */
	public synchronized void start() throws IllegalStateException {
		if (currentNodes.size() > 0) {
			throw new IllegalStateException(
					"This activity diagram has been already started.");
		}
		if (!start(initialNode)) {
			throw new IllegalStateException("Initial node was failed to start.");
		}
		monitor.start();
		enter();
	}

	public synchronized boolean start(Node node) {
		return start(node, true);
	}

	public synchronized boolean start(Node node, boolean repaint) {
		if (!nodes.contains(node)) {
			throw new IllegalStateException("Specified node (" + node
					+ ") is not contained in the activity chart.");
		}
		if (!node.isAllowedEntry()) {
			return false;
		}
		currentNodes.push(node);
		node.enter();
		if (repaint) {
			repaintViewers();
		}
		return true;
	}

	public synchronized void stop() {
		if (monitor.isStarted()) {
			for (Node node : currentNodes) {
				node.leave();
			}
			currentNodes.clear();
			monitor.stop();
			leave();
			repaintViewers();
		}
	}

	public synchronized void stop(Node node) {
		stop(node, true);
	}

	synchronized void stop(Node node, boolean repaint) {
		if (currentNodes.remove(node)) {
			node.leave();
		}
		if (repaint) {
			repaintViewers();
		}
	}

	public void compile() {
		if (compiled) {
			return;
		}

		// Consistency check

		compiled = true;
		return;
	}

	DirectedGraph<Node, Edge> getGraph() {
		return graph;
	}

	public ActivityViewer newActivityViewer() {
		Layout<Node, Edge> layout = new FRLayout2<Node, Edge>(graph);
		ActivityViewer panel = new ActivityViewer(this, layout);
		RenderContext<Node, Edge> context = panel.getRenderContext();
		Color edgeColor = new Color(200, 0, 0);
		Transformer<Edge, Paint> edgeTransformer =
				new ConstantTransformer(edgeColor);
		context.setEdgeDrawPaintTransformer(edgeTransformer);
		context.setArrowDrawPaintTransformer(edgeTransformer);
		context.setArrowFillPaintTransformer(edgeTransformer);
		context.setVertexFillPaintTransformer(
				new NodeFillColorSelector(this, panel.getPickedVertexState()));
		context.setVertexShapeTransformer(new NodeShapeSelector());
		DefaultModalGraphMouse<Node, Transition> gm =
				new DefaultModalGraphMouse<Node, Transition>();
		gm.setMode(DefaultModalGraphMouse.Mode.PICKING);
		gm.setZoomAtMouse(true);
		panel.setGraphMouse(gm);
		panel.getRenderContext().setVertexLabelTransformer(
				new ToStringLabeller<Node>());
		panel.getRenderer().getVertexLabelRenderer().setPosition(
				Renderer.VertexLabel.Position.E);
		panels.push(panel);
		return panel;
	}

	void repaintViewers() {
		for (ActivityViewer viewer : panels) {
			viewer.repaint();
		}
	}

	private class TransitionMonitor extends ServiceAbstractImpl {
		public void run() {
			boolean allIsDone = true;
			boolean repaint = false;
			for (Node node : currentNodes) {
				Set<Transition> transitions = node.getTransitions();
				if (transitions.size() > 0) {
					for (Transition transition : transitions) {
						if (transition.guard()) {
							ActivityDiagram.this.stop(node, false);
							if (!currentNodes.contains(transition
									.getDestination())) {
								ActivityDiagram.this.start(transition
										.getDestination(), false);
							}
							repaint = true;
							break;
						}
					}
				} else if (node.isDone()) {
					ActivityDiagram.this.stop(node);
					repaint = true;
				}
				allIsDone = allIsDone && node.isDone();
			}
			if (allIsDone) {
				ActivityDiagram.this.stop();
			} else if (repaint) {
				repaintViewers();
			}
		}
	}

	private static final class NodeFillColorSelector
			implements Transformer<Node, Paint> {
		private ActivityDiagram ad;
		protected PickedInfo<Node> pi;
		private static final Color defaultColor = Color.lightGray;
		private static final Color currentColor = new Color(200, 0, 0);
		private static final Color pickedColor = Color.gray;
		private static final Color pickedCurrentColor = Color.red;

		public NodeFillColorSelector(ActivityDiagram ad, PickedInfo<Node> pi) {
			this.ad = ad;
			this.pi = pi;
		}

		public Paint transform(Node v) {
			if (pi.isPicked(v)) {
				return ad.currentNodes.contains(v) ? pickedCurrentColor
						: pickedColor;
			} else {
				return ad.currentNodes.contains(v) ? currentColor
						: defaultColor;
			}
		}
	}

	private final static class NodeShapeSelector
			extends AbstractVertexShapeTransformer<Node>
			implements Transformer<Node, Shape> {
		public NodeShapeSelector() {
			setSizeTransformer(new Transformer<Node, Integer>() {
				public Integer transform(Node node) {
					return 20;
				}
			});
		}

		public Shape transform(Node node) {
			if (node instanceof ControlNode) {
				return factory.getRectangle(node);
			} else {
				return factory.getEllipse(node);
			}
		}
	}

	public static class ActivityViewer extends VisualizationViewer<Node, Edge>
			implements DisposableComponent {
		private static final long serialVersionUID = 4184918310080566434L;
		private ActivityDiagram ad;

		public ActivityViewer(ActivityDiagram ad, Layout<Node, Edge> layout) {
			super(layout);
			this.ad = ad;
		}

		public void dispose() {
			ad.panels.remove(this);
		}
	}
}
