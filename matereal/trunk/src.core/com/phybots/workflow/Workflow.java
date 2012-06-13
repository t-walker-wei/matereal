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
package com.phybots.workflow;

import java.util.HashSet;
import java.util.Set;

import javax.swing.JComponent;

import com.phybots.Phybots;
import com.phybots.gui.workflow.WorkflowViewPane;
import com.phybots.message.WorkflowEvent;
import com.phybots.message.WorkflowStatus;
import com.phybots.message.WorkflowUpdateEvent;
import com.phybots.message.WorkflowUpdateStatus;
import com.phybots.service.ServiceAbstractImpl;
import com.phybots.utils.Array;


public class Workflow extends Node {
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

	public Workflow() {
		this(null);
	}

	public Workflow(ResourceContext resourceContext) {
		nodes = new HashSet<Node>();
		transitions = new HashSet<Transition>();
		currentNodes = new Array<Node>();
		isStarted = false;
		isPaused = false;
		isDone = false;
		if (resourceContext == null) {
			this.resourceContext = null;
		} else {
			this.resourceContext = resourceContext;
		}
		instances ++;
		this.name = "Workflow graph (" + instances + ")";
		monitor = new TransitionMonitor();
		Phybots.getInstance().registerWorkflow(this);
		distributeEvent(new WorkflowEvent(this, WorkflowStatus.INSTANTIATED));
		isDisposed = false;
	}

	public synchronized void dispose() {
		if (isStarted()) {
			stop();
		}
		if (!isDisposed() && !Phybots.getInstance().isDisposing()) {
			distributeEvent(new WorkflowEvent(this, WorkflowStatus.DISPOSED));
			Phybots.getInstance().unregisterWorkflow(this);
			clear();
			listeners.clear();
		}
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
			throw new IllegalStateException("Initial node of the workflow graph cannot be set during its runtime.");
		}
		if (!nodes.contains(node)) {
			add(node);
		}
		this.initialNode = node;
		distributeEvent(new WorkflowUpdateEvent(this, WorkflowUpdateStatus.INITIAL_NODE_SET, node));
	}

	public synchronized Node getInitialNode() {
		return initialNode;
	}

	public synchronized void add(Node node) {
		nodes.add(node);
		node.setWorkflow(this);
		distributeEvent(new WorkflowUpdateEvent(this, WorkflowUpdateStatus.NODE_ADDED, node));
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
			node.setWorkflow(null);
			removeRelatedTransitions(node);
			distributeEvent(new WorkflowUpdateEvent(this, WorkflowUpdateStatus.NODE_REMOVED, node));
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
		distributeEvent(new WorkflowUpdateEvent(this, WorkflowUpdateStatus.TRANSITION_ADDED, transition));
	}

	public synchronized boolean removeTransition(Transition transition) {
		Node source = transition.getSource();
		if (source.removeTransition(transition)) {
			transitions.remove(transition);
			distributeEvent(new WorkflowUpdateEvent(this, WorkflowUpdateStatus.TRANSITION_ADDED, transition));
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

	public synchronized void clear() {
		for (Transition transition : getTransitions()) {
			removeTransition(transition);
		}
		for (Node node : getNodes()) {
			remove(node);
		}
	}

	/**
	 * @throws IllegalStateException
	 */
	public synchronized void start() {
		if (isDisposed()) {
			throw new IllegalStateException("This graph is already disposed and cannot be started.");
		}
		if (isStarted()) {
			return;
		}
		if (!start(initialNode)) {
			throw new IllegalStateException("Initial node was failed to start.");
		}
		monitor.start();
		isStarted = true;

		// Distribute this event.
		enter();
		distributeEvent(new WorkflowEvent(this, WorkflowStatus.STARTED));
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

			// Distribute this event.
			leave();
			distributeEvent(new WorkflowEvent(this, WorkflowStatus.STOPPED));
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
		distributeEvent(new WorkflowEvent(this, WorkflowStatus.PAUSED));
	}

	public synchronized void resume() {
		for (Node node : currentNodes) {
			if (node instanceof Action) {
				((Action) node).getTask().resume();
			}
		}
		monitor.resume();
		isPaused = false;
		distributeEvent(new WorkflowEvent(this, WorkflowStatus.RESUMED));
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

	@Override
	protected boolean isAllowedEntry() {
		return !isStarted() && !isDisposed();
	}

	protected synchronized boolean isDone() {
		return isDone;
	}

	@Override
	protected void onEnter() {
		start();
	}

	private class TransitionMonitor extends ServiceAbstractImpl {
		private static final long serialVersionUID = -2426162725934324758L;

		@Override
		public String getName() {
			StringBuilder sb = new StringBuilder();
			sb.append(Workflow.this.getName());
			sb.append(" transition monitor");
			return sb.toString();
		}

		public void run() {
			synchronized (Workflow.this) {
				isDone = true;
				for (Node node : currentNodes) {
					Set<Transition> transitions = node.getTransitionsReference();
					if (transitions.size() > 0) {
						for (Transition transition : transitions) {
							if (transition.guard()) {
								Workflow.this.stop(node);
								if (!currentNodes.contains(transition
										.getDestination())) {
									Workflow.this.start(
											transition.getDestination());
								}
								break;
							}
						}
					} else if (node.isDone()) {
						Workflow.this.stop(node);
					}
					isDone = isDone && node.isDone();
				}
				if (isDone) {
					Workflow.this.stop();
				}
			}
		}

		@Override
		public JComponent getConfigurationComponent() {
			return new WorkflowViewPane(Workflow.this);
		}
	}

	boolean isEntered(Node node) {
		return currentNodes.contains(node);
	}

	public WorkflowViewPane getConfigurationComponent() {
		return new WorkflowViewPane(this);
	}
}
