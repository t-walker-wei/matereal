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
package jp.digitalmuseum.mr.gui;

import jp.digitalmuseum.mr.Matereal;
import jp.digitalmuseum.mr.activity.ActivityDiagram;
import jp.digitalmuseum.mr.gui.activity.ActivityDiagramPane;
import jp.digitalmuseum.mr.message.ActivityDiagramEvent;
import jp.digitalmuseum.mr.message.ActivityDiagramEvent.STATUS;
import jp.digitalmuseum.mr.message.Event;
import jp.digitalmuseum.mr.message.EventListener;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.SwingUtilities;

import java.awt.GridBagLayout;
import javax.swing.JLabel;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.JScrollPane;

import java.awt.Font;
import java.util.HashMap;
import java.util.Map;

import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeSelectionModel;

import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.Rectangle;
import javax.swing.border.SoftBevelBorder;
import java.awt.CardLayout;
import javax.swing.JSplitPane;
import javax.swing.JButton;

/**
 * Monitor panel for entities.
 *
 * @author Jun KATO
 */
public class GraphMonitorPanel extends JPanel implements EventListener, TreeSelectionListener, Runnable, DisposableComponent {

	private static final long serialVersionUID = 3317150753032501439L;

	private JSplitPane jSplitPane = null;

	private JPanel jLeftPanel = null;
	private JPanel jRightViewPanel = null;

	private JScrollPane jScrollPane = null;
	private JTree jTree = null;

	private JLabel jSelectedGraphLabel = null;
	private JPanel graphPanel = null;

	/** Root node for jTree. */
	private DefaultMutableTreeNode root;

	/** Map of entities and their corresponding nodes. */
	private transient Map<ActivityDiagram, DefaultMutableTreeNode> graphNodeMap;

	private transient Map<ActivityDiagram, ActivityDiagramPane> graphComponents;

	private JPanel jRightPanel = null;

	private JButton instantiateButton = null;

	private JButton disposeButton = null;

	/** Singleton constructor. */
	public GraphMonitorPanel() {
		super();

		// Initialize hash maps.
		graphNodeMap = new HashMap<ActivityDiagram, DefaultMutableTreeNode>();
		graphComponents = new HashMap<ActivityDiagram, ActivityDiagramPane>();

		// Root node of the tree view, used at getJTree() etc.
		final Matereal matereal = Matereal.getInstance();
		root = new DefaultMutableTreeNode(matereal);

		// Initialize the monitor pane.
		initialize();
		Matereal.getInstance().addEventListener(this);
		for (ActivityDiagram graph : Matereal.getInstance().getGraphs()) {
			addGraph(graph);
		}
		selectGraph(null);
	}

	/**
	 * This method initializes this
	 *
	 * @return void
	 */
	private void initialize() {
		GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
		gridBagConstraints11.fill = GridBagConstraints.BOTH;
		gridBagConstraints11.weighty = 1.0;
		gridBagConstraints11.weightx = 1.0;
		setPreferredSize(new Dimension(640, 420));
		setLayout(new GridBagLayout());
		setBounds(new Rectangle(0, 0, 480, 320));
		jSelectedGraphLabel = new JLabel();
		jSelectedGraphLabel.setText(Messages.getString("GraphMonitorPanel.selectedGraph")); //$NON-NLS-1$
		jSelectedGraphLabel.setFont(Matereal.getInstance().getDefaultFont().deriveFont(Font.BOLD, 14));
		jSelectedGraphLabel.setToolTipText(Messages.getString("GraphMonitorPanel.nameOfSelectedGraph")); //$NON-NLS-1$
		this.add(getJSplitPane(), gridBagConstraints11);
	}

	public void dispose() {
		Matereal.getInstance().removeEventListener(this);
		for (JComponent serviceComponent : graphComponents.values()) {
			if (serviceComponent != null) {
				if (serviceComponent instanceof DisposableComponent) {
					((DisposableComponent) serviceComponent).dispose();
				}
			}
		}
		graphComponents.clear();
	}

