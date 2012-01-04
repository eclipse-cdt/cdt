/*******************************************************************************
 * Copyright (c) 2006, 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.ui.viewmodel.expression;

import org.eclipse.cdt.dsf.debug.ui.viewmodel.IDebugVMConstants;
import org.eclipse.cdt.dsf.internal.ui.DsfUIPlugin;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IColumnPresentation;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * 
 */
public class ExpressionColumnPresentation implements IColumnPresentation {

    public static final String ID = DsfUIPlugin.PLUGIN_ID + ".EXPRESSION_COLUMN_PRESENTATION_ID"; //$NON-NLS-1$

    @Override
	public void init(IPresentationContext context) {
    }
    
    @Override
	public void dispose() {
    }

    // @see org.eclipse.debug.internal.ui.viewers.provisional.IColumnPresentation#getAvailableColumns()
    @Override
	public String[] getAvailableColumns() {
        return new String[] { IDebugVMConstants.COLUMN_ID__EXPRESSION, IDebugVMConstants.COLUMN_ID__NAME, IDebugVMConstants.COLUMN_ID__TYPE, IDebugVMConstants.COLUMN_ID__VALUE, IDebugVMConstants.COLUMN_ID__DESCRIPTION, IDebugVMConstants.COLUMN_ID__ADDRESS };
    }

    // @see org.eclipse.debug.internal.ui.viewers.provisional.IColumnPresentation#getHeader(java.lang.String)
    @Override
	public String getHeader(String id) {
        if (IDebugVMConstants.COLUMN_ID__EXPRESSION.equals(id)) {
            return MessagesForExpressionVM.ExpressionColumnPresentation_expression; 
        } else if (IDebugVMConstants.COLUMN_ID__NAME.equals(id)) {
            return MessagesForExpressionVM.ExpressionColumnPresentation_name; 
        } else if (IDebugVMConstants.COLUMN_ID__TYPE.equals(id)) {
            return MessagesForExpressionVM.ExpressionColumnPresentation_type;
        } else if (IDebugVMConstants.COLUMN_ID__VALUE.equals(id)) {
            return MessagesForExpressionVM.ExpressionColumnPresentation_value;
        } else if (IDebugVMConstants.COLUMN_ID__DESCRIPTION.equals(id)) {
            return MessagesForExpressionVM.ExpressionColumnPresentation_description;
        } else if (IDebugVMConstants.COLUMN_ID__ADDRESS.equals(id)) {
        	return MessagesForExpressionVM.ExpressionColumnPresentation_address;
        }
        return null;
    }

    // @see org.eclipse.debug.internal.ui.viewers.provisional.IColumnPresentation#getId()
    @Override
	public String getId() {
        return ID;
    }
    
    @Override
	public ImageDescriptor getImageDescriptor(String id) {
        return null;
    } 

    // @see org.eclipse.debug.internal.ui.viewers.provisional.IColumnPresentation#getInitialColumns()
    @Override
	public String[] getInitialColumns() {
        return new String[] { IDebugVMConstants.COLUMN_ID__EXPRESSION, IDebugVMConstants.COLUMN_ID__TYPE, IDebugVMConstants.COLUMN_ID__VALUE };
    }
    
    // @see org.eclipse.debug.internal.ui.viewers.provisional.IColumnPresentation#isOptional()
    @Override
	public boolean isOptional() {
        return true;
    }
}
