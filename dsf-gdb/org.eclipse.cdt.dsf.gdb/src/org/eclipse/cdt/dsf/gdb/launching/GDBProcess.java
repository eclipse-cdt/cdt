/*******************************************************************************
 * Copyright (c) 2010, 2016 Marc-Andre Laperle and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Marc-Andre Laperle - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.launching;

import java.io.IOException;
import java.util.Map;

import org.eclipse.cdt.dsf.mi.service.command.AbstractCLIProcess;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IStreamMonitor;
import org.eclipse.debug.core.model.IStreamsProxy;
import org.eclipse.debug.core.model.RuntimeProcess;

/**
 * A process for the gdb backend to differentiate it from the inferior
 * 
 * @since 3.0
 */
public class GDBProcess extends RuntimeProcess {

	public GDBProcess(ILaunch launch, Process process, String name,
			Map<String, String> attributes) {
		super(launch, process, name, attributes);
	}

	@Override
	public IStreamsProxy getStreamsProxy() {
		IStreamsProxy proxy = super.getStreamsProxy();
		return proxy instanceof NoStreamsProxy ? null : proxy;
	}

	@Override
	protected IStreamsProxy createStreamsProxy() {
		// TRICKY.  This method is called by the constructor of
		// the super class.  This means we don't have time to
		// set any fields in this class by then.  Therefore,
		// we can only use what was set by the base class constructor
		// to figure out how to behave.
		// We can call getSystemProcess() as it is set earlier
		// in the constructor then when this method is called.
		if (!(getSystemProcess() instanceof AbstractCLIProcess)) {
			return new NoStreamsProxy();
		}
		return super.createStreamsProxy();
	}

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
