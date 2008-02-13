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
package org.eclipse.dd.examples.pda.launch;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.dd.dsf.concurrent.DsfExecutor;
import org.eclipse.dd.dsf.concurrent.RequestMonitor;
import org.eclipse.dd.dsf.concurrent.Sequence;
import org.eclipse.dd.dsf.debug.service.BreakpointsMediator;
import org.eclipse.dd.dsf.debug.service.StepQueueManager;
import org.eclipse.dd.dsf.service.DsfServicesTracker;
import org.eclipse.dd.dsf.service.IDsfService;
import org.eclipse.dd.examples.pda.PDAPlugin;
import org.eclipse.dd.examples.pda.service.breakpoints.PDABreakpoints;
import org.eclipse.dd.examples.pda.service.command.PDACommandControl;
import org.eclipse.dd.examples.pda.service.runcontrol.PDARunControl;

public class PDAServicesShutdownSequence extends Sequence {

    String fSessionId;
    DsfServicesTracker fTracker;

    public PDAServicesShutdownSequence(DsfExecutor executor, String sessionId, RequestMonitor requestMonitor) {
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
            assert PDAPlugin.getBundleContext() != null;
            fTracker = new DsfServicesTracker(PDAPlugin.getBundleContext(), fSessionId);
            requestMonitor.done();
        }

        @Override
        public void rollBack(RequestMonitor requestMonitor) {
            fTracker.dispose();
            fTracker = null;
            requestMonitor.done();
        }
    }, 
/*    new Step() {
        @Override
        public void execute(RequestMonitor requestMonitor) {
            shutdownService(MIRegisters.class, requestMonitor);
        }
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
    },
    new Step() {
        @Override
        public void execute(RequestMonitor requestMonitor) {
            shutdownService(MIMemory.class, requestMonitor);
        }
    }, */
    new Step() {
        @Override
        public void execute(RequestMonitor requestMonitor) {
            shutdownService(BreakpointsMediator.class, requestMonitor);
        }
    }, new Step() {
        @Override
        public void execute(RequestMonitor requestMonitor) {
            shutdownService(PDABreakpoints.class, requestMonitor);
        }
    },
    new Step() {
        @Override
        public void execute(RequestMonitor requestMonitor) {
            shutdownService(StepQueueManager.class, requestMonitor);
        }
    },
    new Step() {
        @Override
        public void execute(RequestMonitor requestMonitor) {
            shutdownService(PDARunControl.class, requestMonitor);
        }
    },
    new Step() {
        @Override
        public void execute(RequestMonitor requestMonitor) {
            shutdownService(PDACommandControl.class, requestMonitor);
        }
    }, 
    new Step() {
        @Override
        public void execute(RequestMonitor requestMonitor) {
            fTracker.dispose();
            fTracker = null;
            requestMonitor.done();
        }
    } };

    @SuppressWarnings("unchecked")
    private void shutdownService(Class clazz, final RequestMonitor requestMonitor) {
        IDsfService service = fTracker.getService(clazz);
        if (service != null) {
            service.shutdown(new RequestMonitor(getExecutor(), requestMonitor) {
                @Override
                protected void handleCompleted() {
                    if (!getStatus().isOK()) {
                        PDAPlugin.getDefault().getLog().log(getStatus());
                    }
                    requestMonitor.done();
                }
            });
        } else {
            requestMonitor.setStatus(new Status(IStatus.ERROR, PDAPlugin.PLUGIN_ID, IDsfService.INTERNAL_ERROR,
                "Service '" + clazz.getName() + "' not found.", null)); //$NON-NLS-1$//$NON-NLS-2$
            requestMonitor.done();
        }
    }
}
