/*******************************************************************************
 * Copyright (c) 2016 Oak Ridge National Laboratory and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.remote.internal.proxy.server.core;

import java.nio.ByteBuffer;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.remote.proxy.protocol.core.Protocol;

/**
 * This class controls all aspects of the application's execution
 */
public class Application implements IApplication {
	private Server server = new Server();

	public Object start(IApplicationContext context) throws Exception {
		String[] args = (String[]) context.getArguments().get(IApplicationContext.APPLICATION_ARGS);
		for (String arg : args) {
			if (arg.equals("-magic")) { //$NON-NLS-1$
				ByteBuffer b = ByteBuffer.allocate(4);
				b.putInt(Protocol.MAGIC);
				System.out.write(b.array());
			}
			;
		}
		server.start();
		server.waitFor();
		return IApplication.EXIT_OK;
	}

	public void stop() {
		// Nothing
	}
}
