/*******************************************************************************
 * Copyright (c) 2020 Kichwa Coders Canada Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.tm.terminal.view.ui.internal.dialogs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.tm.terminal.view.ui.interfaces.IConfigurationPanelContainer;
import org.eclipse.tm.terminal.view.ui.panels.AbstractConfigurationPanel;

/**
 * An empty configuration panel implementation.
 */
public class ErrorSettingsPanel extends AbstractConfigurationPanel {

	private String errorMessage;

	/**
	 * Constructor.
	 *
	 * @param container The configuration panel container or <code>null</code>.
	 */
	public ErrorSettingsPanel(IConfigurationPanelContainer container, String errorMessage) {
		super(container);
		this.errorMessage = errorMessage;
	}

	@Override
	public void setupPanel(Composite parent) {
		Composite panel = new Composite(parent, SWT.NONE);
		panel.setLayout(new GridLayout());
		panel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		panel.setBackground(parent.getBackground());

		if (errorMessage != null) {
			Label label = new Label(panel, SWT.NONE);
			label.setText(errorMessage);
		}

		setControl(panel);
	}

	@Override
	public boolean isValid() {
		return false;
	}
}
