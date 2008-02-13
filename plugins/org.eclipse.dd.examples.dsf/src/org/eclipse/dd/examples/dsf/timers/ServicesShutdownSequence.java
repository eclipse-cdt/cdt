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
package org.eclipse.dd.examples.dsf.timers;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.dd.dsf.concurrent.RequestMonitor;
import org.eclipse.dd.dsf.concurrent.Sequence;
import org.eclipse.dd.dsf.service.DsfServicesTracker;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.dd.dsf.service.IDsfService;
import org.eclipse.dd.examples.dsf.DsfExamplesPlugin;

/**
 * Shutdown sequence that stops the services in the timers session.  
 *
 */
class ServicesShutdownSequence extends Sequence {

    DsfSession fSession;
    DsfServicesTracker fTracker;

    ServicesShutdownSequence(DsfSession session) {
        super(session.getExecutor());
        fSession = session;
    }
    
    Step[] fSteps = new Step[] {
        new Step() { 
            @Override
            public void execute(RequestMonitor requestMonitor) {
                fTracker = new DsfServicesTracker(DsfExamplesPlugin.getBundleContext(), fSession.getId());
                requestMonitor.done();
            }
            
            @Override
            public void rollBack(RequestMonitor requestMonitor) {
                fTracker.dispose();
                fTracker = null;
                requestMonitor.done();
            } 
        },
        new Step() { 
            @Override
            public void execute(RequestMonitor requestMonitor) {
                shutdownService(AlarmService.class, requestMonitor);
            }
        },
        new Step() { 
            @Override
            public void execute(RequestMonitor requestMonitor) {
                shutdownService(TimerService.class, requestMonitor);
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
    
    @Override
    public Step[] getSteps() { return fSteps; }

    /**
     * Convenience method that shuts down given service.  Only service class 
     * is used to identify the service. 
     */
    private <V extends IDsfService> void shutdownService(Class<V> clazz, RequestMonitor requestMonitor) {
        IDsfService service = fTracker.getService(clazz);
        if (service != null) {
            service.shutdown(requestMonitor);
        }
        else {
            requestMonitor.setStatus(new Status(IStatus.ERROR, DsfExamplesPlugin.PLUGIN_ID, IDsfService.INTERNAL_ERROR,  
                                      "Service '" + clazz.getName() + "' not found.", null));             //$NON-NLS-1$ //$NON-NLS-2$
            requestMonitor.done();
        }
    }

}
