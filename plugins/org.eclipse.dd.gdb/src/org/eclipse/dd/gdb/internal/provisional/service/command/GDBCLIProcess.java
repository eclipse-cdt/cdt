/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.dd.gdb.internal.provisional.service.command;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;

import org.eclipse.dd.dsf.concurrent.DsfRunnable;
import org.eclipse.dd.dsf.debug.service.command.ICommandControlService;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.dd.mi.service.command.AbstractCLIProcess;

/**
 * 
 */
class GDBCLIProcess extends AbstractCLIProcess {

    public GDBCLIProcess(ICommandControlService commandControl) throws IOException {
        super(commandControl);
    }


    /**
     * @see java.lang.Process#waitFor()
     */
    @Override
    public int waitFor() throws InterruptedException {
        if (!DsfSession.isSessionActive(getSession().getId())) return 0;

        Process process = null;
        try {
            process = getSession().getExecutor().submit(new Callable<Process>() { 
                public Process call() throws Exception {
                    if (isDisposed()) return null;
                    return ((IGDBControl)getCommandControlService()).getGDBProcess();
                }}).get();
        } catch (RejectedExecutionException e) {
        } catch (ExecutionException e) {
        }
        if (process == null) return 0;
        return process.waitFor();
    }

    
    /**
     * @see java.lang.Process#exitValue()
     */
    @Override
    public int exitValue() {
        if (!DsfSession.isSessionActive(getSession().getId())) return 0;
        try {
            return getSession().getExecutor().submit(new Callable<Integer>() { 
                public Integer call() throws Exception {
                    if (!DsfSession.isSessionActive(getSession().getId())) {
                        return new Integer(-1);
                    } else {
                        if (isDisposed()) return new Integer(-1);
                        IGDBControl gdb = (IGDBControl)getCommandControlService();
                        if (!gdb.isGDBExited()) {
                            throw new IllegalThreadStateException("GDB Process has not exited"); //$NON-NLS-1$
                        }
                        return gdb.getGDBExitCode();
                    }
                }}).get().intValue();
        } catch (RejectedExecutionException e) {
        } catch (InterruptedException e) {
        } catch (ExecutionException e) {
            if (e.getCause() instanceof RuntimeException) {
                throw (RuntimeException)e.getCause();
            }
        }
        return 0;
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
                IGDBControl gdb = (IGDBControl)getCommandControlService();
                gdb.destroy();
            }});
        } catch (RejectedExecutionException e) {
            // Session disposed.
        }
    }


}