	/**
	 * This method initializes jSplitPane
	 *
	 * @return javax.swing.JSplitPane
	 */
	private JSplitPane getJSplitPane() {
		if (jSplitPane == null) {
			jSplitPane = new JSplitPane();
			jSplitPane.setLeftComponent(getJLeftPanel());
			jSplitPane.setRightComponent(getJRightPanel());
		}
		return jSplitPane;
	}

	/**
	 * This method initializes jLeftPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJLeftPanel() {
		if (jLeftPanel == null) {
			GridBagConstraints gridBagConstraints12 = new GridBagConstraints();
			gridBagConstraints12.gridx = 1;
			gridBagConstraints12.anchor = GridBagConstraints.WEST;
			gridBagConstraints12.weightx = 1.0D;
			gridBagConstraints12.fill = GridBagConstraints.VERTICAL;
			gridBagConstraints12.insets = new Insets(0, 0, 5, 5);
			gridBagConstraints12.gridy = 1;
			GridBagConstraints gridBagConstraints9 = new GridBagConstraints();
			gridBagConstraints9.gridx = 0;
			gridBagConstraints9.anchor = GridBagConstraints.WEST;
			gridBagConstraints9.insets = new Insets(0, 5, 5, 3);
			gridBagConstraints9.fill = GridBagConstraints.VERTICAL;
			gridBagConstraints9.weightx = 0.0D;
			gridBagConstraints9.gridy = 1;
			GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
			gridBagConstraints6.fill = GridBagConstraints.BOTH;
			gridBagConstraints6.weighty = 1.0;
			gridBagConstraints6.insets = new Insets(5, 5, 5, 5);
			gridBagConstraints6.gridwidth = 2;
			gridBagConstraints6.gridx = 0;
			gridBagConstraints6.gridy = 0;
			gridBagConstraints6.weightx = 1.0;
			jLeftPanel = new JPanel();
			jLeftPanel.setLayout(new GridBagLayout());
			jLeftPanel.add(getJScrollPane(), gridBagConstraints6);
			jLeftPanel.add(getInstantiateButton(), gridBagConstraints9);
			jLeftPanel.add(getDisposeButton(), gridBagConstraints12);
		}
		return jLeftPanel;
	}

	/**
	 * This method initializes jRightViewPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJRightViewPanel() {
		if (jRightViewPanel == null) {
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			gridBagConstraints2.gridx = 0;
			gridBagConstraints2.gridy = 1;
			gridBagConstraints2.fill = GridBagConstraints.BOTH;
			gridBagConstraints2.weightx = 1.0D;
			gridBagConstraints2.weighty = 0.0D;
			gridBagConstraints2.insets = new Insets(5, 5, 5, 5);
			GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
			gridBagConstraints4.gridx = 0;
			gridBagConstraints4.gridy = 3;
			gridBagConstraints4.fill = GridBagConstraints.BOTH;
			gridBagConstraints4.weightx = 1.0D;
			gridBagConstraints4.weighty = 1.0D;
			gridBagConstraints4.insets = new Insets(0, 5, 5, 5);
			jRightViewPanel = new JPanel();
			jRightViewPanel.setLayout(new GridBagLayout());
			jRightViewPanel.setPreferredSize(new Dimension(320, 420));
			jRightViewPanel.setName("jRightViewPanel");
			jRightViewPanel.add(jSelectedGraphLabel, gridBagConstraints2);
			jRightViewPanel.add(getGraphPanel(), gridBagConstraints4);
		}
		return jRightViewPanel;
	}

	/**
	 * This method initializes jScrollPane
	 *
	 * @return javax.swing.JScrollPane
	 */
	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setBorder(null);
			jScrollPane.setComponentOrientation(ComponentOrientation.UNKNOWN);
			jScrollPane.setPreferredSize(new Dimension(200, 420));
			jScrollPane.setViewportView(getJTree());
		}
		return jScrollPane;
	}

	/**
	 * This method initializes jTree
	 *
	 * @return javax.swing.JTree
	 */
	private JTree getJTree() {
		if (jTree == null) {
			jTree = new JTree(root);
			jTree.setSize(new Dimension(120, 420));
			jTree.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
			jTree.setFont(Matereal.getInstance().getDefaultFont().deriveFont(12));
			jTree.getSelectionModel().setSelectionMode(
					TreeSelectionModel.SINGLE_TREE_SELECTION);
			jTree.addTreeSelectionListener(this);
		}
		return jTree;
	}

	/**
	 * This method initializes graphPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getGraphPanel() {
		if (graphPanel == null) {
			graphPanel = new JPanel();
			graphPanel.setPreferredSize(new Dimension(400, 420));
			graphPanel.setLayout(new CardLayout());
			graphPanel.setBorder(new SoftBevelBorder(SoftBevelBorder.LOWERED));
		}
		return graphPanel;
	}

	public void run() {
		((DefaultTreeModel) getJTree().getModel()).reload();
	}

	public void valueChanged(TreeSelectionEvent e) {
		final JTree tree = getJTree();
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)
				tree.getLastSelectedPathComponent();
		if (node == null) {
			return;
		}

		Object nodeInfo = node.getUserObject();
		if (nodeInfo instanceof ActivityDiagram) {
			selectGraph((ActivityDiagram) nodeInfo);
		}
	}

	public void eventOccurred(Event e) {
		if (e instanceof ActivityDiagramEvent) {
			ActivityDiagramEvent ade = (ActivityDiagramEvent) e;
			if (ade.getStatus() == STATUS.INSTANTIATED ||
					ade.getStatus() == STATUS.DISPOSED) {
				ActivityDiagram entity = ade.getSource();

				if (ade.getStatus() == STATUS.INSTANTIATED) {
					addGraph(entity);
				} else {
					removeGraph(entity);
				}

				SwingUtilities.invokeLater(this);
			}
		}
	}

	private void selectGraph(ActivityDiagram graph) {
		if (graph == null) {
			jSelectedGraphLabel.setText(""); //$NON-NLS-1$
			return;
		}
		if (!graphComponents.containsKey(graph)) {
			ActivityDiagramPane graphComponent = graph.getConfigurationComponent();
			if (graphComponent != null) {
				getGraphPanel().add(graphComponent, String.valueOf(graph.hashCode()));
				getGraphPanel().validate();
				graphComponents.put(graph, graphComponent);
			}
		}
		((CardLayout) getGraphPanel().getLayout()).show(
				getGraphPanel(), String.valueOf(graph.hashCode()));
		jSelectedGraphLabel.setText(graph.getName());
	}

	private void addGraph(ActivityDiagram graph) {
		if (graphNodeMap.containsKey(graph)) {
			return;
		}

		final DefaultMutableTreeNode node =
				new DefaultMutableTreeNode(graph);
		root.add(node);
		graphNodeMap.put(graph, node);
	}

	private void removeGraph(ActivityDiagram graph) {

		// Remove from the list view and graphNodeMap.
		MutableTreeNode node;
		node = root;
		node.remove(
				graphNodeMap.remove(graph));

		if (graphComponents.containsKey(graph)) {
			JComponent graphComponent = graphComponents.get(graph);
			getGraphPanel().remove(graphComponent);
			if (graphComponent instanceof DisposableComponent) {
				((DisposableComponent) graphComponent).dispose();
			}
			graphComponents.remove(graph);
		}
	}

	/**
	 * This method initializes jRightPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJRightPanel() {
		if (jRightPanel == null) {
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = -1;
			gridBagConstraints.gridy = -1;
			jRightPanel = new JPanel();
			jRightPanel.setLayout(new CardLayout());
			jRightPanel.add(getJRightViewPanel(), getJRightViewPanel().getName());
		}
		return jRightPanel;
	}

	/**
	 * This method initializes disposeButton
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getDisposeButton() {
		if (disposeButton == null) {
			disposeButton = new JButton();
			disposeButton.setText("-");
		}
		return disposeButton;
	}

	/**
	 * This method initializes instantiateButton
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getInstantiateButton() {
		if (instantiateButton == null) {
			instantiateButton = new JButton();
			instantiateButton.setText("+");
		}
		return instantiateButton;
	}
}
