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

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelProxy;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.Viewer;

/**
 * View Model extension to the platform IModelProxy interface.  This extension
 * allows the IVMProvider implementation to delegate the model proxy implementation
 * into a separate object. 
 * <br/> 
 * Note: The IVMModelProxy.init() may be called twice when installed, as a 
 * workaround for bug 241024. 
 * 
 * @since 1.0
 */
public interface IVMModelProxy extends IModelProxy {

    /**
     * Returns the root element that this model proxy was created for.  
     */
    public Object getRootElement();
    
    /**
     * Returns whether the given event applies to the root element and the 
     * nodes in this model proxy. 
     * <p>
     * This method is the equivalent of calling 
     * <code> getEventDeltaFlags(event) != IModelDelta.NO_CHANGE </code>.
     * </p>
     */
    public boolean isDeltaEvent(Object event);

    /**
     * Creates a model delta for the given event.
     */
    public void createDelta(final Object event, final DataRequestMonitor<IModelDelta> rm);

    /**
     * Sends the given delta to this model proxy's listeners.
     */
    public void fireModelChanged(IModelDelta delta);

    /**
     * Returns the viewer.
     * 
     * @since 2.0
     */
    public Viewer getViewer();

    /**
     * Returns the viewer input that was set to the viewer when this proxy 
     * was created.  
     * 
     * @since 2.0
     */
    public Object getViewerInput();

    /**
     * Returns the full path for the root element.  If the path is empty, it
     * means that the root element is the viewer input.
     * 
     * @since 2.0
     */
    public TreePath getRootPath();

    /**
     * Returns the delta flags associated with this event.  This method is   
     * 
     * @param event 
     * @return
     * 
     * @since 2.1
     */
    public int getEventDeltaFlags(Object event);

}
