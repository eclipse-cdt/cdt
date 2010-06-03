/*******************************************************************************
 * Copyright (c) 2007, 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.cdt.dsf.concurrent.ConfinedToDsfExecutor;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.concurrent.ImmediateExecutor;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.mi.service.IMIBackend;
import org.eclipse.cdt.dsf.mi.service.IMIBackend.BackendStateChangedEvent;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * CLI Process object implementation which uses the {@link IMIBackend} service
 * to monitor and control the underlying process.
 * 
 * @since 1.1
 */
public class MIBackendCLIProcess extends AbstractCLIProcess {

	private IMIBackend fMIBackend;
	private AtomicInteger fExitCode = new AtomicInteger(-1); 
	private BackedExitedEventListener fExitedEventListener;
	
    @ConfinedToDsfExecutor("getSession()#getExecutor")
	public MIBackendCLIProcess(ICommandControlService commandControl, IMIBackend backend) throws IOException {
		super(commandControl);
		fMIBackend = backend;
		if (fMIBackend.getState() == IMIBackend.State.TERMINATED) {
		    fExitCode.set(fMIBackend.getExitCode());
		}
		fExitedEventListener = new BackedExitedEventListener();
        getSession().addServiceEventListener(fExitedEventListener, null);		
	}

    public class BackedExitedEventListener {
        private final List<RequestMonitor> fWaitForRMs = new ArrayList<RequestMonitor>();
        
        @DsfServiceEventHandler
        public void eventDispatched(BackendStateChangedEvent event) {
            if (event.getState() == IMIBackend.State.TERMINATED &&
            		event.getBackendId().equals(fMIBackend.getId())) 
            {
                fExitCode.set(fMIBackend.getExitCode());
                for (RequestMonitor rm : fWaitForRMs) {
                    rm.done();
                }
                fWaitForRMs.clear();
            }
        }
        
        void dispose() {
            for (RequestMonitor rm : fWaitForRMs) {
                rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, IDsfStatusConstants.REQUEST_FAILED, "Backend terminate event never received", null)); //$NON-NLS-1$
                rm.done();
            }
            fWaitForRMs.clear();
        }
    }
    
    /**
     * @see java.lang.Process#waitFor()
     */
    @Override
    public int waitFor() throws InterruptedException {
        if (!DsfSession.isSessionActive(getSession().getId())) {
            return fExitCode.get();
        }

        try {
            Query<Object> query = new Query<Object>() {
                @Override
                protected void execute(final DataRequestMonitor<Object> rm) {
                    if ( !DsfSession.isSessionActive(getSession().getId()) || 
                         isDisposed() ||
                         fMIBackend.getState() == IMIBackend.State.TERMINATED ) 
                    {
                        rm.setData(new Object());
                        rm.done();
                    } else {
                        fExitedEventListener.fWaitForRMs.add(
                            new RequestMonitor(ImmediateExecutor.getInstance(), rm) {
                                @Override
                                protected void handleSuccess() {
                                    rm.setData(new Object());
                                    rm.done();
                                }
                            });
                    }
                }
            };
            getSession().getExecutor().execute(query);
            query.get();
        } catch (RejectedExecutionException e) {
        } catch (ExecutionException e) {
        }
        return fExitCode.get();
    }

    
    /**
     * @see java.lang.Process#exitValue()
     */
    @Override
    public int exitValue() {
        if (!DsfSession.isSessionActive(getSession().getId())) {
            return fExitCode.get();
        }
        try {
            getSession().getExecutor().submit(new Callable<Object>() { 
                public Object call() throws Exception {
                    if (fMIBackend.getState() != IMIBackend.State.TERMINATED) {
                        throw new IllegalThreadStateException("Backend Process has not exited"); //$NON-NLS-1$
                    }
                    return null;
                }}).get();
        } catch (RejectedExecutionException e) {
        } catch (InterruptedException e) {
        } catch (ExecutionException e) {
            if (e.getCause() instanceof RuntimeException) {
                throw (RuntimeException)e.getCause();
            }
        }
        return fExitCode.get();
    }
    /**
     * @see java.lang.Process#destroy()
     */
    @Override
    public void destroy() {
        try {
            getSession().getExecutor().execute(new DsfRunnable() { public void run() {
                if (!DsfSession.isSessionActive(getSession().getId())) return;
                if (isDisposed()) return;

                fMIBackend.destroy();
            }});
        } catch (RejectedExecutionException e) {
            // Session disposed.
        }
    }
    
    @Override
    public void dispose() {
        fExitedEventListener.dispose();
        getSession().removeServiceEventListener(fExitedEventListener);        
        super.dispose();
    }
}
