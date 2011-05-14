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
import jp.digitalmuseum.mr.message.Event;
import jp.digitalmuseum.mr.message.EventListener;
import jp.digitalmuseum.mr.message.ServiceEvent;
import jp.digitalmuseum.mr.message.ServiceEvent.STATUS;
import jp.digitalmuseum.mr.service.Service;
import jp.digitalmuseum.mr.service.ServiceGroup;
import jp.digitalmuseum.mr.service.ServiceHolder;

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

import javax.swing.SwingConstants;
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
 * Monitor panel for service and service groups.
 *
 * @author Jun KATO
 */
public class ServiceMonitorPanel extends JPanel implements EventListener, TreeSelectionListener, Runnable, DisposableComponent {

	private static final long serialVersionUID = 3317150753032501439L;

	private JSplitPane jSplitPane = null;

	private JPanel jLeftPanel = null;
	private JPanel jRightPanel = null;

	private JScrollPane jScrollPane = null;
	private JTree jTree = null;

	private JLabel jSelectedServiceHolderLabel = null;
	private JLabel jSelectedServiceLabel = null;
	private JPanel jServiceInformationPanel = null;
	private JLabel jLabel = null;
	private JLabel jLabel3 = null;
	private JLabel jLabel4 = null;
	private JPanel jServicePanel = null;

	/** Root node for jTree. */
	private DefaultMutableTreeNode root;

	/** Map of service groups and their corresponding nodes. */
	private HashMap<ServiceHolder, DefaultMutableTreeNode> groupNodeMap;

	/** Map of services and their corresponding nodes. */
	private HashMap<Service, DefaultMutableTreeNode> serviceNodeMap;

	/** Map of services and their corresponding service groups. */
	private HashMap<Service, ServiceHolder> serviceGroupMap;

	private transient Map<Service, JComponent> serviceComponents;

	private JLabel jLabel1 = null;

	/** Singleton constructor. */
	public ServiceMonitorPanel() {
		super();

		// Initialize hash maps.
		groupNodeMap = new HashMap<ServiceHolder, DefaultMutableTreeNode>();
		serviceNodeMap = new HashMap<Service, DefaultMutableTreeNode>();
		serviceGroupMap = new HashMap<Service, ServiceHolder>();
		serviceComponents = new HashMap<Service, JComponent>();

		// Root node of the tree view, used at getJTree() etc.
		final Matereal matereal = Matereal.getInstance();
		root = new DefaultMutableTreeNode(matereal);
		groupNodeMap.put(matereal, root);

		// Initialize the monitor pane.
		initialize();
		Matereal.getInstance().addEventListener(this);
		for (Service service : Matereal.getInstance().getServices()) {
			serviceGroupMap.put(service, service.getServiceGroup());
			updateService(service);
		}
		selectService(null);
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
		jSelectedServiceHolderLabel = new JLabel();
		jSelectedServiceHolderLabel.setText(""); //$NON-NLS-1$
		jSelectedServiceHolderLabel.setFont(new Font("Dialog", Font.BOLD, 12)); //$NON-NLS-1$
		jSelectedServiceHolderLabel.setToolTipText(Messages.getString("MonitorPanel.nameOfSelectedServiceGroup")); //$NON-NLS-1$
		jSelectedServiceLabel = new JLabel();
		jSelectedServiceLabel.setText(""); //$NON-NLS-1$
		jSelectedServiceLabel.setFont(new Font("Dialog", Font.BOLD, 14)); //$NON-NLS-1$
		jSelectedServiceLabel.setToolTipText(Messages.getString("MonitorPanel.nameOfSelectedService")); //$NON-NLS-1$
		this.add(getJSplitPane(), gridBagConstraints11);
	}

