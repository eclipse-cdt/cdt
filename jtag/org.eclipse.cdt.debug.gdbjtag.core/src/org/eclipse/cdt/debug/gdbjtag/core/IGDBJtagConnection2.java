/*******************************************************************************
 * Copyright (c) 2018 Kichwa Coders Ltd. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jonah Graham (Kichwa Coders) - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.gdbjtag.core;

import java.util.Collection;

/**
 * Extension to IGDBJtagConnection for connections supporting extended-remote
 *
 * @since 9.2
 */
public interface IGDBJtagConnection2 extends IGDBJtagConnection {

	/**
	 * Whether target support extended-remote for connections.
	 */
	default public boolean getSupportsExtendedRemote() {
		return true;
	}

	/**
	 * Supports only extended remote.
	 */
	default public boolean getSupportsOnlyExtendedRemote() {
		return false;
	}

	/**
	 * Commands to connect to extended-remote JTAG device
	 *
	 * @param connection
	 *            defines the gdb string required to establish a connection to the
	 *            target
	 * @param commands
	 *            gdb commands to execute on the remote device (usually the target
	 *            probe)
	 */
	default public void doExtendedRemote(String connection, Collection<String> commands) {
		throw new UnsupportedOperationException("Connection does not support extended-remote connection");
	}
}
