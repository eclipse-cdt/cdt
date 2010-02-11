/*******************************************************************************
 * Copyright (c) 2009, 2010 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Ericsson                    - Added support for IRunToAddress for DSF DisassemblyView (302324)
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.actions;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.core.model.IRunToAddress;
import org.eclipse.cdt.debug.core.model.IRunToLine;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.concurrent.ImmediateExecutor;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.debug.service.IRunControl;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
import org.eclipse.cdt.dsf.mi.service.IMIRunControl;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.ui.actions.IRunToLineTarget;

/**
 * Implements the CDT's run to line interface.  This interface is called by CDT's
 * {@link IRunToLineTarget} implementation.
 * 
 * @since 2.0
 */
public class GdbRunToLine implements IRunToLine, IRunToAddress {

    private final IExecutionDMContext fContext;

    public GdbRunToLine(IExecutionDMContext context) {
        fContext = context;
    }
    
    public boolean canRunToLine(IFile file, int lineNumber) {
        return canRunToLocation();
    }

    public boolean canRunToLine(String fileName, int lineNumber) {
        return canRunToLocation();
    }

    public void runToLine(IFile file, int lineNumber, boolean skipBreakpoints) throws DebugException {
    	runToLine(file.getLocation().makeAbsolute().toOSString(), lineNumber, skipBreakpoints);
    }
    
    public void runToLine(String fileName, int lineNumber, boolean skipBreakpoints) throws DebugException {
    	runToLocation(fileName + ":" + lineNumber, skipBreakpoints); //$NON-NLS-1$
    }
    
    /** @since 2.1 */
	public boolean canRunToAddress(IAddress address) {
        return canRunToLocation();
	}

    /** @since 2.1 */
	public void runToAddress(IAddress address, boolean skipBreakpoints) throws DebugException {
    	runToLocation("*0x" + address.toString(16), skipBreakpoints); //$NON-NLS-1$
	}
	
    private boolean canRunToLocation() {
        DsfSession session = DsfSession.getSession(fContext.getSessionId());
        if (session != null && session.isActive()) {
            try {
                Query<Boolean> query = new Query<Boolean>() {
                    @Override
                    protected void execute(DataRequestMonitor<Boolean> rm) {
                        DsfServicesTracker tracker = 
                            new DsfServicesTracker(GdbUIPlugin.getBundleContext(), fContext.getSessionId());
                        
                        IRunControl runControl = tracker.getService(IRunControl.class);
                        if (runControl != null) {
                            runControl.canResume(fContext, rm);
                        } else {
                            rm.setData(false);
                            rm.done();
                        }
                        tracker.dispose();
                    }
                };
                session.getExecutor().execute(query);
                return query.get();
            } catch (RejectedExecutionException e) {
            } catch (InterruptedException e) {
            } catch (ExecutionException e) {
            }
        }
        return false;
    }
    
    private void runToLocation(final String location, final boolean skipBreakpoints) throws DebugException {
        DsfSession session = DsfSession.getSession(fContext.getSessionId());
        if (session != null && session.isActive()) {
            Throwable exception = null;
            try {
                Query<Object> query = new Query<Object>() {
                    @Override
                    protected void execute(final DataRequestMonitor<Object> rm) {
                        DsfServicesTracker tracker = 
                            new DsfServicesTracker(GdbUIPlugin.getBundleContext(), fContext.getSessionId());
                        
                        IMIRunControl miRunControl = tracker.getService(IMIRunControl.class);
                        if (miRunControl != null) {
                            miRunControl.runToLocation(
                                fContext, location, skipBreakpoints,
                                new DataRequestMonitor<MIInfo>(ImmediateExecutor.getInstance(), rm));
                        } else {
                            rm.setStatus(new Status(IStatus.ERROR, GdbUIPlugin.PLUGIN_ID, IDsfStatusConstants.NOT_SUPPORTED, "MIRunControl service not available", null)); //$NON-NLS-1$
                            rm.done();
                        }
                        tracker.dispose();
                    }
                };
                session.getExecutor().execute(query);
                query.get();
            } catch (RejectedExecutionException e) {
                exception = e;
            } catch (InterruptedException e) {
                exception = e;
            } catch (ExecutionException e) {
                exception = e;
            }
            if (exception != null) {
                throw new DebugException(new Status(IStatus.ERROR, GdbUIPlugin.PLUGIN_ID, DebugException.REQUEST_FAILED, "Faild executing run to line", exception)); //$NON-NLS-1$
            }
        } else {
            throw new DebugException(new Status(IStatus.ERROR, GdbUIPlugin.PLUGIN_ID, DebugException.REQUEST_FAILED, "Debug session is not active", null)); //$NON-NLS-1$            
        }
    }
}
