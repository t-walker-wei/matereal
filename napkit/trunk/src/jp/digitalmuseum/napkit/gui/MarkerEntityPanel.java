/*
 * PROJECT: napkit at http://mr.digitalmuseum.jp/
 * ----------------------------------------------------------------------------
 *
 * This file is part of NyARToolkit Application Toolkit.
 *
 * NyARToolkit Application Toolkit, or simply "napkit",
 * is a simple wrapper library for NyARToolkit.
 *
 * ----------------------------------------------------------------------------
 *
 * License version: GPL 3.0
 *
 * napkit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * napkit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with napkit. If not, see <http://www.gnu.org/licenses/>.
 */
package jp.digitalmuseum.napkit.gui;

import java.awt.Component;
import java.awt.GridBagLayout;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.util.HashMap;

import javax.swing.JList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import jp.digitalmuseum.napkit.NapMarker;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import javax.swing.ListSelectionModel;
import java.awt.Insets;
import java.awt.Font;
import javax.swing.SwingConstants;

import com.phybots.entity.Entity;
import com.phybots.message.Event;
import com.phybots.message.EventListener;
import com.phybots.message.ServiceEvent;
import com.phybots.message.ServiceStatus;
import com.phybots.message.ServiceUpdateEvent;
import com.phybots.service.MarkerDetector;

