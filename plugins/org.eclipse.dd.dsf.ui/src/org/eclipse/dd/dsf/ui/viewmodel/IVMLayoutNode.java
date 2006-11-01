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

import org.eclipse.dd.dsf.concurrent.ConfinedToDsfExecutor;
import org.eclipse.dd.dsf.concurrent.Done;
import org.eclipse.dd.dsf.concurrent.GetDataDone;
import org.eclipse.debug.internal.ui.viewers.provisional.ILabelRequestMonitor;
import org.eclipse.debug.internal.ui.viewers.provisional.IModelDelta;

/**
 * View model layout nodes are combined together into a tree, to collectively 
 * define the layout of a view.  Each schema node generates elements of type 
 * IVMContext, and provide label information about these elements.
 * <p>
 * Besides the standard Data Model Context based implementation, this 
 * node could be implemented to present data from any source, not necessarily
 * DSF services.  It could also define a static node which operates on basis
 * of other data in the view tree.   
 * @see VMProvider
 */
@ConfinedToDsfExecutor("")
@SuppressWarnings("restriction")
public interface IVMLayoutNode {
    
    /**
     * Retrieves information whether for the given parent node, there are any elements
     * available.
     * @param parentVmc Parent node, for which to calculate elements at the
     * current level.
     * @param done The data return token.
     */
    public void hasElements(IVMContext parentVmc, GetDataDone<Boolean> done);
    
    /**
     * Retrieves the list of elements.
     * @param parentVmc Parent node, for which to calculate elements at the
     * current level.
     * @param done The data return token.
     */
    public void getElements(IVMContext parentVmc, GetDataDone<IVMContext[]> done);
    
    /**
     * Retrieves the label for the given element. 
     * @param vmc Element for which to retrieve label information.
     * @param result Monitor which accepts the data.
     */
    public void retrieveLabel(IVMContext vmc, ILabelRequestMonitor result);

    /**
     * Configures the child layout nodes for this node.
     * @param childNodes
     */
    public void setChildNodes(IVMLayoutNode[] childNodes);

    /**
     * Returns the list of child layout nodes which are configured for this node.
     */
    public IVMLayoutNode[] getChildLayoutNodes();
    
    /**
     * Returns true/false indicating whether the given even will cause this 
     * schema node to generate a model delta.
     * @param event Event to process.
     * @return True if this node (or its children) would generate delta data
     * due to this event. 
     * @see IModelDelta
     */
    public boolean hasDeltaFlags(Object event);
    
    /**
     * Builds model delta information based on the given event.
     * @param event Event to process.
     * @param parent Parent model delta node that this object should add delta
     * data to.
     * @param done Return token, which notifies the caller that the calculation is
     * complete.
     */
    public void buildDelta(Object event, VMDelta parent, Done done);
    
    /**
     * Disposes the resources held by this node.
     */
    public void sessionDispose();
}