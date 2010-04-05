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

import org.eclipse.cdt.debug.core.model.ICDebugElement;
import org.eclipse.cdt.debug.core.model.ICDebugTarget;
import org.eclipse.cdt.debug.internal.ui.CRegisterManagerProxies;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.internal.ui.model.elements.ViewerInputProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerInputProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate;
import org.eclipse.debug.ui.IDebugUIConstants;

public class CViewerInputProvider extends ViewerInputProvider implements IViewerInputProvider {

    /* (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.model.elements.ViewerInputProvider#getViewerInput(java.lang.Object, org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext, org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate)
     */
    @Override
    protected Object getViewerInput( Object source, IPresentationContext context, IViewerUpdate update ) throws CoreException {
        if ( IDebugUIConstants.ID_REGISTER_VIEW.equals( context.getId() ) && source instanceof ICDebugElement ) {
            ICDebugTarget target = (ICDebugTarget)((ICDebugElement)source).getDebugTarget();
            return CRegisterManagerProxies.getInstance().getRegisterManagerProxy( target );
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.model.elements.ViewerInputProvider#supportsContextId(java.lang.String)
     */
    @Override
    protected boolean supportsContextId( String id ) {
        return IDebugUIConstants.ID_REGISTER_VIEW.equals( id );
    }    
}
