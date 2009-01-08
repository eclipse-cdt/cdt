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

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;

/**
 * Special type of the view model node, which can be used as a root node
 * for a hierarchy.  The root node of a layout hierarchy has to implement this
 * interface.
 */
public interface IRootVMNode extends IVMNode{
    
    /**
     * Returns whether the given event should be processed for delta generation.
     * Root node is different than other nodes in that there is only one root
     * element in the view model provider hierarchy.  This method allows the root
     * node to match up the root object of the provider with the given event.  If 
     * the root node can determine that the given event does not apply to the root
     * object, it should return false so that the event is ignored.
     *  
     * @param rootObject The root object of the VM provider 
     * @param event
     * @return
     */
    public boolean isDeltaEvent(Object rootObject, Object event);
    
    /**
     * Version of the {@link IVMNode#buildDelta(Object, ViewModelDelta, org.eclipse.cdt.dsf.concurrent.RequestMonitor)}
     * method, which creates and returns the root node of the delta.  It does 
     * not require a parent object for the delta, as this is the root node. 
     * @param event Event to process.
     * @param rm Result notification, contains the root of the delta.
     */
    public void createRootDelta(Object rootObject, Object event, DataRequestMonitor<VMDelta> rm);
}
