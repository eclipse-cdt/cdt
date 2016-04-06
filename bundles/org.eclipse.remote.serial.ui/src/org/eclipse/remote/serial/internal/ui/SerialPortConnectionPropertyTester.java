/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - initial contribution
 *******************************************************************************/
package org.eclipse.remote.serial.internal.ui;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.serial.core.ISerialPortService;

public class SerialPortConnectionPropertyTester extends PropertyTester {

	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if (receiver instanceof IRemoteConnection) {
			IRemoteConnection remote = (IRemoteConnection) receiver;
			return remote.hasService(ISerialPortService.class);
		} else {
			return false;
		}
	}

}
