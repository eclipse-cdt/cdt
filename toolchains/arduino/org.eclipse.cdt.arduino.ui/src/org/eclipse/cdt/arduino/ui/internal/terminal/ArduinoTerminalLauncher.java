/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.arduino.ui.internal.terminal;

import org.eclipse.tm.terminal.connector.cdtserial.launcher.SerialLauncherDelegate;
import org.eclipse.tm.terminal.view.ui.interfaces.IConfigurationPanel;
import org.eclipse.tm.terminal.view.ui.interfaces.IConfigurationPanelContainer;
import org.eclipse.tm.terminal.view.ui.interfaces.ILauncherDelegate;

public class ArduinoTerminalLauncher extends SerialLauncherDelegate implements ILauncherDelegate {

	@Override
	public IConfigurationPanel getPanel(IConfigurationPanelContainer container) {
		return new ArduinoTerminalConfigPanel(container);
	}

}
