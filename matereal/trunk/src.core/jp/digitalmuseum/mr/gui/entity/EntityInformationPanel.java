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
package jp.digitalmuseum.mr.gui.entity;

import javax.swing.JPanel;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import java.awt.GridBagConstraints;
import java.awt.Insets;

import jp.digitalmuseum.mr.entity.Entity;

public class EntityInformationPanel extends JPanel {
	private static final long serialVersionUID = 1899762075174274625L;

	private JLabel jEntityNameLabel = null;
	private JLabel jEntityTypeNameLabel = null;
	private JPanel jEntityShapePanel = null;

	private transient Entity entity;

	/**
	 * This method initializes
	 *
	 */
	public EntityInformationPanel(Entity entity) {
		super();
		this.entity = entity;
		initialize();
	}

	/**
	 * This method initializes this
	 *
	 */
	private void initialize() {
        GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
        gridBagConstraints2.gridx = 0;
        gridBagConstraints2.gridheight = 2;
        gridBagConstraints2.insets = new Insets(5, 0, 5, 5);
        gridBagConstraints2.gridy = 0;
        GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
        gridBagConstraints1.gridx = 1;
        gridBagConstraints1.insets = new Insets(0, 5, 5, 5);
        gridBagConstraints1.gridy = 1;
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new Insets(5, 5, 5, 5);
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        jEntityTypeNameLabel = new JLabel();
        jEntityTypeNameLabel.setText(entity.getTypeName());
        jEntityNameLabel = new JLabel();
        jEntityNameLabel.setText(entity.getName());
        this.setLayout(new GridBagLayout());
        this.add(getJEntityShapePanel(), gridBagConstraints2);
        this.add(jEntityNameLabel, gridBagConstraints);
        this.add(jEntityTypeNameLabel, gridBagConstraints1);

	}

	/**
	 * This method initializes jEntityShapePanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJEntityShapePanel() {
		if (jEntityShapePanel == null) {
			jEntityShapePanel = new EntityShapePanel(entity);
		}
		return jEntityShapePanel;
	}

}
