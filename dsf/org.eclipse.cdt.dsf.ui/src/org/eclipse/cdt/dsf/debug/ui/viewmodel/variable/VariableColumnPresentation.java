/*******************************************************************************
 * Copyright (c) 2006, 2010 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.ui.viewmodel.variable;

import org.eclipse.cdt.dsf.debug.ui.viewmodel.IDebugVMConstants;
import org.eclipse.cdt.dsf.internal.ui.DsfUIPlugin;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IColumnPresentation;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * 
 */
public class VariableColumnPresentation implements IColumnPresentation {
    public static final String ID = DsfUIPlugin.PLUGIN_ID + ".VARIABLES_COLUMN_PRESENTATION_ID"; //$NON-NLS-1$
    
    // @see org.eclipse.debug.internal.ui.viewers.provisional.IColumnPresentation#init(org.eclipse.debug.internal.ui.viewers.provisional.IPresentationContext)
    @Override
	public void init(IPresentationContext context) {}

    // @see org.eclipse.debug.internal.ui.viewers.provisional.IColumnPresentation#dispose()
    @Override
	public void dispose() {}

    // @see org.eclipse.debug.internal.ui.viewers.provisional.IColumnPresentation#getAvailableColumns()
    @Override
	public String[] getAvailableColumns() {
        return new String[] { IDebugVMConstants.COLUMN_ID__NAME, IDebugVMConstants.COLUMN_ID__TYPE, IDebugVMConstants.COLUMN_ID__VALUE, IDebugVMConstants.COLUMN_ID__ADDRESS };
    }

    // @see org.eclipse.debug.internal.ui.viewers.provisional.IColumnPresentation#getHeader(java.lang.String)
    @Override
	public String getHeader(String id) {
        if (IDebugVMConstants.COLUMN_ID__NAME.equals(id)) {
            return MessagesForVariablesVM.VariableColumnPresentation_name; 
        } else if (IDebugVMConstants.COLUMN_ID__TYPE.equals(id)) {
            return MessagesForVariablesVM.VariableColumnPresentation_type;
        } else if (IDebugVMConstants.COLUMN_ID__VALUE.equals(id)) {
            return MessagesForVariablesVM.VariableColumnPresentation_value;
        } else if (IDebugVMConstants.COLUMN_ID__ADDRESS.equals(id)) {
        	return MessagesForVariablesVM.VariableColumnPresentation_location;
        } 
        return null;
    }

    // @see org.eclipse.debug.internal.ui.viewers.provisional.IColumnPresentation#getId()
    @Override
	public String getId() {
        return ID;
    }

    // @see org.eclipse.debug.internal.ui.viewers.provisional.IColumnPresentation#getImageDescriptor(java.lang.String)
    @Override
	public ImageDescriptor getImageDescriptor(String id) {
        return null;
    }

    // @see org.eclipse.debug.internal.ui.viewers.provisional.IColumnPresentation#getInitialColumns()
    @Override
	public String[] getInitialColumns() {
        return new String[] { IDebugVMConstants.COLUMN_ID__NAME, IDebugVMConstants.COLUMN_ID__TYPE, IDebugVMConstants.COLUMN_ID__VALUE };
    }

    // @see org.eclipse.debug.internal.ui.viewers.provisional.IColumnPresentation#isOptional()
    @Override
	public boolean isOptional() {
        return true;
    }

}
