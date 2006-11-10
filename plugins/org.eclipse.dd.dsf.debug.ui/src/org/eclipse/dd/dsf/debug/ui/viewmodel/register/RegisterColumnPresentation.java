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
package org.eclipse.dd.dsf.debug.ui.viewmodel.register;

import org.eclipse.dd.dsf.debug.ui.DsfDebugUIPlugin;
import org.eclipse.debug.internal.ui.viewers.provisional.IColumnPresentation;
import org.eclipse.debug.internal.ui.viewers.provisional.IPresentationContext;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * 
 */
@SuppressWarnings("restriction")
public class RegisterColumnPresentation implements IColumnPresentation {

    public static final String ID = DsfDebugUIPlugin.PLUGIN_ID + ".REGISTERS_COLUMN_PRESENTATION_ID"; //$NON-NLS-1$
    public static final String COL_NAME = ID + ".COL_NAME"; //$NON-NLS-1$
    public static final String COL_VALUE = ID + ".COL_VALUE"; //$NON-NLS-1$
    public static final String COL_DESCRIPTION = ID + ".COL_DESCRIPTION"; //$NON-NLS-1$
    
    // @see org.eclipse.debug.internal.ui.viewers.provisional.IColumnPresentation#init(org.eclipse.debug.internal.ui.viewers.provisional.IPresentationContext)
    public void init(IPresentationContext context) {}

    // @see org.eclipse.debug.internal.ui.viewers.provisional.IColumnPresentation#dispose()
    public void dispose() {}

    // @see org.eclipse.debug.internal.ui.viewers.provisional.IColumnPresentation#getAvailableColumns()
    public String[] getAvailableColumns() {
        return new String[] { COL_NAME, COL_VALUE, COL_DESCRIPTION };
    }

    // @see org.eclipse.debug.internal.ui.viewers.provisional.IColumnPresentation#getHeader(java.lang.String)
    public String getHeader(String id) {
        if (COL_NAME.equals(id)) {
            return MessagesForRegisterVM.RegisterColumnPresentation_name; 
        } else if (COL_VALUE.equals(id)) {
            return MessagesForRegisterVM.RegisterColumnPresentation_value;
        } else if (COL_DESCRIPTION.equals(id)) {
            return MessagesForRegisterVM.RegisterColumnPresentation_description;
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
        return new String[] { COL_NAME, COL_VALUE };
    }

    // @see org.eclipse.debug.internal.ui.viewers.provisional.IColumnPresentation#isOptional()
    public boolean isOptional() {
        return true;
    }

}
