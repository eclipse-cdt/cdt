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
package org.eclipse.dd.dsf.debug.internal.provisional.ui.viewmodel.register;

import java.util.concurrent.RejectedExecutionException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.dd.dsf.concurrent.DataRequestMonitor;
import org.eclipse.dd.dsf.concurrent.DsfRunnable;
import org.eclipse.dd.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.dd.dsf.concurrent.ImmediateExecutor;
import org.eclipse.dd.dsf.concurrent.RequestMonitor;
import org.eclipse.dd.dsf.datamodel.DMContexts;
import org.eclipse.dd.dsf.datamodel.IDMContext;
import org.eclipse.dd.dsf.debug.internal.provisional.ui.viewmodel.IDebugVMConstants;
import org.eclipse.dd.dsf.debug.internal.provisional.ui.viewmodel.expression.AbstractExpressionVMNode;
import org.eclipse.dd.dsf.debug.internal.provisional.ui.viewmodel.numberformat.IFormattedValuePreferenceStore;
import org.eclipse.dd.dsf.debug.internal.provisional.ui.viewmodel.numberformat.IFormattedValueVMContext;
import org.eclipse.dd.dsf.debug.internal.provisional.ui.viewmodel.register.RegisterBitFieldCellModifier.BitFieldEditorStyle;
import org.eclipse.dd.dsf.debug.internal.ui.DsfDebugUIPlugin;
import org.eclipse.dd.dsf.debug.service.IFormattedValues;
import org.eclipse.dd.dsf.debug.service.IRegisters;
import org.eclipse.dd.dsf.debug.service.IRunControl;
import org.eclipse.dd.dsf.debug.service.IFormattedValues.FormattedValueDMContext;
import org.eclipse.dd.dsf.debug.service.IFormattedValues.FormattedValueDMData;
import org.eclipse.dd.dsf.debug.service.IRegisters.IBitFieldChangedDMEvent;
import org.eclipse.dd.dsf.debug.service.IRegisters.IBitFieldDMContext;
import org.eclipse.dd.dsf.debug.service.IRegisters.IBitFieldDMData;
import org.eclipse.dd.dsf.debug.service.IRegisters.IMnemonic;
import org.eclipse.dd.dsf.debug.service.IRegisters.IRegisterDMContext;
import org.eclipse.dd.dsf.debug.service.IRegisters.IRegisterDMData;
import org.eclipse.dd.dsf.debug.service.IRegisters.IRegisterGroupDMData;
import org.eclipse.dd.dsf.debug.service.IRunControl.ISuspendedDMEvent;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.dd.dsf.ui.viewmodel.IVMContext;
import org.eclipse.dd.dsf.ui.viewmodel.VMDelta;
import org.eclipse.dd.dsf.ui.viewmodel.datamodel.AbstractDMVMProvider;
import org.eclipse.dd.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementEditor;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementLabelProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.ui.actions.IWatchExpressionFactoryAdapter2;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.swt.widgets.Composite;

