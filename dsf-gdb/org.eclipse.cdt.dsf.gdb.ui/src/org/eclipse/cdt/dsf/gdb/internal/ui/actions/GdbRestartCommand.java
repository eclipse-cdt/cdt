/*******************************************************************************
 * Copyright (c) 2006, 2008 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.actions;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.cdt.debug.core.model.IRestart;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
import org.eclipse.cdt.dsf.gdb.launching.GdbLaunch;
import org.eclipse.cdt.dsf.gdb.service.IGDBBackend;
import org.eclipse.cdt.dsf.gdb.service.command.IGDBControl;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IProcess;

public class GdbRestartCommand implements IRestart {
    private final DsfExecutor fExecutor;
    private final DsfServicesTracker fTracker;
    private final GdbLaunch fLaunch;
    
    public GdbRestartCommand(DsfSession session, GdbLaunch launch) {
        fExecutor = session.getExecutor();
        fLaunch = launch;
        fTracker = new DsfServicesTracker(GdbUIPlugin.getBundleContext(), session.getId());
    }    

    public void dispose() {
        fTracker.dispose();
    }

    // Run control may not be available after a connection is terminated and shut down.
    public boolean canRestart() {
    	Query<Boolean> canRestart = new Query<Boolean>() {
    		@Override
    		protected void execute(DataRequestMonitor<Boolean> rm) {
    			IGDBControl gdbControl = fTracker.getService(IGDBControl.class);
				if (gdbControl != null) {
					rm.setData(gdbControl.canRestart());
				} else {
					rm.setData(false);
				}
				
				rm.done();
    		}
    	};

    	fExecutor.execute(canRestart);
        try {
        	return canRestart.get();
        } catch (InterruptedException e1) {
        } catch (ExecutionException e1) {
        }
        return false;
    }


    public void restart() throws DebugException
    {	
        final AtomicReference<IPath> execPathRef = new AtomicReference<IPath>();
    	Query<Object> restartQuery = new Query<Object>() {
    		@Override
    		protected void execute(final DataRequestMonitor<Object> rm) {
    			final IGDBControl gdbControl = fTracker.getService(IGDBControl.class);
    			final IGDBBackend backend = fTracker.getService(IGDBBackend.class);
				if (gdbControl != null && backend != null) {		
                    execPathRef.set(backend.getProgramPath());
                    gdbControl.initInferiorInputOutput(new RequestMonitor(fExecutor, rm) {
                    	@Override
                    	protected void handleSuccess() {
                    		gdbControl.createInferiorProcess();
                    		gdbControl.restart(fLaunch, rm);
                    	}
                    });
				} else {
					rm.done();
				}
    		}
    	};

    	fExecutor.execute(restartQuery);
        try {
        	restartQuery.get();
        } catch (InterruptedException e1) {
        } catch (ExecutionException e1) {
        }

        // Now that we restarted the inferior, we must add it to our launch
        // we must do this here because we cannot do it in the executor, or else
        // it deadlocks
        // We must first remove the old inferior from our launch (since it uses
        // the same name and we use that name to find the old one)
        //
        // Remove
        String inferiorLabel = execPathRef.get().lastSegment();

        IProcess[] launchProcesses = fLaunch.getProcesses();
        for (IProcess p : launchProcesses) {
        	if (p.getLabel().equals(inferiorLabel)) {
            	fLaunch.removeProcess(p);
            	break;
        	}
        }
        // Add
        try {
            fLaunch.addInferiorProcess(inferiorLabel);
        } catch (CoreException e) {
        	throw new DebugException(e.getStatus());
        }
    }    
}

