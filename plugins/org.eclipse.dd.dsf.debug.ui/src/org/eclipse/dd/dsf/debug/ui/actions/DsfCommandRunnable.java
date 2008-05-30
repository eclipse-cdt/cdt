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
package org.eclipse.dd.dsf.debug.ui.actions;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.dd.dsf.concurrent.DsfRunnable;
import org.eclipse.dd.dsf.concurrent.Immutable;
import org.eclipse.dd.dsf.datamodel.DMContexts;
import org.eclipse.dd.dsf.debug.internal.ui.DsfDebugUIPlugin;
import org.eclipse.dd.dsf.debug.service.IRunControl;
import org.eclipse.dd.dsf.debug.service.StepQueueManager;
import org.eclipse.dd.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.dd.dsf.service.DsfServicesTracker;
import org.eclipse.dd.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.debug.core.commands.IDebugCommandRequest;

@Immutable
public abstract class DsfCommandRunnable extends DsfRunnable {
    private final IExecutionDMContext fContext;
    private final DsfServicesTracker fTracker;
    private final IDebugCommandRequest fRequest;
    
    public IExecutionDMContext getContext() { return fContext; }
    public IRunControl getRunControl() {
        return fTracker.getService(IRunControl.class);
    }
    public StepQueueManager getStepQueueMgr() {
        return fTracker.getService(StepQueueManager.class);
    }

    public DsfCommandRunnable(DsfServicesTracker servicesTracker, Object element, IDebugCommandRequest request) {
        fTracker = servicesTracker;
        if (element instanceof IDMVMContext) {
            IDMVMContext vmc = (IDMVMContext)element;
            fContext = DMContexts.getAncestorOfType(vmc.getDMContext(), IExecutionDMContext.class);
        } else {
            fContext = null;
        }
            
        fRequest = request;
    }
    
    public final void run() {
        if (fRequest.isCanceled()) {
            fRequest.done();
            return;
        }
        if (getContext() == null) {
            fRequest.setStatus(makeError("Selected object does not support run control.", null));             //$NON-NLS-1$
        } else if (getRunControl() == null || getStepQueueMgr() == null) {
            fRequest.setStatus(makeError("Run Control not available", null)); //$NON-NLS-1$
        } else {
            doExecute();
        }
        fRequest.done();
    }

    /**
     * Method to perform the actual work.  It should not call monitor.done(), because
     * it will be called in the super-class.
     */
    protected abstract void doExecute();
    
    protected IStatus makeError(String message, Throwable e) {
        return new Status(IStatus.ERROR, DsfDebugUIPlugin.PLUGIN_ID, -1, message, e);
    }

}
