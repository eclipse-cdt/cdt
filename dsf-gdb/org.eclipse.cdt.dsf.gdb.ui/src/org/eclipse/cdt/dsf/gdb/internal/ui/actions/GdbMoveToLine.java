/*******************************************************************************
 * Copyright (c) 2010 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.actions;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.core.model.IMoveToAddress;
import org.eclipse.cdt.debug.core.model.IMoveToLine;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints;
import org.eclipse.cdt.dsf.debug.service.IRunControl;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointDMContext;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointsTargetDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
import org.eclipse.cdt.dsf.mi.service.IMIExecutionDMContext;
import org.eclipse.cdt.dsf.mi.service.IMIRunControl;
import org.eclipse.cdt.dsf.mi.service.MIBreakpointDMData;
import org.eclipse.cdt.dsf.mi.service.MIBreakpoints;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;

/**
 * Implements the CDT's move to line interface.
 * 
 * @since 2.1
 */
public class GdbMoveToLine implements IMoveToLine, IMoveToAddress {

    private final IExecutionDMContext fContext;

    public GdbMoveToLine(IExecutionDMContext context) {
        fContext = context;
    }

	public boolean canMoveToLine(String fileName, int lineNumber) {
		return canMoveToLocation();
	}

	public void moveToLine(String fileName, int lineNumber) throws DebugException {
		IMIExecutionDMContext threadExecDmc = DMContexts.getAncestorOfType(fContext, IMIExecutionDMContext.class);
		if (threadExecDmc == null) {
            throw new DebugException(new Status(IStatus.ERROR, GdbUIPlugin.PLUGIN_ID, DebugException.REQUEST_FAILED, "Invalid thread context", null)); //$NON-NLS-1$
		}

		// Create the breakpoint attributes
		Map<String,Object> attr = new HashMap<String,Object>();
    	attr.put(MIBreakpoints.BREAKPOINT_TYPE, MIBreakpoints.BREAKPOINT);
    	attr.put(MIBreakpoints.FILE_NAME, fileName);
    	attr.put(MIBreakpoints.LINE_NUMBER, lineNumber);
    	attr.put(MIBreakpointDMData.IS_TEMPORARY, true);
    	attr.put(MIBreakpointDMData.THREAD_ID, Integer.toString(threadExecDmc.getThreadId()));
    	
    	// Now do the operation
    	moveToLocation(fileName + ":" + lineNumber, attr); //$NON-NLS-1$
	}
	
	public boolean canMoveToAddress(IAddress address) {
		return canMoveToLocation();
	}

	public void moveToAddress(IAddress address) throws DebugException {
		IMIExecutionDMContext threadExecDmc = DMContexts.getAncestorOfType(fContext, IMIExecutionDMContext.class);
		if (threadExecDmc == null) {
            throw new DebugException(new Status(IStatus.ERROR, GdbUIPlugin.PLUGIN_ID, DebugException.REQUEST_FAILED, "Invalid thread context", null)); //$NON-NLS-1$
		}

		// Create the breakpoint attributes
		Map<String,Object> attr = new HashMap<String,Object>();
    	attr.put(MIBreakpoints.BREAKPOINT_TYPE, MIBreakpoints.BREAKPOINT);
    	attr.put(MIBreakpoints.ADDRESS, "0x" + address.toString(16)); //$NON-NLS-1$
    	attr.put(MIBreakpointDMData.IS_TEMPORARY, true);
    	attr.put(MIBreakpointDMData.THREAD_ID,  Integer.toString(threadExecDmc.getThreadId()));
    	
    	// Now do the operation
    	moveToLocation("*0x" + address.toString(16), attr);		 //$NON-NLS-1$
	}
	
	private boolean canMoveToLocation() {
        DsfSession session = DsfSession.getSession(fContext.getSessionId());
        if (session != null && session.isActive()) {
            try {
                Query<Boolean> query = new Query<Boolean>() {
                    @Override
                    protected void execute(DataRequestMonitor<Boolean> rm) {
                        DsfServicesTracker tracker = 
                            new DsfServicesTracker(GdbUIPlugin.getBundleContext(), fContext.getSessionId());
                        
                        IRunControl runControl = tracker.getService(IRunControl.class);
                        tracker.dispose();
                        if (runControl != null) {
                            runControl.canResume(fContext, rm);
                        } else {
                            rm.setData(false);
                            rm.done();
                        }
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
    

	private void moveToLocation(final String location, final Map<String,Object> bpAttributes) throws DebugException {
        final DsfSession session = DsfSession.getSession(fContext.getSessionId());
        if (session != null && session.isActive()) {
            Throwable exception = null;
            try {
                Query<Object> query = new Query<Object>() {
                    @Override
                    protected void execute(final DataRequestMonitor<Object> rm) {
                    	// first create a temporary breakpoint to stop the execution at
                    	// the location we are about to jump to
                        final DsfServicesTracker tracker = 
                            new DsfServicesTracker(GdbUIPlugin.getBundleContext(), fContext.getSessionId());
                        IBreakpoints bpService = tracker.getService(IBreakpoints.class);
                        IBreakpointsTargetDMContext bpDmc = DMContexts.getAncestorOfType(fContext, IBreakpointsTargetDMContext.class);
                        if (bpService != null && bpDmc != null) {                        	
                        	bpService.insertBreakpoint(
                        			bpDmc, bpAttributes,
                        			new DataRequestMonitor<IBreakpointDMContext>(session.getExecutor(), rm) {
                        				@Override
                        				protected void handleSuccess() {
                        					// Now resume at the proper location
                        					IMIRunControl miRunControl = tracker.getService(IMIRunControl.class);
                        					tracker.dispose();
                        					if (miRunControl != null) {
                        						miRunControl.resumeAtLocation(fContext, location, rm);
                        					} else {
                        						rm.setStatus(new Status(IStatus.ERROR, GdbUIPlugin.PLUGIN_ID, IDsfStatusConstants.NOT_SUPPORTED, "MIRunControl service not available", null)); //$NON-NLS-1$
                        						rm.done();
                        					}
                        				};
                        				@Override
                        				protected void handleFailure() {
                                            tracker.dispose();
                                            super.handleFailure();
                        				};
                        			});
                        } else {
                            rm.setStatus(new Status(IStatus.ERROR, GdbUIPlugin.PLUGIN_ID, IDsfStatusConstants.NOT_SUPPORTED, "Unable to set breakpoint", null)); //$NON-NLS-1$
                            rm.done();                        	
                            tracker.dispose();
                        }
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
                throw new DebugException(new Status(IStatus.ERROR, GdbUIPlugin.PLUGIN_ID, DebugException.REQUEST_FAILED, "Faild executing move to line", exception)); //$NON-NLS-1$
            }
        } else {
            throw new DebugException(new Status(IStatus.ERROR, GdbUIPlugin.PLUGIN_ID, DebugException.REQUEST_FAILED, "Debug session is not active", null));         //$NON-NLS-1$
        }
    }
}
