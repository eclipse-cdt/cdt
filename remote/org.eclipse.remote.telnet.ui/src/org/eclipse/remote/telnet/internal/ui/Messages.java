/*******************************************************************************
 * Copyright (c) 2015 IBM Corporation, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial contribution
 *******************************************************************************/
package org.eclipse.remote.telnet.internal.ui;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.remote.telnet.internal.ui.messages"; //$NON-NLS-1$
	public static String NewSerialPortConnectionWizardPage_BaudRateLabel;
	public static String NewSerialPortConnectionWizardPage_ByteSizeLabel;
	public static String NewSerialPortConnectionWizardPage_Description;
	public static String NewSerialPortConnectionWizardPage_NameLabel;
	public static String NewSerialPortConnectionWizardPage_ParityLabel;
	public static String NewSerialPortConnectionWizardPage_PortLabel;
	public static String NewSerialPortConnectionWizardPage_StopBitsLabel;
	public static String NewSerialPortConnectionWizardPage_Title;
	public static String TelnetConnectionWizardPage_0;
	public static String TelnetConnectionWizardPage_1;
	public static String TelnetConnectionWizardPage_2;
	public static String TelnetConnectionWizardPage_3;
	public static String TelnetConnectionWizardPage_4;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
