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

import javax.swing.JComponent;

import jp.digitalmuseum.mr.Matereal;
import jp.digitalmuseum.mr.gui.activity.ActivityDiagramPane;
import jp.digitalmuseum.mr.message.ActivityDiagramEvent;
import jp.digitalmuseum.mr.message.ActivityDiagramEvent.STATUS;
import jp.digitalmuseum.mr.service.ServiceAbstractImpl;
import jp.digitalmuseum.utils.Array;

public class ActivityDiagram extends Node {
	private static final long serialVersionUID = -5950299182178722307L;
	private static int instances;
	private Node initialNode;
	private Set<Node> nodes;
	private Set<Transition> transitions;
	private String name;
	private transient Array<Node> currentNodes;
	private transient TransitionMonitor monitor;
	private transient boolean isStarted;
	private transient boolean isDone;
	private transient boolean isPaused;
	private transient boolean isDisposed;
	private transient ResourceContext resourceContext;

	public ActivityDiagram() {
		this(null);
	}

	public ActivityDiagram(ResourceContext resourceContext) {
		nodes = new HashSet<Node>();
		transitions = new HashSet<Transition>();
		currentNodes = new Array<Node>();
		monitor = new TransitionMonitor();
		isStarted = false;
		isPaused = false;
		isDone = false;
		if (resourceContext == null) {
			this.resourceContext = null;
		} else {
			this.resourceContext = resourceContext;
		}
		instances ++;
		this.name = "Activity diagram (" + instances + ")";
		Matereal.getInstance().registerGraph(this);
		distributeEvent(new ActivityDiagramEvent(this, STATUS.INSTANTIATED));
		isDisposed = false;
	}

	public synchronized void dispose() {
		stop();
		Matereal.getInstance().unregisterGraph(this);
		distributeEvent(new ActivityDiagramEvent(this, STATUS.DISPOSED));
		isDisposed = true;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return getName();
	}

	synchronized ResourceContext getResourceContext() {
		return resourceContext;
	}

	public synchronized void setInitialNode(Node node) {
		if (isStarted()) {
			throw new IllegalStateException("Initial node of the activity diagram cannot be set during its runtime.");
		}
		if (!nodes.contains(node)) {
			add(node);
		}
		this.initialNode = node;
		distributeEvent(new ActivityDiagramEvent(this, STATUS.INITIAL_NODE_SET, node));
	}

	public synchronized Node getInitialNode() {
		return initialNode;
	}

	public synchronized void add(Node node) {
		nodes.add(node);
		node.setActivityDiagram(this);
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
				stop(node);
			}
			node.setActivityDiagram(null);
			removeRelatedTransitions(node);
			distributeEvent(new ActivityDiagramEvent(this, STATUS.NODE_REMOVED, node));
			return true;
		}
		return false;
	}

	public synchronized Set<Node> getNodes() {
		return new HashSet<Node>(nodes);
	}

	public synchronized void getNodesOut(Set<Node> nodes) {
		nodes.clear();
		nodes.addAll(nodes);
	}

	public synchronized void addTransition(Transition transition) {
		Node source = transition.getSource();
		source.addTransition(transition);
		transitions.add(transition);
		distributeEvent(new ActivityDiagramEvent(this, STATUS.TRANSITION_ADDED, transition));
	}

	public synchronized boolean removeTransition(Transition transition) {
		Node source = transition.getSource();
		if (source.removeTransition(transition)) {
			transitions.remove(transition);
			distributeEvent(new ActivityDiagramEvent(this, STATUS.TRANSITION_ADDED, transition));
			return true;
		}
		return false;
	}

	public synchronized void removeRelatedTransitions(Node node) {
		for (Node n : nodes) {
			if (n == node) {
				n.clearTransitions();
			} else {
				n.removeRelatedTransitions(node);
			}
		}
	}

	public synchronized Set<Transition> getTransitions() {
		return new HashSet<Transition>(transitions);
	}

	public synchronized void getTransitionsOut(Set<Transition> transitions) {
		transitions.clear();
		transitions.addAll(this.transitions);
	}

	/**
	 * @throws IllegalStateException
	 */
	public synchronized void start() {
		if (isDisposed()) {
			throw new IllegalStateException("This diagram is already disposed and cannot be started.");
		}
		if (isStarted()) {
			return;
		}
		if (!start(initialNode)) {
			throw new IllegalStateException("Initial node was failed to start.");
		}
		monitor.start();
		enter();
		isStarted = true;
	}

	synchronized boolean start(Node node) {
		if (node == null
				|| !node.isAllowedEntry()
				|| currentNodes.contains(node)) {
			return false;
		}
		currentNodes.push(node);
		node.enter();
		return true;
	}

	public synchronized void stop() {
		if (isStarted()) {
			for (Node node : currentNodes) {
				node.leave();
			}
			currentNodes.clear();
			monitor.stop();
			isStarted = false;
			isPaused = false;
			leave();
		}
	}

	void stop(Node node) {
		if (currentNodes.remove(node)) {
			node.leave();
		}
	}

	public synchronized void pause() {
		if (!isStarted() || isPaused()) {
			return;
		}
		monitor.pause();
		for (Node node : currentNodes) {
			if (node instanceof Action) {
				((Action) node).getTask().pause();
			}
		}
		isPaused = true;
	}

	public synchronized void resume() {
		for (Node node : currentNodes) {
			if (node instanceof Action) {
				((Action) node).getTask().resume();
			}
		}
		monitor.resume();
		isPaused = false;
	}

	public synchronized boolean isDisposed() {
		return isDisposed;
	}

	public synchronized boolean isStarted() {
		return isStarted;
	}

	public synchronized boolean isPaused() {
		return isPaused;
	}

	protected synchronized boolean isDone() {
		return isDone;
	}

	private class TransitionMonitor extends ServiceAbstractImpl {
		private static final long serialVersionUID = -2426162725934324758L;

		@Override
		public String getName() {
			return "Activity Diagram";
		}

		public void run() {
			synchronized (ActivityDiagram.this) {
				isDone = true;
				for (Node node : currentNodes) {
					Set<Transition> transitions = node.getTransitionsReference();
					if (transitions.size() > 0) {
						for (Transition transition : transitions) {
							if (transition.guard()) {
								ActivityDiagram.this.stop(node);
								if (!currentNodes.contains(transition
										.getDestination())) {
									ActivityDiagram.this.start(
											transition.getDestination());
								}
								break;
							}
						}
					} else if (node.isDone()) {
						ActivityDiagram.this.stop(node);
					}
					isDone = isDone && node.isDone();
				}
				if (isDone) {
					ActivityDiagram.this.stop();
				}
			}
		}

		@Override
		public JComponent getConfigurationComponent() {
			return new ActivityDiagramPane(ActivityDiagram.this);
		}
	}

	boolean isEntered(Node node) {
		return currentNodes.contains(node);
	}
}
