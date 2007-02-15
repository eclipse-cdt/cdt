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

import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.rse.ui.view.IContextObject;

import org.eclipse.ui.progress.DeferredTreeContentManager;
import org.eclipse.ui.progress.IDeferredWorkbenchAdapter;

public class SystemDeferredTreeContentManager extends
		DeferredTreeContentManager {

	public SystemDeferredTreeContentManager(ITreeContentProvider provider,
			AbstractTreeViewer viewer) 
	{
		super(provider, viewer);
		// TODO Auto-generated constructor stub
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

}
