/*******************************************************************************
 * Copyright (c) 2006, 2010 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.launching;

import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Sequence;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints;
import org.eclipse.cdt.dsf.debug.service.IDisassembly;
import org.eclipse.cdt.dsf.debug.service.IExpressions;
import org.eclipse.cdt.dsf.debug.service.IMemory;
import org.eclipse.cdt.dsf.debug.service.IModules;
import org.eclipse.cdt.dsf.debug.service.IProcesses;
import org.eclipse.cdt.dsf.debug.service.IRegisters;
import org.eclipse.cdt.dsf.debug.service.IRunControl;
import org.eclipse.cdt.dsf.debug.service.ISourceLookup;
import org.eclipse.cdt.dsf.debug.service.IStack;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControl;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.gdb.service.IGDBTraceControl;
import org.eclipse.cdt.dsf.mi.service.IMIBackend;
import org.eclipse.cdt.dsf.mi.service.MIBreakpointsManager;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.IDsfService;

public class ShutdownSequence extends Sequence {

    String fSessionId;

    String fApplicationName;

    String fDebugModelId;

    DsfServicesTracker fTracker;

    public ShutdownSequence(DsfExecutor executor, String sessionId, RequestMonitor requestMonitor) {
        super(executor, requestMonitor);
        fSessionId = sessionId;
    }

    @Override
    public Step[] getSteps() {
        return fSteps;
    }

    private final Step[] fSteps = new Step[] { new Step() {
        @Override
        public void execute(RequestMonitor requestMonitor) {
            assert GdbPlugin.getBundleContext() != null;
            fTracker = new DsfServicesTracker(GdbPlugin.getBundleContext(), fSessionId);
            requestMonitor.done();
        }

        @Override
        public void rollBack(RequestMonitor requestMonitor) {
            fTracker.dispose();
            fTracker = null;
            requestMonitor.done();
        }
    }, new Step() {
        @Override
        public void execute(RequestMonitor requestMonitor) {
            shutdownService(IGDBTraceControl.class, requestMonitor);
        }
    }, new Step() {
        @Override
        public void execute(RequestMonitor requestMonitor) {
            shutdownService(IDisassembly.class, requestMonitor);
        }
    }, new Step() {
        @Override
        public void execute(RequestMonitor requestMonitor) {
            shutdownService(IRegisters.class, requestMonitor);
        }
    }, new Step() {
        @Override
        public void execute(RequestMonitor requestMonitor) {
            shutdownService(MIBreakpointsManager.class, requestMonitor);
        }
    }, new Step() {
        @Override
        public void execute(RequestMonitor requestMonitor) {
            shutdownService(IBreakpoints.class, requestMonitor);
        }
    }, new Step() {
        @Override
        public void execute(RequestMonitor requestMonitor) {
            shutdownService(ISourceLookup.class, requestMonitor);
        }
    }, new Step() {
        @Override
        public void execute(RequestMonitor requestMonitor) {
            shutdownService(IExpressions.class, requestMonitor);
        }
    }, new Step() {
        @Override
        public void execute(RequestMonitor requestMonitor) {
            shutdownService(IStack.class, requestMonitor);
        }
    }, new Step() {
        @Override
        public void execute(RequestMonitor requestMonitor) {
            shutdownService(IModules.class, requestMonitor);
        }
    }, new Step() {
        @Override
        public void execute(RequestMonitor requestMonitor) {
            shutdownService(IMemory.class, requestMonitor);
        }
    }, new Step() {
        @Override
        public void execute(RequestMonitor requestMonitor) {
            shutdownService(IRunControl.class, requestMonitor);
        }
    }, new Step() {
        @Override
        public void execute(RequestMonitor requestMonitor) {
            shutdownService(IProcesses.class, requestMonitor);
        }
    }, new Step() {
        @Override
        public void execute(RequestMonitor requestMonitor) {
            shutdownService(ICommandControl.class, requestMonitor);
        }
    }, new Step() {
        @Override
        public void execute(RequestMonitor requestMonitor) {
            shutdownService(IMIBackend.class, requestMonitor);
        }
    }, new Step() {
        @Override
        public void execute(RequestMonitor requestMonitor) {
            fTracker.dispose();
            fTracker = null;
            requestMonitor.done();
        }
    } };

    @SuppressWarnings("unchecked")
    private void shutdownService(Class clazz, final RequestMonitor requestMonitor) {
        IDsfService service = (IDsfService)fTracker.getService(clazz);
        if (service != null) {
            service.shutdown(new RequestMonitor(getExecutor(), requestMonitor) {
                @Override
                protected void handleCompleted() {
                    if (!isSuccess()) {
                        GdbPlugin.getDefault().getLog().log(getStatus());
                    }
                    requestMonitor.done();
                }
            });
        } else {
        	// It is possible that a particular service was not instantiated at all
        	// depending on our backend
            requestMonitor.done();
        }
    }
}
