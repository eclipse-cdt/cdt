/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems and others.
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
import org.eclipse.dd.dsf.service.DsfServicesTracker;
import org.eclipse.dd.dsf.service.IDsfService;
import org.eclipse.dd.gdb.internal.GdbPlugin;
import org.eclipse.dd.gdb.internal.provisional.service.GDBRunControl;
import org.eclipse.dd.gdb.internal.provisional.service.command.GDBControl;
import org.eclipse.dd.mi.service.CSourceLookup;
import org.eclipse.dd.mi.service.ExpressionService;
import org.eclipse.dd.mi.service.MIBreakpoints;
import org.eclipse.dd.mi.service.MIBreakpointsManager;
import org.eclipse.dd.mi.service.MIDisassembly;
import org.eclipse.dd.mi.service.MIMemory;
import org.eclipse.dd.mi.service.MIModules;
import org.eclipse.dd.mi.service.MIRegisters;
import org.eclipse.dd.mi.service.MIStack;

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
            shutdownService(MIDisassembly.class, requestMonitor);
        }
    }, new Step() {
        @Override
        public void execute(RequestMonitor requestMonitor) {
            shutdownService(MIRegisters.class, requestMonitor);
        }
// TODO: As Pawel about the necessity of this step
// Not clear on the purpose of this step since the next one does it also
// (stopTrackingBreakpoints() is called as part of the shutdown method)
// Besides, the run control is already gone so removing breakpoints from
// the back-end is bound to fail...
//    }, new Step() {
//        @Override
//        public void execute(final RequestMonitor requestMonitor) {
//            MIBreakpointsManager bpm = fTracker.getService(MIBreakpointsManager.class);
//            GDBControl commandControl = fTracker.getService(GDBControl.class);
//            if (bpm != null && commandControl != null) {
//            	bpm.stopTrackingBreakpoints(
//                    commandControl.getGDBDMContext(), 
//                    new RequestMonitor(getExecutor(), requestMonitor) {
//                        @Override
//                        protected void handleCompleted() {
//                            // If un-installing breakpoints fails, log the error but continue shutting down.
//                            if (!isSuccess) {
//                                DsfGdbPlugin.getDefault().getLog().log(getStatus());
//                            }
//                            requestMonitor.done();
//                        }
//                    });
//            } else {
//                requestMonitor.setStatus(new Status(IStatus.ERROR, DsfGdbPlugin.PLUGIN_ID, IDsfStatusConstants.INTERNAL_ERROR,
//                    "Needed services not found.", null)); //$NON-NLS-1$
//                requestMonitor.done();
//            }
//        }
    }, new Step() {
        @Override
        public void execute(RequestMonitor requestMonitor) {
            shutdownService(MIBreakpointsManager.class, requestMonitor);
        }
    }, new Step() {
        @Override
        public void execute(RequestMonitor requestMonitor) {
            shutdownService(MIBreakpoints.class, requestMonitor);
        }
    }, new Step() {
        @Override
        public void execute(RequestMonitor requestMonitor) {
            shutdownService(CSourceLookup.class, requestMonitor);
        }
    }, new Step() {
        @Override
        public void execute(RequestMonitor requestMonitor) {
            shutdownService(ExpressionService.class, requestMonitor);
        }
    }, new Step() {
        @Override
        public void execute(RequestMonitor requestMonitor) {
            shutdownService(MIStack.class, requestMonitor);
        }
    }, new Step() {
        @Override
        public void execute(RequestMonitor requestMonitor) {
            shutdownService(MIModules.class, requestMonitor);
        }
    }, new Step() {
        @Override
        public void execute(RequestMonitor requestMonitor) {
            shutdownService(MIMemory.class, requestMonitor);
        }
    }, new Step() {
        @Override
        public void execute(RequestMonitor requestMonitor) {
            shutdownService(GDBRunControl.class, requestMonitor);
        }
    }, new Step() {
        @Override
        public void execute(RequestMonitor requestMonitor) {
            shutdownService(GDBControl.class, requestMonitor);
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
