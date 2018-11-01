/*******************************************************************************
 * Copyright (c) 2006, 2018 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License 2.0 
 * which accompanies this distribution, and is available at 
 * https://www.eclipse.org/legal/epl-2.0/ 
 * 
 * Contributors: 
 * Michael Scharf (Wind River) - initial API and implementation
 * Martin Oberhuber (Wind River) - fixed copyright headers and beautified
 *******************************************************************************/
package org.eclipse.tm.terminal.connector.serial.connector;

import org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore;

public interface ISerialSettings {

	String getSerialPort();
	int getBaudRate();
	int getDataBits();
	int getStopBits();
	int getParity();
	int getFlowControl();
	int getTimeout();
	String getSummary();
	void load(ISettingsStore store);
	void save(ISettingsStore store);
}