/*******************************************************************************
 * Copyright (c) 2012, 2015 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.terminal.connector.serial.nls;

import org.eclipse.osgi.util.NLS;

/**
 * Externalized strings management.
 */
public class Messages extends NLS {

	// The plug-in resource bundle name
	private static final String BUNDLE_NAME = "org.eclipse.tm.terminal.view.ui.serial.nls.Messages"; //$NON-NLS-1$

	/**
	 * Static constructor.
	 */
	static {
		// Load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	// **** Declare externalized string id's down here *****

	public static String SerialLauncherDelegate_terminalTitle;
	public static String SerialLauncherDelegate_terminalTitle_default;

	public static String SerialLinePanel_hostTTYDevice_label;
	public static String SerialLinePanel_hostTTYSpeed_label;
	public static String SerialLinePanel_hostTTYDatabits_label;
	public static String SerialLinePanel_hostTTYParity_label;
	public static String SerialLinePanel_hostTTYStopbits_label;
	public static String SerialLinePanel_hostTTYFlowControl_label;
	public static String SerialLinePanel_hostTTYTimeout_label;
	public static String SerialLinePanel_customSerialBaudRate_title;
	public static String SerialLinePanel_customSerialBaudRate_message;
	public static String SerialLinePanel_error_invalidCharactes;
	public static String SerialLinePanel_error_invalidCharactesBaudRate;
	public static String SerialLinePanel_error_emptyHostTTYDevice;
	public static String SerialLinePanel_error_emptyHostTTYSpeedRate;
	public static String SerialLinePanel_error_emptyHostTTYDatabits;
	public static String SerialLinePanel_error_emptyHostTTYParity;
	public static String SerialLinePanel_error_emptyHostTTYStopbits;
	public static String SerialLinePanel_error_emptyHostTTYFlowControl;
	public static String SerialLinePanel_error_emptyHostTTYTimeout;
	public static String SerialLinePanel_info_editableTTYDeviceSelected;
	public static String SerialLinePanel_info_editableTTYBaudRateSelected;
	public static String SerialLinePanel_warning_FailedToLoadSerialPorts;

	public static String SerialPortAddressDialog_dialogtitle;
	public static String SerialPortAddressDialog_title;
	public static String SerialPortAddressDialog_message;
	public static String SerialPortAddressDialog_address;
	public static String SerialPortAddressDialog_port;
	public static String SerialPortAddressDialog_Information_MissingTargetNameAddress;
	public static String SerialPortAddressDialog_Error_InvalidTargetNameAddress;
	public static String SerialPortAddressDialog_Error_InvalidTargetIpAddress;
	public static String SerialPortAddressDialog_Information_MissingPort;
	public static String SerialPortAddressDialog_Error_InvalidPort;
	public static String SerialPortAddressDialog_Error_InvalidPortRange;

	public static String SerialConnector_Error_LiberayNotInstalled;

	public static String SerialConnectWorker_PROP_TITLE;
	public static String SerialConnectWorker_PORT_IN_USE;
	public static String SerialConnectWorker_ANOTHER_TERMINAL;
	public static String SerialConnectWorker_PORT_STOLEN;
	public static String SerialConnectWorker_PORT_NOT_STOLEN;
	public static String SerialConnectWorker_NO_SUCH_PORT;
	public static String SerialConnectWorker_OWNERSHIP_GRANTED;

}
