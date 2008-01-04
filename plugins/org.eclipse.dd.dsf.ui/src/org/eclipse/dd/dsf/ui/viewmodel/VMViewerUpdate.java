/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.dd.dsf.ui.viewmodel;

import java.util.concurrent.RejectedExecutionException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.dd.dsf.concurrent.RequestMonitor;
import org.eclipse.dd.dsf.ui.DsfUIPlugin;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementContentProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate;
import org.eclipse.jface.viewers.TreePath;

/** 
 * Helper class implementation of an update object to use with IElementContentProvider.
 * @see IElementContentProvider
 */
@SuppressWarnings("restriction")
public class VMViewerUpdate implements IViewerUpdate {
    final protected RequestMonitor fRequestMonitor;
    final protected IViewerUpdate fClientUpdate;
    
    public VMViewerUpdate(IViewerUpdate clientUpdate, RequestMonitor requestMonitor) {
        fRequestMonitor = requestMonitor;
        fClientUpdate = clientUpdate;
    }

    public Object getViewerInput() { return fClientUpdate.getViewerInput(); }
    public Object getElement() { return fClientUpdate.getElement(); }
    public TreePath getElementPath() { return fClientUpdate.getElementPath(); }
    public IPresentationContext getPresentationContext() { return fClientUpdate.getPresentationContext(); }

    public IStatus getStatus() { return fRequestMonitor.getStatus(); }
    public void setStatus(IStatus status) { fRequestMonitor.setStatus(status); }
    public boolean isCanceled() { return fClientUpdate.isCanceled(); }
    public void cancel() {
        fClientUpdate.cancel();
    }

    public void done() { 
        try {
            if ( isCanceled() ) {
                fRequestMonitor.setStatus(new Status( IStatus.CANCEL, DsfUIPlugin.PLUGIN_ID," Update was cancelled") ); //$NON-NLS-1$
            }
            fRequestMonitor.done();
        } catch (RejectedExecutionException e) { // Ignore
        }
    }

}
