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
import jp.digitalmuseum.mr.service.Service;
import jp.digitalmuseum.mr.service.ServiceAbstractImpl;
import jp.digitalmuseum.mr.service.ServiceGroup;
import jp.digitalmuseum.mr.service.ServiceHolder;

import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.SwingUtilities;

import java.awt.GridBagLayout;
import javax.swing.JLabel;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.JTable;
import javax.swing.JScrollPane;

import java.awt.Font;
import java.awt.GridLayout;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import javax.swing.SwingConstants;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;

import java.awt.FlowLayout;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.Rectangle;

/**
 * Status monitor panel of matereal. Singleton.
 *
 * @author Jun KATO
 */
public class MonitorPanel extends JPanel {

	private JTree jTree = null;
	private JPanel jPanel = null;
	private JPanel jPanel1 = null;
	private JLabel jLabel1 = null;
	private JLabel jLabel2 = null;
	private JTable jTable = null;
	private JScrollPane jScrollPane = null;
	private JPanel jPanel2 = null;
	private JLabel jLabel = null;
	private JLabel jLabel3 = null;
	private JLabel jLabel4 = null;
	private static final long serialVersionUID = 1L;

	/** Root node for jTree. */
	private DefaultMutableTreeNode root;

	/** Map of service groups and their corresponding nodes. */
	private HashMap<ServiceHolder, DefaultMutableTreeNode> groupNodeMap;
	/** Map of services and their corresponding nodes. */
	private HashMap<Service, DefaultMutableTreeNode> serviceNodeMap;
	/** List of services. */
	private HashMap<ServiceHolder, List<Service>> serviceMap;

	private MonitorService monitorService;  //  @jve:decl-index=0:

	final private transient Runnable reloadJTree;

	/** Singleton constructor. */
	public MonitorPanel() {
		super();

		// Initialize hash maps.
		groupNodeMap = new HashMap<ServiceHolder, DefaultMutableTreeNode>();
		serviceNodeMap = new HashMap<Service, DefaultMutableTreeNode>();
		serviceMap = new HashMap<ServiceHolder, List<Service>>();

		// Root node of the tree view, used at getJTree() etc.
		final Matereal matereal = Matereal.getInstance();
		root = new DefaultMutableTreeNode(matereal);
		groupNodeMap.put(matereal, root);

		// Initialize the monitor pane.
		initialize();
		reloadJTree = new Runnable() {
			public void run() {
				((DefaultTreeModel) getJTree().getModel()).reload();
			}
		};
		selectServiceGroup(matereal);
		monitorService = new MonitorService();
		monitorService.start();
	}

	/**
	 * This method initializes this
	 *
	 * @return void
	 */
	private void initialize() {
		GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
		gridBagConstraints4.fill = GridBagConstraints.BOTH;
		gridBagConstraints4.weightx = 0.3D;
		gridBagConstraints4.weighty = 1.0;
		GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
		gridBagConstraints3.weightx = 0.7D;
		gridBagConstraints3.fill = GridBagConstraints.BOTH;
		gridBagConstraints3.gridx = 1;
		gridBagConstraints3.gridy = 0;
		gridBagConstraints3.gridheight = 1;
		gridBagConstraints3.weighty = 1.0D;
		setPreferredSize(new Dimension(480, 320));
		setLayout(new GridBagLayout());
		setBounds(new Rectangle(0, 0, 480, 320));
		add(getJScrollPane(), gridBagConstraints4);
		add(getJPanel(), gridBagConstraints3);
	}

