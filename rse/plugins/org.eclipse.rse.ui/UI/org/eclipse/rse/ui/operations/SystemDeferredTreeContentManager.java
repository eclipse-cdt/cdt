/********************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 ********************************************************************************/
package org.eclipse.rse.ui.operations;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.rse.ui.view.IContextObject;
import org.eclipse.ui.progress.DeferredTreeContentManager;
import org.eclipse.ui.progress.IDeferredWorkbenchAdapter;
import org.eclipse.ui.progress.PendingUpdateAdapter;

public class SystemDeferredTreeContentManager extends
		DeferredTreeContentManager {

	private List _pendingQueries;
	
	public SystemDeferredTreeContentManager(ITreeContentProvider provider,
			AbstractTreeViewer viewer) 
	{
		super(provider, viewer);
		_pendingQueries = new ArrayList();
	}

    /**
     * Return the IDeferredWorkbenchAdapter for element or the element if it is
     * an instance of IDeferredWorkbenchAdapter. If it does not exist return
     * null.
     * 
     * @param element
     * @return IDeferredWorkbenchAdapter or <code>null</code>
     */
    protected IDeferredWorkbenchAdapter getAdapter(Object element) 
    {
    	
    	if (element instanceof IContextObject)
    	{
    		element = ((IContextObject)element).getModelObject();
    	}
        return super.getAdapter(element);
    }

    /**
     * Returns the child elements of the given element, or in the case of a
     * deferred element, returns a placeholder. If a deferred element is used, a
     * job is created to fetch the children in the background.
     * 
     * @param parent
     *            The parent object.
     * @return Object[] or <code>null</code> if parent is not an instance of
     *         IDeferredWorkbenchAdapter.
     */
    public Object[] getChildren(final Object parent) {
        IDeferredWorkbenchAdapter element = getAdapter(parent);
        if (element == null) {
			return null;
		}
        PendingUpdateAdapter placeholder = createPendingUpdateAdapter();
       
        if (!_pendingQueries.contains(parent))
        {
        	startFetchingDeferredChildren(parent, element, placeholder);
        	_pendingQueries.add(parent);
        	return new Object[] { placeholder };
        }
        return null;
    }
    
    protected void addChildren(final Object parent, final Object[] children,
            IProgressMonitor monitor) 
    {
    	super.addChildren(parent, children, monitor);
    	_pendingQueries.remove(parent);
    }
}
