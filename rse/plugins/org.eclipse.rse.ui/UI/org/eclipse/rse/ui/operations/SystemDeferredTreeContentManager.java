/********************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight.
 * 
 * Contributors:
 * Martin Oberhuber (Wind River) - [181145] restore selection after deferred query
 ********************************************************************************/
package org.eclipse.rse.ui.operations;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
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

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.progress.DeferredTreeContentManager#getAdapter(java.lang.Object)
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
     * When the job for fetching the children is finished and the placeholder
     * removed, the original selection is restored.
     * 
     * @param parent
     *            The parent object.
     * @param viewer
     *            The viewer
     * @return Object[] or <code>null</code> if parent is not an instance of
     *         IDeferredWorkbenchAdapter.
     */
    public Object[] getChildren(final Object parent, final Viewer viewer) {
    	final ISelection selection = viewer.getSelection();
        IDeferredWorkbenchAdapter element = getAdapter(parent);
        if (element == null) {
			return null;
		}
        PendingUpdateAdapter placeholder = new PendingUpdateAdapter() {
            protected void setRemoved(boolean removedValue) {
                super.setRemoved(removedValue);
                ISelection curSel = viewer.getSelection();
                //Only restore selection if the user has not changed it manually.
                //TODO in some cases we might need to send an EVENT_SELECT_REMOTE
                //listing the absolute paths of the original selection, in order
                //to properly find the items that should be selected.
                if (isSelectionContainedIn(curSel, selection)) {
                    viewer.setSelection(selection);
                }
            }
        };
       
        if (!_pendingQueries.contains(parent))
        {
        	startFetchingDeferredChildren(parent, element, placeholder);
        	_pendingQueries.add(parent);
        	return new Object[] { placeholder };
        }
        return null;
    }
    
    /**
     * Test whether a given selection is a subset of another (parent) selection.
     * @param sel Selection to check
     * @param parent Parent selection
     * @return <code>true</code> if the given selection is a subset.
     */
    private boolean isSelectionContainedIn(ISelection sel, ISelection parent) {
    	if (sel.isEmpty())
    		return true;
    	if (sel.equals(parent))
    		return false;
    	if ((sel instanceof IStructuredSelection) && (parent instanceof IStructuredSelection)) {
    		IStructuredSelection ssel = (IStructuredSelection)sel;
    		List spar = ((IStructuredSelection)parent).toList();
    		Iterator it = ssel.iterator();
    		while (it.hasNext()) {
    			Object o = it.next();
    			if (!spar.contains(o))
    				return false;
    		}
    		return true;
    	}
    	return false;
    }
    
    protected void addChildren(final Object parent, final Object[] children,
            IProgressMonitor monitor) 
    {
    	super.addChildren(parent, children, monitor);
    	_pendingQueries.remove(parent);
    }
}
