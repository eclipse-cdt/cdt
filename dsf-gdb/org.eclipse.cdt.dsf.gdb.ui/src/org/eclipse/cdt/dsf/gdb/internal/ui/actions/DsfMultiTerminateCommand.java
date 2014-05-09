/*******************************************************************************
 * Copyright (c) 2014 Mentor Graphics and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Mentor Graphics - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.internal.ui.actions;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.RejectedExecutionException;

import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.concurrent.ImmediateDataRequestMonitor;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IProcessDMContext;
import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
import org.eclipse.cdt.dsf.gdb.launching.GdbLaunch;
import org.eclipse.cdt.dsf.gdb.service.IMultiTerminate;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.debug.core.commands.IDebugCommandRequest;
import org.eclipse.debug.core.commands.IEnabledStateRequest;

public class DsfMultiTerminateCommand extends DsfTerminateCommand {

    private final DsfExecutor fExecutor;
    private final DsfServicesTracker fTracker;

	public DsfMultiTerminateCommand(DsfSession session) {
		super(session);
        fExecutor = session.getExecutor();
        fTracker = new DsfServicesTracker(GdbUIPlugin.getBundleContext(), session.getId());
	}

    @Override
	public void dispose() {
        fTracker.dispose();
    }

	@Override
	public void canExecute(final IEnabledStateRequest request) {
		if (request.getElements().length == 0) {
			request.setEnabled(false);
			request.done();
			return;
		}

		if (request.getElements().length == 1) {
			super.canExecute(request);
			return;
		}

		final Set<IProcessDMContext> procDmcs = new HashSet<IProcessDMContext>();
		for (Object el : request.getElements()) {
			if (el instanceof GdbLaunch) {
				super.execute((GdbLaunch)el, request);
				return;
			}
			if (el instanceof IDMVMContext) {
				IProcessDMContext procDmc = DMContexts.getAncestorOfType(((IDMVMContext)el).getDMContext(), IProcessDMContext.class);
				if (procDmc != null) {
					procDmcs.add(procDmc);
				}
			}
		}
		if (procDmcs.size() == 0) {
			request.setEnabled(false);
			request.done();
			return;
		}
		
        try {
            fExecutor.execute(
                new DsfRunnable() { 
                    @Override
                    public void run() {
                    	IMultiTerminate multiTerminate = fTracker.getService(IMultiTerminate.class);
                        if (multiTerminate == null) {
                        	DsfMultiTerminateCommand.super.canExecute(request);
                        } 
                        else {
                        	multiTerminate.canTerminateSome(
                        		procDmcs.toArray(new IProcessDMContext[procDmcs.size()]), 
                        		new ImmediateDataRequestMonitor<Boolean>() {
	                        		@Override
	                        		protected void handleCompleted() {
	                        			request.setEnabled(isSuccess() && getData());
	                        			request.done();
	                        		}
	                        	});
                        }
                    }
                });
        } 
        catch (RejectedExecutionException e) {
            request.setEnabled(false);
            request.done();
        }
	}

	@Override
	public boolean execute(final IDebugCommandRequest request) {
		if (request.getElements().length == 0) {
			request.done();
			return false;
		}

		if (request.getElements().length == 1) {
			return super.execute(request);
		}

		final Set<IProcessDMContext> procDmcs = new HashSet<IProcessDMContext>();
		for (Object el : request.getElements()) {
			if (el instanceof GdbLaunch) {
				return super.execute((GdbLaunch)el, request);
			}
			if (el instanceof IDMVMContext) {
				IProcessDMContext procDmc = DMContexts.getAncestorOfType(((IDMVMContext)el).getDMContext(), IProcessDMContext.class);
				if (procDmc != null) {
					procDmcs.add(procDmc);
				}
			}
		}
		if (procDmcs.size() == 0) {
			request.done();
			return false;
		}
		
        try {
            fExecutor.execute(
                new DsfRunnable() { 
                    @Override
                    public void run() {
                    	IMultiTerminate multiTerminate = fTracker.getService(IMultiTerminate.class);
                        if (multiTerminate == null) {
                        	DsfMultiTerminateCommand.super.execute(request);
                        } 
                        else {
                        	multiTerminate.terminate(
                        		procDmcs.toArray(new IProcessDMContext[procDmcs.size()]), 
                        		new ImmediateDataRequestMonitor<Boolean>() {
	                        		@Override
	                        		protected void handleCompleted() {
	                                	if (!isSuccess()) {
	                                		request.setStatus(getStatus());
	                                		request.done();
	                                	}
	                                	else {
	                                		waitForTermination(request);
	                                	}
	                        		}
	                        	});
                        }
                    }
                });
        } 
        catch (RejectedExecutionException e) {
            request.done();
        }
        return false;
	}
}
