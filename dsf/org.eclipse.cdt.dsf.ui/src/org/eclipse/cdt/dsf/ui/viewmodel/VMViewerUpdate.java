/*******************************************************************************
 * Copyright (c) 2007, 2010 Wind River Systems and others.
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
import org.eclipse.cdt.dsf.concurrent.DsfExecutable;
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
public class VMViewerUpdate extends DsfExecutable implements IViewerUpdate {
    
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
     * Place holder for the client update.  It is only used if the client update is
     * not specified.
     */
    private static class ClientUpdatePlaceHolder implements IViewerUpdate {
        
        ClientUpdatePlaceHolder(TreePath elementPath, Object viewerInput, IPresentationContext presentationContext)
        {
            fViewerInput = viewerInput;
            fElementPath = elementPath;
            fPresentationContext = presentationContext;
        }
        /**
         * The flag indicating whether this update was canceled.
         */
        private volatile boolean fCanceled;
        
        /**
         * The viewer input object for this update.
         */
        final private Object fViewerInput;
        
        /**
         * The element path of this update.
         */
        final private TreePath fElementPath;
        
        /**
         * The presentation context of this update.
         */
        final private IPresentationContext fPresentationContext;

        public void cancel() {
            fCanceled = true;
        }
        
        public boolean isCanceled() { 
            return fCanceled; 
        }

        public IPresentationContext getPresentationContext() {
            return fPresentationContext;
        }

        public Object getElement() {
            return fElementPath.getSegmentCount() != 0 ? fElementPath.getLastSegment() : fViewerInput;
        }

        public TreePath getElementPath() {
            return fElementPath;
        }

        public Object getViewerInput() {
            return fViewerInput;
        }
        
        public void done() { assert false; } // not used
        public void setStatus(IStatus status) {assert false; } // not used
        public IStatus getStatus() { assert false; return null; } // not used

    }
    
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
        fRequestMonitor = requestMonitor;
        fClientUpdate = clientUpdate;
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
        Object viewerInput = elementList.remove(0);
        fClientUpdate = new ClientUpdatePlaceHolder(
            new TreePath(elementList.toArray()), viewerInput, presentationContext);
        fRequestMonitor = requestMonitor;
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
        fRequestMonitor = requestMonitor;
        fClientUpdate = new ClientUpdatePlaceHolder(elementPath, viewerInput, presentationContext);
    }
    
    protected RequestMonitor getRequestMonitor() {
        return fRequestMonitor;
    }
    
    public Object getViewerInput() { return fClientUpdate.getViewerInput(); }
    public Object getElement() { return fClientUpdate.getElement(); }
    public TreePath getElementPath() { return fClientUpdate.getElementPath(); }
    public IPresentationContext getPresentationContext() { return fClientUpdate.getPresentationContext(); }
    public IStatus getStatus() { return fRequestMonitor.getStatus(); }
    public void setStatus(IStatus status) { fRequestMonitor.setStatus(status); }
    
    public boolean isCanceled() { 
        return fClientUpdate.isCanceled();
    }
    public void cancel() {
        fClientUpdate.cancel();
    }

    public void done() { 
    	setSubmitted();
        if ( isCanceled() ) {
            fRequestMonitor.cancel();
            fRequestMonitor.setStatus(new Status( IStatus.CANCEL, DsfUIPlugin.PLUGIN_ID," Update was canceled") ); //$NON-NLS-1$
        }
        fRequestMonitor.done();
    }

}
