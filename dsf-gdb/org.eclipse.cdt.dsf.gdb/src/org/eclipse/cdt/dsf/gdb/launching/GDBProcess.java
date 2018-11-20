/*******************************************************************************
 * Copyright (c) 2010, 2016 Marc-Andre Laperle and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Marc-Andre Laperle - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.launching;

import java.io.IOException;
import java.util.Map;

import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IStreamMonitor;
import org.eclipse.debug.core.model.IStreamsProxy;
import org.eclipse.debug.core.model.RuntimeProcess;

/**
 * A process for the gdb backend to differentiate it from the inferior.
 *
 * This process disables the base class handling of IO streams since
 * all IO is handled by the different specialized {@link IDebuggerConsole}
 *
 * @since 3.0
 */
public class GDBProcess extends RuntimeProcess {

	public GDBProcess(ILaunch launch, Process process, String name, Map<String, String> attributes) {
		super(launch, process, name, attributes);
	}

	@Override
	public IStreamsProxy getStreamsProxy() {
		/**
		 * Returning null insures that there will not be a
		 * text console automatically created for this process
		 * in the standard console view.
		 *
		 * @see {@link ProcessConsoleManager#launchChanged}
		 */
		return null;
	}

	@Override
	protected IStreamsProxy createStreamsProxy() {
		/**
		 * The I/O handling does not go through this RuntimeProcess.
		 * Instead, the different consoles will connect directly to
		 * the process to obtain the input, output and error streams.
		 *
		 * @see {@link GdbFullCliConsolePage} and {@link GdbBasicCliConsole}
		 */
		return new NoStreamsProxy();
	}

	/**
	 * Class that provides a streams proxy that actually
	 * ignores the I/O streams.  We use this because the I/O
	 * is handled directly by the different {@link IDebuggerConsole}.
	 *
	 * This is different than NullStreamsProxy which would
	 * still read but discard the IO, which is not what we want.
	 */
	private class NoStreamsProxy implements IStreamsProxy {
		@Override
		public IStreamMonitor getErrorStreamMonitor() {
			return null;
		}

		@Override
		public IStreamMonitor getOutputStreamMonitor() {
			return null;
		}

		@Override
		public void write(String input) throws IOException {
		}
	}
}
