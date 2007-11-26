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
package org.eclipse.dd.dsf.ui.viewmodel.dm;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.dd.dsf.datamodel.IDMContext;
import org.eclipse.dd.dsf.ui.viewmodel.dm.AbstractDMVMLayoutNode.DMVMContext;
import org.eclipse.jface.viewers.TreePath;

/**
 * Object used to combine several DM Contexts found in a tree path of a viewer 
 * update.  This object allows the view model to pass complete data model context 
 * information found in the view to the services.
 */
public class CompositeDMContext implements IDMContext {
    
    /**
     * The input object to the view.  This object is not included in the tree 
     * path. 
     */
    private final Object fViewerInputObject;
    
    /**
     * The tree path for which the context is created.  
     */
    private final TreePath fTreePath;
    
    /**
     * The list of parent contexts derived from the input object and 
     * the path.  It is calculated on demand.
     */
    private IDMContext[] fParents;
    
    /** 
     * Main constructor provides all data needed to implement the IModelContext
     * interface.
     */
    public CompositeDMContext(Object viewerInputObject, TreePath treePath) {
        fViewerInputObject = viewerInputObject;
        fTreePath = treePath;
    }

    /**
     * Returns the session ID of the last DMVMContext element found in the tree 
     * path of this composite context.  May return an empty string if no DMVMContext 
     * is found in path.
     * <p>
     * Note: The session ID is primarily used by UI components to get access to the 
     * correct session and executor for the given context.  The composite context is
     * intended to be created by UI clients which already know the session ID so 
     * the fact that this method may not return a reliable result is acceptable.
     * </p>
     */
    public String getSessionId() { 
        return getElement().getDMC().getSessionId();
    }
    
    public IDMContext[] getParents() {
        if (fParents == null) {
            List<IDMContext> parentsList = new ArrayList<IDMContext>(fTreePath.getSegmentCount() + 1);
            for (int i = fTreePath.getSegmentCount() - 1; i >=0 ; i--) {
                if (fTreePath.getSegment(i) instanceof DMVMContext) {
                    parentsList.add( ((DMVMContext)fTreePath.getSegment(i)).getDMC() );
                }
            }
            if (fViewerInputObject instanceof DMVMContext) {
                parentsList.add( ((DMVMContext)fViewerInputObject).getDMC() );
            }
            
            fParents = parentsList.toArray(new IDMContext[parentsList.size()]);
        }                
        return fParents; 
    }
        
    /**
     * Returns the given adapter of the last DMVMContext element found in the tree 
     * path of this composite context.  Will return null if no DMVMContext is found
     * in path.
     * @see #getSessionId()
     */
    @SuppressWarnings("unchecked")
    public Object getAdapter(Class adapterType) {
        return getElement().getAdapter(adapterType);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof CompositeDMContext && 
               ((CompositeDMContext)obj).fTreePath.equals(fTreePath) &&
               ((CompositeDMContext)obj).fViewerInputObject.equals(fViewerInputObject);
    }

    @Override
    public int hashCode() {
        return fTreePath.hashCode() + fViewerInputObject.hashCode();
    }

    /**
     * Returns the principal element that this composite context is based on.  
     * It is used for calculating the session ID and the adapters of this 
     * context.  May return <code>null</code> if no DMVMContext is found in
     * path. 
     */
    private DMVMContext getElement() {
        for (int i = fTreePath.getSegmentCount() - 1; i >= 0; i--) {
            if (fTreePath.getSegment(i) instanceof DMVMContext) {
                return (DMVMContext)fTreePath.getSegment(i);
            }
        }
        if (fViewerInputObject instanceof DMVMContext) {
            return (DMVMContext)fViewerInputObject;
        }
        
        return null;
    }
}
