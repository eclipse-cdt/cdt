/*******************************************************************************
 * Copyright (c) 2007, 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.ui.viewmodel.datamodel;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.dsf.datamodel.CompositeDMContext;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate;
import org.eclipse.jface.viewers.TreePath;

/**
 * Object used to combine several DM Contexts found in a tree path of a viewer 
 * update.  This object allows the view model to pass complete data model context 
 * information found in the view to the services.  
 * 
 * @since 1.0
 */
public class CompositeDMVMContext extends CompositeDMContext {
    
    /**
     * Have to pass in an empty array of contexts to parent constructor
     * in order to be able to calculate the 
     */
    private static IDMContext[] EMPTY_CONTEXTS_ARRAY = new IDMContext[0];

    /**
     * The list of parent contexts derived from the input object and 
     * the path.  It is calculated on demand.
     */
    private IDMContext[] fParents;

    /**
     * Creates a composite context based in a viewer update.
     */
    public CompositeDMVMContext(IViewerUpdate update) {
        this(update.getViewerInput(), update.getElementPath());
    }

    /** 
     * Creates a composite context based on a viewer input and a tree path. 
     */
    public CompositeDMVMContext(Object viewerInputObject, TreePath treePath) {
        super(EMPTY_CONTEXTS_ARRAY);
        List<IDMContext> parentsList = new ArrayList<IDMContext>(treePath.getSegmentCount() + 1);
        for (int i = treePath.getSegmentCount() - 1; i >=0 ; i--) {
            if (treePath.getSegment(i) instanceof IDMVMContext) {
                parentsList.add( ((IDMVMContext)treePath.getSegment(i)).getDMContext() );
            }
        }
        if (viewerInputObject instanceof IDMVMContext) {
            parentsList.add( ((IDMVMContext)viewerInputObject).getDMContext() );
        }
        
        fParents = parentsList.toArray(new IDMContext[parentsList.size()]);
}

    @Override
    public IDMContext[] getParents() {
        return fParents;
    }
}
