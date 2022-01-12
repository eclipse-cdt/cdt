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
package org.eclipse.remote.core;

import org.eclipse.core.expressions.PropertyTester;

/**
 * @since 2.1
 */
public class RemoteConnectionPropertyTester extends PropertyTester {

	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if (receiver instanceof IRemoteConnection) {
			if (property.equals("isConnectionType")) {
				IRemoteConnection connection = (IRemoteConnection) receiver;
				if (connection.getConnectionType().getId().equals(expectedValue)) {
					return true;
				}
			}
		}
		return false;
	}
}
