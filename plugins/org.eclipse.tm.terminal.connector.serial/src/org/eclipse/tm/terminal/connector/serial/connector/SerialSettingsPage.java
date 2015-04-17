/*******************************************************************************
 * Copyright (c) 2003, 2013 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following Wind River employees contributed to the Terminal component
 * that contains this file: Chris Thew, Fran Litterio, Stephen Lamb,
 * Helmut Haigermoser and Ted Williams.
 *
 * Contributors:
 * Michael Scharf (Wind River) - extracted from TerminalSettingsDlg
 * Martin Oberhuber (Wind River) - fixed copyright headers and beautified
 * Martin Oberhuber (Wind River) - [206917] Add validation for Terminal Settings
 *******************************************************************************/
package org.eclipse.tm.terminal.connector.serial.connector;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.tm.internal.terminal.provisional.api.AbstractSettingsPage;

@SuppressWarnings("restriction")
public class SerialSettingsPage extends AbstractSettingsPage {
	private final SerialSettings fTerminalSettings;

	public SerialSettingsPage(SerialSettings settings) {
		fTerminalSettings=settings;
	}

	@Override
    public void createControl(Composite parent) {
    }

	@Override
    public void loadSettings() {
    }

	@Override
    public void saveSettings() {
    }

	@Override
    public boolean validateSettings() {
	    return false;
    }
}
