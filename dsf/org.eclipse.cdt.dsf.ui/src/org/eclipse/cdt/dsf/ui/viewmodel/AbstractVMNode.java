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

    @Override
	public IVMProvider getVMProvider() {
        return fProvider;
    }
    
    @Override
	public void dispose() {
        fDisposed = true;
    }

    @Override
	public void getContextsForEvent(VMDelta parentDelta, Object event, DataRequestMonitor<IVMContext[]> rm) {
        rm.setStatus(new Status(IStatus.ERROR, DsfUIPlugin.PLUGIN_ID, IDsfStatusConstants.NOT_SUPPORTED, "", null)); //$NON-NLS-1$
        rm.done();
    }
    
    protected boolean isDisposed() { 
        return fDisposed;
    }

	/**
	 * Checks whether there are any fundamental roadblocks which will prevent
	 * this request from being serviced. The base implementation simply checks
	 * to see if the request has been canceled or if this node has been
	 * disposed. Subclasses should override this if it can do additional checks,
	 * for example check that needed services are still available. Overrides
	 * should probably call the base implementation first; if the base
	 * determines the request is a no-go, there's no point in the subclass doing
	 * any further checking.
	 * 
	 * <p>
	 * If a roadblock is found, the implementation should give the update
	 * request some appropriate default result (if applicable) and call its
	 * <code>done</code> method.
	 *
     * @param update the update request
     * @return false if a roadblock is found, otherwise true
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