public class MarkerEntityPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private JList jMarkerList = null;
	private MarkerImagePanel markerImagePanel = null;
	private JPanel jEntityInformationPanel = null;
	private transient MarkerDetector markerDetector;
	private transient DefaultListModel model;
	private transient HashMap<Object, Icon> icons;  //  @jve:decl-index=0:
	private JLabel jLabel = null;
	private JLabel jLabel1 = null;

	/**
	 * This is the default constructor
	 */
	public MarkerEntityPanel(MarkerDetector markerDetector) {
		super();
		initialize();
		this.markerDetector = markerDetector;
		synchronized (this.markerDetector) {
			for (NapMarker marker : markerDetector.getMarkers()) {
				addMarker(marker);
			}
			this.markerDetector.addEventListener(new EventListener() {

				public void eventOccurred(Event e) {
					if (e instanceof ServiceUpdateEvent) {
						String prm = ((ServiceUpdateEvent) e).getParameter();
						if (prm == "marker registration") { //$NON-NLS-1$
							NapMarker marker = (NapMarker) ((ServiceUpdateEvent) e).getValue();
							addMarker(marker);
						} else if (prm == "marker unregistration") { //$NON-NLS-1$
							NapMarker marker = (NapMarker) ((ServiceUpdateEvent) e).getValue();
							removeMarker(marker);
						}
					} else if (e instanceof ServiceEvent
							&& ((ServiceEvent) e).getStatus() == ServiceStatus.DISPOSED) {
						setEnabled(false);
						MarkerEntityPanel.this.markerDetector = null;
					}
				}
			});
		}
		getJMarkerList().addListSelectionListener(new ListSelectionListener() {

			public void valueChanged(ListSelectionEvent e) {
				Object value = getJMarkerList().getSelectedValue();
				if (value != null && value instanceof NapMarker) {
					selectMarker((NapMarker) value);
				}
			}
		});
	}

	private void addMarker(NapMarker marker) {
		model.addElement(marker);
	}

	private void removeMarker(NapMarker marker) {
		model.removeElement(marker);
	}

	private void selectMarker(NapMarker marker) {
		getMarkerImagePanel().setMarker(marker);
	}

	/**
	 * This method initializes this
	 *
	 * @return void
	 */
	private void initialize() {
		GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
		gridBagConstraints4.gridx = 1;
		gridBagConstraints4.gridwidth = 2;
		gridBagConstraints4.fill = GridBagConstraints.BOTH;
		gridBagConstraints4.weighty = 0.0D;
		gridBagConstraints4.insets = new Insets(0, 0, 5, 0);
		gridBagConstraints4.gridy = 1;
		jLabel1 = new JLabel();
		jLabel1.setText(Messages.getString("MarkerEntityPanel.0")); //$NON-NLS-1$
		jLabel1.setHorizontalAlignment(SwingConstants.RIGHT);
		GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
		gridBagConstraints3.fill = GridBagConstraints.BOTH;
		gridBagConstraints3.insets = new Insets(0, 0, 5, 5);
		jLabel = new JLabel();
		jLabel.setText(Messages.getString("MarkerEntityPanel.2")); //$NON-NLS-1$
		jLabel.setFont(new Font("Dialog", Font.BOLD, 12)); //$NON-NLS-1$
		GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
		gridBagConstraints2.gridx = 1;
		gridBagConstraints2.gridy = 2;
		gridBagConstraints2.fill = GridBagConstraints.BOTH;
		gridBagConstraints2.gridwidth = 2;
		gridBagConstraints2.weighty = 0.85D;
		GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
		gridBagConstraints11.gridx = 2;
		gridBagConstraints11.gridy = 0;
		gridBagConstraints11.weightx = 0.85D;
		gridBagConstraints11.fill = GridBagConstraints.BOTH;
		gridBagConstraints11.insets = new Insets(0, 0, 5, 0);
		gridBagConstraints11.weighty = 0.15D;
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.weightx = 0.15D;
		gridBagConstraints.insets = new Insets(0, 0, 0, 5);
		gridBagConstraints.gridheight = 3;
		gridBagConstraints.weighty = 1D;
		this.setSize(375, 300);
		this.setLayout(new GridBagLayout());
		this.setPreferredSize(new Dimension(375, 300));
		this.add(getJMarkerList(), gridBagConstraints);
		this.add(jLabel, gridBagConstraints3);
		this.add(getMarkerImagePanel(), gridBagConstraints11);
		this.add(getJEntityInformationPanel(), gridBagConstraints2);
		this.add(jLabel1, gridBagConstraints4);
		icons = new HashMap<Object, Icon>();
		model = new DefaultListModel();
		getJMarkerList().setModel(model);
	}

	/**
	 * Get icon object for the provided marker object.
	 * @param marker napkit marker object
	 * @return Icon object
	 */
	private Icon getIcon(Object marker) {
		if (icons.containsKey(marker)) {
			return icons.get(marker);
		} else if (marker instanceof NapMarker) {
			Icon icon = new ImageIcon(((NapMarker) marker).getImage(0));
			icons.put(marker, icon);
			return icon;
		}
		return null;
	}

	/**
	 * This method initializes jMarkerList
	 *
	 * @return javax.swing.JList
	 */
	private JList getJMarkerList() {
		if (jMarkerList == null) {
			jMarkerList = new JList();
			jMarkerList.setCellRenderer(new DefaultListCellRenderer() {
				private static final long serialVersionUID = 1L;
				@Override
				public Component getListCellRendererComponent(JList jList,
						Object value, int modelIndex, boolean isSelected,
						boolean cellHasFocus) {
					JLabel label = (JLabel) super.getListCellRendererComponent(
							jList, value, modelIndex, isSelected, cellHasFocus);
					label.setIcon(MarkerEntityPanel.this.getIcon(value));
					if (value instanceof NapMarker) {
						Entity e = markerDetector.getEntity((NapMarker) value);
						label.setText(e == null ? "" : e.getName());
					}
					return label;
				}
			});
			jMarkerList.setFixedCellHeight(20);
			jMarkerList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			jMarkerList.setPreferredSize(new Dimension(160, 200));
		}
		return jMarkerList;
	}

	/**
	 * This method initializes jMarkerImagePanel
	 *
	 * @return javax.swing.JPanel
	 */
	private MarkerImagePanel getMarkerImagePanel() {
		if (markerImagePanel == null) {
			markerImagePanel = new MarkerImagePanel();
			markerImagePanel.setMinimumSize(new Dimension(115, 42));
			markerImagePanel.setPreferredSize(new Dimension(200, 48));
		}
		return markerImagePanel;
	}

	/**
	 * This method initializes jEntityInformationPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJEntityInformationPanel() {
		if (jEntityInformationPanel == null) {
			jEntityInformationPanel = new JPanel();
			jEntityInformationPanel.setLayout(new GridBagLayout());
		}
		return jEntityInformationPanel;
	}

}
