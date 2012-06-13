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
import javax.swing.JButton;

import com.phybots.Phybots;
import com.phybots.message.Event;
import com.phybots.message.EventListener;
import com.phybots.message.ServiceEvent;
import com.phybots.message.ServiceStatus;
import com.phybots.message.ServiceUpdateEvent;
import com.phybots.service.Service;
import com.phybots.service.ServiceGroup;

/**
 * Monitor panel for service and service groups.
 *
 * @author Jun Kato
 */
public class ServiceMonitorPanel extends JPanel implements EventListener, TreeSelectionListener, Runnable, DisposableComponent {

	private static final long serialVersionUID = 3317150753032501439L;

	private JSplitPane jSplitPane = null;

	private JPanel leftPanel = null;

	private JScrollPane jScrollPane = null;
	private JTree jTree = null;
	private JButton instantiateButton = null;
	private JButton disposeButton = null;

	private JPanel rightPanel = null;

	private JPanel rightViewPanel = null;
	private JPanel selectedServicePanel = null;
	private JLabel selectedServiceLabel = null;
	private JButton startServiceButton = null;
	private JButton stopServiceButton = null;
	private JPanel serviceInformationPanel = null;
	private JLabel jLabel = null;
	private JLabel jLabel3 = null;
	private JLabel jLabel4 = null;
	private JLabel jLabel1 = null;
	private JLabel serviceGroupLabel = null;
	private JPanel servicePanel = null;

	private JPanel rightInstantiatePanel = null;

	/** Root node for jTree. */
	private DefaultMutableTreeNode root;

	/** Map of service groups and their corresponding nodes. */
	private HashMap<ServiceGroup, DefaultMutableTreeNode> groupNodeMap;

	/** Map of services and their corresponding nodes. */
	private HashMap<Service, DefaultMutableTreeNode> serviceNodeMap;

	/** Map of services and their corresponding service groups. */
	private HashMap<Service, ServiceGroup> serviceGroupMap;

	private transient Map<Service, JComponent> serviceComponents;

	private transient Service selectedService;  //  @jve:decl-index=0:

	/** Singleton constructor. */
	public ServiceMonitorPanel() {
		super();

		// Initialize hash maps.
		groupNodeMap = new HashMap<ServiceGroup, DefaultMutableTreeNode>();
		serviceNodeMap = new HashMap<Service, DefaultMutableTreeNode>();
		serviceGroupMap = new HashMap<Service, ServiceGroup>();
		serviceComponents = new HashMap<Service, JComponent>();

		// Root node of the tree view, used at getJTree() etc.
		final Phybots phybots = Phybots.getInstance();
		root = new DefaultMutableTreeNode(phybots);

		// Initialize the monitor pane.
		initialize();
		Phybots.getInstance().addEventListener(this);
		for (Service service : Phybots.getInstance().getServices()) {
			updateService(service);
		}
		showService(null);
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
		selectedServiceLabel = new JLabel();
		selectedServiceLabel.setText(""); //$NON-NLS-1$
		selectedServiceLabel.setFont(Phybots.getInstance().getDefaultFont().deriveFont(Font.BOLD, 14));
		selectedServiceLabel.setToolTipText(Messages.getString("MonitorPanel.nameOfSelectedService")); //$NON-NLS-1$
		serviceGroupLabel = new JLabel();
		serviceGroupLabel.setText(""); //$NON-NLS-1$
		serviceGroupLabel.setFont(Phybots.getInstance().getDefaultFont());
		serviceGroupLabel.setToolTipText(Messages.getString("MonitorPanel.nameOfSelectedServiceGroup")); //$NON-NLS-1$
		this.add(getJSplitPane(), gridBagConstraints11);
	}

	public void dispose() {
		Phybots.getInstance().removeEventListener(this);
		for (JComponent serviceComponent : serviceComponents.values()) {
			if (serviceComponent != null) {
				if (serviceComponent instanceof DisposableComponent) {
					((DisposableComponent) serviceComponent).dispose();
				}
			}
		}
		serviceComponents.clear();
	}