@SuppressWarnings("restriction")
public class RegisterBitFieldVMNode extends AbstractExpressionVMNode 
    implements IElementEditor, IElementLabelProvider 
{

    protected class BitFieldVMC extends DMVMContext
        implements IFormattedValueVMContext
    {
        private IExpression fExpression;
        public BitFieldVMC(IDMContext dmc) {
            super(dmc);
        }
        
        public IFormattedValuePreferenceStore getPreferenceStore() {
            return fFormattedPrefStore;
        }
        
        public void setExpression(IExpression expression) {
            fExpression = expression;
        }
        
        @Override
        @SuppressWarnings("unchecked") 
        public Object getAdapter(Class adapter) {
            if (fExpression != null && adapter.isAssignableFrom(fExpression.getClass())) {
                return fExpression;
            } else if (adapter.isAssignableFrom(IWatchExpressionFactoryAdapter2.class)) {
                return fBitFieldExpressionFactory;
            } else {
                return super.getAdapter(adapter);
            }
        }
    
        @Override
        public boolean equals(Object other) {
            if (other instanceof BitFieldVMC && super.equals(other)) {
                BitFieldVMC otherBitField = (BitFieldVMC)other;
                return (otherBitField.fExpression == null && fExpression == null) ||
                       (otherBitField.fExpression != null && otherBitField.fExpression.equals(fExpression));
            }
            return false;
        }
        
        @Override
        public int hashCode() {
            return super.hashCode() + (fExpression != null ? fExpression.hashCode() : 0);
        }
    }

    protected class BitFieldExpressionFactory implements IWatchExpressionFactoryAdapter2 {
        
        public boolean canCreateWatchExpression(Object element) {
            return element instanceof BitFieldVMC;
        }
        
        /**
         * Expected format: GRP( GroupName ).REG( RegisterName ).BFLD( BitFieldname )
         */
        public String createWatchExpression(Object element) throws CoreException {
            IRegisterGroupDMData groupData = fDataAccess.getRegisterGroupDMData(element);
            IRegisterDMData registerData = fDataAccess.getRegisterDMData(element);
            IBitFieldDMData bitFieldData = fDataAccess.getBitFieldDMData(element);

            if (groupData != null && registerData != null && bitFieldData != null) { 
                StringBuffer exprBuf = new StringBuffer();
                
                exprBuf.append("GRP( ");   exprBuf.append(groupData.getName());    exprBuf.append(" )"); //$NON-NLS-1$ //$NON-NLS-2$
                exprBuf.append(".REG( ");  exprBuf.append(registerData.getName()); exprBuf.append(" )"); //$NON-NLS-1$ //$NON-NLS-2$
                exprBuf.append(".BFLD( "); exprBuf.append(bitFieldData.getName()); exprBuf.append(" )"); //$NON-NLS-1$ //$NON-NLS-2$
                
                return exprBuf.toString();
            }

            return null;
        }
    }
    
    private SyncRegisterDataAccess fDataAccess = null;
    final protected BitFieldExpressionFactory fBitFieldExpressionFactory = new BitFieldExpressionFactory(); 
    private final IFormattedValuePreferenceStore fFormattedPrefStore;
    
    public RegisterBitFieldVMNode(IFormattedValuePreferenceStore prefStore, AbstractDMVMProvider provider, DsfSession session, SyncRegisterDataAccess access) {
        super(provider, session, IRegisters.IBitFieldDMContext.class);
        fDataAccess = access;
        fFormattedPrefStore = prefStore;
    }
    
    public IFormattedValuePreferenceStore getPreferenceStore() {
        return fFormattedPrefStore;
    }
    /**
     *  Private data access routine which performs the extra level of data access needed to
     *  get the formatted data value for a specific register.
     */
    private void updateFormattedRegisterValue(final ILabelUpdate update, final int labelIndex, final IBitFieldDMContext dmc, final IBitFieldDMData data)
    {
        final IRegisters regService = getServicesTracker().getService(IRegisters.class);
        
        /*
         *  First select the format to be used. This involves checking so see that the preference
         *  page format is supported by the register service. If the format is not supported then 
         *  we will pick the first available format.
         */
        final IPresentationContext context  = update.getPresentationContext();
        final String preferencePageFormatId = fFormattedPrefStore.getCurrentNumericFormat(context) ;
        
        regService.getAvailableFormats(
            dmc,
            new DataRequestMonitor<String[]>(getSession().getExecutor(), null) {
                @Override
                public void handleCompleted() {
                    if (!isSuccess()) {
                        handleFailedUpdate(update);
                        return;
                    }
                    
                    /*
                     *  See if the desired format is supported.
                     */
                    String[] formatIds = getData();
                    String   finalFormatId = IFormattedValues.HEX_FORMAT;
                    boolean  requestedFormatIsSupported = false;
                    
                    for ( String fId : formatIds ) {
                        if ( preferencePageFormatId.equals(fId) ) {
                            /*
                             *  Desired format is supported.
                             */
                            finalFormatId = preferencePageFormatId;
                            requestedFormatIsSupported = true;
                            break;
                        }
                    }
                    
                    if ( ! requestedFormatIsSupported ) {
                        /*
                         *  Desired format is not supported. If there are any formats supported
                         *  then use the first available.
                         */
                        if ( formatIds.length != 0 ) {
                            finalFormatId = formatIds[0];
                        }
                        else {
                            /*
                             *  Register service does not support any format.
                             */
                            handleFailedUpdate(update);
                            return;
                        }
                    }
                    
                    /*
                     *  Format has been validated. Get the formatted value.
                     */
                    FormattedValueDMContext valueDmc = regService.getFormattedValueContext(dmc, finalFormatId);
                    
                    getDMVMProvider().getModelData(
                        RegisterBitFieldVMNode.this, update, regService, valueDmc, 
                        new DataRequestMonitor<FormattedValueDMData>(getSession().getExecutor(), null) {
                            @Override
                            public void handleCompleted() {
                                if (!isSuccess()) {
                                    handleFailedUpdate(update);
                                    return;
                                }

                                /*
                                 *  Fill the label/column with the properly formatted data value.
                                 */
                                IMnemonic mnemonic = data.getCurrentMnemonicValue();
                                if ( mnemonic != null ) {
                                    String mnemstr = mnemonic.getLongName() + " - " + getData().getFormattedValue(); //$NON-NLS-1$
                                    update.setLabel(mnemstr , labelIndex);
                                }
                                else {
                                    update.setLabel(getData().getFormattedValue() , labelIndex);
                                }
                                update.done();
                            }
                        },
                        getExecutor()
                    );
                }
            }
        );
    }
    
    
    public void update(final ILabelUpdate[] updates) {
        try {
            getSession().getExecutor().execute(new DsfRunnable() {
                public void run() {
                    updateLabelInSessionThread(updates);
                }});
        } catch (RejectedExecutionException e) {
            for (ILabelUpdate update : updates) {
                handleFailedUpdate(update);
            }
        }
    }

    
    protected void updateLabelInSessionThread(ILabelUpdate[] updates) {
        for (final ILabelUpdate update : updates) {
            
            if (!checkService(IRegisters.class, null, update)) continue;
            
            final IBitFieldDMContext dmc = findDmcInPath(update.getViewerInput(), update.getElementPath(), IRegisters.IBitFieldDMContext.class);
            
            getDMVMProvider().getModelData(
                this, update, 
				getServicesTracker().getService(IRegisters.class),
                dmc, 
                new DataRequestMonitor<IBitFieldDMData>(getSession().getExecutor(), null) { 
                    @Override
                    protected void handleCompleted() {
                        /*
                         * Check that the request was evaluated and data is still
                         * valid.  The request could fail if the state of the 
                         * service changed during the request, but the view model
                         * has not been updated yet.
                         */ 
                        if (!isSuccess()) {
                            assert getStatus().isOK() || 
                                   getStatus().getCode() != IDsfStatusConstants.INTERNAL_ERROR || 
                                   getStatus().getCode() != IDsfStatusConstants.NOT_SUPPORTED;
                            handleFailedUpdate(update);
                            return;
                        }
                        
                        /*
                         * If columns are configured, extract the selected values for each
                         * understood column. First we fill all of those columns which can
                         * be filled without the extra data mining. We also note if we  do
                         * have to datamine. Any columns need to set the processing flag
                         * so we know we have further work to do. If there are more columns
                         * which need data extraction they need to be added in both "for"
                         * loops.
                         */
                        String[] localColumns = update.getColumnIds();
                        if (localColumns == null) localColumns = new String[] { IDebugVMConstants.COLUMN_ID__NAME };
                        
                        boolean weAreExtractingFormattedData = false;
                        
                        for (int idx = 0; idx < localColumns.length; idx++) {
                            if (IDebugVMConstants.COLUMN_ID__NAME.equals(localColumns[idx])) {
                                update.setLabel(getData().getName(), idx);
                            } else if (IDebugVMConstants.COLUMN_ID__VALUE.equals(localColumns[idx])) {
                                weAreExtractingFormattedData = true;
                            } else if (IDebugVMConstants.COLUMN_ID__TYPE.equals(localColumns[idx])) {
                                IBitFieldDMData data = getData();
                                String typeStr      = "Unsigned"; //$NON-NLS-1$
                                String ReadAttrStr  = "ReadNone"; //$NON-NLS-1$
                                String WriteAddrStr = "WriteNone"; //$NON-NLS-1$
                                
                                     if ( data.isReadOnce() ) { ReadAttrStr = "ReadOnce"; } //$NON-NLS-1$
                                else if ( data.isReadable() ) { ReadAttrStr = "Readable"; } //$NON-NLS-1$
                                
                                     if ( data.isReadOnce() ) { WriteAddrStr = "WriteOnce"; } //$NON-NLS-1$
                                else if ( data.isReadable() ) { WriteAddrStr = "Writeable"; } //$NON-NLS-1$
                                
                                typeStr += " - " + ReadAttrStr + "/" + WriteAddrStr; //$NON-NLS-1$ //$NON-NLS-2$
                                update.setLabel(typeStr, idx);
                            } else if (IDebugVMConstants.COLUMN_ID__DESCRIPTION.equals(localColumns[idx])) {
                                update.setLabel(getData().getDescription(), idx);
                            } else if (IDebugVMConstants.COLUMN_ID__EXPRESSION.equals(localColumns[idx])) {
                                IVMContext vmc = (IVMContext)update.getElement();
                                IExpression expression = (IExpression)vmc.getAdapter(IExpression.class);
                                if (expression != null) {
                                    update.setLabel(expression.getExpressionText(), idx);
                                } else {
                                    update.setLabel(getData().getName(), idx);
                                } 
                            }
                        }
                        
                        if ( ! weAreExtractingFormattedData ) {
                            update.done();
                        } else {
                            for (int idx = 0; idx < localColumns.length; idx++) {
                                if (IDebugVMConstants.COLUMN_ID__VALUE.equals(localColumns[idx])) {
                                    updateFormattedRegisterValue(update, idx, dmc, getData() );
                                }
                            }
                        }
                    }
                },
                getExecutor()
            );
        }
    }
    
    @Override
    protected void updateElementsInSessionThread(final IChildrenUpdate update) {
        final IRegisterDMContext regDmc = findDmcInPath(update.getViewerInput(), update.getElementPath(), IRegisterDMContext.class);

        if (regDmc == null) {
            handleFailedUpdate(update);
            return;
        }          
        
        if (!checkService(IRegisters.class, null, update)) return;
        
        getServicesTracker().getService(IRegisters.class).getBitFields(
            regDmc,
            new DataRequestMonitor<IBitFieldDMContext[]>(getSession().getExecutor(), null) {
                @Override
                protected void handleFailure() {
                    handleFailedUpdate(update);
                }

                @Override
                protected void handleSuccess() {
                    fillUpdateWithVMCs(update, getData());
                    update.done();
                }
            });            
    }
    
    @Override
    protected IDMVMContext createVMContext(IDMContext dmc) {
        return new BitFieldVMC(dmc);
    }
    
    public int getDeltaFlags(Object e) {
        if (e instanceof IRunControl.ISuspendedDMEvent) {
            return IModelDelta.CONTENT;
        }
        
        if (e instanceof IRegisters.IBitFieldChangedDMEvent) {
            return IModelDelta.STATE;
        }

        if (e instanceof PropertyChangeEvent && 
            ((PropertyChangeEvent)e).getProperty() == IDebugVMConstants.CURRENT_FORMAT_STORAGE) 
        {
            return IModelDelta.CONTENT;            
        }
        
        return IModelDelta.NO_CHANGE;
    }

    public void buildDelta(Object e, VMDelta parentDelta, int nodeOffset, RequestMonitor rm) {
        if (e instanceof IRunControl.ISuspendedDMEvent) {
            // Create a delta that the whole register group has changed.
            parentDelta.setFlags(parentDelta.getFlags() | IModelDelta.CONTENT);
        } 

        if (e instanceof IRegisters.IBitFieldChangedDMEvent) {
            
            /*
             *  Create a delta indicating the bit field has changed.
             */
            parentDelta.addNode( createVMContext(((IRegisters.IBitFieldChangedDMEvent)e).getDMContext()), IModelDelta.STATE );
        } 

        if (e instanceof PropertyChangeEvent && 
            ((PropertyChangeEvent)e).getProperty() == IDebugVMConstants.CURRENT_FORMAT_STORAGE) 
        {
            parentDelta.setFlags(parentDelta.getFlags() | IModelDelta.CONTENT);
        }
        
        rm.done();
    }

    public CellEditor getCellEditor(IPresentationContext context, String columnId, Object element, Composite parent) {
        
        if (IDebugVMConstants.COLUMN_ID__VALUE.equals(columnId)) {
            /*
             *   In order to decide what kind of editor to present we need to know if there are 
             *   mnemonics which can be used to represent the values. If there are then we will
             *   create a Combo editor for them. Otherwise we will just make a normal text cell
             *   editor.  If there are bit groups then the modifier will check the size of  the
             *   value being entered.
             */
            IBitFieldDMData bitFieldData = fDataAccess.readBitField(element);

            if ( bitFieldData != null && bitFieldData.isWriteable() ) {

                IMnemonic[] mnemonics = bitFieldData.getMnemonics();

                if ( mnemonics != null  && mnemonics.length != 0 ) {

                    /*
                     *   Create the list of readable dropdown selections.
                     */
                    String[] StringValues = new String[ mnemonics.length ];

                    int idx = 0 ;
                    for ( IMnemonic mnemonic : mnemonics ) {
                        StringValues[ idx ++ ] = mnemonic.getLongName();
                    }

                    /*
                     *  Not we are complex COMBO and return the right editor.
                     */
                    return new ComboBoxCellEditor(parent, StringValues); 
                }
                else {
                    /*
                     *  Text editor even if we need to clamp the value entered.
                     */
                    return new TextCellEditor(parent); 
                }
            }
        } else if (IDebugVMConstants.COLUMN_ID__EXPRESSION.equals(columnId)) {
            return new TextCellEditor(parent);            
        }
        return null;
    }

    public ICellModifier getCellModifier(IPresentationContext context, Object element) {
        
        /*
         *   In order to decide what kind of modifier to present we need to know if there
         *   are mnemonics which can be used to represent the values. 
         */
        IBitFieldDMData bitFieldData = fDataAccess.readBitField(element);

        if ( bitFieldData != null && bitFieldData.isWriteable() ) {

            IMnemonic[] mnemonics = bitFieldData.getMnemonics();

            if ( mnemonics != null  && mnemonics.length != 0 ) {
                /*
                 *  Note we are complex COMBO and return the right editor.
                 */
                return new RegisterBitFieldCellModifier(
                    getDMVMProvider(), fFormattedPrefStore, BitFieldEditorStyle.BITFIELDCOMBO, fDataAccess );
            }
            else {
                /*
                 *  Text editor even if we need to clamp the value entered.
                 */
                return new RegisterBitFieldCellModifier( 
                    getDMVMProvider(), fFormattedPrefStore, BitFieldEditorStyle.BITFIELDTEXT, fDataAccess );
            }
        }
        else {
            return null;
        }
    }

    /**
     * Expected format: GRP( GroupName ).REG( RegisterName ).BFLD( BitFieldname )
     */
    
    public boolean canParseExpression(IExpression expression) {
        return parseExpressionForBitFieldName(expression.getExpressionText()) != null;
    }
    
    private String parseExpressionForBitFieldName(String expression) {
    	
    	if (expression.startsWith("GRP(")) { //$NON-NLS-1$
    		
    		/*
    		 *  Get the group portion.
    		 */
    		int startIdx = "GRP(".length(); //$NON-NLS-1$
            int endIdx = expression.indexOf(')', startIdx);
            String remaining = expression.substring(endIdx+1);
            if ( ! remaining.startsWith(".REG(") ) { //$NON-NLS-1$
                return null;
            }
            
            /*
             * Get the register portion.
             */
            startIdx = ".REG(".length(); //$NON-NLS-1$
            endIdx = remaining.indexOf(')', startIdx);
            remaining = remaining.substring(endIdx+1);
            
            /*
             * Get the bit-field portion.
             */
            if ( ! remaining.startsWith(".BFLD(") ) { //$NON-NLS-1$
                return null;
            }
            startIdx = ".BFLD(".length(); //$NON-NLS-1$
            endIdx = remaining.indexOf(')', startIdx);
            String bitFieldName = remaining.substring(startIdx, endIdx);
            
            /*
             * Make sure there is nothing following. If there is then this
             * is not a properly formed expression and we do not claim it.
             */
            remaining = remaining.substring( endIdx + 1);
            
            if ( remaining.length() != 0 ) {
            	return null;
            }
            
            return bitFieldName.trim();
        }
    	
        return null;
    }
    
    @Override
    protected void testElementForExpression(Object element, IExpression expression, final DataRequestMonitor<Boolean> rm) {
        if (!(element instanceof IDMVMContext)) {
            rm.setStatus(new Status(IStatus.ERROR, DsfDebugUIPlugin.PLUGIN_ID, IDsfStatusConstants.INVALID_HANDLE, "Invalid context", null)); //$NON-NLS-1$
            rm.done();
            return;
        }
        
        final IBitFieldDMContext dmc = DMContexts.getAncestorOfType(((IDMVMContext)element).getDMContext(), IBitFieldDMContext.class);
        if (dmc == null) {
            rm.setStatus(new Status(IStatus.ERROR, DsfDebugUIPlugin.PLUGIN_ID, IDsfStatusConstants.INVALID_HANDLE, "Invalid context", null)); //$NON-NLS-1$
            rm.done();
            return;
        }
        
        final String bitFieldName = parseExpressionForBitFieldName(expression.getExpressionText());
        try {
            getSession().getExecutor().execute(new DsfRunnable() {
                public void run() {
                    IRegisters registersService = getServicesTracker().getService(IRegisters.class);
                    if (registersService != null) {
                        registersService.getBitFieldData(
                            dmc, 
                            new DataRequestMonitor<IBitFieldDMData>(ImmediateExecutor.getInstance(), rm) {
                                @Override
                                protected void handleSuccess() {
                                    rm.setData( getData().getName().equals(bitFieldName) );
                                    rm.done();
                                }
                            });
                    } else {
                        rm.setStatus(new Status(IStatus.WARNING, DsfDebugUIPlugin.PLUGIN_ID, IDsfStatusConstants.INVALID_STATE, "Register service not available", null)); //$NON-NLS-1$                        
                        rm.done();
                    }
                }
            });
        } catch (RejectedExecutionException e) {
            rm.setStatus(new Status(IStatus.WARNING, DsfDebugUIPlugin.PLUGIN_ID, IDsfStatusConstants.INVALID_STATE, "DSF session shut down", null)); //$NON-NLS-1$
            rm.done();
        }
    }

    @Override
    protected void associateExpression(Object element, IExpression expression) {
        if (element instanceof BitFieldVMC) {
            ((BitFieldVMC)element).setExpression(expression);
        }
    }
    
    public int getDeltaFlagsForExpression(IExpression expression, Object event) {
        if (event instanceof IRunControl.ISuspendedDMEvent) {
            return IModelDelta.CONTENT;
        }

        if (event instanceof PropertyChangeEvent && 
            ((PropertyChangeEvent)event).getProperty() == IDebugVMConstants.CURRENT_FORMAT_STORAGE) 
        {
            return IModelDelta.CONTENT;            
        }

        return IModelDelta.NO_CHANGE;
    }
    
    public void buildDeltaForExpression(final IExpression expression, final int elementIdx, final Object event, final VMDelta parentDelta, final TreePath path, final RequestMonitor rm) 
    {
        if (event instanceof ISuspendedDMEvent) {
            // Mark the parent delta indicating that elements were added and/or removed.
            parentDelta.setFlags(parentDelta.getFlags() | IModelDelta.CONTENT);
        } else if (event instanceof IRegisters.IRegisterChangedDMEvent) {
            parentDelta.setFlags(parentDelta.getFlags() | IModelDelta.CONTENT);
        } 
        rm.done();
    }

    public void buildDeltaForExpressionElement(Object element, int elementIdx, Object event, VMDelta parentDelta, final RequestMonitor rm) 
    {
        if (event instanceof IBitFieldChangedDMEvent) {
            parentDelta.addNode(element, IModelDelta.STATE);
        } 
        
        if (event instanceof PropertyChangeEvent && 
            ((PropertyChangeEvent)event).getProperty() == IDebugVMConstants.CURRENT_FORMAT_STORAGE) 
        {
            parentDelta.addNode(element, IModelDelta.CONTENT);
        }

        rm.done();
    }
}