	public void dispose() {
		Matereal.getInstance().removeEventListener(this);
		for (JComponent serviceComponent : serviceComponents.values()) {
			if (serviceComponent != null) {
				if (serviceComponent instanceof DisposableComponent) {
					((DisposableComponent) serviceComponent).dispose();
				}
			}
		}
		serviceComponents.clear();
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
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 0;
			gridBagConstraints.gridy = 0;
			gridBagConstraints.fill = GridBagConstraints.BOTH;
			gridBagConstraints.weightx = 1.0D;
			gridBagConstraints.weighty = 0.0D;
			gridBagConstraints.insets = new Insets(5, 5, 5, 5);
			GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
			gridBagConstraints3.gridx = 0;
			gridBagConstraints3.gridy = 2;
			gridBagConstraints3.fill = GridBagConstraints.BOTH;
			gridBagConstraints3.weightx = 1.0D;
			gridBagConstraints3.weighty = 0.0D;
			gridBagConstraints3.insets = new Insets(0, 5, 5, 5);
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
			jRightPanel.add(jSelectedServiceLabel, gridBagConstraints);
			jRightPanel.add(getJServiceInformationPanel(), gridBagConstraints3);
			jRightPanel.add(getJServicePanel(), gridBagConstraints4);
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
	 * This method initializes jServiceInformationPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJServiceInformationPanel() {
		if (jServiceInformationPanel == null) {
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			gridBagConstraints2.fill = GridBagConstraints.BOTH;
			gridBagConstraints2.gridx = 1;
			gridBagConstraints2.gridy = 1;
			gridBagConstraints2.gridwidth = 2;
			gridBagConstraints2.insets = new Insets(0, 5, 0, 0);
			gridBagConstraints2.weighty = 0.0D;
			GridBagConstraints gridBagConstraints8 = new GridBagConstraints();
			gridBagConstraints8.gridx = 0;
			gridBagConstraints8.anchor = GridBagConstraints.WEST;
			gridBagConstraints8.insets = new Insets(0, 5, 0, 0);
			gridBagConstraints8.fill = GridBagConstraints.BOTH;
			gridBagConstraints8.gridy = 1;
			jLabel1 = new JLabel();
			jLabel1.setText(Messages.getString("MonitorPanel.serviceGroup"));
			GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
			gridBagConstraints7.gridy = 0;
			gridBagConstraints7.weightx = 0.7D;
			gridBagConstraints7.anchor = GridBagConstraints.WEST;
			gridBagConstraints7.insets = new Insets(0, 5, 5, 0);
			gridBagConstraints7.fill = GridBagConstraints.BOTH;
			gridBagConstraints7.gridx = 2;
			GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
			gridBagConstraints5.gridy = 0;
			gridBagConstraints5.weightx = 0.0D;
			gridBagConstraints5.fill = GridBagConstraints.BOTH;
			gridBagConstraints5.insets = new Insets(0, 5, 5, 0);
			gridBagConstraints5.gridx = 1;
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			gridBagConstraints1.gridy = 0;
			gridBagConstraints1.insets = new Insets(0, 5, 5, 0);
			gridBagConstraints1.weightx = 0.3D;
			gridBagConstraints1.anchor = GridBagConstraints.WEST;
			gridBagConstraints1.fill = GridBagConstraints.BOTH;
			gridBagConstraints1.gridx = 0;
			jLabel4 = new JLabel();
			jLabel4.setText(Messages.getString("MonitorPanel.millisecond")); //$NON-NLS-1$
			jLabel4.setFont(new Font("Dialog", Font.PLAIN, 12)); //$NON-NLS-1$
			jLabel3 = new JLabel();
			jLabel3.setHorizontalTextPosition(SwingConstants.TRAILING);
			jLabel3.setHorizontalAlignment(SwingConstants.RIGHT);
			jLabel3.setFont(new Font("Dialog", Font.PLAIN, 12)); //$NON-NLS-1$
			jLabel = new JLabel();
			jLabel.setText(Messages.getString("MonitorPanel.interval")); //$NON-NLS-1$
			jServiceInformationPanel = new JPanel();
			jServiceInformationPanel.setLayout(new GridBagLayout());
			jServiceInformationPanel.add(jLabel, gridBagConstraints1);
			jServiceInformationPanel.add(jLabel3, gridBagConstraints5);
			jServiceInformationPanel.add(jLabel4, gridBagConstraints7);
			jServiceInformationPanel.add(jLabel1, gridBagConstraints8);
			jServiceInformationPanel.add(jSelectedServiceHolderLabel, gridBagConstraints2);
		}
		return jServiceInformationPanel;
	}

	/**
	 * This method initializes jServicePanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJServicePanel() {
		if (jServicePanel == null) {
			jServicePanel = new JPanel();
			jServicePanel.setPreferredSize(new Dimension(400, 420));
			jServicePanel.setLayout(new CardLayout());
			jServicePanel.setBorder(new SoftBevelBorder(SoftBevelBorder.LOWERED));

		}
		return jServicePanel;
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
		if (nodeInfo instanceof Service) {
			selectService((Service) nodeInfo);
		} else if (nodeInfo instanceof ServiceGroup) {
			selectServiceHolder((ServiceGroup) nodeInfo);
		}
	}

	public void eventOccurred(Event e) {
		if (e instanceof ServiceEvent) {
			ServiceEvent se = (ServiceEvent) e;
			if (se.getStatus() == STATUS.STARTED ||
					se.getStatus() == STATUS.STOPPED) {
				Service service = se.getSource();

				if (service instanceof ServiceGroup) {
					if (se.getStatus() == STATUS.STARTED) {
						addServiceHolder((ServiceHolder) service);
					} else {
						removeServiceHolder((ServiceHolder) service);
					}
				} else {
					if (se.getStatus() == STATUS.STARTED) {
						serviceGroupMap.put(service, service.getServiceGroup());
						updateService(service);
					} else {
						removeService(service);
					}
				}

				SwingUtilities.invokeLater(this);
			}
		}
	}

	private void selectService(Service service) {
		if (service == null) {
			jSelectedServiceLabel.setText("-"); //$NON-NLS-1$
			jLabel3.setText("-"); //$NON-NLS-1$
			return;
		}
		if (!serviceComponents.containsKey(service)) {
			JComponent serviceComponent = service.getConfigurationComponent();
			if (serviceComponent != null) {
				getJServicePanel().add(serviceComponent, String.valueOf(service.hashCode()));
				getJServicePanel().validate();
				serviceComponents.put(service, serviceComponent);
			}
		}
		((CardLayout) getJServicePanel().getLayout()).show(
				getJServicePanel(), String.valueOf(service.hashCode()));
		jSelectedServiceLabel.setText(service.toString());
		jLabel3.setText(String.valueOf(service.getInterval()));
	}

	private void updateService(Service service) {
		if (service instanceof ServiceGroup) {
			addServiceHolder((ServiceHolder) service);
			return;
		}
		if (serviceNodeMap.containsKey(service)) {
			removeServiceFromView(service);
		}

		final DefaultMutableTreeNode node =
				new DefaultMutableTreeNode(service);
		final ServiceHolder serviceHolder = serviceGroupMap.get(service);
		if (serviceHolder == null) {
			root.add(node);
		} else {
			DefaultMutableTreeNode root = groupNodeMap.get(serviceHolder);
			if (root == null) {
				root = addServiceHolder(serviceHolder);
			}
			root.add(node);
		}
		serviceNodeMap.put(service, node);
	}

	private void removeService(Service service) {

		// Remove from the list view and serviceNodeMap.
		removeServiceFromView(service);

		serviceGroupMap.remove(service);

		if (serviceComponents.containsKey(service)) {
			JComponent serviceComponent = serviceComponents.get(service);
			getJServicePanel().remove(serviceComponent);
			if (serviceComponent instanceof DisposableComponent) {
				((DisposableComponent) serviceComponent).dispose();
			}
			serviceComponents.remove(service);
		}
	}

	private void removeServiceFromView(Service service) {

		// Remove from the list view and serviceNodeMap.
		MutableTreeNode node;
		if (service.getServiceGroup() == null) {
			node = root;
		} else {
			node = groupNodeMap.get(service.getServiceGroup());
		}
		node.remove(
				serviceNodeMap.remove(service));
	}

	private void selectServiceHolder(ServiceHolder serviceHolder) {
		if (serviceHolder == null ||
				serviceHolder == Matereal.getInstance()) {
			jSelectedServiceHolderLabel.setText("-"); //$NON-NLS-1$
		} else {
			jSelectedServiceHolderLabel.setText(serviceHolder.toString());
		}
	}

	private DefaultMutableTreeNode addServiceHolder(ServiceHolder serviceHolder) {
		if (groupNodeMap.containsKey(serviceHolder)) {
			return groupNodeMap.get(serviceHolder);
		}

		final DefaultMutableTreeNode node =
				new DefaultMutableTreeNode(serviceHolder);
		root.add(node);
		groupNodeMap.put(serviceHolder, node);
		return node;
	}

	private void removeServiceHolder(ServiceHolder serviceHolder) {

		// Remove from the list view.
		MutableTreeNode node = groupNodeMap.get(serviceHolder);
		root.remove(node);

		// Remove from groupNodeMap
		groupNodeMap.remove(serviceHolder);
	}
}
