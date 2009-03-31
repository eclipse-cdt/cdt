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
package org.eclipse.cdt.dsf.ui.viewmodel;

import java.util.concurrent.Executor;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.internal.ui.DsfUIPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenCountUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IHasChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate;

/**
 * Base implementation of the view model node.  
 * The main functionality implemented here is for building the view model 
 * deltas (IModelDelta), based on the flags returned by child nodes. 
 * 
 * @since 1.0
 */
abstract public class AbstractVMNode implements IVMNode {

    private final IVMProvider fProvider;
    private boolean fDisposed = false;
    
    public AbstractVMNode(IVMProvider provider) {
        fProvider = provider;
    }

    /**
     * Accessor method for sub-classes.
     */
    protected Executor getExecutor() {
        return fProvider.getExecutor();
    }

    public IVMProvider getVMProvider() {
        return fProvider;
    }
    
    public void dispose() {
        fDisposed = true;
    }

    public void getContextsForEvent(VMDelta parentDelta, Object event, DataRequestMonitor<IVMContext[]> rm) {
        rm.setStatus(new Status(IStatus.ERROR, DsfUIPlugin.PLUGIN_ID, IDsfStatusConstants.NOT_SUPPORTED, "", null)); //$NON-NLS-1$
        rm.done();
    }
    
    protected boolean isDisposed() { 
        return fDisposed;
    }
    
    /**
     * Convenience method that returns a token value in case when the services
     * that the layout node depends on, are not available.
     */
    protected boolean checkUpdate(IViewerUpdate update) {
        if (update.isCanceled()) {
            update.done();
            return false;
        }
        if (fDisposed) {
            handleFailedUpdate(update);
            return false;
        }
        return true;
    }
    
    /**
     * A convenience method that completes update object in case of an error.
     * Different types of update need to have some data configured to exhibit
     * desired behavior in the viewer.
     * @param update Update to handle.
     */
    protected void handleFailedUpdate(IViewerUpdate update) {
        if (update instanceof IHasChildrenUpdate) {
            ((IHasChildrenUpdate)update).setHasChilren(false);
        } else if (update instanceof IChildrenCountUpdate) {
            ((IChildrenCountUpdate)update).setChildCount(0);            
        } else if (update instanceof ILabelUpdate) {
            ILabelUpdate labelUpdate = (ILabelUpdate)update;
            String[] columns = labelUpdate.getColumnIds();
            for (int i = 0; i < (columns != null ? columns.length : 1); i++) {
                labelUpdate.setLabel("...", i); //$NON-NLS-1$
            }
        }
        update.done();
    }
}
