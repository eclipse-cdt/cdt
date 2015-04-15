/*******************************************************************************
 * Copyright (c) 2012, 2015 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.terminals.serial.interfaces;

/**
 * The properties specific to the wire type &quot;serial&quot;.
 */
public interface IWireTypeSerial {

	/**
	 * The data container.
	 */
	public static String PROPERTY_CONTAINER_NAME = "serial"; //$NON-NLS-1$

	/**
	 * The serial device name.
	 */
	public static final String PROPERTY_SERIAL_DEVICE = "device"; //$NON-NLS-1$

	/**
	 * The baud rate.
	 */
	public static final String PROPERTY_SERIAL_BAUD_RATE = "baudrate"; //$NON-NLS-1$

	/**
	 * The data bits
	 */
	public static final String PROPERTY_SERIAL_DATA_BITS = "databits"; //$NON-NLS-1$

	/**
	 * The parity
	 */
	public static final String PROPERTY_SERIAL_PARITY = "parity"; //$NON-NLS-1$

	/**
	 * The stop bits
	 */
	public static final String PROPERTY_SERIAL_STOP_BITS = "stopbits"; //$NON-NLS-1$

	/**
	 * The flow control
	 */
	public static final String PROPERTY_SERIAL_FLOW_CONTROL = "flowcontrol"; //$NON-NLS-1$
}
