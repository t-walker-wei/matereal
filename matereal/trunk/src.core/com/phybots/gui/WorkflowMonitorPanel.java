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
package com.phybots.gui;


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

import com.phybots.Phybots;
import com.phybots.gui.workflow.WorkflowViewPane;
import com.phybots.message.Event;
import com.phybots.message.EventListener;
import com.phybots.message.WorkflowEvent;
import com.phybots.message.WorkflowStatus;
import com.phybots.workflow.Workflow;

/**
 * Monitor panel for workflows.
 *
 * @author Jun Kato
 */
public class WorkflowMonitorPanel extends JPanel implements EventListener, TreeSelectionListener, Runnable, DisposableComponent {

	private static final long serialVersionUID = 3317150753032501439L;

	private JSplitPane jSplitPane = null;

	private JPanel leftPanel = null;

	private JScrollPane jScrollPane = null;
	private JTree jTree = null;
	private JButton instantiateButton = null;
	private JButton disposeButton = null;

	private JPanel rightPanel = null;

	private JPanel rightViewPanel = null;
	private JPanel selectedWorkflowPanel = null;
	private JLabel selectedWorkflowLabel = null;
	private JButton startWorkflowButton = null;
	private JButton stopWorkflowButton = null;
	private JPanel workflowPanel = null;

	/** Root node for jTree. */
	private DefaultMutableTreeNode root;

	/** Map of entities and their corresponding nodes. */
	private transient Map<Workflow, DefaultMutableTreeNode> workflowNodeMap;

	private transient Map<Workflow, WorkflowViewPane> workflowComponents;

	private transient Workflow selectedWorkflow;  //  @jve:decl-index=0:

	/** Singleton constructor. */
	public WorkflowMonitorPanel() {
		super();

		// Initialize hash maps.
		workflowNodeMap = new HashMap<Workflow, DefaultMutableTreeNode>();
		workflowComponents = new HashMap<Workflow, WorkflowViewPane>();

		// Root node of the tree view, used at getJTree() etc.
		final Phybots phybots = Phybots.getInstance();
		root = new DefaultMutableTreeNode(phybots);

		// Initialize the monitor pane.
		initialize();
		Phybots.getInstance().addEventListener(this);
		for (Workflow graph : Phybots.getInstance().getWorkflows()) {
			addGraph(graph);
		}
		showWorkflow(null);
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
		selectedWorkflowLabel = new JLabel();
		selectedWorkflowLabel.setText(Messages.getString("GraphMonitorPanel.selectedGraph")); //$NON-NLS-1$
		selectedWorkflowLabel.setFont(Phybots.getInstance().getDefaultFont().deriveFont(Font.BOLD, 14));
		selectedWorkflowLabel.setToolTipText(Messages.getString("GraphMonitorPanel.nameOfSelectedGraph")); //$NON-NLS-1$
		this.add(getJSplitPane(), gridBagConstraints11);
	}

	public void dispose() {
		Phybots.getInstance().removeEventListener(this);
		for (JComponent serviceComponent : workflowComponents.values()) {
			if (serviceComponent != null) {
				if (serviceComponent instanceof DisposableComponent) {
					((DisposableComponent) serviceComponent).dispose();
				}
			}
		}
		workflowComponents.clear();
	}

	/**
	 * This method initializes jSplitPane
	 *
	 * @return javax.swing.JSplitPane
	 */
	private JSplitPane getJSplitPane() {
		if (jSplitPane == null) {
			jSplitPane = new JSplitPane();
			jSplitPane.setLeftComponent(getLeftPanel());
			jSplitPane.setRightComponent(getRightPanel());
		}
		return jSplitPane;
	}

