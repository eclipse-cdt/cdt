/*******************************************************************************
 * Copyright (c) 2018, 2022 Kichwa Coders Ltd. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jonah Graham (Kichwa Coders) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.gdbjtag.core.tests.jtagdevice;

import java.util.Collection;

import org.eclipse.cdt.debug.gdbjtag.core.IGDBJtagConnection;
import org.eclipse.cdt.debug.gdbjtag.core.jtagdevice.DefaultGDBJtagDeviceImpl;

public class GenericSerialNoExtendedRemoteInfo extends DefaultGDBJtagDeviceImpl implements IGDBJtagConnection {

	private String connection;

	@Override
	public void setDefaultDeviceConnection(String connection) {
		this.connection = connection;
	}

	@Override
	public void doRemote(String connection, Collection<String> commands) {
		if (connection != null) {
			addCmd(commands, "-target-select remote " + connection);
		}
	}

	@Override
	public String getDefaultDeviceConnection() {
		return connection;
	}

}
