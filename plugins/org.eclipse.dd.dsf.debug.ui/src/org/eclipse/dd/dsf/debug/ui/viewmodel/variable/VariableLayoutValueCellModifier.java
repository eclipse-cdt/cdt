/*
 * VariableLayoutValueCellModifier.java
 * Created on May 22, 2007
 *
 * Copyright 2007 Wind River Systems Inc. All rights reserved.
*/
package org.eclipse.dd.dsf.debug.ui.viewmodel.variable;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.dd.dsf.debug.service.IExpressions.IExpressionDMContext;
import org.eclipse.dd.dsf.debug.ui.viewmodel.IDebugVMConstants;
import org.eclipse.dd.dsf.debug.ui.viewmodel.expression.WatchExpressionCellModifier;
import org.eclipse.dd.dsf.debug.ui.viewmodel.formatsupport.IFormattedValuePreferenceStore;

public class VariableLayoutValueCellModifier extends WatchExpressionCellModifier {
    
    private SyncVariableDataAccess fDataAccess = null;
    private IFormattedValuePreferenceStore fFormattedValuePreferenceStore;
    
    public VariableLayoutValueCellModifier(IFormattedValuePreferenceStore formattedValuePreferenceStore, SyncVariableDataAccess access) {
        fDataAccess = access;
        fFormattedValuePreferenceStore = formattedValuePreferenceStore;
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

    @Override
    public boolean canModify(Object element, String property) {
        // If we're in the column value, modify the register data.  Otherwise, call the super-class to edit
        // the watch expression.

        if (IDebugVMConstants.COLUMN_ID__VALUE.equals(property)) { 
            // Make sure we are are dealing with a valid set of information.
                
            if (getVariableDMC(element) == null)
                return false;
            
            return true ;
        }

        return super.canModify(element, property);
    }

    @Override
    public Object getValue(Object element, String property) {
        // If we're in the column value, modify the variable value.  Otherwise, call the super-class to edit
        // the watch expression.

        if (IDebugVMConstants.COLUMN_ID__VALUE.equals(property)) {
            // Make sure we are working on the editable areas.
            
            // Write the value in the currently requested format. Since they could
            // have freeformed typed in any format this is just a guess and may not
            // really accomplish anything.

            String value = fDataAccess.getFormattedValue(element, fFormattedValuePreferenceStore.getDefaultFormatId());
            
            if (value == null)
                return "...";  //$NON-NLS-1$

            return value;
        }

        return super.getValue(element, property);
    }

    @Override
    public void modify(Object element, String property, Object value) {
        // If we're in the column value, modify the register data.  Otherwise, call the super-class to edit
        // the watch expression.

        if (IDebugVMConstants.COLUMN_ID__VALUE.equals(property)) {
            if (value instanceof String) {
                 // PREFPAGE : We are using a default format until the preference page is created.
                fDataAccess.writeVariable(element, (String) value, fFormattedValuePreferenceStore.getDefaultFormatId());
            }
        }
        else {
            super.modify(element, property, value);
        }
    }

}
