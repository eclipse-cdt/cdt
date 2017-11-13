/*******************************************************************************
 * Copyright (c) 2017 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.launch.serial.ui.internal;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.cdt.launch.serial.ui.internal.messages"; //$NON-NLS-1$
	public static String NewSerialFlashTargetWizard_Title;
	public static String NewSerialFlashTargetWizardPage_CPUArchitecture;
	public static String NewSerialFlashTargetWizardPage_Description;
	public static String NewSerialFlashTargetWizardPage_Fetching;
	public static String NewSerialFlashTargetWizardPage_Name;
	public static String NewSerialFlashTargetWizardPage_OperatingSystem;
	public static String NewSerialFlashTargetWizardPage_SerialPort;
	public static String NewSerialFlashTargetWizardPage_Title;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
