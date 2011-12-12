/*******************************************************************************
 * Copyright (c) 2010 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson - initial API and implementation
 *     Marc-Andre Laperle - fix for bug 269838
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.service.macos;

import java.util.Hashtable;

import org.eclipse.cdt.dsf.concurrent.ImmediateRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.debug.service.IProcesses;
import org.eclipse.cdt.dsf.debug.service.IRunControl.ISuspendedDMEvent;
import org.eclipse.cdt.dsf.gdb.service.GDBProcesses;
import org.eclipse.cdt.dsf.mi.service.IMIProcesses;
import org.eclipse.cdt.dsf.mi.service.MIProcesses;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.DsfSession;

/** @since 3.0 */
public class MacOSGDBProcesses extends GDBProcesses {

	public MacOSGDBProcesses(DsfSession session) {
		super(session);
	}
	
    @Override
    public void initialize(final RequestMonitor requestMonitor) {
    	super.initialize(new ImmediateRequestMonitor(requestMonitor) {
    		@Override
    		protected void handleSuccess() {
    			doInitialize(requestMonitor);
			}
		});
	}
    
	private void doInitialize(RequestMonitor requestMonitor) {
		// Register this service.
		register(new String[] { IProcesses.class.getName(),
				                IMIProcesses.class.getName(),
				                MIProcesses.class.getName(),
				                GDBProcesses.class.getName(),
				                MacOSGDBProcesses.class.getName() },
				 new Hashtable<String, String>());
        
        getSession().addServiceEventListener(this, null);

		requestMonitor.done();
	}
	
	@Override
	public void shutdown(RequestMonitor requestMonitor) {
        getSession().removeServiceEventListener(this);
		super.shutdown(requestMonitor);
	}
	
	@DsfServiceEventHandler
    public void eventDispatchedMacOS(ISuspendedDMEvent e) {
		// With Apple-gdb, we flush the command cache because we need
		// new results from -thread-list-ids
		// This is because there is no IStartedDMEvent triggered by GDB on Apple-gdb
		flushCache(null);
    }
}
