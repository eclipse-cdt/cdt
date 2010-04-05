/*******************************************************************************
 * Copyright (c) 2010 CodeSourcery and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * CodeSourcery - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.internal.ui.elements.adapters;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.IRegisterGroup;
import org.eclipse.debug.internal.ui.model.elements.ElementContentProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate;
import org.eclipse.debug.ui.IDebugUIConstants;

public class CRegisterManagerContentProvider extends ElementContentProvider {

    /* (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.model.elements.ElementContentProvider#getChildren(java.lang.Object, int, int, org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext, org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate)
     */
    @Override
    protected Object[] getChildren( Object parent, int index, int length, IPresentationContext context, IViewerUpdate monitor ) throws CoreException {
        return getElements( getRegisterGroups( (CRegisterManagerProxy)parent ), index, length );
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.model.elements.ElementContentProvider#getChildCount(java.lang.Object, org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext, org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate)
     */
    @Override
    protected int getChildCount( Object element, IPresentationContext context, IViewerUpdate monitor ) throws CoreException {
        return getRegisterGroups( (CRegisterManagerProxy)element ).length;
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.model.elements.ElementContentProvider#supportsContextId(java.lang.String)
     */
    @Override
    protected boolean supportsContextId( String id ) {
        return IDebugUIConstants.ID_REGISTER_VIEW.equals( id );
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.model.elements.ElementContentProvider#hasChildren(java.lang.Object, org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext, org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate)
     */
    @Override
    protected boolean hasChildren( Object element, IPresentationContext context, IViewerUpdate monitor ) throws CoreException {
        return getRegisterGroups( (CRegisterManagerProxy)element ).length > 0;
    }
    
    private IRegisterGroup[] getRegisterGroups( CRegisterManagerProxy rmp ) {
        return rmp.getRegisterGroups();
    }
}