	public void run() {
		((DefaultTreeModel) getJTree().getModel()).reload();
		updateServiceButtons();
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
			showService((Service) nodeInfo);
		}
	}

	public void eventOccurred(Event e) {
		if (e instanceof ServiceEvent) {
			ServiceEvent se = (ServiceEvent) e;
			if (se.getStatus() == ServiceStatus.INSTANTIATED ||
					se.getStatus() == ServiceStatus.DISPOSED) {
				Service service = se.getSource();

				if (service instanceof ServiceGroup) {
					if (se.getStatus() == ServiceStatus.INSTANTIATED) {
						addServiceGroup((ServiceGroup) service);
					} else {
						removeServiceGroup((ServiceGroup) service);
					}
				} else {
					if (se.getStatus() == ServiceStatus.INSTANTIATED) {
						updateService(service);
					} else {
						removeService(service);
						showService(null);
					}
				}

				SwingUtilities.invokeLater(this);
			}

			else if (se.getStatus() == ServiceStatus.STARTED ||
					se.getStatus() == ServiceStatus.STOPPED) {
				Service service = se.getSource();

				if (!(service instanceof ServiceGroup)) {
					if (se.getStatus() == ServiceStatus.STARTED) {
						updateService(service);
					}
				}

				SwingUtilities.invokeLater(this);
			}
		}
		else if (e instanceof ServiceUpdateEvent) {
			ServiceUpdateEvent sue = (ServiceUpdateEvent) e;
			if (selectedService == e.getSource() &&
					"name".equals(sue.getParameter())) {
				if (selectedServiceLabel != null) {
					selectedServiceLabel.setText(sue.getValue().toString());
				}
			}
		}
	}

	public Service getSelectedService() {
		return selectedService;
	}

	void showInstantiatePanel() {
		((CardLayout) getRightPanel().getLayout()).show(getRightPanel(),
				String.valueOf(getRightInstantiatePanel().hashCode()));
	}

	public void showService(Service service) {
		((CardLayout) getRightPanel().getLayout()).show(getRightPanel(),
				String.valueOf(getRightViewPanel().hashCode()));
		if (service == null) {
			selectedServiceLabel.setText("-"); //$NON-NLS-1$
			jLabel3.setText("-"); //$NON-NLS-1$
			selectedService = null;
			return;
		}
		if (!serviceComponents.containsKey(service)) {
			JComponent serviceComponent = service.getConfigurationComponent();
			if (serviceComponent != null) {
				getServicePanel().add(serviceComponent, String.valueOf(service.hashCode()));
				getServicePanel().validate();
				serviceComponents.put(service, serviceComponent);
			}
		}
		((CardLayout) getServicePanel().getLayout()).show(
				getServicePanel(), String.valueOf(service.hashCode()));
		selectedServiceLabel.setText(service.getName());
		jLabel3.setText(String.valueOf(service.getInterval()));
		selectServiceGroup(service.getServiceGroup());
		selectedService = service;
		updateServiceButtons();
	}

	private void updateServiceButtons() {
		Service selectedService = getSelectedService();
		if (selectedService == null || selectedService.isDisposed()) {
			getStartServiceButton().setEnabled(false);
			getStopServiceButton().setEnabled(false);
		} else if (selectedService.isStarted()) {
			getStartServiceButton().setEnabled(false);
			getStopServiceButton().setEnabled(true);
		} else {
			getStartServiceButton().setEnabled(true);
			getStopServiceButton().setEnabled(false);
		}
	}

	private void updateService(Service service) {
		if (service instanceof ServiceGroup) {
			addServiceGroup((ServiceGroup) service);
			return;
		}
		if (serviceNodeMap.containsKey(service)) {
			removeServiceFromView(service);
		}

		DefaultMutableTreeNode node =
				new DefaultMutableTreeNode(service);
		ServiceGroup serviceGroup = service.getServiceGroup();
		if (serviceGroup == null) {
			root.add(node);
		} else {
			DefaultMutableTreeNode root = groupNodeMap.get(serviceGroup);
			if (root == null) {
				root = addServiceGroup(serviceGroup);
			}
			root.add(node);
		}

		serviceGroupMap.put(service, serviceGroup);
		serviceNodeMap.put(service, node);
	}

	private void removeService(Service service) {

		// Remove from the list view and serviceNodeMap.
		removeServiceFromView(service);

		serviceGroupMap.remove(service);

		if (serviceComponents.containsKey(service)) {
			JComponent serviceComponent = serviceComponents.get(service);
			getServicePanel().remove(serviceComponent);
			if (serviceComponent instanceof DisposableComponent) {
				((DisposableComponent) serviceComponent).dispose();
			}
			serviceComponents.remove(service);
		}

		if (service == selectedService) {
			showService(null);
		}
	}

	private void removeServiceFromView(Service service) {

		if (!serviceNodeMap.containsKey(service)) {
			return;
		}

		ServiceGroup serviceGroup = serviceGroupMap.get(service);
		MutableTreeNode node;
		if (serviceGroup == null) {
			node = root;
		} else {
			node = groupNodeMap.get(serviceGroup);
		}
		node.remove(serviceNodeMap.remove(service));
	}

	private void selectServiceGroup(ServiceGroup serviceGroup) {
		if (serviceGroup == null) {
			serviceGroupLabel.setText("-"); //$NON-NLS-1$
		} else {
			serviceGroupLabel.setText(serviceGroup.toString());
		}
	}

	private DefaultMutableTreeNode addServiceGroup(ServiceGroup serviceGroup) {
		if (groupNodeMap.containsKey(serviceGroup)) {
			return groupNodeMap.get(serviceGroup);
		}

		final DefaultMutableTreeNode node =
				new DefaultMutableTreeNode(serviceGroup);
		root.add(node);
		groupNodeMap.put(serviceGroup, node);
		return node;
	}

	private void removeServiceGroup(ServiceGroup serviceGroup) {

		// Remove from the list view.
		MutableTreeNode node = groupNodeMap.get(serviceGroup);
		root.remove(node);

		// Remove from groupNodeMap
		groupNodeMap.remove(serviceGroup);
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
			gridBagConstraints6.gridx = 0;
			gridBagConstraints6.gridy = 0;
			gridBagConstraints6.gridwidth = 2;
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
	 * This method initializes instantiateButton
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getInstantiateButton() {
		if (instantiateButton == null) {
			instantiateButton = new JButton();
			instantiateButton.setAction(new ServiceInstantiateAction(this));
			instantiateButton.setFont(Phybots.getInstance().getDefaultFont());
			instantiateButton.setText("+");
		}
		return instantiateButton;
	}

	/**
	 * This method initializes disposeButton
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getDisposeButton() {
		if (disposeButton == null) {
			disposeButton = new JButton();
			disposeButton.setAction(new ServiceDisposeAction(this));
			disposeButton.setFont(Phybots.getInstance().getDefaultFont());
			disposeButton.setText("-");
		}
		return disposeButton;
	}

	/**
	 * This method initializes rightPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getRightPanel() {
		if (rightPanel == null) {
			GridBagConstraints gridBagConstraints10 = new GridBagConstraints();
			gridBagConstraints10.gridx = -1;
			gridBagConstraints10.gridy = -1;
			rightPanel = new JPanel();
			rightPanel.setLayout(new CardLayout());
			rightPanel.add(getRightViewPanel(), String.valueOf(getRightViewPanel().hashCode()));
			rightPanel.add(getRightInstantiatePanel(), String.valueOf(getRightInstantiatePanel().hashCode()));
		}
		return rightPanel;
	}

	/**
	 * This method initializes rightViewPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getRightViewPanel() {
		if (rightViewPanel == null) {
			GridBagConstraints gridBagConstraints14 = new GridBagConstraints();
			gridBagConstraints14.gridx = 0;
			gridBagConstraints14.fill = GridBagConstraints.BOTH;
			gridBagConstraints14.insets = new Insets(5, 5, 5, 5);
			gridBagConstraints14.gridy = 0;
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
			rightViewPanel = new JPanel();
			rightViewPanel.setLayout(new GridBagLayout());
			rightViewPanel.setPreferredSize(new Dimension(320, 420));
			rightViewPanel.setName("jRightPanel");
			rightViewPanel.add(getSelectedServicePanel(), gridBagConstraints14);
			rightViewPanel.add(getServiceInformationPanel(), gridBagConstraints3);
			rightViewPanel.add(getServicePanel(), gridBagConstraints4);
		}
		return rightViewPanel;
	}

	/**
	 * This method initializes serviceInformationPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getServiceInformationPanel() {
		if (serviceInformationPanel == null) {
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
			serviceInformationPanel = new JPanel();
			serviceInformationPanel.setLayout(new GridBagLayout());
			serviceInformationPanel.add(jLabel, gridBagConstraints1);
			serviceInformationPanel.add(jLabel3, gridBagConstraints5);
			serviceInformationPanel.add(jLabel4, gridBagConstraints7);
			serviceInformationPanel.add(jLabel1, gridBagConstraints8);
			serviceInformationPanel.add(serviceGroupLabel, gridBagConstraints2);
		}
		return serviceInformationPanel;
	}

	/**
	 * This method initializes servicePanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getServicePanel() {
		if (servicePanel == null) {
			servicePanel = new JPanel();
			servicePanel.setPreferredSize(new Dimension(400, 420));
			servicePanel.setLayout(new CardLayout());
			servicePanel.setBorder(new SoftBevelBorder(SoftBevelBorder.LOWERED));
		}
		return servicePanel;
	}

	/**
	 * This method initializes rightInstantiatePanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getRightInstantiatePanel() {
		if (rightInstantiatePanel == null) {
			rightInstantiatePanel = new ServiceInstantiatePanel(this);
		}
		return rightInstantiatePanel;
	}

	/**
	 * This method initializes selectedServicePanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getSelectedServicePanel() {
		if (selectedServicePanel == null) {
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
			selectedServicePanel = new JPanel();
			selectedServicePanel.setLayout(new GridBagLayout());
			selectedServicePanel.add(selectedServiceLabel, gridBagConstraints);
			selectedServicePanel.add(getStartServiceButton(), gridBagConstraints13);
			selectedServicePanel.add(getStopServiceButton(), gridBagConstraints21);
		}
		return selectedServicePanel;
	}

	/**
	 * This method initializes jStartServiceButton
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getStartServiceButton() {
		if (startServiceButton == null) {
			startServiceButton = new JButton();
			startServiceButton.setAction(new ServiceStartAction(this));
			startServiceButton.setText(Messages.getString("ServiceMonitorPanel.startIcon"));
			startServiceButton.setToolTipText(Messages.getString("ServiceMonitorPanel.start"));
			startServiceButton.setEnabled(false);
		}
		return startServiceButton;
	}

	/**
	 * This method initializes stopServiceButton
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getStopServiceButton() {
		if (stopServiceButton == null) {
			stopServiceButton = new JButton();
			stopServiceButton.setAction(new ServiceStopAction(this));
			stopServiceButton.setText(Messages.getString("ServiceMonitorPanel.stopIcon"));
			stopServiceButton.setToolTipText(Messages.getString("ServiceMonitorPanel.stop"));
			stopServiceButton.setEnabled(false);
		}
		return stopServiceButton;
	}
}
