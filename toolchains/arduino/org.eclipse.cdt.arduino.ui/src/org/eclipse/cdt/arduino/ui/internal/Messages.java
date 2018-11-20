/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.arduino.ui.internal;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.cdt.arduino.ui.internal.messages"; //$NON-NLS-1$
	public static String ArduinoLaunchConsole_0;
	public static String ArduinoLaunchConsole_1;
	public static String ArduinoLaunchConsole_2;
	public static String ArduinoTargetPropertyPage_0;
	public static String ArduinoTargetPropertyPage_1;
	public static String ArduinoTargetPropertyPage_2;
	public static String NewArduinoProjectWizard_0;
	public static String NewArduinoTargetWizardPage_0;
	public static String NewArduinoTargetWizardPage_1;
	public static String NewArduinoTargetWizardPage_2;
	public static String NewArduinoTargetWizardPage_3;
	public static String NewArduinoTargetWizardPage_4;
	public static String NewArduinoTargetWizardPage_5;
	public static String LibrariesPropertyPage_0;
	public static String LibrariesPropertyPage_1;
	public static String LibrariesPropertyPage_desc;
	public static String ArduinoPreferencePage_desc;
	public static String PlatformDetailsDialog_0;
	public static String PlatformDetailsDialog_1;
	public static String ArduinoTerminalSettingsPage_BoardName;
	public static String ArduinoTerminalSettingsPage_SerialPort;
	public static String ArduinoTerminalSettingsPage_BaudRate;
	public static String ArduinoTerminalSettingsPage_DataSize;
	public static String ArduinoTerminalSettingsPage_Parity;
	public static String ArduinoTerminalSettingsPage_StopBits;
	public static String ArduinoTerminalSettingsPage_UnknownPort;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
