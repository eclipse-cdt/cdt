/*******************************************************************************
 * Copyright (c) 2010 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Marc-Andre Laperle - Mac OS version, fix for bug 265483
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.service.macos;

import java.util.Hashtable;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.debug.service.IRunControl;
import org.eclipse.cdt.dsf.debug.service.command.ICommand;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.gdb.service.GDBRunControl;
import org.eclipse.cdt.dsf.mi.service.IMICommandControl;
import org.eclipse.cdt.dsf.mi.service.IMIExecutionDMContext;
import org.eclipse.cdt.dsf.mi.service.IMIRunControl;
import org.eclipse.cdt.dsf.mi.service.MIRunControl;
import org.eclipse.cdt.dsf.mi.service.command.CommandFactory;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/** @since 3.0 */
public class MacOSGDBRunControl extends GDBRunControl {

	private CommandFactory fCommandFactory;

	public MacOSGDBRunControl(DsfSession session) {
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
		register(new String[]{ IRunControl.class.getName(), 
                   			   IMIRunControl.class.getName(),
				               MIRunControl.class.getName(), 
				               GDBRunControl.class.getName(),
				               MacOSGDBRunControl.class.getName() }, 
				 new Hashtable<String,String>());

		fCommandFactory = getServicesTracker().getService(IMICommandControl.class).getCommandFactory();

		requestMonitor.done();
	}
	
	@Override
	public void shutdown(RequestMonitor requestMonitor) {
		super.shutdown(requestMonitor);
	}

	// We use the MIControl code since -exec-interrupt works with Apple gdb
	// but SIGINT does not; it seems it runs in async mode by default
	@Override
	public void suspend(final IExecutionDMContext context, final RequestMonitor rm){
		assert context != null;

		canSuspend(
				context, 
				new DataRequestMonitor<Boolean>(getExecutor(), rm) {
					@Override
					protected void handleSuccess() {
						if (getData()) {
							ICommand<MIInfo> cmd = null;
							if (context instanceof IContainerDMContext) {
								cmd = fCommandFactory.createMIExecInterrupt(context);
							} else {
								IMIExecutionDMContext dmc = DMContexts.getAncestorOfType(context, IMIExecutionDMContext.class);
								if (dmc == null) {
									rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, NOT_SUPPORTED, "Given context: " + context + " is not an execution context.", null)); //$NON-NLS-1$ //$NON-NLS-2$
									rm.done();
									return;
								}
								cmd = fCommandFactory.createMIExecInterrupt(dmc);
							}
							getConnection().queueCommand(cmd, new DataRequestMonitor<MIInfo>(getExecutor(), rm));
						} else {
							rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_STATE, "Context cannot be suspended.", null)); //$NON-NLS-1$
							rm.done();
						}
					}
				});
	}
}
