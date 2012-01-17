/*******************************************************************************
 * Copyright (c) 2010, 2011 CodeSourcery and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     CodeSourcery - Initial API and implementation
 *     Wind River Systems - flexible hierarchy Signals view (bug 338908)
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.elements.adapters;

import org.eclipse.cdt.debug.internal.ui.views.signals.SignalsViewColumnPresentation;
import org.eclipse.cdt.debug.ui.ICDebugUIConstants;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IColumnPresentation;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IColumnPresentationFactory;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;

public class DefaultViewColumnPresentationFactory implements IColumnPresentationFactory {

    @Override
	public IColumnPresentation createColumnPresentation( IPresentationContext context, Object element ) {
        if ( context.getId().equals( ICDebugUIConstants.ID_SIGNALS_VIEW ) )
            return new SignalsViewColumnPresentation();
        return null;
    }

    @Override
	public String getColumnPresentationId( IPresentationContext context, Object element ) {
        if ( context.getId().equals( ICDebugUIConstants.ID_SIGNALS_VIEW ) )
            return SignalsViewColumnPresentation.ID;
        return null;
    }
}
