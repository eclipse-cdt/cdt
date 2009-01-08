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

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelProxy;

/**
 * View Model extension to the platform IModelProxy interface.  This extension
 * allows the IVMProvider implementation to delegate the model proxy implementation
 * into a separate object. 
 * <br/> 
 * Note: The IVMModelProxy.init() may be called twice when installed, as a 
 * workaround for bug 241024. 
 */
@SuppressWarnings("restriction")
public interface IVMModelProxy extends IModelProxy {

    /**
     * Returns the root element that this model proxy was created for.  
     */
    public Object getRootElement();
    
    /**
     * Returns whether the given event applies to the root element and the 
     * nodes in this model proxy.   
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

}
