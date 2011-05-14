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
import jp.digitalmuseum.mr.message.EntityEvent.STATUS;
import jp.digitalmuseum.mr.message.Event;
import jp.digitalmuseum.mr.message.EventListener;

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

/**
 * Monitor panel for entities.
 *
 * @author Jun KATO
 */
public class EntityMonitorPanel extends JPanel implements EventListener, TreeSelectionListener, Runnable, DisposableComponent {

	private static final long serialVersionUID = 3317150753032501439L;

	private JSplitPane jSplitPane = null;

	private JPanel jLeftPanel = null;
	private JPanel jRightPanel = null;

	private JScrollPane jScrollPane = null;
	private JTree jTree = null;

	private JLabel jSelectedEntityLabel = null;
	private JPanel jEntityPanel = null;

	/** Root node for jTree. */
	private DefaultMutableTreeNode root;

	/** Map of entities and their corresponding nodes. */
	private transient Map<Entity, DefaultMutableTreeNode> entityNodeMap;

	private transient Map<Entity, JComponent> entityComponents;

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
		selectEntity(null);
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
		jSelectedEntityLabel = new JLabel();
		jSelectedEntityLabel.setText(Messages.getString("EntityMonitorPanel.selectedEntity")); //$NON-NLS-1$
		jSelectedEntityLabel.setFont(new Font("Dialog", Font.BOLD, 14)); //$NON-NLS-1$
		jSelectedEntityLabel.setToolTipText(Messages.getString("EntityMonitorPanel.nameOfSelectedEntity")); //$NON-NLS-1$
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
			jSplitPane.setRightComponent(getJRightPanel());
			jSplitPane.setLeftComponent(getJLeftPanel());
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
			GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
			gridBagConstraints6.fill = GridBagConstraints.BOTH;
			gridBagConstraints6.weighty = 1.0;
			gridBagConstraints6.insets = new Insets(5, 5, 5, 5);
			gridBagConstraints6.weightx = 1.0;
			jLeftPanel = new JPanel();
			jLeftPanel.setLayout(new GridBagLayout());
			jLeftPanel.add(getJScrollPane(), gridBagConstraints6);
		}
		return jLeftPanel;
	}

	/**
	 * This method initializes jRightPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJRightPanel() {
		if (jRightPanel == null) {
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
			jRightPanel = new JPanel();
			jRightPanel.setLayout(new GridBagLayout());
			jRightPanel.setPreferredSize(new Dimension(320, 420));
			jRightPanel.add(jSelectedEntityLabel, gridBagConstraints2);
			jRightPanel.add(getJEntityPanel(), gridBagConstraints4);
		}
		return jRightPanel;
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
			jTree.getSelectionModel().setSelectionMode(
					TreeSelectionModel.SINGLE_TREE_SELECTION);
			jTree.addTreeSelectionListener(this);
		}
		return jTree;
	}

	/**
	 * This method initializes jEntityPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJEntityPanel() {
		if (jEntityPanel == null) {
			jEntityPanel = new JPanel();
			jEntityPanel.setPreferredSize(new Dimension(400, 420));
			jEntityPanel.setLayout(new CardLayout());
			jEntityPanel.setBorder(new SoftBevelBorder(SoftBevelBorder.LOWERED));
		}
		return jEntityPanel;
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
			selectEntity((Entity) nodeInfo);
		}
	}

	public void eventOccurred(Event e) {
		if (e instanceof EntityEvent) {
			EntityEvent se = (EntityEvent) e;
			if (se.getStatus() == STATUS.INSTANTIATED ||
					se.getStatus() == STATUS.DISPOSED) {
				Entity entity = se.getSource();

				if (se.getStatus() == STATUS.INSTANTIATED) {
					addEntity(entity);
				} else {
					removeEntity(entity);
				}

				SwingUtilities.invokeLater(this);
			}
		}
	}

	private void selectEntity(Entity entity) {
		if (entity == null) {
			jSelectedEntityLabel.setText(""); //$NON-NLS-1$
			return;
		}
		if (!entityComponents.containsKey(entity)) {
			JComponent entityComponent = entity.getConfigurationComponent();
			if (entityComponent != null) {
				getJEntityPanel().add(entityComponent, String.valueOf(entity.hashCode()));
				getJEntityPanel().validate();
				entityComponents.put(entity, entityComponent);
			}
		}
		((CardLayout) getJEntityPanel().getLayout()).show(
				getJEntityPanel(), String.valueOf(entity.hashCode()));
		jSelectedEntityLabel.setText(entity.getName());
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
			getJEntityPanel().remove(entityComponent);
			if (entityComponent instanceof DisposableComponent) {
				((DisposableComponent) entityComponent).dispose();
			}
			entityComponents.remove(entity);
		}
	}
}
