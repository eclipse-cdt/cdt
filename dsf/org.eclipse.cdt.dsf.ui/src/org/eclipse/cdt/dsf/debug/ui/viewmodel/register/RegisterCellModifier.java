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
package org.eclipse.cdt.dsf.debug.ui.viewmodel.register;

import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues;
import org.eclipse.cdt.dsf.debug.service.IRegisters.IRegisterDMContext;
import org.eclipse.cdt.dsf.debug.service.IRegisters.IRegisterDMData;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.IDebugVMConstants;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.expression.WatchExpressionCellModifier;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.numberformat.FormattedValueVMUtil;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMContext;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.cdt.dsf.ui.viewmodel.update.AbstractCachingVMProvider;
import org.eclipse.cdt.dsf.ui.viewmodel.update.UserEditEvent;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;

public class RegisterCellModifier extends WatchExpressionCellModifier {
    
    private AbstractCachingVMProvider fProvider; 
    private SyncRegisterDataAccess fDataAccess = null;
    
    public RegisterCellModifier(AbstractCachingVMProvider provider, SyncRegisterDataAccess access) 
    {
        fProvider = provider;
        fDataAccess = access;
    }
    
    public SyncRegisterDataAccess getRegisterDataAccess() {
        return fDataAccess;
    }
    
    /*
     *  Used to make sure we are dealing with a valid register.
     */
    protected IRegisterDMContext getRegisterDMC(Object element) {
        if (element instanceof IDMVMContext) {
            IDMContext dmc = ((IDMVMContext)element).getDMContext();
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
             *  We let the Model provider supply the current format.
             */
            String formatId;
            
            if ( element instanceof IVMContext) {
                /*
                 *  Find the presentation context and then use it to get the current desired format.
                 */
                IVMContext ctx = (IVMContext) element;
                IPresentationContext presCtx = ctx.getVMNode().getVMProvider().getPresentationContext();
                formatId = FormattedValueVMUtil.getPreferredFormat(presCtx);
            }
            else {
                formatId = IFormattedValues.NATURAL_FORMAT;
            }
            
            String value = 
                
                fDataAccess.getFormattedRegisterValue(element, formatId);
            
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
                 *  We let the Model provider supply the current format.
                 */
                String formatId;
                
                if ( element instanceof IVMContext) {
                    /*
                     *  Find the presentation context and then use it to get the current desired format.
                     */
                    IVMContext ctx = (IVMContext) element;
                    IPresentationContext presCtx = ctx.getVMNode().getVMProvider().getPresentationContext();
                    formatId = FormattedValueVMUtil.getPreferredFormat(presCtx);
                }
                else {
                    formatId = IFormattedValues.NATURAL_FORMAT;
                }
                
                fDataAccess.writeRegister(element, (String) value, formatId);
                fProvider.handleEvent(new UserEditEvent(element));
            }
        } else {
            super.modify(element, property, value);
        }
    }
}
