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

import java.awt.BasicStroke;
import java.awt.Color;
import java.util.Deque;
import java.util.LinkedList;

import jp.digitalmuseum.mr.activity.Node;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.nodes.PPath;

public abstract class PNodeAbstractImpl extends PPath {
	private static final long serialVersionUID = 3592199380497357141L;
	private int depth = 0;
	private Node node;
	private Deque<PNodeAbstractImpl> children;
	public double y;

	public PNodeAbstractImpl(Node node) {
		this.node = node;
		setPaint(Color.white);
		setStrokePaint(Color.black);
		children = new LinkedList<PNodeAbstractImpl>();
		y = 0;
	}

	void setAsInitialNode() {
		setStroke(new BasicStroke(2f));
	}

	void setDepth(int depth) {
		this.depth = depth;
	}

	public int getDepth() {
		return depth;
	}

	public Node getNode() {
		return node;
	}

	public void onEnter() {
		setStrokePaint(Color.red);
		repaint();
	}

	public void onLeave() {
		setStrokePaint(Color.black);
		repaint();
	}

	Deque<PNodeAbstractImpl> getUnmanagedChildrenReference() {
		return children;
	}

	@Override
	public void addChild(int i, PNode pNode) {
		super.addChild(i, pNode);
		if (pNode instanceof PNodeAbstractImpl) {
			children.add((PNodeAbstractImpl) pNode);
		}
	}

	@Override
	public PNode removeChild(int i) {
		PNode removedPNode = super.removeChild(i);
		if (removedPNode != null &&
				removedPNode instanceof PNodeAbstractImpl) {
			children.remove(removedPNode);
		}
		return removedPNode;
	}
}
