/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.dd.dsf.ui.viewmodel;

import org.eclipse.dd.dsf.concurrent.GetDataDone;
import org.eclipse.debug.internal.ui.viewers.provisional.IModelDelta;

/**
 * Special type of the view model layout node, which can be used as a root node
 * for a hierarchy.  The root node of a layout hierarchy has to implement this
 * interface.
 */
@SuppressWarnings("restriction")
public interface IVMRootLayoutNode extends IVMLayoutNode{
    
    /**
     * The root VMC object of this root layout node.  There can be only element 
     * in the root node at a time, and this element must implement this 
     * interface.    
     */
    public interface IRootVMC extends IVMContext {
        /**
         * Returns the view's "input" object.  This could be the actual input
         * object for the whole view, if this view model hierarchy fills the 
         * whole view. Or this could an element in the tree from which this 
         * hierarchy starts.  This is the case 
         */
        Object getInputObject();
    }

    /**
     * Returns the single element of this node.  Root layout node can only have
     * one element, and this is a convenience method to access this element.
     * Alternatively getElements() could be used.  
     * @return
     */
    public IRootVMC getRootVMC();
    
    /**
     * Version of the {@link IVMLayoutNode#buildDelta(Object, ViewModelDelta, org.eclipse.dd.dsf.concurrent.Done)}
     * method, which creates and returns the root node of the delta.  It does 
     * not require a parent object for the delta, as this is the root node. 
     * @param event Event to process.
     * @param done Result notification, contains the root of the delta.
     */
    public void createDelta(Object event, GetDataDone<IModelDelta> done);
}
