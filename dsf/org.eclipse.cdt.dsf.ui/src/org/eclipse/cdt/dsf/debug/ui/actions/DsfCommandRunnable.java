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
package org.eclipse.cdt.dsf.debug.ui.actions;

import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.concurrent.Immutable;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.debug.service.IProcesses;
import org.eclipse.cdt.dsf.debug.service.IRunControl;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.SteppingController;
import org.eclipse.cdt.dsf.internal.ui.DsfUIPlugin;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.commands.IDebugCommandRequest;

/**
 * @since 1.0
 */
@Immutable
public abstract class DsfCommandRunnable extends DsfRunnable {
    private final IExecutionDMContext fContext;
    private final DsfServicesTracker fTracker;
    private final IDebugCommandRequest fRequest;
    
    public IExecutionDMContext getContext() { return fContext; }
    public IRunControl getRunControl() {
        return fTracker.getService(IRunControl.class);
    }

    /**
	 * @since 1.1
	 */
    public SteppingController getSteppingController() {
    	if (fContext != null) {
    		return (SteppingController) fContext.getAdapter(SteppingController.class);
    	}
    	return null;
    }

    /**
     * @since 1.1
     */
    public IProcesses getProcessService() {
    	return fTracker.getService(IProcesses.class);
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
    
    @Override
	public final void run() {
        if (fRequest.isCanceled()) {
            fRequest.done();
            return;
        }
        if (getContext() == null) {
            fRequest.setStatus(makeError("Selected object does not support run control.", null));             //$NON-NLS-1$
        } else if (getRunControl() == null || getSteppingController() == null) {
            fRequest.setStatus(makeError("Run Control not available", null)); //$NON-NLS-1$
        } else {
            doExecute();
        }
        fRequest.done();
    }

    /**
     * Method to perform the actual work.
     */
    protected abstract void doExecute();
    
    protected IStatus makeError(String message, Throwable e) {
        return new Status(IStatus.ERROR, DsfUIPlugin.PLUGIN_ID, -1, message, e);
    }

}
