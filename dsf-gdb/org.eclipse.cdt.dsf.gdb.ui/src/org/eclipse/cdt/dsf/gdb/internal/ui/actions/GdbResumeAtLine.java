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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.core.model.IResumeAtAddress;
import org.eclipse.cdt.debug.core.model.IResumeAtLine;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.debug.service.IRunControl;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
import org.eclipse.cdt.dsf.mi.service.IMIRunControl;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;

/**
 * Implements the CDT's resume at line interface.
 * 
 * @since 2.1
 */
public class GdbResumeAtLine implements IResumeAtLine, IResumeAtAddress {

    private final IExecutionDMContext fContext;

    public GdbResumeAtLine(IExecutionDMContext context) {
        fContext = context;
    }

	public boolean canResumeAtLine(IFile file, int lineNumber) {
		return canResumeAtLocation();
	}

	public boolean canResumeAtLine(String fileName, int lineNumber) {
		return canResumeAtLocation();
	}
	
	public void resumeAtLine(IFile file, int lineNumber) throws DebugException {
		resumeAtLine(file.getLocation().makeAbsolute().toOSString(), lineNumber);
	}
	
	public void resumeAtLine(String fileName, int lineNumber) throws DebugException {
    	resumeAtLocation(fileName + ":" + lineNumber); //$NON-NLS-1$
	}
	
	public boolean canResumeAtAddress(IAddress address) {
		return canResumeAtLocation();
	}

	public void resumeAtAddress(IAddress address) throws DebugException {
    	resumeAtLocation("*0x" + address.toString(16));		 //$NON-NLS-1$
	}
	
	private boolean canResumeAtLocation() {
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
    

	private void resumeAtLocation(final String location) throws DebugException {
        final DsfSession session = DsfSession.getSession(fContext.getSessionId());
        if (session != null && session.isActive()) {
            Throwable exception = null;
            try {
                Query<Object> query = new Query<Object>() {
                    @Override
                    protected void execute(final DataRequestMonitor<Object> rm) {
                        final DsfServicesTracker tracker = 
                            new DsfServicesTracker(GdbUIPlugin.getBundleContext(), fContext.getSessionId());

                        IMIRunControl miRunControl = tracker.getService(IMIRunControl.class);
                        tracker.dispose();
                        if (miRunControl != null) {
                        	miRunControl.resumeAtLocation(fContext, location, rm);
                        } else {
                        	rm.setStatus(new Status(IStatus.ERROR, GdbUIPlugin.PLUGIN_ID, IDsfStatusConstants.NOT_SUPPORTED, "MIRunControl service not available", null)); //$NON-NLS-1$
                        	rm.done();
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
                throw new DebugException(new Status(IStatus.ERROR, GdbUIPlugin.PLUGIN_ID, DebugException.REQUEST_FAILED, "Faild executing resume at line", exception)); //$NON-NLS-1$
            }
        } else {
            throw new DebugException(new Status(IStatus.ERROR, GdbUIPlugin.PLUGIN_ID, DebugException.REQUEST_FAILED, "Debug session is not active", null));         //$NON-NLS-1$
        }
    }


}
