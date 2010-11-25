package jp.digitalmuseum.napkit.gui;

import java.awt.Component;
import java.awt.GridBagLayout;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.util.HashMap;

import javax.swing.JList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import jp.digitalmuseum.mr.message.Event;
import jp.digitalmuseum.mr.message.EventListener;
import jp.digitalmuseum.mr.message.ServiceUpdateEvent;
import jp.digitalmuseum.mr.service.MarkerDetector;
import jp.digitalmuseum.napkit.NapMarker;
import java.awt.Dimension;

public class MarkerEntityPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private JList jMarkerList = null;
	private JPanel jEntityPanel = null;
	private MarkerImagePanel markerImagePanel = null;
	private JPanel jEntityInformationPanel = null;
	private transient MarkerDetector markerDetector;
	private transient DefaultListModel model;
	private transient HashMap<Object, Icon> icons;  //  @jve:decl-index=0:

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
						if (prm == "marker registration") {
							NapMarker marker = (NapMarker) ((ServiceUpdateEvent) e).getValue();
							addMarker(marker);
						} else if (prm == "marker unregistration") {
							NapMarker marker = (NapMarker) ((ServiceUpdateEvent) e).getValue();
							removeMarker(marker);
						}
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
		this.setSize(300, 200);
		this.setLayout(new BorderLayout());
		this.add(getJMarkerList(), BorderLayout.WEST);
		this.add(getJEntityPanel(), BorderLayout.CENTER);
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
					return label;
				}
			});
			jMarkerList.setFixedCellHeight(20);
			jMarkerList.setPreferredSize(new Dimension(200, 200));
		}
		return jMarkerList;
	}

	/**
	 * This method initializes jEntityPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJEntityPanel() {
		if (jEntityPanel == null) {
			jEntityPanel = new JPanel();
			jEntityPanel.setLayout(new BorderLayout());
			jEntityPanel.add(getMarkerImagePanel(), BorderLayout.NORTH);
			jEntityPanel.add(getJEntityInformationPanel(), BorderLayout.CENTER);
		}
		return jEntityPanel;
	}

	/**
	 * This method initializes jMarkerImagePanel
	 *
	 * @return javax.swing.JPanel
	 */
	private MarkerImagePanel getMarkerImagePanel() {
		if (markerImagePanel == null) {
			markerImagePanel = new MarkerImagePanel();
			markerImagePanel.setPreferredSize(new Dimension(240, 48));
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
