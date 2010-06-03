/*******************************************************************************
 * Copyright (c) 2008, 2009 Wind River Systems and others.
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
import org.eclipse.cdt.dsf.debug.service.IRegisters.IBitFieldDMContext;
import org.eclipse.cdt.dsf.debug.service.IRegisters.IBitFieldDMData;
import org.eclipse.cdt.dsf.debug.service.IRegisters.IMnemonic;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.IDebugVMConstants;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.expression.WatchExpressionCellModifier;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.numberformat.FormattedValueVMUtil;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMContext;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.cdt.dsf.ui.viewmodel.update.AbstractCachingVMProvider;
import org.eclipse.cdt.dsf.ui.viewmodel.update.UserEditEvent;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;

public class RegisterBitFieldCellModifier extends WatchExpressionCellModifier {
    
    public static enum BitFieldEditorStyle { NOTHING, BITFIELDCOMBO, BITFIELDTEXT }
    
    private AbstractCachingVMProvider fProvider;
    private BitFieldEditorStyle fStyle;
    private IBitFieldDMData fBitFieldData = null;
    private Object fElement = null;
    private SyncRegisterDataAccess fDataAccess = null;
    
    /**
     * @since 2.0
     */
    public RegisterBitFieldCellModifier(AbstractCachingVMProvider provider, 
        BitFieldEditorStyle style, SyncRegisterDataAccess access ) 
    {
        fProvider = provider;
        fStyle = style;
        fDataAccess = access;
    }

    /*
     *  Used to make sure we are dealing with a valid register.
     */
    private IBitFieldDMContext getBitFieldDMC(Object element) {
        if (element instanceof IDMVMContext) {
            IDMContext dmc = ((IDMVMContext)element).getDMContext();
            return DMContexts.getAncestorOfType(dmc, IBitFieldDMContext.class);
        }
        return null;
    }
    
    @Override
    public boolean canModify(Object element, String property) {
        
        /*
         * If we're in the column value, modify the register data.
         * Otherwise, call the super-class to edit the watch expression.
         */
        if ( IDebugVMConstants.COLUMN_ID__VALUE.equals(property) ) {
            /*
             *  Make sure we are are dealing with a valid set of information.
             */
            if ( getBitFieldDMC(element) == null ) return false;
    
            fElement = element;
            
            /*
             *  We need to read the register in order to get the attributes.
             */
            fBitFieldData = fDataAccess.readBitField(element);
            
            if ( ( fBitFieldData != null ) && ( ! fBitFieldData.isWriteable() ) ) return false;
            
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
            if ( element != fElement ) return false; 
    
            if ( fStyle == BitFieldEditorStyle.BITFIELDTEXT ) {
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
                
                String value = fDataAccess.getFormattedBitFieldValue(fElement, formatId);
                
                if ( value == null ) { value = "..."; } //$NON-NLS-1$
                
                return value;
            }
            else {
                /*
                 *  This is a COMBO BOX. So we need to take the value of the bitfield and
                 *  compare it to the associated mnemonic values to see which mnemonic is
                 *  representing the current value. At this point the Bitfield Model data
                 *  has already been established since the "canModify()" method is called
                 *  first by the flexible hierarchy proxies.
                 */
                IMnemonic curMnemonic = fBitFieldData.getCurrentMnemonicValue();
                
                int index = 0 ;
                for ( IMnemonic mnemonic : fBitFieldData.getMnemonics() ) {
                    if ( mnemonic.equals( curMnemonic ) ) {
                        return new Integer( index );
                    }
                    index ++;
                }
                
                return null;
            }
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
            if ( fStyle == BitFieldEditorStyle.BITFIELDTEXT ) {
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
                    fDataAccess.writeBitField(element, (String) value, formatId);
                    fProvider.handleEvent(new UserEditEvent(element));
                }
            }
            else {
                if (value instanceof Integer) {
                    /*
                     *  Get the integer value corresponding to the selected entry.
                     */
                    Integer val = (Integer) value;
                    
                    /*
                     *  Write the bit field using the selected mnemonic.
                     */
                    fDataAccess.writeBitField(element, fBitFieldData.getMnemonics()[val.intValue()]);
                    fProvider.handleEvent(new UserEditEvent(element));
                }
            }
        } else {
            super.modify(element, property, value);
        }
    }
}

