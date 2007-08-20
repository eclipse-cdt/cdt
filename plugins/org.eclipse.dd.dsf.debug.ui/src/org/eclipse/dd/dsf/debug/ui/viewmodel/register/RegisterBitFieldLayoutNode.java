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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.dd.dsf.concurrent.DataRequestMonitor;
import org.eclipse.dd.dsf.concurrent.RequestMonitor;
import org.eclipse.dd.dsf.datamodel.DMContexts;
import org.eclipse.dd.dsf.datamodel.IDMContext;
import org.eclipse.dd.dsf.datamodel.IDMEvent;
import org.eclipse.dd.dsf.datamodel.IDMService;
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
import org.eclipse.dd.dsf.debug.service.IRegisters.IRegisterGroupDMContext;
import org.eclipse.dd.dsf.debug.service.IRunControl.ISuspendedDMEvent;
import org.eclipse.dd.dsf.debug.ui.DsfDebugUIPlugin;
import org.eclipse.dd.dsf.debug.ui.viewmodel.IDebugVMConstants;
import org.eclipse.dd.dsf.debug.ui.viewmodel.expression.AbstractExpressionLayoutNode;
import org.eclipse.dd.dsf.debug.ui.viewmodel.formatsupport.IFormattedValuePreferenceStore;
import org.eclipse.dd.dsf.debug.ui.viewmodel.formatsupport.IFormattedValueVMContext;
import org.eclipse.dd.dsf.debug.ui.viewmodel.register.RegisterBitFieldLayoutCellModifier.BitFieldEditorStyle;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.dd.dsf.service.IDsfService;
import org.eclipse.dd.dsf.ui.viewmodel.AbstractVMProvider;
import org.eclipse.dd.dsf.ui.viewmodel.IVMContext;
import org.eclipse.dd.dsf.ui.viewmodel.VMDelta;
import org.eclipse.dd.dsf.ui.viewmodel.dm.AbstractDMVMLayoutNode;
import org.eclipse.dd.dsf.ui.viewmodel.update.VMCacheManager;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementEditor;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.ui.actions.IWatchExpressionFactoryAdapterExtension;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.swt.widgets.Composite;

@SuppressWarnings("restriction")
public class RegisterBitFieldLayoutNode extends AbstractExpressionLayoutNode<IBitFieldDMData> implements IElementEditor {

