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
package org.eclipse.dd.dsf.ui.model;

import org.eclipse.dd.dsf.concurrent.Done;
import org.eclipse.dd.dsf.concurrent.GetDataDone;
import org.eclipse.dd.dsf.model.IDataModelEvent;
import org.eclipse.debug.internal.ui.viewers.provisional.ILabelRequestMonitor;
import org.eclipse.debug.internal.ui.viewers.provisional.IModelDelta;

/**
 * Schema nodes are combined together into a tree, to collectively define the 
 * layout of a view.  Each schema node generates elements of type 
 * IViewModelContext, and provide label information about these elements.
 * <p>
 * Besides the standard data model context (DMC) based implementation, this 
 * node could be implemented to present data from any source, not necessarily
 * DSF services.  It could also define a static node which operates on basis
 * of other data in the view tree.   
 * @see ViewModelProvider
 */
@SuppressWarnings("restriction")
public interface IViewModelSchemaNode {
    
    /**
     * Retrieves information whether for the given parent node, there are any elements
     * available.
     * @param parentVmc Parent node, for which to calculate elements at the
     * current level.
     * @param done The data return token.
     */
    public void hasElements(IViewModelContext parentVmc, GetDataDone<Boolean> done);
    
    /**
     * Retrieves the list of elements.
     * @param parentVmc Parent node, for which to calculate elements at the
     * current level.
     * @param done The data return token.
     */
    public void getElements(final IViewModelContext parentVmc, GetDataDone<IViewModelContext[]> done);
    
    /**
     * Retrieves the label for the given element. 
     * @param vmc Element for which to retrieve label information.
     * @param result Monitor which accepts the data.
     */
    public void retrieveLabel(IViewModelContext vmc, final ILabelRequestMonitor result);
    
    /**
     * Returns the list of child schema nodes which are configured for this node.
     */
    public IViewModelSchemaNode[] getChildNodes();
    
    /**
     * Returns true/false indicating whether the given even will cause this 
     * schema node to generate a model delta.
     * @param e Data model event to process.
     * @return True if this node (or its children) would generate delta data
     * due to this event. 
     * @see IModelDelta
     */
    public boolean hasDeltaFlags(IDataModelEvent e);
    
    /**
     * Builds model delta information based on the given event.
     * @param e Data model event to process.
     * @param parent Parent model delta node that this object should add delta
     * data to.
     * @param done Return token, which notifies the caller that the calculation is
     * complete.
     */
    public void buildDelta(IDataModelEvent e, ViewModelDelta parent, Done done);
    
    /**
     * Disposes the resources held by this node.
     */
    public void sessionDispose();
}