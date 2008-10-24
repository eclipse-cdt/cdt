/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.dd.dsf.debug.internal.provisional.ui.viewmodel.actions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.dd.dsf.ui.viewmodel.IVMAdapterExtension;
import org.eclipse.dd.dsf.ui.viewmodel.IVMProvider;
import org.eclipse.dd.dsf.ui.viewmodel.update.ICachingVMProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

/**
 * The default implementation of the refresh all debug target which 
 * calls the active VM providers, to ask them to refresh.
 * 
 * @since 1.1
 */
public class DefaultRefreshAllTarget implements IRefreshAllTarget {

    public void refresh(ISelection debugContext) throws CoreException {
        IVMAdapterExtension adapter = getActiveVMAdapter( debugContext );

        if (adapter != null) {
            for (IVMProvider provider : adapter.getActiveProviders()) {
                if (provider instanceof ICachingVMProvider) {
                    ((ICachingVMProvider)provider).refresh();
                }
            }
        }
    }

    protected IVMAdapterExtension getActiveVMAdapter(ISelection debugContext) {
        
        if (debugContext instanceof IStructuredSelection) {
            Object activeElement = ((IStructuredSelection)debugContext).getFirstElement();
            if (activeElement instanceof IAdaptable) {
                return (IVMAdapterExtension)((IAdaptable)activeElement).getAdapter(IVMAdapterExtension.class);
            }
        }
        return null;
    }
}
