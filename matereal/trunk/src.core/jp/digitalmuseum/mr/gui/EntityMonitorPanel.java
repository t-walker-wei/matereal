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
import jp.digitalmuseum.mr.entity.Entity;
import jp.digitalmuseum.mr.message.EntityEvent;
import jp.digitalmuseum.mr.message.EntityStatus;
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
public class EntityMonitorPanel extends JPanel implements EventListener, TreeSelectionListener, Runnable, DisposableComponent {

	private static final long serialVersionUID = 3317150753032501439L;

	private JSplitPane jSplitPane = null;

	private JPanel leftPanel = null;
	private JPanel rightViewPanel = null;

	private JScrollPane jScrollPane = null;
	private JTree jTree = null;

	private JLabel selectedEntityLabel = null;
	private JPanel entityPanel = null;

	/** Root node for jTree. */
	private DefaultMutableTreeNode root;

	/** Map of entities and their corresponding nodes. */
	private transient Map<Entity, DefaultMutableTreeNode> entityNodeMap;

	private transient Map<Entity, JComponent> entityComponents;

	private JPanel rightPanel = null;

	private JButton instantiateButton = null;

	private JButton disposeButton = null;

	private JPanel rightAddPanel = null;

	/** Singleton constructor. */
	public EntityMonitorPanel() {
		super();

		// Initialize hash maps.
		entityNodeMap = new HashMap<Entity, DefaultMutableTreeNode>();
		entityComponents = new HashMap<Entity, JComponent>();

		// Root node of the tree view, used at getJTree() etc.
		final Matereal matereal = Matereal.getInstance();
		root = new DefaultMutableTreeNode(matereal);

		// Initialize the monitor pane.
		initialize();
		Matereal.getInstance().addEventListener(this);
		for (Entity entity : Matereal.getInstance().getEntities()) {
			addEntity(entity);
		}
		showEntity(null);
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
		selectedEntityLabel = new JLabel();
		selectedEntityLabel.setText(Messages.getString("EntityMonitorPanel.selectedEntity")); //$NON-NLS-1$
		selectedEntityLabel.setFont(Matereal.getInstance().getDefaultFont().deriveFont(Font.BOLD, 14));
		selectedEntityLabel.setToolTipText(Messages.getString("EntityMonitorPanel.nameOfSelectedEntity")); //$NON-NLS-1$
		this.add(getJSplitPane(), gridBagConstraints11);
	}

	public void dispose() {
		Matereal.getInstance().removeEventListener(this);
		for (JComponent serviceComponent : entityComponents.values()) {
			if (serviceComponent != null) {
				if (serviceComponent instanceof DisposableComponent) {
					((DisposableComponent) serviceComponent).dispose();
				}
			}
		}
		entityComponents.clear();
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
			rightViewPanel = new JPanel();
			rightViewPanel.setLayout(new GridBagLayout());
			rightViewPanel.setPreferredSize(new Dimension(320, 420));
			rightViewPanel.add(selectedEntityLabel, gridBagConstraints2);
			rightViewPanel.add(getEntityPanel(), gridBagConstraints4);
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
			jTree.setFont(Matereal.getInstance().getDefaultFont().deriveFont(12));
			jTree.getSelectionModel().setSelectionMode(
					TreeSelectionModel.SINGLE_TREE_SELECTION);
			jTree.addTreeSelectionListener(this);
		}
		return jTree;
	}

	/**
	 * This method initializes entityPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getEntityPanel() {
		if (entityPanel == null) {
			entityPanel = new JPanel();
			entityPanel.setPreferredSize(new Dimension(400, 420));
			entityPanel.setLayout(new CardLayout());
			entityPanel.setBorder(new SoftBevelBorder(SoftBevelBorder.LOWERED));
		}
		return entityPanel;
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
		if (nodeInfo instanceof Entity) {
			showEntity((Entity) nodeInfo);
		}
	}

	public void eventOccurred(Event e) {
		if (e instanceof EntityEvent) {
			EntityEvent ee = (EntityEvent) e;
			if (ee.getStatus() == EntityStatus.INSTANTIATED ||
					ee.getStatus() == EntityStatus.DISPOSED) {
				Entity entity = ee.getSource();

				if (ee.getStatus() == EntityStatus.INSTANTIATED) {
					addEntity(entity);
				} else {
					removeEntity(entity);
				}

				SwingUtilities.invokeLater(this);
			}
		}
	}

	public void showAddPanel() {
		((CardLayout) getRightPanel().getLayout()).show(getRightPanel(),
				String.valueOf(getRightAddPanel().hashCode()));
	}

	public void showEntity(Entity entity) {
		if (entity == null) {
			((CardLayout) getRightPanel().getLayout()).show(getRightPanel(),
					String.valueOf(getRightViewPanel().hashCode()));
			selectedEntityLabel.setText(""); //$NON-NLS-1$
			return;
		}
		if (!entityComponents.containsKey(entity)) {
			JComponent entityComponent = entity.getConfigurationComponent();
			if (entityComponent != null) {
				getEntityPanel().add(entityComponent, String.valueOf(entity.hashCode()));
				getEntityPanel().validate();
				entityComponents.put(entity, entityComponent);
			}
		}
		((CardLayout) getEntityPanel().getLayout()).show(
				getEntityPanel(), String.valueOf(entity.hashCode()));
		selectedEntityLabel.setText(entity.getName());
	}

	private void addEntity(Entity entity) {
		if (entityNodeMap.containsKey(entity)) {
			return;
		}

		final DefaultMutableTreeNode node =
				new DefaultMutableTreeNode(entity);
		root.add(node);
		entityNodeMap.put(entity, node);
	}

	private void removeEntity(Entity entity) {

		// Remove from the list view and entityNodeMap.
		MutableTreeNode node;
		node = root;
		node.remove(
				entityNodeMap.remove(entity));

		if (entityComponents.containsKey(entity)) {
			JComponent entityComponent = entityComponents.get(entity);
			getEntityPanel().remove(entityComponent);
			if (entityComponent instanceof DisposableComponent) {
				((DisposableComponent) entityComponent).dispose();
			}
			entityComponents.remove(entity);
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
			rightPanel.add(getRightViewPanel(), String.valueOf(getRightViewPanel().hashCode()));
			rightPanel.add(getRightAddPanel(), String.valueOf(getRightAddPanel().hashCode()));
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
			disposeButton.setFont(Matereal.getInstance().getDefaultFont());
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
			instantiateButton.setAction(new EntityAddAction(this));
			instantiateButton.setFont(Matereal.getInstance().getDefaultFont());
			instantiateButton.setText("+");
		}
		return instantiateButton;
	}

	/**
	 * This method initializes rightAddPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getRightAddPanel() {
		if (rightAddPanel == null) {
			rightAddPanel = new EntityAddPanel();
		}
		return rightAddPanel;
	}
}
