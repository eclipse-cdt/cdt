/*******************************************************************************
 * Copyright (c) 2006, 2012 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Marc Khouzam (Ericsson) - Added support for multiple selection (bug 330974)
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.ui.actions;

import java.util.HashSet;
import java.util.Set;

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
    private final IExecutionDMContext[] fContexts;
    private final DsfServicesTracker fTracker;
    private final IDebugCommandRequest fRequest;
    
    // For backwards compatibility, keep this method that returns the first selection.  This method
    // is meaningful when we only support a single selection.
    public IExecutionDMContext getContext() { return (fContexts != null && fContexts.length > 0) ? fContexts[0] : null; }
    /**
     * Return all selected contexts. 
     * @since 2.3 
     */
    public IExecutionDMContext[] getContexts() { return fContexts; }
    public IRunControl getRunControl() {
        return fTracker.getService(IRunControl.class);
    }

    /**
	 * @since 1.1
	 */
    public SteppingController getSteppingController() {
    	if (fContexts != null && fContexts.length > 0) {
    		return (SteppingController) fContexts[0].getAdapter(SteppingController.class);
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
    	this(servicesTracker, new Object[] { element }, request);
    }
    
    /** @since 2.3 */
    public DsfCommandRunnable(DsfServicesTracker servicesTracker, Object[] elements, IDebugCommandRequest request) {
    	fTracker = servicesTracker;
    	fRequest = request;

		// Extract all selected execution contexts, using a set to avoid duplicates.  Duplicates will
    	// happen if multiple stack frames of the same thread are selected.
		Set<IExecutionDMContext> execDmcSet = new HashSet<IExecutionDMContext>(request.getElements().length);
		for (Object element : request.getElements()) {
			if (element instanceof IDMVMContext) {
				IDMVMContext vmc = (IDMVMContext)element;
				IExecutionDMContext execDmc = DMContexts.getAncestorOfType(vmc.getDMContext(), IExecutionDMContext.class);
				if (execDmc != null) {
					// We have a thread or a process
					execDmcSet.add(execDmc);
				}
			}
		}

		if (execDmcSet.size() == 0) {
			fContexts = null;
		} else {
			fContexts = execDmcSet.toArray(new IExecutionDMContext[execDmcSet.size()]);
		}
    }
    
    @Override
	public final void run() {
        if (fRequest.isCanceled()) {
            fRequest.done();
            return;
        }
        if (getContexts() == null || getContexts().length == 0) {
            fRequest.setStatus(makeError("Selected objects do not support run control.", null));   //$NON-NLS-1$
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
