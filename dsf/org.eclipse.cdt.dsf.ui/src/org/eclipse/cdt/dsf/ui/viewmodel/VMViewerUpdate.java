/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.ui.viewmodel;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.internal.ui.DsfUIPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.IRequest;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementContentProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementLabelProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementMementoProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate;
import org.eclipse.jface.viewers.TreePath;

/** 
 * Helper class implementation of the update objects used with 
 * {@link IElementContentProvider}, {@link IElementLabelProvider}, 
 * and {@link IElementMementoProvider}.  The viewer update can be constructed 
 * using a higher level update object or a set of parameters to fulfill the 
 * <code>IViewerUpdate</code> interface. 
 * 
 * @since 1.0
 */
public class VMViewerUpdate implements IViewerUpdate {
    
    /**
     * The request monitor to be called when this update is completed.
     */
    final private RequestMonitor fRequestMonitor;
    
    /**
     * A higher-level update that this update is based on.  If specified, the given
     * update is used to delegate calls to {@link #cancel()} and {@link #isCanceled()}.
     */
    final private IViewerUpdate fClientUpdate;
    
    /**
     * The flag indicating whether this update was cancelled.  This flag is not used
     * if the {@link #fClientUpdate} is initialized.
     */
    final private AtomicBoolean fCanceled;
    
    /**
     * The viewer input object for this update.
     */
    final private Object fViewerInput;
    
    /**
     * The element object of this update.
     */
    final private Object fElement;
    
    /**
     * The element path of this update.
     */
    final private TreePath fElementPath;
    
    /**
     * The presentation context of this update.
     */
    final private IPresentationContext fPresentationContext;

    /**
     * Creates a viewer update based on a higher-level update.  The update element
     * information as well as cancel requests are delegated to the given client
     * update.
     * <p/>
     * Note: this update will not automatically call the client update's 
     * {@link IRequest#done()} method.  The user of this update should supply
     * a request monitor which properly completes the client update. 
     * 
     * @param clientUpdate Client update that this update is based on.
     * @param requestMonitor Call-back invoked when this update completes.  
     */
    public VMViewerUpdate(IViewerUpdate clientUpdate, RequestMonitor requestMonitor) {
        fViewerInput = clientUpdate.getViewerInput();
        fElement = clientUpdate.getElement();
        fElementPath = clientUpdate.getElementPath();
        fPresentationContext = clientUpdate.getPresentationContext();
        fRequestMonitor = requestMonitor;
        fClientUpdate = clientUpdate;
        fCanceled = null;
    }

    /**
     * Request monitor which uses a model delta to calculate the element information.
     * This update is useful when calculating a model delta for a given view model node.
     * 
     * @param delta Model delta of a parent element.
     * @param presentationContext Presentation context for this update.
     * @param requestMonitor Call-back invoked when this update completes.  
     */
    public VMViewerUpdate(IModelDelta delta, IPresentationContext presentationContext, RequestMonitor requestMonitor) {
        List<Object> elementList = new LinkedList<Object>();
        IModelDelta listDelta = delta;
        elementList.add(0, listDelta.getElement());
        while (listDelta.getParentDelta() != null) {
            listDelta = listDelta.getParentDelta();
            elementList.add(0, listDelta.getElement());
        }
        fViewerInput = elementList.get(0);
        fElement = elementList.get(elementList.size() - 1);
        elementList.remove(0);
        fElementPath = new TreePath(elementList.toArray());
        fPresentationContext = presentationContext;
        fRequestMonitor = requestMonitor;
        fClientUpdate = null;
        fCanceled = new AtomicBoolean(false);
    }

    /**
     * Creates a viewer update with the given parameters.
     * 
     * @param elementPath The path to the element for which the update is generated.
     * @param viewerInput Input into the viewer of the update.
     * @param presentationContext Presentation context for this update.
     * @param requestMonitor Call-back invoked when this update completes.  
     */
    public VMViewerUpdate(TreePath elementPath, Object viewerInput, IPresentationContext presentationContext, RequestMonitor requestMonitor) {
        fViewerInput = viewerInput;
        fElement = elementPath.getSegmentCount() != 0 ? elementPath.getLastSegment() : viewerInput;
        fElementPath = elementPath;
        fPresentationContext = presentationContext;
        fRequestMonitor = requestMonitor;
        fClientUpdate = null;
        fCanceled = new AtomicBoolean(false);
    }
    
    protected RequestMonitor getRequestMonitor() {
        return fRequestMonitor;
    }
    
    public Object getViewerInput() { return fViewerInput; }
    public Object getElement() { return fElement; }
    public TreePath getElementPath() { return fElementPath; }
    public IPresentationContext getPresentationContext() { return fPresentationContext; }
    public IStatus getStatus() { return fRequestMonitor.getStatus(); }
    public void setStatus(IStatus status) { fRequestMonitor.setStatus(status); }
    
    public boolean isCanceled() { 
        if (fClientUpdate != null) {
            return fClientUpdate.isCanceled();
        } else {
            return fCanceled.get();
        }
    }
    public void cancel() {
        if (fClientUpdate != null) {
            fClientUpdate.cancel();
        } else {
            fCanceled.set(true);
        }
    }

    public void done() { 
        try {
            if ( isCanceled() ) {
                fRequestMonitor.cancel();
                fRequestMonitor.setStatus(new Status( IStatus.CANCEL, DsfUIPlugin.PLUGIN_ID," Update was cancelled") ); //$NON-NLS-1$
            }
            fRequestMonitor.done();
        } catch (RejectedExecutionException e) {
            // If the request monitor cannot be invoked still, try to complete the update to avoid
            // leaving the viewer in an inconsistent state.
            if (fClientUpdate != null) {
                fClientUpdate.done();
            }
        }
    }

}
