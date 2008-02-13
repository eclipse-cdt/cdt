/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.dd.examples.dsf.timers;

import org.eclipse.dd.examples.dsf.DsfExamplesPlugin;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IColumnPresentation;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * 
 */
@SuppressWarnings("restriction")
public class TimersViewColumnPresentation implements IColumnPresentation {

    public static final String ID = DsfExamplesPlugin.PLUGIN_ID + ".TIMER_COLUMN_PRESENTATION_ID"; //$NON-NLS-1$
    public static final String COL_ID = ID + ".COL_ID"; //$NON-NLS-1$
    public static final String COL_VALUE = ID + ".COL_VALUE"; //$NON-NLS-1$
    
    // @see org.eclipse.debug.internal.ui.viewers.provisional.IColumnPresentation#init(org.eclipse.debug.internal.ui.viewers.provisional.IPresentationContext)
    public void init(IPresentationContext context) {}

    // @see org.eclipse.debug.internal.ui.viewers.provisional.IColumnPresentation#dispose()
    public void dispose() {}

    // @see org.eclipse.debug.internal.ui.viewers.provisional.IColumnPresentation#getAvailableColumns()
    public String[] getAvailableColumns() {
        return new String[] { COL_ID, COL_VALUE };
    }

    // @see org.eclipse.debug.internal.ui.viewers.provisional.IColumnPresentation#getHeader(java.lang.String)
    public String getHeader(String id) {
        if (COL_ID.equals(id)) {
            return "ID"; //$NON-NLS-1$
        } else if (COL_VALUE.equals(id)) {
            return "Value"; //$NON-NLS-1$
        }
        return null;
    }

    // @see org.eclipse.debug.internal.ui.viewers.provisional.IColumnPresentation#getId()
    public String getId() {
        return ID;
    }

    // @see org.eclipse.debug.internal.ui.viewers.provisional.IColumnPresentation#getImageDescriptor(java.lang.String)
    public ImageDescriptor getImageDescriptor(String id) {
        return null;
    }

    // @see org.eclipse.debug.internal.ui.viewers.provisional.IColumnPresentation#getInitialColumns()
    public String[] getInitialColumns() {
        return getAvailableColumns();
    }

    // @see org.eclipse.debug.internal.ui.viewers.provisional.IColumnPresentation#isOptional()
    public boolean isOptional() {
        return true;
    }

}
