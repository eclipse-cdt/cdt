/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems and others.
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
import org.eclipse.dd.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.dd.dsf.concurrent.RequestMonitor;
import org.eclipse.dd.dsf.concurrent.Sequence;
import org.eclipse.dd.dsf.debug.service.BreakpointsMediator;
import org.eclipse.dd.dsf.debug.service.StepQueueManager;
import org.eclipse.dd.dsf.service.DsfServicesTracker;
import org.eclipse.dd.dsf.service.IDsfService;
import org.eclipse.dd.examples.pda.PDAPlugin;
import org.eclipse.dd.examples.pda.service.PDABreakpoints;
import org.eclipse.dd.examples.pda.service.PDACommandControl;
import org.eclipse.dd.examples.pda.service.PDAExpressions;
import org.eclipse.dd.examples.pda.service.PDARunControl;
import org.eclipse.dd.examples.pda.service.PDAStack;

/**
 * The shutdown sequence for PDA debugger services.  This sequence contains
 * the series of steps that are executed to properly shutdown the PDA-DSF debug
 * session.  If any of the individual steps fail, the shutdown will abort.
 * <p>
 * Services are shut down in the reverse order of initialization.
 * </p>   
 */
public class PDAServicesShutdownSequence extends Sequence {
    
    private final Step[] fSteps = new Step[] { 
        new Step() {
            @Override
            public void execute(RequestMonitor requestMonitor) {
                // Initialize services tracker.
                assert PDAPlugin.getBundleContext() != null;
                fTracker = new DsfServicesTracker(PDAPlugin.getBundleContext(), fSessionId);
                requestMonitor.done();
            }
    
            @Override
            public void rollBack(RequestMonitor requestMonitor) {
                // In case the shutdown sequence aborts, ensure that the 
                // tracker is properly disposed.
                fTracker.dispose();
                fTracker = null;
                requestMonitor.done();
            }
        }, 
        new Step() {
            @Override
            public void execute(RequestMonitor requestMonitor) {
                shutdownService(PDAExpressions.class, requestMonitor);
            }
        }, 
        new Step() {
            @Override
            public void execute(RequestMonitor requestMonitor) {
                shutdownService(PDAStack.class, requestMonitor);
            }
        },
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
        } 
    };

    private String fSessionId;
    private DsfServicesTracker fTracker;
    
    public PDAServicesShutdownSequence(DsfExecutor executor, String sessionId, RequestMonitor requestMonitor) {
        super(executor, requestMonitor);
        fSessionId = sessionId;
    }

    @Override
    public Step[] getSteps() {
        return fSteps;
    }

    @SuppressWarnings("unchecked")
    private void shutdownService(Class clazz, final RequestMonitor requestMonitor) {
        IDsfService service = (IDsfService)fTracker.getService(clazz);
        if (service != null) {
            service.shutdown(new RequestMonitor(getExecutor(), requestMonitor) {
                @Override
                protected void handleCompleted() {
                    if (!isSuccess()) {
                        PDAPlugin.getDefault().getLog().log(getStatus());
                    }
                    requestMonitor.done();
                }
            });
        } else {
            requestMonitor.setStatus(new Status(IStatus.ERROR, PDAPlugin.PLUGIN_ID, IDsfStatusConstants.INTERNAL_ERROR,
                "Service '" + clazz.getName() + "' not found.", null)); //$NON-NLS-1$//$NON-NLS-2$
            requestMonitor.done();
        }
    }
}
