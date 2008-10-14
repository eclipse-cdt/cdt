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
package org.eclipse.dd.gdb.internal.provisional.launching;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.dd.dsf.concurrent.DsfExecutor;
import org.eclipse.dd.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.dd.dsf.concurrent.RequestMonitor;
import org.eclipse.dd.dsf.concurrent.Sequence;
import org.eclipse.dd.dsf.debug.service.IBreakpoints;
import org.eclipse.dd.dsf.debug.service.IDisassembly;
import org.eclipse.dd.dsf.debug.service.IExpressions;
import org.eclipse.dd.dsf.debug.service.IMemory;
import org.eclipse.dd.dsf.debug.service.IModules;
import org.eclipse.dd.dsf.debug.service.IProcesses;
import org.eclipse.dd.dsf.debug.service.IRegisters;
import org.eclipse.dd.dsf.debug.service.IRunControl;
import org.eclipse.dd.dsf.debug.service.ISourceLookup;
import org.eclipse.dd.dsf.debug.service.IStack;
import org.eclipse.dd.dsf.debug.service.command.ICommandControl;
import org.eclipse.dd.dsf.service.DsfServicesTracker;
import org.eclipse.dd.dsf.service.IDsfService;
import org.eclipse.dd.gdb.internal.GdbPlugin;
import org.eclipse.dd.mi.service.IMIBackend;
import org.eclipse.dd.mi.service.MIBreakpointsManager;

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
            requestMonitor.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, IDsfStatusConstants.INTERNAL_ERROR,
                "Service '" + clazz.getName() + "' not found.", null)); //$NON-NLS-1$//$NON-NLS-2$
            requestMonitor.done();
        }
    }
}
