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
package com.phybots.gui.entity;

import javax.swing.JPanel;

import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;


import javax.swing.JLabel;

import com.phybots.Phybots;
import com.phybots.entity.Entity;
import com.phybots.gui.DisposableComponent;
import com.phybots.gui.Messages;
import com.phybots.message.EntityEvent;
import com.phybots.message.EntityStatus;
import com.phybots.message.Event;
import com.phybots.message.EventListener;

import java.awt.Insets;
import java.awt.Dimension;

public class EntityPanel extends JPanel implements DisposableComponent {

	private static final long serialVersionUID = 5147751877583600512L;
	private EntityShapePanel entityShapePanel = null;
	private transient Entity entity;
	private JLabel entityTypeLabel = null;
	private JLabel entityTypeNameLabel = null;

	/**
	 * This method initializes
	 *
	 */
	public EntityPanel(Entity entity) {
		super();
		this.entity = entity;
		entity.addEventListener(new EventListener() {
			public void eventOccurred(Event e) {
				if (e instanceof EntityEvent
						&& ((EntityEvent) e).getStatus() == EntityStatus.DISPOSED) {
					dispose();
				}
			}
		});
		initialize();
	}

	public void dispose() {
		setEnabled(false);
		entity = null;
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
		entityTypeNameLabel = new JLabel();
		final Dimension d = new Dimension(320, 70);
		entityTypeNameLabel.setMinimumSize(d);
		entityTypeNameLabel.setPreferredSize(d);
		entityTypeNameLabel.setFont(Phybots.getInstance().getDefaultFont());
		entityTypeNameLabel.setText(entity.getTypeName());
		GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
		gridBagConstraints2.fill = GridBagConstraints.BOTH;
		gridBagConstraints2.gridx = 0;
		gridBagConstraints2.gridy = 0;
		gridBagConstraints2.insets = new Insets(5, 5, 5, 5);
		gridBagConstraints2.weightx = 1.0D;
		gridBagConstraints2.weighty = 0.0D;
		gridBagConstraints2.gridwidth = 2;
		entityTypeLabel = new JLabel();
		entityTypeLabel.setFont(Phybots.getInstance().getDefaultFont().deriveFont(Font.BOLD));
		entityTypeLabel.setText(Messages.getString("EntityPanel.entityType"));
		GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
		gridBagConstraints1.gridx = 0;
		gridBagConstraints1.gridy = 1;
		gridBagConstraints1.insets = new Insets(0, 5, 5, 0);
		gridBagConstraints1.weighty = 1.0D;
		gridBagConstraints1.anchor = GridBagConstraints.NORTHWEST;
		gridBagConstraints1.fill = GridBagConstraints.HORIZONTAL;
		this.setLayout(new GridBagLayout());
		this.add(entityTypeLabel, gridBagConstraints2);
		this.add(getEntityShapePanel(), gridBagConstraints1);
		this.add(entityTypeNameLabel, gridBagConstraints);
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