	public void dispose() {
		monitorService.stop();
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
			jTree.addTreeSelectionListener(new SelectionListener());
		}
		return jTree;
	}

	/**
	 * This method initializes jPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel() {
		if (jPanel == null) {
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			gridBagConstraints1.fill = GridBagConstraints.BOTH;
			gridBagConstraints1.gridx = 0;
			gridBagConstraints1.gridy = 1;
			gridBagConstraints1.weightx = 1.0;
			gridBagConstraints1.weighty = 1.0;
			gridBagConstraints1.insets = new Insets(0, 5, 5, 5);
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.insets = new Insets(5, 5, 5, 5);
			gridBagConstraints.gridy = 0;
			gridBagConstraints.ipadx = 0;
			gridBagConstraints.anchor = GridBagConstraints.WEST;
			gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints.gridx = 0;
			jPanel = new JPanel();
			jPanel.setLayout(new GridBagLayout());
			jPanel.setPreferredSize(new Dimension(320, 320));
			jPanel.add(getJPanel1(), gridBagConstraints);
			jPanel.add(getJTable(), gridBagConstraints1);
		}
		return jPanel;
	}

	/**
	 * This method initializes jPanel1
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel1() {
		if (jPanel1 == null) {
			GridLayout gridLayout = new GridLayout();
			gridLayout.setRows(3);
			gridLayout.setVgap(5);
			jLabel2 = new JLabel();
			jLabel2.setText(Messages.getString("MonitorPanel.selectedService")); //$NON-NLS-1$
			jLabel2.setFont(new Font("Dialog", Font.BOLD, 12)); //$NON-NLS-1$
			jLabel2.setToolTipText(Messages.getString("MonitorPanel.nameOfSelectedService")); //$NON-NLS-1$
			jLabel1 = new JLabel();
			jLabel1.setText(Messages.getString("MonitorPanel.selectedServiceGroup")); //$NON-NLS-1$
			jLabel1.setFont(new Font("Dialog", Font.BOLD, 14)); //$NON-NLS-1$
			jLabel1.setToolTipText(Messages.getString("MonitorPanel.nameOfSelectedServiceGroup")); //$NON-NLS-1$
			jPanel1 = new JPanel();
			jPanel1.setPreferredSize(new Dimension(310, 67));
			jPanel1.setLayout(gridLayout);
			jPanel1.add(jLabel1, null);
			jPanel1.add(getJPanel2(), null);
			jPanel1.add(jLabel2, null);
		}
		return jPanel1;
	}

	/**
	 * This method initializes jTable
	 *
	 * @return javax.swing.JTable
	 */
	private JTable getJTable() {
		if (jTable == null) {
			jTable = new JTable();
			jTable.setRowHeight(20);
		}
		return jTable;
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
			jScrollPane.setPreferredSize(new Dimension(160, 320));
			jScrollPane.setViewportView(getJTree());
		}
		return jScrollPane;
	}

	/**
	 * This method initializes jPanel2
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel2() {
		if (jPanel2 == null) {
			FlowLayout flowLayout = new FlowLayout();
			flowLayout.setAlignment(FlowLayout.LEFT);
			flowLayout.setVgap(0);
			jLabel4 = new JLabel();
			jLabel4.setText(Messages.getString("MonitorPanel.millisecond")); //$NON-NLS-1$
			jLabel4.setFont(new Font("Dialog", Font.PLAIN, 12)); //$NON-NLS-1$
			jLabel3 = new JLabel();
			jLabel3.setText("JLabel"); //$NON-NLS-1$
			jLabel3.setHorizontalTextPosition(SwingConstants.TRAILING);
			jLabel3.setHorizontalAlignment(SwingConstants.RIGHT);
			jLabel3.setFont(new Font("Dialog", Font.PLAIN, 12)); //$NON-NLS-1$
			jLabel = new JLabel();
			jLabel.setText(Messages.getString("MonitorPanel.interval")); //$NON-NLS-1$
			jPanel2 = new JPanel();
			jPanel2.setLayout(flowLayout);
			jPanel2.add(jLabel, null);
			jPanel2.add(jLabel3, null);
			jPanel2.add(jLabel4, null);
		}
		return jPanel2;
	}

	private void refreshServiceGroups() {
		final Matereal matereal = Matereal.getInstance();
		final Set<ServiceGroup> currentGroups = matereal.getServiceGroups();

		// Retain all groups.
		for (Iterator<Entry<ServiceHolder, DefaultMutableTreeNode>>
				it = groupNodeMap.entrySet().iterator(); it.hasNext();) {
			final Entry<ServiceHolder, DefaultMutableTreeNode> entry = it.next();
			final ServiceHolder group = entry.getKey();
			if (!currentGroups.contains(group) && group != matereal) {

				// Remove from the list view.
				root.remove(entry.getValue());

				// Remove services from serviceMap and serviceNodeMap.
				final List<Service> removedServices = serviceMap.remove(group);
				for (Service service : removedServices) {
					serviceNodeMap.remove(service);
				}

				// Remove from groupNodeMap
				it.remove();
			}
		}

		// Add new groups.
		for (ServiceHolder group : currentGroups) {
			if (!groupNodeMap.containsKey(group)) {
				final DefaultMutableTreeNode node =
						new DefaultMutableTreeNode(group);
				root.add(node);
				groupNodeMap.put(group, node);
			}
		}
	}

	private void refreshServices() {

		// Check services.
		boolean changed = refreshServices(Matereal.getInstance());
		for (ServiceHolder group : groupNodeMap.keySet()) {
			changed |= refreshServices(group);
		}

		// Refresh the view if needed.
		if (changed) {
			SwingUtilities.invokeLater(reloadJTree);
		}
	}

	private boolean refreshServices(ServiceHolder serviceHolder) {
		final List<Service> currentServices = serviceHolder.getServices();
		final DefaultMutableTreeNode root = groupNodeMap.get(serviceHolder);
		boolean changed = false;

		// Retain services.
		final List<Service> services = serviceMap.get(serviceHolder);
		if (services != null) {
			for (Service service : services) {
				if (!currentServices.contains(service)) {

					// Remove from the list view and serviceNodeMap.
					// System.out.println("removed: "+service);
					root.remove(
							serviceNodeMap.remove(service));
					changed = true;
				}
			}
		}

		// Refresh serviceMap.
		serviceMap.put(serviceHolder, currentServices);

		// Add new Services.
		for (Service service : currentServices) {
			if (!serviceNodeMap.containsKey(service)) {
				// System.out.println("added: "+service+" ("+service.getClass()+")");
				final DefaultMutableTreeNode node =
					new DefaultMutableTreeNode(service);
				root.add(node);
				serviceNodeMap.put(service, node);
				changed = true;
			}
		}
		return changed;
	}

	private void selectService(Service service) {
		if (service == null) {
			jLabel2.setText(""); //$NON-NLS-1$
			jLabel3.setText(""); //$NON-NLS-1$
			return;
		}
		jLabel2.setText(service.toString());
		jLabel3.setText(String.valueOf(service.getInterval()));
	}

	private void selectServiceGroup(ServiceHolder serviceHolder) {
		jLabel1.setText(serviceHolder.toString());
		selectService(null);
	}

	private class MonitorService extends ServiceAbstractImpl {
		final public static String SERVICE_NAME = "Monitor Pane Service"; //$NON-NLS-1$

		/** Get a name. */
		public String getName() { return SERVICE_NAME; }

		/**
		 * Refresh information.
		 */
		public void run() {
			refreshServiceGroups();
			refreshServices();
		}
	}

	private class SelectionListener implements TreeSelectionListener {

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
				selectServiceGroup((ServiceGroup) nodeInfo);
			}
		}

	}

}