	/**
	 * This method initializes leftPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getLeftPanel() {
		if (leftPanel == null) {
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
			leftPanel = new JPanel();
			leftPanel.setLayout(new GridBagLayout());
			leftPanel.add(getJScrollPane(), gridBagConstraints6);
			leftPanel.add(getInstantiateButton(), gridBagConstraints9);
			leftPanel.add(getDisposeButton(), gridBagConstraints12);
		}
		return leftPanel;
	}

	/**
	 * This method initializes rightViewPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getRightViewPanel() {
		if (rightViewPanel == null) {
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			gridBagConstraints2.gridx = 0;
			gridBagConstraints2.gridy = 0;
			gridBagConstraints2.fill = GridBagConstraints.BOTH;
			gridBagConstraints2.weightx = 1.0D;
			gridBagConstraints2.weighty = 0.0D;
			gridBagConstraints2.insets = new Insets(5, 5, 5, 5);
			GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
			gridBagConstraints4.gridx = 0;
			gridBagConstraints4.gridy = 1;
			gridBagConstraints4.fill = GridBagConstraints.BOTH;
			gridBagConstraints4.weightx = 1.0D;
			gridBagConstraints4.weighty = 1.0D;
			gridBagConstraints4.insets = new Insets(0, 5, 5, 5);
			rightViewPanel = new JPanel();
			rightViewPanel.setLayout(new GridBagLayout());
			rightViewPanel.setPreferredSize(new Dimension(320, 420));
			rightViewPanel.setName("jRightViewPanel");
			rightViewPanel.add(getSelectedWorkflowPanel(), gridBagConstraints2);
			rightViewPanel.add(getWorkflowPanel(), gridBagConstraints4);
		}
		return rightViewPanel;
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
			jTree.setFont(Phybots.getInstance().getDefaultFont().deriveFont(12));
			jTree.getSelectionModel().setSelectionMode(
					TreeSelectionModel.SINGLE_TREE_SELECTION);
			jTree.addTreeSelectionListener(this);
		}
		return jTree;
	}

	/**
	 * This method initializes workflowPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getWorkflowPanel() {
		if (workflowPanel == null) {
			workflowPanel = new JPanel();
			workflowPanel.setPreferredSize(new Dimension(400, 420));
			workflowPanel.setLayout(new CardLayout());
			workflowPanel.setBorder(new SoftBevelBorder(SoftBevelBorder.LOWERED));
		}
		return workflowPanel;
	}

	public void run() {
		((DefaultTreeModel) getJTree().getModel()).reload();
		updateWorkflowButtons();
	}

	public void valueChanged(TreeSelectionEvent e) {
		final JTree tree = getJTree();
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)
				tree.getLastSelectedPathComponent();
		if (node == null) {
			return;
		}

		Object nodeInfo = node.getUserObject();
		if (nodeInfo instanceof Workflow) {
			showWorkflow((Workflow) nodeInfo);
		}
	}

	public void eventOccurred(Event e) {
		if (e instanceof WorkflowEvent) {
			WorkflowEvent we = (WorkflowEvent) e;
			if (we.getStatus() == WorkflowStatus.INSTANTIATED ||
					we.getStatus() == WorkflowStatus.DISPOSED) {
				Workflow entity = we.getSource();

				if (we.getStatus() == WorkflowStatus.INSTANTIATED) {
					addGraph(entity);
				} else {
					removeGraph(entity);
				}

				SwingUtilities.invokeLater(this);
			} else if (we.getStatus() == WorkflowStatus.STARTED ||
					we.getStatus() == WorkflowStatus.STOPPED) {
				SwingUtilities.invokeLater(this);
			}
		}
	}

	public Workflow getSelectedWorkflow() {
		return selectedWorkflow;
	}

	public void showWorkflow(Workflow workflow) {
		if (workflow == null) {
			selectedWorkflowLabel.setText(""); //$NON-NLS-1$
			selectedWorkflow = null;
			return;
		}
		if (!workflowComponents.containsKey(workflow)) {
			WorkflowViewPane workflowComponent = workflow.getConfigurationComponent();
			if (workflowComponent != null) {
				getWorkflowPanel().add(workflowComponent, String.valueOf(workflow.hashCode()));
				getWorkflowPanel().validate();
				workflowComponents.put(workflow, workflowComponent);
			}
		}
		((CardLayout) getWorkflowPanel().getLayout()).show(
				getWorkflowPanel(), String.valueOf(workflow.hashCode()));
		selectedWorkflowLabel.setText(workflow.getName());
		selectedWorkflow = workflow;
		updateWorkflowButtons();
	}

	private void updateWorkflowButtons() {
		Workflow selectedWorkflow = getSelectedWorkflow();
		if (selectedWorkflow == null || selectedWorkflow.isDisposed()) {
			getStartWorkflowButton().setEnabled(false);
			getStopWorkflowButton().setEnabled(false);
		} else if (selectedWorkflow.isStarted()) {
			getStartWorkflowButton().setEnabled(false);
			getStopWorkflowButton().setEnabled(true);
		} else {
			getStartWorkflowButton().setEnabled(true);
			getStopWorkflowButton().setEnabled(false);
		}
	}


	private void addGraph(Workflow graph) {
		if (workflowNodeMap.containsKey(graph)) {
			return;
		}

		final DefaultMutableTreeNode node =
				new DefaultMutableTreeNode(graph);
		root.add(node);
		workflowNodeMap.put(graph, node);
	}

	private void removeGraph(Workflow graph) {

		// Remove from the list view and graphNodeMap.
		MutableTreeNode node;
		node = root;
		node.remove(
				workflowNodeMap.remove(graph));

		if (workflowComponents.containsKey(graph)) {
			JComponent workflowComponent = workflowComponents.get(graph);
			getWorkflowPanel().remove(workflowComponent);
			if (workflowComponent instanceof DisposableComponent) {
				((DisposableComponent) workflowComponent).dispose();
			}
			workflowComponents.remove(graph);
		}
	}

	/**
	 * This method initializes rightPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getRightPanel() {
		if (rightPanel == null) {
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = -1;
			gridBagConstraints.gridy = -1;
			rightPanel = new JPanel();
			rightPanel.setLayout(new CardLayout());
			rightPanel.add(getRightViewPanel(), getRightViewPanel().getName());
		}
		return rightPanel;
	}

	/**
	 * This method initializes disposeButton
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getDisposeButton() {
		if (disposeButton == null) {
			disposeButton = new JButton();
			disposeButton.setAction(new WorkflowDisposeAction(this));
			disposeButton.setFont(Phybots.getInstance().getDefaultFont());
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
			instantiateButton.setFont(Phybots.getInstance().getDefaultFont());
			instantiateButton.setEnabled(false);
			instantiateButton.setText("+");
		}
		return instantiateButton;
	}

	/**
	 * This method initializes selectedWorkflowPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getSelectedWorkflowPanel() {
		if (selectedWorkflowPanel == null) {
			GridBagConstraints gridBagConstraints21 = new GridBagConstraints();
			gridBagConstraints21.weightx = 0.0D;
			gridBagConstraints21.insets = new Insets(0, 5, 0, 0);
			gridBagConstraints21.gridx = 2;
			gridBagConstraints21.gridy = 0;
			gridBagConstraints21.fill = GridBagConstraints.BOTH;
			GridBagConstraints gridBagConstraints13 = new GridBagConstraints();
			gridBagConstraints13.weightx = 0.0D;
			gridBagConstraints13.insets = new Insets(0, 5, 0, 0);
			gridBagConstraints13.gridx = 1;
			gridBagConstraints13.gridy = 0;
			gridBagConstraints13.fill = GridBagConstraints.BOTH;
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.fill = GridBagConstraints.BOTH;
			gridBagConstraints.gridx = 0;
			gridBagConstraints.gridy = 0;
			gridBagConstraints.weightx = 1.0D;
			gridBagConstraints.weighty = 0.0D;
			gridBagConstraints.insets = new Insets(0, 0, 0, 0);
			selectedWorkflowPanel = new JPanel();
			selectedWorkflowPanel.setLayout(new GridBagLayout());
			selectedWorkflowPanel.add(selectedWorkflowLabel, gridBagConstraints);
			selectedWorkflowPanel.add(getStartWorkflowButton(), gridBagConstraints13);
			selectedWorkflowPanel.add(getStopWorkflowButton(), gridBagConstraints21);
		}
		return selectedWorkflowPanel;
	}

	/**
	 * This method initializes jStartServiceButton
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getStartWorkflowButton() {
		if (startWorkflowButton == null) {
			startWorkflowButton = new JButton();
			startWorkflowButton.setAction(new WorkflowStartAction(this));
			startWorkflowButton.setText(Messages.getString("ServiceMonitorPanel.startIcon"));
			startWorkflowButton.setToolTipText(Messages.getString("ServiceMonitorPanel.start"));
			startWorkflowButton.setEnabled(false);
		}
		return startWorkflowButton;
	}

	/**
	 * This method initializes stopWorkflowButton
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getStopWorkflowButton() {
		if (stopWorkflowButton == null) {
			stopWorkflowButton = new JButton();
			stopWorkflowButton.setAction(new WorkflowStopAction(this));
			stopWorkflowButton.setText(Messages.getString("ServiceMonitorPanel.stopIcon"));
			stopWorkflowButton.setToolTipText(Messages.getString("ServiceMonitorPanel.stop"));
			stopWorkflowButton.setEnabled(false);
		}
		return stopWorkflowButton;
	}
}
