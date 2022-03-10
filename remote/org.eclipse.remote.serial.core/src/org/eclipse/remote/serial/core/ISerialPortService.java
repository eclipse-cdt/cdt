/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems, and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * QNX Software Systems - initial contribution
 *******************************************************************************/
package org.eclipse.remote.serial.core;

import org.eclipse.cdt.serial.SerialPort;
import org.eclipse.remote.core.IRemoteConnection;

public interface ISerialPortService extends IRemoteConnection.Service {

	static final String CONNECTION_TYPE_ID = "org.eclipse.remote.serial.core.connectionType"; //$NON-NLS-1$
	static final String PORT_NAME_ATTR = "serial.portName"; //$NON-NLS-1$
	static final String BAUD_RATE_ATTR = "serial.baudRate"; //$NON-NLS-1$
	static final String BYTE_SIZE_ATTR = "serial.byteSize"; //$NON-NLS-1$
	static final String PARITY_ATTR = "serial.parity"; //$NON-NLS-1$
	static final String STOP_BITS_ATTR = "serial.stopBits"; //$NON-NLS-1$

	SerialPort getSerialPort();

}
