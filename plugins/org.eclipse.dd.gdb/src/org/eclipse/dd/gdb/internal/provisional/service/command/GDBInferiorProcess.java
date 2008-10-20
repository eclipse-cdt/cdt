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

import java.io.OutputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;

import org.eclipse.cdt.utils.pty.PTY;
import org.eclipse.dd.dsf.concurrent.DsfRunnable;
import org.eclipse.dd.dsf.concurrent.ThreadSafeAndProhibitedFromDsfExecutor;
import org.eclipse.dd.dsf.debug.service.command.ICommandControlService;
import org.eclipse.dd.gdb.internal.provisional.service.IGDBBackend;
import org.eclipse.dd.mi.service.command.MIInferiorProcess;

/**
 * 
 */
class GDBInferiorProcess extends MIInferiorProcess {

    private IGDBBackend fBackend;
    
    public GDBInferiorProcess(ICommandControlService commandControl, IGDBBackend backend, PTY p) {
        super(commandControl, p);
        fBackend = backend;
    }

    public GDBInferiorProcess(ICommandControlService commandControl, IGDBBackend backend, OutputStream gdbOutputStream) {
        super(commandControl, gdbOutputStream);
        fBackend = backend;
    }

    @Override
    @ThreadSafeAndProhibitedFromDsfExecutor("getSession#getExecutor")
    public void destroy() {
        try {
            getSession().getExecutor().submit(new DsfRunnable() {
                public void run() {
                    if (isDisposed() || !getSession().isActive()) return;
                    
                    // An inferior will be destroy():interrupt and kill if
                    // - For attach session:
                    //   never (we don't kill an independent process.)
                    // - For Program session:
                    //   if the inferior is still running.
                    // - For PostMortem(Core): send event
                    // else noop
                    if (fBackend.getIsAttachSession() == false) {
                        // Try to interrupt the inferior, first.
                        if (getState() == State.RUNNING) {
                            fBackend.interrupt();
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
