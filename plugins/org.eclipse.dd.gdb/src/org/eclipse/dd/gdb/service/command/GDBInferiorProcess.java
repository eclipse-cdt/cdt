/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.dd.gdb.service.command;

import java.io.OutputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;

import org.eclipse.cdt.utils.pty.PTY;
import org.eclipse.dd.dsf.concurrent.DsfRunnable;
import org.eclipse.dd.dsf.concurrent.ThreadSafeAndProhibitedFromDsfExecutor;
import org.eclipse.dd.gdb.service.command.GDBControl.SessionType;
import org.eclipse.dd.mi.service.command.MIInferiorProcess;

/**
 * 
 */
class GDBInferiorProcess extends MIInferiorProcess {

    
    public GDBInferiorProcess(GDBControl commandControl, PTY p) {
        super(commandControl, p);
    }

    public GDBInferiorProcess(GDBControl commandControl, OutputStream gdbOutputStream) {
        super(commandControl, gdbOutputStream);
    }

    @Override
    @ThreadSafeAndProhibitedFromDsfExecutor("getSession#getExecutor")
    public void destroy() {
        try {
            getSession().getExecutor().submit(new DsfRunnable() {
                public void run() {
                    if (isDisposed() || !getSession().isActive()) return;
                    GDBControl gdb = (GDBControl)getCommandControl();
                    if (gdb == null) return;
                    
                    // An inferior will be destroy():interrupt and kill if
                    // - For attach session:
                    //   the inferior was not disconnected yet (no need to try
                    //   to kill a disconnected program).
                    // - For Program session:
                    //   if the inferior was not terminated.
                    // - For PostMortem(Core): send event
                    // else noop
                    if ((gdb.getSessionType() == SessionType.ATTACH && gdb.isConnected()) || 
                        (gdb.getSessionType() == SessionType.RUN && getState() != State.TERMINATED)) 
                    {
                        // Try to interrupt the inferior, first.
                        if (getState() == State.RUNNING) {
                            gdb.interrupt();
                        }
                    }
                }
            }).get();
        } catch (RejectedExecutionException e) {
        } catch (InterruptedException e) {
        } catch (ExecutionException e) {
        } finally {
            super.destroy();
        }
    }
}
