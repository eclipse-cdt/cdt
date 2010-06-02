/*******************************************************************************
 * Copyright (c) 2006, 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.events;

import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Sequence;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.service.IDsfService;
import org.eclipse.cdt.tests.dsf.DsfTestPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

class ShutdownSequence extends Sequence {

    DsfSession fSession;
    DsfServicesTracker fTracker;

    ShutdownSequence(DsfSession session) {
        super(session.getExecutor());
        fSession = session;
    }

    @Override
    public Step[] getSteps() { return fSteps; }

    final Step[] fSteps = new Step[] {
        new Step() { 
            @Override public void execute(RequestMonitor requestMonitor) {
                fTracker = new DsfServicesTracker(DsfTestPlugin.getBundleContext(), fSession.getId());
                requestMonitor.done();
            }
            
            @Override public void rollBack(RequestMonitor requestMonitor) {
                fTracker.dispose();
                fTracker = null;
                requestMonitor.done();
            } 
        },        
        new Step() { @Override public void execute(RequestMonitor requestMonitor) {
            shutdownService(Service3.class, requestMonitor);
        }},
        new Step() { @Override public void execute(RequestMonitor requestMonitor) {
            shutdownService(Service2.class, requestMonitor);
        }},
        new Step() { @Override public void execute(RequestMonitor requestMonitor) {
            shutdownService(Service1.class, requestMonitor);
        }},
        new Step() { @Override public void execute(RequestMonitor requestMonitor) {
            fTracker.dispose();
            fTracker = null;
            requestMonitor.done();
        }}
    };
    
    private void shutdownService(Class<? extends IDsfService> clazz, RequestMonitor requestMonitor) {
        IDsfService service = fTracker.getService(clazz);
        if (service != null) {
            service.shutdown(requestMonitor);
        }
        else {
            requestMonitor.setStatus(new Status(IStatus.ERROR, DsfTestPlugin.PLUGIN_ID, -1,  "Service '" + clazz.getName() + "' not found.", null));              //$NON-NLS-1$//$NON-NLS-2$
            requestMonitor.done();
        }
    }

}
