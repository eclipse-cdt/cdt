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
package org.eclipse.cdt.dsf.debug.internal.ui.actions;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.core.model.IResumeAtAddress;
import org.eclipse.cdt.debug.core.model.IResumeAtLine;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.debug.service.IRunControl2;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.internal.ui.DsfUIPlugin;
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
public class ResumeAtLine implements IResumeAtLine, IResumeAtAddress {

    private final IExecutionDMContext fContext;

    public ResumeAtLine(IExecutionDMContext context) {
        fContext = context;
    }

	@Override
	public boolean canResumeAtLine(IFile file, final int lineNumber) {
		return canResumeAtLine(file.getLocation().makeAbsolute().toOSString(), lineNumber);
	}

	@Override
	public boolean canResumeAtLine(final String fileName, final int lineNumber) {
        DsfSession session = DsfSession.getSession(fContext.getSessionId());
        if (session != null && session.isActive()) {
            try {
                Query<Boolean> query = new Query<Boolean>() {
                    @Override
                    protected void execute(DataRequestMonitor<Boolean> rm) {
                        DsfServicesTracker tracker = 
                            new DsfServicesTracker(DsfUIPlugin.getBundleContext(), fContext.getSessionId());
                        
                        IRunControl2 runControl = tracker.getService(IRunControl2.class);
                        if (runControl != null) {
                            runControl.canMoveToLine(fContext, fileName, lineNumber, true, rm);
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
	
	@Override
	public void resumeAtLine(IFile file, int lineNumber) throws DebugException {
		resumeAtLine(file.getLocation().makeAbsolute().toOSString(), lineNumber);
	}
	
	@Override
	public void resumeAtLine(final String fileName, final int lineNumber) throws DebugException {
        DsfSession session = DsfSession.getSession(fContext.getSessionId());
        if (session != null && session.isActive()) {
            Throwable exception = null;
            try {
                Query<Object> query = new Query<Object>() {
                    @Override
                    protected void execute(final DataRequestMonitor<Object> rm) {
                        DsfServicesTracker tracker = 
                            new DsfServicesTracker(DsfUIPlugin.getBundleContext(), fContext.getSessionId());
                        
                        IRunControl2 runControl = tracker.getService(IRunControl2.class);
                        if (runControl != null) {
                        	runControl.moveToLine(
                                fContext, fileName, lineNumber, true, rm);
                        } else {
                            rm.setStatus(new Status(IStatus.ERROR, DsfUIPlugin.PLUGIN_ID, IDsfStatusConstants.NOT_SUPPORTED, "IRunControl2 service not available", null)); //$NON-NLS-1$
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
                throw new DebugException(new Status(IStatus.ERROR, DsfUIPlugin.PLUGIN_ID, DebugException.REQUEST_FAILED, "Failed executing move to line", exception)); //$NON-NLS-1$
            }
        } else {
            throw new DebugException(new Status(IStatus.ERROR, DsfUIPlugin.PLUGIN_ID, DebugException.REQUEST_FAILED, "Debug session is not active", null)); //$NON-NLS-1$            
        }
    }
	
	@Override
	public boolean canResumeAtAddress(final IAddress address) {
        DsfSession session = DsfSession.getSession(fContext.getSessionId());
        if (session != null && session.isActive()) {
            try {
                Query<Boolean> query = new Query<Boolean>() {
                    @Override
                    protected void execute(DataRequestMonitor<Boolean> rm) {
                        DsfServicesTracker tracker = 
                            new DsfServicesTracker(DsfUIPlugin.getBundleContext(), fContext.getSessionId());
                        
                        IRunControl2 runControl = tracker.getService(IRunControl2.class);
                        if (runControl != null) {
                            runControl.canMoveToAddress(fContext, address, true, rm);
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

	@Override
	public void resumeAtAddress(final IAddress address) throws DebugException {
        DsfSession session = DsfSession.getSession(fContext.getSessionId());
        if (session != null && session.isActive()) {
            Throwable exception = null;
            try {
                Query<Object> query = new Query<Object>() {
                    @Override
                    protected void execute(final DataRequestMonitor<Object> rm) {
                        DsfServicesTracker tracker = 
                            new DsfServicesTracker(DsfUIPlugin.getBundleContext(), fContext.getSessionId());
                        
                        IRunControl2 runControl = tracker.getService(IRunControl2.class);
                        if (runControl != null) {
                        	runControl.moveToAddress(
                                fContext, address, true, rm);
                        } else {
                            rm.setStatus(new Status(IStatus.ERROR, DsfUIPlugin.PLUGIN_ID, IDsfStatusConstants.NOT_SUPPORTED, "IRunControl2 service not available", null)); //$NON-NLS-1$
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
                throw new DebugException(new Status(IStatus.ERROR, DsfUIPlugin.PLUGIN_ID, DebugException.REQUEST_FAILED, "Failed executing move to line", exception)); //$NON-NLS-1$
            }
        } else {
            throw new DebugException(new Status(IStatus.ERROR, DsfUIPlugin.PLUGIN_ID, DebugException.REQUEST_FAILED, "Debug session is not active", null)); //$NON-NLS-1$            
        }
    }
 
}
