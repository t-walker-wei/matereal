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

import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import jp.digitalmuseum.mr.message.ActivityEvent;
import jp.digitalmuseum.mr.message.Event;
import jp.digitalmuseum.mr.message.EventListener;
import jp.digitalmuseum.mr.message.EventProvider;
import jp.digitalmuseum.mr.message.ActivityEvent.STATUS;
import jp.digitalmuseum.utils.Array;

public class Node implements EventProvider {
	private long entranceDate;
	private ActivityDiagram activityDiagram;
	private Set<Transition> transitions;
	private Array<EventListener> listeners;
	private boolean done;

	public Node() {
		listeners = new Array<EventListener>();
		transitions = new HashSet<Transition>();
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

	protected final void setDone() {
		done = true;
		distributeEvent(new ActivityEvent(this, STATUS.DONE));
	}

	protected boolean isAllowedEntry() {
		return true;
	}

	protected void onEnter() {
		//
	}

	protected void onLeave() {
		//
	}

	void setActivityDiagram(ActivityDiagram activityDiagram) {
		this.activityDiagram = activityDiagram;
	}

	ActivityDiagram getActivityDiagram() {
		return activityDiagram;
	}

	boolean isDone() {
		return done;
	}

	Set<Transition> getTransitions() {
		return transitions;
	}

	void clearTransitions() {
		for (Transition t : transitions) {
			activityDiagram.getGraph().removeEdge(t);
		}
		transitions.clear();
	}

	void addTransition(Transition transition) {
		transitions.add(transition);
	}

	boolean removeTransition(Transition transition) {
		if (transitions.remove(transition)) {
			activityDiagram.getGraph().removeEdge(transition);
			return true;
		}
		return false;
	}

	void removeRelatedTransitions(Node node) {
		Iterator<Transition> it;
		for (it = transitions.iterator(); it.hasNext();) {
			Transition t = it.next();
			if (t.getSource() == node ||
					t.getDestination() == node) {
				activityDiagram.getGraph().removeEdge(t);
				it.remove();
			}
		}
	}

	final void enter() {
		entranceDate = Calendar.getInstance().getTimeInMillis();
		done = false;
		distributeEvent(
				new ActivityEvent(this, STATUS.ENTERED));
		onEnter();
	}

	final void leave() {
		distributeEvent(
				new ActivityEvent(this, STATUS.LEFT));
		onLeave();
	}
}
