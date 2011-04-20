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

import jp.digitalmuseum.mr.activity.ActivityDiagram;
import jp.digitalmuseum.mr.activity.Node;
import jp.digitalmuseum.mr.gui.DisposableComponent;
import jp.digitalmuseum.mr.gui.activity.layout.SugiyamaLayout;
import jp.digitalmuseum.mr.message.ActivityDiagramEvent;
import jp.digitalmuseum.mr.message.ActivityEvent;
import jp.digitalmuseum.mr.message.Event;
import jp.digitalmuseum.mr.message.EventListener;
import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PCanvas;
import edu.umd.cs.piccolo.PLayer;
import edu.umd.cs.piccolo.activities.PTransformActivity;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.nodes.PText;

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

	private SugiyamaLayout layout;

	private PLayer pNodeLayer;
	private PLayer pLineLayer;

	private PPath sticky;
	private PText stickyText;

	private PTransformActivity cameraActivity;

	public ActivityDiagramCanvas(final ActivityDiagram ad) {
		this.ad = ad;

		layout = new SugiyamaLayout();

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

		synchronized (ad) {
			if (ad.getInitialNode() != null) {
				initialize();
			}
			adel = new ActivityDiagramEventListener();
			ad.addEventListener(adel);
			ael = new ActivityEventListener();
			for (Node node : ad.getNodes()) {
				node.addEventListener(ael);
				if (node.isEntered()) {
					onEnter(node);
				}
			}
		}
	}

	public void animateViewToCenterDiagram() {
		if (cameraActivity != null) {
			cameraActivity.terminate();
		}
		// TODO implement this.
		/*
		PBounds pBounds = initialVertex.getPiccoloNode().getGlobalFullBounds();
		pBounds.x -= 10;
		pBounds.y -= 10;
		pBounds.width += 20;
		pBounds.height += 20;
		cameraActivity = getCamera().animateViewToCenterBounds(pBounds, true, 500);
		 */
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
		pNodeLayer.removeAllChildren();
		pLineLayer.removeAllChildren();

		// Construct a new graph.
		layout.doLayout(ad.getInitialNode());

		// initialPNode = nodeMap.get(ad.getInitialNode());
		// initialPNode.setAsInitialNode();
	}

	private void onEnter(Node node) {
		// pNodeAbstractImpl.onEnter();
	}

	private void onLeave(Node node) {
		// pNodeAbstractImpl.onLeave();
	}

	private class ActivityDiagramEventListener implements EventListener {
		public void eventOccurred(Event e) {
			if (e instanceof ActivityDiagramEvent) {
				/*
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
				*/
				initialize();
			}
		}
	}

	private class ActivityEventListener implements EventListener {
		public void eventOccurred(Event e) {
			if (e instanceof ActivityEvent) {
				ActivityEvent ae = (ActivityEvent) e;
				switch (ae.getStatus()) {
				case ENTERED:
					onEnter(ae.getSource());
					break;
				case LEFT:
					onLeave(ae.getSource());
					break;
				}
			}
		}
	}

}
