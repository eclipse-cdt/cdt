/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.arduino.core;

import org.eclipse.remote.core.IRemoteConnection;

/**
 * Arduino specific extensions to IRemoteConnection.
 * 
 * @author dschaefer
 *
 */
public interface IArduinoRemoteConnection extends IRemoteConnection.Service {

	final String TYPE_ID = "org.eclipse.cdt.arduino.core.connectionType"; //$NON-NLS-1$
	final String PORT_NAME = "ardiuno.portname"; //$NON-NLS-1$
	final String BOARD_ID = "arduino.board"; //$NON-NLS-1$
	final String PLATFORM_ID = "arduino.platform"; //$NON-NLS-1$
	final String PACKAGE_ID = "arduino.package"; //$NON-NLS-1$

	/**
	 * Return the serial port name.
	 * 
	 * @return serial port name
	 */
	String getPortName();

	String getBoardId();

	String getPlatformId();

	String getPackageId();

	void pause();

	void resume();

}
