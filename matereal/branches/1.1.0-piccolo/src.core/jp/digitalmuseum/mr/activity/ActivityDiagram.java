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

import java.util.HashSet;
import java.util.Set;

import edu.umd.cs.piccolox.swing.PCacheCanvas;

import jp.digitalmuseum.mr.message.ActivityDiagramEvent;
import jp.digitalmuseum.mr.message.Event;
import jp.digitalmuseum.mr.message.EventListener;
import jp.digitalmuseum.mr.message.ActivityDiagramEvent.STATUS;
import jp.digitalmuseum.mr.service.ServiceAbstractImpl;
import jp.digitalmuseum.utils.Array;

public class ActivityDiagram extends Node {
	private Node initialNode;
	private Set<Node> nodes;
	private Set<Transition> transitions;
	private Array<Node> currentNodes;
	private TransitionMonitor monitor;
	private Array<ActivityDiagramEditor> panels;
	private boolean compiled;

	public ActivityDiagram() {
		nodes = new HashSet<Node>();
		transitions = new HashSet<Transition>();
		currentNodes = new Array<Node>();
		monitor = new TransitionMonitor();
		panels = new Array<ActivityDiagramEditor>();
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
		node.setActivityDiagram(this);
		compiled = false;
		distributeEvent(new ActivityDiagramEvent(this, STATUS.NODE_ADDED, node));
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
			node.setActivityDiagram(null);
			removeRelatedTransitions(node);
			compiled = false;
			repaintViewers();
			distributeEvent(new ActivityDiagramEvent(this, STATUS.NODE_REMOVED, node));
			return true;
		}
		return false;
	}

	public void addTransition(Transition transition) {
		Node source = transition.getSource();
		source.addTransition(transition);
		transitions.add(transition);
		compiled = false;
		distributeEvent(new ActivityDiagramEvent(this, STATUS.TRANSITION_ADDED, transition));
	}

	public boolean removeTransition(Transition transition) {
		Node source = transition.getSource();
		if (source.removeTransition(transition)) {
			transitions.remove(transition);
			compiled = false;
			distributeEvent(new ActivityDiagramEvent(this, STATUS.TRANSITION_ADDED, transition));
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

	public ActivityDiagramEditor newActivityDiagramEditor() {
		return new ActivityDiagramEditor(this);
	}

	void repaintViewers() {
		for (ActivityDiagramEditor viewer : panels) {
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

	public static class ActivityDiagramEditor extends PCacheCanvas {
		private static final long serialVersionUID = 1L;
		private ActivityDiagram ad;

		public ActivityDiagramEditor(ActivityDiagram ad) {
			this.ad = ad;
			ad.addEventListener(new ActivityDiagramListener());
		}

		private class ActivityDiagramListener implements EventListener {
			public ActivityDiagramListener() {

			}

			public void eventOccurred(Event e) {
				if (e.getSource() == ad &&
						e instanceof ActivityDiagramEvent) {
					final ActivityDiagramEvent ade = (ActivityDiagramEvent) e;
					switch (ade.getStatus()) {
						case NODE_ADDED:
						case NODE_REMOVED:
						case TRANSITION_ADDED:
						case TRANSITION_REMOVED:
						default:
							break;
					}
				}
			}
		}
	}
}
