/*******************************************************************************
 * Copyright (c) 2007, 2011 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Winnie Lai (Texas Instruments) - Individual Element Number Format in editing (Bug 343021)
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.ui.viewmodel.variable;

import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMContext;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.IDebugVMConstants;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.expression.WatchExpressionCellModifier;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMContext;
import org.eclipse.cdt.dsf.ui.viewmodel.update.AbstractCachingVMProvider;
import org.eclipse.cdt.dsf.ui.viewmodel.update.UserEditEvent;
import org.eclipse.core.runtime.IAdaptable;

public class VariableCellModifier extends WatchExpressionCellModifier {
    
    private AbstractCachingVMProvider fProvider;
    private SyncVariableDataAccess fDataAccess = null;
    protected String formatInEditing;
    
    public VariableCellModifier(AbstractCachingVMProvider provider, SyncVariableDataAccess access) 
    {
        fProvider = provider;
        fDataAccess = access;
    }
    
    /*
     *  Used to make sure we are dealing with a valid variable.
     */
    private IExpressionDMContext getVariableDMC(Object element) {
        if (element instanceof IAdaptable) {
            return (IExpressionDMContext)((IAdaptable)element).getAdapter(IExpressionDMContext.class);
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.dsf.debug.ui.viewmodel.expression.WatchExpressionCellModifier#canModify(java.lang.Object, java.lang.String)
     */
    @Override
    public boolean canModify(Object element, String property) {
        // If we're in the column value, modify the register data.  Otherwise, call the super-class to edit
        // the watch expression.

        if (IDebugVMConstants.COLUMN_ID__VALUE.equals(property)) { 
            // Make sure we are are dealing with a valid set of information.
                
            if (getVariableDMC(element) == null) {
                return false;
            }
            
           return fDataAccess.canWriteExpression(element);
        }

        return super.canModify(element, property);
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.dsf.debug.ui.viewmodel.expression.WatchExpressionCellModifier#getValue(java.lang.Object, java.lang.String)
     */
    @Override
    public Object getValue(Object element, String property) {
        // If we're in the column value, modify the variable value.  Otherwise, call the super-class to edit
        // the watch expression.

        if (IDebugVMConstants.COLUMN_ID__VALUE.equals(property)) {
            /*
             *  We let the Model provider supply the current format.
             */
            String formatId = null;
            
            if ( element instanceof IVMContext) {
            	formatId = queryFormat((IVMContext) element);
            }
            else {
                formatId = IFormattedValues.NATURAL_FORMAT;
            }
            formatInEditing = formatId;
            String value = fDataAccess.getEditableValue(element, formatId);
            
            if (value == null) {
                return "...";  //$NON-NLS-1$
            }

            return value;
        }

        return super.getValue(element, property);
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.dsf.debug.ui.viewmodel.expression.WatchExpressionCellModifier#modify(java.lang.Object, java.lang.String, java.lang.Object)
     */
    @Override
    public void modify(final Object element, String property, Object value) {
		/*
		 * If we're in the Value column, modify the variable/register data. The
		 * other columns in the Variables and Registers view are non-modifiable.
		 * If we're called for another column, pass the request to our super
		 * class; the column is likely a column it handles (Expression, for
		 * example).
		 */ 
        if (IDebugVMConstants.COLUMN_ID__VALUE.equals(property)) {
            if (value instanceof String) {
                /*
                 *  We let the Model provider supply the current format.
                 */
                String formatId = formatInEditing;
                
                if ( element instanceof IVMContext) {
                	if (formatId == null) {
                		formatId = queryFormat((IVMContext) element);
                	}
                }
                else {
                    formatId = IFormattedValues.NATURAL_FORMAT;
                }
                
                fDataAccess.writeVariable(element, (String) value, formatId);
                fProvider.getExecutor().execute(new DsfRunnable() {
                    @Override
					public void run() {
                        fProvider.handleEvent(new UserEditEvent(element));
                    }
                });
            }
        }
        else {
            super.modify(element, property, value);
        }
    }

}
