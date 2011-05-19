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

import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;

import jp.digitalmuseum.mr.entity.Entity;
import jp.digitalmuseum.mr.gui.Messages;

import javax.swing.JLabel;
import java.awt.Insets;
import java.awt.Dimension;

public class EntityPanel extends JPanel {

	private static final long serialVersionUID = 5147751877583600512L;
	private EntityShapePanel entityShapePanel = null;
	private transient Entity entity;
	private JLabel jEntityTypeLabel = null;
	private JLabel jEntityTypeNameLabel = null;

	/**
	 * This method initializes
	 *
	 */
	public EntityPanel(Entity entity) {
		super();
		this.entity = entity;
		initialize();
	}

	/**
	 * This method initializes this
	 *
	 */
	private void initialize() {
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.weightx = 1.0D;
        gridBagConstraints.insets = new Insets(0, 5, 5, 5);
        gridBagConstraints.weighty = 1.0D;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.gridx = 1;
        jEntityTypeNameLabel = new JLabel();
		final Dimension d = new Dimension(320, 70);
		jEntityTypeNameLabel.setMinimumSize(d);
		jEntityTypeNameLabel.setPreferredSize(d);
        jEntityTypeNameLabel.setText(entity.getTypeName());
        GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
        gridBagConstraints2.fill = GridBagConstraints.BOTH;
        gridBagConstraints2.gridx = 0;
        gridBagConstraints2.gridy = 0;
        gridBagConstraints2.insets = new Insets(5, 5, 5, 5);
        gridBagConstraints2.weightx = 1.0D;
        gridBagConstraints2.weighty = 0.0D;
        gridBagConstraints2.gridwidth = 2;
        jEntityTypeLabel = new JLabel();
		jEntityTypeLabel.setFont(new Font("Dialog", Font.BOLD, 12)); //$NON-NLS-1$
        jEntityTypeLabel.setText(Messages.getString("EntityTypePanel.entityType"));
        GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 1;
        gridBagConstraints1.insets = new Insets(0, 5, 5, 0);
        gridBagConstraints1.weighty = 1.0D;
        gridBagConstraints1.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints1.fill = GridBagConstraints.HORIZONTAL;
        this.setLayout(new GridBagLayout());
        this.add(jEntityTypeLabel, gridBagConstraints2);
        this.add(getEntityShapePanel(), gridBagConstraints1);
        this.add(jEntityTypeNameLabel, gridBagConstraints);
	}

	/**
	 * This method initializes jPanel1
	 *
	 * @return javax.swing.JPanel
	 */
	private EntityShapePanel getEntityShapePanel() {
		if (entityShapePanel == null) {
			entityShapePanel = new EntityShapePanel(entity);
			final Dimension d = new Dimension(70, 70);
			entityShapePanel.setMinimumSize(d);
			entityShapePanel.setPreferredSize(d);
		}
		return entityShapePanel;
	}

}