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

import java.awt.GridBagLayout;
import javax.swing.JPanel;
import java.awt.BorderLayout;

import javax.swing.JLabel;

import jp.digitalmuseum.mr.entity.Resource;
import jp.digitalmuseum.mr.resource.Wheels;
import java.awt.GridBagConstraints;
import java.awt.Insets;

public class ResourceViewer extends JPanel implements DisposableComponent {

	private static final long serialVersionUID = 1L;
	private JPanel jInformationPanel = null;
	private JLabel jClassTitleLabel = null;
	private JLabel jClassLabel = null;
	private JLabel jStatusTitleLabel = null;
	private JLabel jStatusLabel = null;
	private JPanel jStatusPanel = null;
	private transient Resource resource;

	/**
	 * This is the default constructor
	 */
	public ResourceViewer(Resource resource) {
		super();
		this.resource = resource;
		initialize();
		update();
	}

	/**
	 * This method initializes this
	 *
	 * @return void
	 */
	private void initialize() {
		this.setLayout(new BorderLayout());
		this.setSize(300, 200);
		this.add(getJInformationPanel(), BorderLayout.NORTH);
	}

	public void dispose() {
		if (getJStatusPanel() instanceof DisposableComponent) {
			((DisposableComponent) getJStatusPanel()).dispose();
		}
	}

	/**
	 * This method initializes jInformationPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJInformationPanel() {
		if (jInformationPanel == null) {
			GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
			gridBagConstraints3.gridx = 1;
			gridBagConstraints3.gridy = 1;
			gridBagConstraints3.anchor = GridBagConstraints.WEST;
			gridBagConstraints3.insets = new Insets(5, 5, 5, 5);
			gridBagConstraints3.fill = GridBagConstraints.BOTH;
			gridBagConstraints3.gridwidth = 3;
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			gridBagConstraints2.gridy = 1;
			gridBagConstraints2.anchor = GridBagConstraints.WEST;
			gridBagConstraints2.insets = new Insets(5, 5, 5, 5);
			gridBagConstraints2.gridx = 0;
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			gridBagConstraints1.gridy = 0;
			gridBagConstraints1.anchor = GridBagConstraints.WEST;
			gridBagConstraints1.weightx = 0.9D;
			gridBagConstraints1.insets = new Insets(5, 5, 5, 5);
			gridBagConstraints1.gridx = 1;
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridy = 0;
			gridBagConstraints.anchor = GridBagConstraints.WEST;
			gridBagConstraints.weightx = 0.1D;
			gridBagConstraints.insets = new Insets(5, 5, 5, 5);
			gridBagConstraints.gridx = 0;
			jStatusLabel = new JLabel();
			jStatusLabel.setText("JLabel");
			jStatusTitleLabel = new JLabel();
			jStatusTitleLabel.setText("Status:");
			jClassLabel = new JLabel();
			jClassLabel.setText("JLabel");
			jClassTitleLabel = new JLabel();
			jClassTitleLabel.setText("Implementation:");
			jInformationPanel = new JPanel();
			jInformationPanel.setLayout(new GridBagLayout());
			jInformationPanel.add(jClassTitleLabel, gridBagConstraints);
			jInformationPanel.add(jClassLabel, gridBagConstraints1);
			jInformationPanel.add(jStatusTitleLabel, gridBagConstraints2);
			jInformationPanel.add(getJStatusPanel(), gridBagConstraints3);
		}
		return jInformationPanel;
	}

	/**
	 * This method initializes jStatusPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJStatusPanel() {
		if (jStatusPanel == null) {
			if (resource instanceof Wheels) {
				jStatusPanel = new WheelsViewer((Wheels) resource);
			} else {
				jStatusPanel = new JPanel();
			}
		}
		return jStatusPanel;
	}

	public void update() {
		jClassLabel.setText(resource.getClass().getSimpleName());
	}

}
