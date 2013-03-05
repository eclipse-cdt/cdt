/*******************************************************************************
 * Copyright (c) 2013 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Marc Khouzam (Ericsson) - Initial API and implementation 
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.service;

import java.util.Hashtable;

import org.eclipse.cdt.dsf.concurrent.ImmediateRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints;
import org.eclipse.cdt.dsf.debug.service.IBreakpointsExtension;
import org.eclipse.cdt.dsf.mi.service.IMICommandControl;
import org.eclipse.cdt.dsf.mi.service.MIBreakpoints;
import org.eclipse.cdt.dsf.service.DsfSession;

/**
 * Breakpoint service for GDB 7.6.
 *
 * @since 4.2
 */
public class GDBBreakpoints_7_6 extends GDBBreakpoints_7_4
{
	private IMICommandControl fConnection;

	public GDBBreakpoints_7_6(DsfSession session) {
		super(session);
	}

	@Override
	public void initialize(final RequestMonitor rm) {
		super.initialize(new ImmediateRequestMonitor(rm) {
			@Override
			protected void handleSuccess() {
				doInitialize(rm);
			}
		});
	}

	private void doInitialize(final RequestMonitor rm) {
    	// Get the services references
		fConnection = getServicesTracker().getService(IMICommandControl.class);
		
		// Register this service
		register(new String[] { IBreakpoints.class.getName(),
		                        IBreakpointsExtension.class.getName(),
								MIBreakpoints.class.getName(),
								GDBBreakpoints_7_0.class.getName(),
								GDBBreakpoints_7_2.class.getName(),
								GDBBreakpoints_7_4.class.getName(),
								GDBBreakpoints_7_6.class.getName() },
				new Hashtable<String, String>());

		rm.done();
	}

	@Override
	public void shutdown(RequestMonitor requestMonitor) {
        unregister();
		super.shutdown(requestMonitor);
	}
	
	@Override
	protected boolean bpThreadGroupInfoAvailable() {
		return true;
	}
}
