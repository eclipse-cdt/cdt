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
import org.eclipse.debug.internal.ui.model.elements.DebugElementMementoProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.ui.IDebugUIConstants;

public class CRegisterManagerProxyMementoProvider extends DebugElementMementoProvider {

    /* (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.model.elements.DebugElementMementoProvider#getElementName(java.lang.Object, org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext)
     */
    @Override
    protected String getElementName( Object element, IPresentationContext context ) throws CoreException {
        if ( element instanceof CRegisterManagerProxy ) {
            if ( IDebugUIConstants.ID_REGISTER_VIEW.equals( context.getId() ) ) {
                return ((CRegisterManagerProxy)element).getModelIdentifier();
            }
        }
        return null;
    }
}