    protected class BitFieldVMC extends DMVMContext
        implements IVariable, IFormattedValueVMContext
    {
        private IExpression fExpression;
        public BitFieldVMC(IDMContext<?> dmc) {
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
            } else if (adapter.isAssignableFrom(IWatchExpressionFactoryAdapterExtension.class)) {
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
    
        public String getName() throws DebugException { return toString(); }
        public String getReferenceTypeName() throws DebugException { return ""; } //$NON-NLS-1$
        public IValue getValue() throws DebugException { return null; }
        public boolean hasValueChanged() throws DebugException { return false; }
        public void setValue(IValue value) throws DebugException {}
        public void setValue(String expression) throws DebugException {}
        public boolean supportsValueModification() { return false; }
        public boolean verifyValue(IValue value) throws DebugException { return false; }
        public boolean verifyValue(String expression) throws DebugException { return false; }
        public IDebugTarget getDebugTarget() { return null;}
        public ILaunch getLaunch() { return null; }
        public String getModelIdentifier() { return DsfDebugUIPlugin.PLUGIN_ID; }
    }

    protected class BitFieldExpressionFactory implements IWatchExpressionFactoryAdapterExtension {
        
        public boolean canCreateWatchExpression(IVariable variable) {
            return variable instanceof BitFieldVMC;
        }
        
        public String createWatchExpression(IVariable variable) throws CoreException {
            BitFieldVMC bitFieldVmc = ((BitFieldVMC)variable);

            StringBuffer exprBuf = new StringBuffer();
            IRegisterGroupDMContext groupDmc = 
                DMContexts.getAncestorOfType(bitFieldVmc.getDMC(), IRegisterGroupDMContext.class);
            if (groupDmc != null) {
                exprBuf.append("$$\""); //$NON-NLS-1$
                exprBuf.append(groupDmc.getName());
                exprBuf.append('"');
            }
            
            IRegisterDMContext registerDmc = 
                DMContexts.getAncestorOfType(bitFieldVmc.getDMC(), IRegisterDMContext.class);
            if (registerDmc != null) {
                exprBuf.append('$');
                exprBuf.append(registerDmc.getName());
            }

            IBitFieldDMContext bitFieldDmc = 
                DMContexts.getAncestorOfType(bitFieldVmc.getDMC(), IBitFieldDMContext.class);
            if (bitFieldDmc != null) {
                exprBuf.append('.');
                exprBuf.append(bitFieldDmc.getName());
            }

            return exprBuf.toString();
        }
    }
    
    private SyncRegisterDataAccess fDataAccess = null;
    final protected BitFieldExpressionFactory fBitFieldExpressionFactory = new BitFieldExpressionFactory(); 
    private final IFormattedValuePreferenceStore fFormattedPrefStore;
    
    public RegisterBitFieldLayoutNode(IFormattedValuePreferenceStore prefStore, AbstractVMProvider provider, DsfSession session, SyncRegisterDataAccess access) {
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
        
        regService.getAvailableFormattedValues(
            dmc,
            new DataRequestMonitor<String[]>(getSession().getExecutor(), null) {
                @Override
                public void handleCompleted() {
                    if (!getStatus().isOK()) {
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
                    FormattedValueDMContext valueDmc = regService.getFormattedValue(dmc, finalFormatId);
                    
                    VMCacheManager.getVMCacheManager().getCache(update.getPresentationContext())
        				.getModelData(regService,
        				valueDmc, 
                        new DataRequestMonitor<FormattedValueDMData>(getSession().getExecutor(), null) {
                            @Override
                            public void handleCompleted() {
                                if (!getStatus().isOK()) {
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
    
    /*
     *  We override the Abstract method because we now need to perform an extra level of data fetch
     *  to get the formatted value represenatation of the register. Before we obtained the data from
     *  the IDMData returned for the Register DMC. Now basically the level of information returned
     *  is attribute information and the formatted value requires a separate transaction.
     * 
     * @see org.eclipse.dd.dsf.ui.viewmodel.dm.AbstractDMVMLayoutNode#updateLabelInSessionThread(org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate[])
     */
    @Override
    protected void updateLabelInSessionThread(ILabelUpdate[] updates) {
        for (final ILabelUpdate update : updates) {
            
            final IBitFieldDMContext dmc = findDmcInPath(update.getElementPath(), IRegisters.IBitFieldDMContext.class);
            
            VMCacheManager.getVMCacheManager().getCache(update.getPresentationContext())
				.getModelData((IDMService)getServicesTracker().getService(null, dmc.getServiceFilter()),
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
                        if (!getStatus().isOK() || !getData().isValid()) {
                            assert getStatus().isOK() || 
                                   getStatus().getCode() != IDsfService.INTERNAL_ERROR || 
                                   getStatus().getCode() != IDsfService.NOT_SUPPORTED;
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
                        String[] localColumns = update.getPresentationContext().getColumns();
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
        final IRegisterDMContext regDmc = findDmcInPath(update.getElementPath(), IRegisterDMContext.class);

        if (regDmc == null) {
            handleFailedUpdate(update);
            return;
        }          
        
        getServicesTracker().getService(IRegisters.class).getBitFields(
            regDmc,
            new DataRequestMonitor<IBitFieldDMContext[]>(getSession().getExecutor(), null) {
                @Override
                protected void handleErrorOrCancel() {
                    handleFailedUpdate(update);
                }

                @Override
                protected void handleOK() {
                    fillUpdateWithVMCs(update, getData());
                    update.done();
                }
            });            
    }
    
    @Override
    protected IVMContext createVMContext(IDMContext<IBitFieldDMData> dmc) {
        return new BitFieldVMC(dmc);
    }
    
    @Override
    protected int getNodeDeltaFlagsForDMEvent(IDMEvent<?> e) {
        if (e instanceof IRunControl.ISuspendedDMEvent) {
            return IModelDelta.CONTENT;
        } else if (e instanceof IRegisters.IBitFieldChangedDMEvent) {
            return IModelDelta.STATE;
        }
        return IModelDelta.NO_CHANGE;
    }

    @Override
    protected void buildDeltaForDMEvent(IDMEvent<?> e, VMDelta parent, int nodeOffset, RequestMonitor rm) {
        if (e instanceof IRunControl.ISuspendedDMEvent) {
            // Create a delta that the whole register group has changed.
            parent.addFlags(IModelDelta.CONTENT);
        } 
        if (e instanceof IRegisters.IBitFieldChangedDMEvent) {
            /*
             * Flush the cache.
             */
            VMCacheManager.getVMCacheManager().flush(super.getVMProvider().getPresentationContext());
            
            /*
             *  Create a delta indicating the bit field has changed.
             */
            parent.addNode( createVMContext(((IRegisters.IBitFieldChangedDMEvent)e).getDMContext()), IModelDelta.STATE );
        } 
        
        super.buildDeltaForDMEvent(e, parent, nodeOffset, rm);
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
                return new RegisterBitFieldLayoutCellModifier( fFormattedPrefStore, BitFieldEditorStyle.BITFIELDCOMBO, fDataAccess );
            }
            else {
                /*
                 *  Text editor even if we need to clamp the value entered.
                 */
                return new RegisterBitFieldLayoutCellModifier( fFormattedPrefStore, BitFieldEditorStyle.BITFIELDTEXT, fDataAccess );
            }
        }
        else {
            return null;
        }
    }

    @Override
    protected void testContextForExpression(Object element, final String expression, final DataRequestMonitor<Boolean> rm) {
        if (!(element instanceof AbstractDMVMLayoutNode.DMVMContext)) {
            rm.setStatus(new Status(IStatus.ERROR, DsfDebugUIPlugin.PLUGIN_ID, IDsfService.INVALID_HANDLE, "Invalid context", null)); //$NON-NLS-1$
            rm.done();
            return;
        }
        
        final IBitFieldDMContext dmc = DMContexts.getAncestorOfType(((DMVMContext)element).getDMC(), IBitFieldDMContext.class);
        if (dmc == null) {
            rm.setStatus(new Status(IStatus.ERROR, DsfDebugUIPlugin.PLUGIN_ID, IDsfService.INVALID_HANDLE, "Invalid context", null)); //$NON-NLS-1$
            rm.done();
            return;
        }
        
        String bitFieldName = expression.substring(1);
        if (bitFieldName.equals(dmc.getName())) {
            rm.setData(Boolean.TRUE);
        } else {
            rm.setData(Boolean.FALSE);
        }
        rm.done();
    }

    public int getExpressionLength(String expression) {
        if (expression.charAt(0) == '.' && Character.isLetterOrDigit(expression.charAt(1))) {
            int length = 1;
            while( length < expression.length() && Character.isLetterOrDigit(expression.charAt(length)) ) {
                length++;
            }
            return length;
        } else {
            return -1;
        }
    }
    
    @Override
    protected void associateExpression(Object element, IExpression expression) {
        if (element instanceof BitFieldVMC) {
            ((BitFieldVMC)element).setExpression(expression);
        }
    }
    
    @Override
    protected int getDeltaFlagsForExpressionPart(Object event) {
        if (event instanceof IRunControl.ISuspendedDMEvent) {
            return IModelDelta.CONTENT;
        }

        return IModelDelta.NO_CHANGE;
    }
    
    @Override
    public void buildDeltaForExpression(final IExpression expression, final int elementIdx, final String expressionText, final Object event, final VMDelta parentDelta, final TreePath path, final RequestMonitor rm) 
    {
        if (event instanceof ISuspendedDMEvent) {
            // Mark the partent delta indicating that elements were added and/or removed.
            parentDelta.addFlags(IModelDelta.CONTENT);
        } else if (event instanceof IRegisters.IRegisterChangedDMEvent) {
            parentDelta.addFlags(IModelDelta.CONTENT);
        } 
        
        super.buildDeltaForExpression(expression, elementIdx, expressionText, event, parentDelta, path, rm);
    }

    @Override
    protected void buildDeltaForExpressionElement(Object element, int elementIdx, Object event, VMDelta parentDelta, final RequestMonitor rm) 
    {
        if (event instanceof IBitFieldChangedDMEvent) {
            parentDelta.addNode(element, IModelDelta.STATE);
        } 
        
        super.buildDeltaForExpressionElement(element, elementIdx, event, parentDelta, rm);
    }
}
