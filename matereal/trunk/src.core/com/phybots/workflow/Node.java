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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.phybots.message.Event;
import com.phybots.message.EventListener;
import com.phybots.message.EventProvider;
import com.phybots.message.WorkflowNodeEvent;
import com.phybots.message.WorkflowNodeStatus;
import com.phybots.utils.Array;


public abstract class Node implements EventProvider, Serializable {
	private static final long serialVersionUID = -5556889510147602017L;
	private long entranceDate;
	private Workflow workflow;
	private Set<Transition> transitions;
	protected transient Array<EventListener> listeners;

	public Node() {
		transitions = new HashSet<Transition>();
		listeners = new Array<EventListener>();
	}

	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
		ois.defaultReadObject();
		listeners = new Array<EventListener>();
	}

	public final boolean isEntered() {
		return workflow.isEntered(this);
	}

	public final long getEntranceDate() {
		return entranceDate;
	}

	public final long getAliveTime() {
		return Calendar.getInstance().getTimeInMillis() - entranceDate;
	}

	public final void addEventListener(EventListener listener) {
		listeners.push(listener);
	}

	public final boolean removeEventListener(EventListener listener) {
		return listeners.remove(listener);
	}

	protected final void distributeEvent(Event e) {
		for (EventListener listener : listeners) {
			listener.eventOccurred(e);
		}
	}

	protected boolean isAllowedEntry() {
		return true;
	}

	protected void onEnter() {
		//
	}

	protected abstract boolean isDone();

	protected void onLeave() {
		//
	}

	void setWorkflow(Workflow workflow) {
		this.workflow = workflow;
	}

	Workflow getWorkflow() {
		return workflow;
	}

	Set<Transition> getTransitionsReference() {
		return transitions;
	}

	public Set<Transition> getTransitions() {
		Set<Transition> transitions = new HashSet<Transition>();
		getTransitionsOut(transitions);
		return transitions;
	}

	public void getTransitionsOut(Set<Transition> transitions) {
		transitions.clear();
		transitions.addAll(this.transitions);
	}

	void clearTransitions() {
		for (Transition t : transitions) {
			workflow.removeTransition(t);
		}
		transitions.clear();
	}

	void addTransition(Transition transition) {
		transitions.add(transition);
	}

	boolean removeTransition(Transition transition) {
		if (transitions.remove(transition)) {
			workflow.removeTransition(transition);
			return true;
		}
		return false;
	}

	void removeRelatedTransitions(Node node) {
		Iterator<Transition> it;
		for (it = transitions.iterator(); it.hasNext();) {
			Transition t = it.next();
			if (t.getDestination() == node) {
				workflow.removeTransition(t);
				it.remove();
			}
		}
	}

	final void enter() {
		entranceDate = Calendar.getInstance().getTimeInMillis();
		distributeEvent(
				new WorkflowNodeEvent(this, WorkflowNodeStatus.ENTERED));
		onEnter();
	}

	final void leave() {
		distributeEvent(
				new WorkflowNodeEvent(this, WorkflowNodeStatus.LEFT));
		onLeave();
	}
}
