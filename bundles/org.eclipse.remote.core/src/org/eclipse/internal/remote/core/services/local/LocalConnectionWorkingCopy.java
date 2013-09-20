/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.internal.remote.core.services.local;

import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionWorkingCopy;

public class LocalConnectionWorkingCopy extends LocalConnection implements IRemoteConnectionWorkingCopy {

	private final LocalConnection fOriginal;

	public LocalConnectionWorkingCopy(LocalConnection connection) {
		super(connection.getRemoteServices());
		fOriginal = connection;
	}

	@Override
	public IRemoteConnection save() {
		return fOriginal;
	}

	@Override
	public void setAddress(String address) {
		// Do nothing
	}

	@Override
	public void setAttribute(String key, String value) {
		// Do nothing
	}

	@Override
	public void setName(String name) {
		// Do nothing
	}

	@Override
	public void setPassword(String password) {
		// Do nothing
	}

	@Override
	public void setPort(int port) {
		// Do nothing
	}

	@Override
	public void setUsername(String username) {
		// Do nothing
	}
}
