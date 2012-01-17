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

import org.eclipse.debug.internal.ui.viewers.model.provisional.IColumnPresentation;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IColumnPresentationFactory;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.ui.IDebugUIConstants;

public class RegistersViewColumnPresentationFactory implements IColumnPresentationFactory {

    /* (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IColumnPresentationFactory#createColumnPresentation(org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext, java.lang.Object)
     */
    @Override
	public IColumnPresentation createColumnPresentation( IPresentationContext context, Object element ) {
        if ( context.getId().equals( IDebugUIConstants.ID_REGISTER_VIEW ) )
            return new RegistersViewColumnPresentation();
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IColumnPresentationFactory#getColumnPresentationId(org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext, java.lang.Object)
     */
    @Override
	public String getColumnPresentationId( IPresentationContext context, Object element ) {
        if ( context.getId().equals( IDebugUIConstants.ID_REGISTER_VIEW ) )
            return RegistersViewColumnPresentation.ID;
        return null;
    }
}
