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


import org.eclipse.dd.dsf.datamodel.DMContexts;
import org.eclipse.dd.dsf.datamodel.IDMContext;
import org.eclipse.dd.dsf.debug.service.IRegisters.IRegisterDMContext;
import org.eclipse.dd.dsf.debug.service.IRegisters.IRegisterDMData;
import org.eclipse.dd.dsf.debug.ui.viewmodel.IDebugVMConstants;
import org.eclipse.dd.dsf.debug.ui.viewmodel.expression.WatchExpressionCellModifier;
import org.eclipse.dd.dsf.debug.ui.viewmodel.formatsupport.IFormattedValuePreferenceStore;
import org.eclipse.dd.dsf.ui.viewmodel.dm.AbstractDMVMLayoutNode;

public class RegisterLayoutValueCellModifier extends WatchExpressionCellModifier {
    
    private SyncRegisterDataAccess fDataAccess = null;
    private IFormattedValuePreferenceStore fFormattedValuePreferenceStore;
    
    public RegisterLayoutValueCellModifier(IFormattedValuePreferenceStore formattedValuePreferenceStore, SyncRegisterDataAccess access) {
        fDataAccess = access;
        fFormattedValuePreferenceStore = formattedValuePreferenceStore;
    }
    
    public SyncRegisterDataAccess getRegisterDataAccess() {
        return fDataAccess;
    }
    
    public IFormattedValuePreferenceStore getPreferenceStore() {
        return fFormattedValuePreferenceStore;
    }
    /*
     *  Used to make sure we are dealing with a valid register.
     */
    protected IRegisterDMContext getRegisterDMC(Object element) {
        if (element instanceof AbstractDMVMLayoutNode.DMVMContext) {
            IDMContext<?> dmc = ((AbstractDMVMLayoutNode.DMVMContext)element).getDMC();
            return DMContexts.getAncestorOfType(dmc, IRegisterDMContext.class);
        }
        return null;
    }
    
    @Override
    public boolean canModify(Object element, String property) {

        /*
         * If we're in the column value, modify the register data.
         * Otherwise, call the super-class to edit the watch expression.
         */
        if (IDebugVMConstants.COLUMN_ID__VALUE.equals(property)) { 
            /*
             *  Make sure we are are dealing with a valid set of information.
             */
            if ( getRegisterDMC(element) == null ) return false;
            
            /*
             *  We need to read the register in order to get the attributes.
             */
            
            IRegisterDMData regData = fDataAccess.readRegister(element);
            
            if ( ( regData != null ) && ( ! regData.isWriteable() ) ) return false;
            
            return true ;
        } else {
            return super.canModify(element, property);
        }
    }

    @Override
    public Object getValue(Object element, String property) {
        /*
         * If we're in the column value, modify the register data.
         * Otherwise, call the super-class to edit the watch expression.
         */
        if ( IDebugVMConstants.COLUMN_ID__VALUE.equals(property) ) {
            /*
             *  Make sure we are working on the editable areas.
             */
            String value = fDataAccess.getFormattedRegisterValue(element, fFormattedValuePreferenceStore.getDefaultFormatId());
            
            if ( value == null ) { return "..."; } //$NON-NLS-1$
            else                 { return value; }
        } else {
            return super.getValue(element, property);
        }
    }
    
    @Override
    public void modify(Object element, String property, Object value) {
        /*
         * If we're in the column value, modify the register data.
         * Otherwise, call the super-class to edit the watch expression.
         */

        if ( IDebugVMConstants.COLUMN_ID__VALUE.equals(property) ) {
            
            if (value instanceof String) {
                /*
                 *  PREFPAGE : We are using a default format until the preference page is created.
                 */
                fDataAccess.writeRegister(element, (String) value, fFormattedValuePreferenceStore.getDefaultFormatId());
            }
        } else {
            super.modify(element, property, value);
        }
    }
}
