/*******************************************************************************
 * Copyright (c) 2006, 2008 Wind River Systems and others.
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
import org.eclipse.dd.dsf.debug.service.IFormattedValues.FormattedValueDMContext;
import org.eclipse.dd.dsf.debug.service.IFormattedValues.FormattedValueDMData;
import org.eclipse.dd.dsf.debug.service.IMemory.IMemoryChangedEvent;
import org.eclipse.dd.dsf.debug.service.IRegisters.IBitFieldChangedDMEvent;
import org.eclipse.dd.dsf.debug.service.IRegisters.IBitFieldDMContext;
import org.eclipse.dd.dsf.debug.service.IRegisters.IBitFieldDMData;
import org.eclipse.dd.dsf.debug.service.IRegisters.IMnemonic;
import org.eclipse.dd.dsf.debug.service.IRegisters.IRegisterChangedDMEvent;
import org.eclipse.dd.dsf.debug.service.IRegisters.IRegisterDMContext;
import org.eclipse.dd.dsf.debug.service.IRegisters.IRegisterDMData;
import org.eclipse.dd.dsf.debug.service.IRegisters.IRegisterGroupDMData;
import org.eclipse.dd.dsf.debug.service.IRunControl.ISuspendedDMEvent;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.dd.dsf.ui.concurrent.ViewerDataRequestMonitor;
import org.eclipse.dd.dsf.ui.viewmodel.IVMContext;
import org.eclipse.dd.dsf.ui.viewmodel.VMDelta;
import org.eclipse.dd.dsf.ui.viewmodel.datamodel.AbstractDMVMProvider;
import org.eclipse.dd.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementCompareRequest;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementEditor;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementLabelProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementMementoProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementMementoRequest;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.actions.IWatchExpressionFactoryAdapter2;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.swt.widgets.Composite;

@SuppressWarnings("restriction")
public class RegisterBitFieldVMNode extends AbstractExpressionVMNode 
    implements IElementEditor, IElementLabelProvider, IElementMementoProvider
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
                return getWatchExpressionFactory();
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
            IRegisterGroupDMData groupData = getSyncRegisterDataAccess().getRegisterGroupDMData(element);
            IRegisterDMData registerData   = getSyncRegisterDataAccess().getRegisterDMData(element);
            IBitFieldDMData bitFieldData   = getSyncRegisterDataAccess().getBitFieldDMData(element);

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
    
    private SyncRegisterDataAccess fSyncRegisterDataAccess = null;
    protected IWatchExpressionFactoryAdapter2 fBitFieldExpressionFactory = null; 
    private final IFormattedValuePreferenceStore fFormattedPrefStore;
    
    public RegisterBitFieldVMNode(IFormattedValuePreferenceStore prefStore, AbstractDMVMProvider provider, DsfSession session, SyncRegisterDataAccess access) {
        super(provider, session, IBitFieldDMContext.class);
        fSyncRegisterDataAccess = access;
        fFormattedPrefStore = prefStore;
    }
    
    
    @Override
    public String toString() {
        return "RegisterBitFieldVMNode(" + getSession().getId() + ")";  //$NON-NLS-1$ //$NON-NLS-2$
    }

    public IFormattedValuePreferenceStore getPreferenceStore() {
        return fFormattedPrefStore;
    }
    
    /**
     * @since 1.1
     */
    public SyncRegisterDataAccess getSyncRegisterDataAccess() {
        return fSyncRegisterDataAccess;
    }

    /**
     * @since 1.1
     */
    public IWatchExpressionFactoryAdapter2 getWatchExpressionFactory() {
    	if ( fBitFieldExpressionFactory == null ) {
    		fBitFieldExpressionFactory = new BitFieldExpressionFactory();
    	}
    	return fBitFieldExpressionFactory;
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
            new ViewerDataRequestMonitor<String[]>(getSession().getExecutor(), update) {
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
                    final FormattedValueDMContext valueDmc = regService.getFormattedValueContext(dmc, finalFormatId);
                    
                    getDMVMProvider().getModelData(
                        RegisterBitFieldVMNode.this, update, regService, valueDmc, 
                        new ViewerDataRequestMonitor<FormattedValueDMData>(getSession().getExecutor(), update) {
                            @Override
                            public void handleCompleted() {
                                if (!isSuccess()) {
                                	if (getStatus().getCode() == IDsfStatusConstants.INVALID_STATE) {
	                                    update.setLabel("...", labelIndex); //$NON-NLS-1$
	                                } else {
	                                    update.setLabel("Error: " + getStatus().getMessage(), labelIndex); //$NON-NLS-1$
	                                }
	                                update.setFontData(JFaceResources.getFontDescriptor(IInternalDebugUIConstants.VARIABLE_TEXT_FONT).getFontData()[0], labelIndex);
	                                update.done();
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
                                
                                update.setFontData(JFaceResources.getFontDescriptor(IInternalDebugUIConstants.VARIABLE_TEXT_FONT).getFontData()[0], labelIndex);
                                
                                // color based on change history
	                            
	                            FormattedValueDMData oldData = (FormattedValueDMData) getDMVMProvider().getArchivedModelData(
	                                RegisterBitFieldVMNode.this, update, valueDmc);
	                            if(oldData != null && !oldData.getFormattedValue().equals(getData().getFormattedValue())) {
	                                update.setBackground(
	                                    DebugUIPlugin.getPreferenceColor(IInternalDebugUIConstants.PREF_CHANGED_VALUE_BACKGROUND).getRGB(), labelIndex);
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
     * (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IElementLabelProvider#update(org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate[])
     */
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

    /*
     * Updates the requested label based on the specified column.
     */
    protected void updateLabelInSessionThread(ILabelUpdate[] updates) {
        for (final ILabelUpdate update : updates) {
            
        	final IRegisters regService = getServicesTracker().getService(IRegisters.class);
            
            if ( regService == null ) {
            	handleFailedUpdate(update);
                continue;
            }
            
            final IBitFieldDMContext dmc = findDmcInPath(update.getViewerInput(), update.getElementPath(), IBitFieldDMContext.class);
            
            getDMVMProvider().getModelData(
                this, 
                update, 
				regService,
                dmc, 
                new ViewerDataRequestMonitor<IBitFieldDMData>(getSession().getExecutor(), update) { 
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
                            /*
                             *  Instead of just failing this outright we are going to attempt to do more here.
                             *  Failing it outright causes the view to display ... for all columns in the line
                             *  and this is uninformative about what is happening. We may be trying to show  a
                             *  register whos retrieval has been cancelled by the lower level. Perhaps because
                             *  we are stepping extremely fast and state changes cause the register service to
                             *  return these requests without ever sending them to the debug engine.
                             *  
                             */
                            String[] localColumns = update.getColumnIds();
                            if (localColumns == null)
                                localColumns = new String[] { IDebugVMConstants.COLUMN_ID__NAME };
                            
                            for (int idx = 0; idx < localColumns.length; idx++) {
                                if (IDebugVMConstants.COLUMN_ID__NAME.equals(localColumns[idx])) {
                                	/*
                                	 *  This used to be easy in that the DMC contained the name.  Which allowed us
                                	 *  to display the register name and an error message across from it. Now that
                                	 *  name must come from the data and we could not retrieve the data we do  not
                                	 *  have anything intelligent to show here. I think this is going to look very
                                	 *  ugly and will need to be worked on. We know the service has the name  with
                                	 *  it, it is just the dynamic part which cannot be obtained ( as explained in
                                	 *  comments above ). 
                                	 */
                                    update.setLabel("Unknown name", idx); //$NON-NLS-1$
                                    update.setImageDescriptor(DebugPluginImages.getImageDescriptor(IDebugUIConstants.IMG_OBJS_REGISTER), idx);
                                } else if (IDebugVMConstants.COLUMN_ID__TYPE.equals(localColumns[idx])) {
                                    update.setLabel("", idx); //$NON-NLS-1$
                                } else if (IDebugVMConstants.COLUMN_ID__VALUE.equals(localColumns[idx])) {
                                    if (getStatus().getCode() == IDsfStatusConstants.INVALID_STATE) {
                                        update.setLabel("...", idx); //$NON-NLS-1$
                                    } else {
                                        update.setLabel("Error: " + getStatus().getMessage(), idx); //$NON-NLS-1$
                                    }
                                } else if (IDebugVMConstants.COLUMN_ID__DESCRIPTION.equals(localColumns[idx])) {
                                    update.setLabel("...", idx); //$NON-NLS-1$
                                } else if (IDebugVMConstants.COLUMN_ID__EXPRESSION.equals(localColumns[idx])) {
                                    update.setLabel("", idx); //$NON-NLS-1$
                                }
                                
                                update.setFontData(JFaceResources.getFontDescriptor(IInternalDebugUIConstants.VARIABLE_TEXT_FONT).getFontData()[0], idx);
                            }
                            
                            update.done();
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
                            
                            update.setFontData(JFaceResources.getFontDescriptor(IInternalDebugUIConstants.VARIABLE_TEXT_FONT).getFontData()[0], idx);
                        }
                        
                        if ( ! weAreExtractingFormattedData ) {
                            update.done();
                        } else {
                            for (int idx = 0; idx < localColumns.length; idx++) {
                                if (IDebugVMConstants.COLUMN_ID__VALUE.equals(localColumns[idx])) {
                                    updateFormattedRegisterValue(update, idx, dmc, getData() );
                                }
                                update.setFontData(JFaceResources.getFontDescriptor(IInternalDebugUIConstants.VARIABLE_TEXT_FONT).getFontData()[0], idx);
                            }
                        }
                    }
                },
                getExecutor()
            );
        }
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.dd.dsf.ui.viewmodel.datamodel.AbstractDMVMNode#updateElementsInSessionThread(org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate)
     */
    @Override
    protected void updateElementsInSessionThread(final IChildrenUpdate update) {
        final IRegisterDMContext regDmc = findDmcInPath(update.getViewerInput(), update.getElementPath(), IRegisterDMContext.class);

        if (regDmc == null) {
            handleFailedUpdate(update);
            return;
        }          
        
        IRegisters regService = getServicesTracker().getService(IRegisters.class);
        
        if ( regService == null ) {
        	handleFailedUpdate(update);
            return;
        }
        
        
        regService.getBitFields(
            regDmc,
            new ViewerDataRequestMonitor<IBitFieldDMContext[]>(getSession().getExecutor(), update) {
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
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.dd.dsf.ui.viewmodel.datamodel.AbstractDMVMNode#createVMContext(org.eclipse.dd.dsf.datamodel.IDMContext)
     */
    @Override
    protected IDMVMContext createVMContext(IDMContext dmc) {
        return new BitFieldVMC(dmc);
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.dd.dsf.ui.viewmodel.IVMNode#getDeltaFlags(java.lang.Object)
     */
    public int getDeltaFlags(Object e) {
        if ( e instanceof ISuspendedDMEvent || 
             e instanceof IMemoryChangedEvent ||
             e instanceof IRegisterChangedDMEvent || 
             (e instanceof PropertyChangeEvent &&
              ((PropertyChangeEvent)e).getProperty() == IDebugVMConstants.CURRENT_FORMAT_STORAGE) ) 
        {
            return IModelDelta.CONTENT;
        } 

        if (e instanceof IBitFieldChangedDMEvent) {
            return IModelDelta.STATE;
        }

        return IModelDelta.NO_CHANGE;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.dd.dsf.ui.viewmodel.IVMNode#buildDelta(java.lang.Object, org.eclipse.dd.dsf.ui.viewmodel.VMDelta, int, org.eclipse.dd.dsf.concurrent.RequestMonitor)
     */
    public void buildDelta(Object e, VMDelta parentDelta, int nodeOffset, RequestMonitor rm) {
        // The following events can affect any bit field's values, 
        // refresh the contents of the parent element (i.e. all the registers). 
        if ( e instanceof ISuspendedDMEvent || 
             e instanceof IMemoryChangedEvent ||
             e instanceof IRegisterChangedDMEvent || 
             (e instanceof PropertyChangeEvent &&
              ((PropertyChangeEvent)e).getProperty() == IDebugVMConstants.CURRENT_FORMAT_STORAGE) ) 
        {
            parentDelta.setFlags(parentDelta.getFlags() | IModelDelta.CONTENT);
        } 

        if (e instanceof IBitFieldChangedDMEvent) {
            // Create a delta indicating that the value of bit field has changed.
            parentDelta.addNode( createVMContext(((IBitFieldChangedDMEvent)e).getDMContext()), IModelDelta.STATE );
        } 

        rm.done();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IElementEditor#getCellEditor(org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext, java.lang.String, java.lang.Object, org.eclipse.swt.widgets.Composite)
     */
    public CellEditor getCellEditor(IPresentationContext context, String columnId, Object element, Composite parent) {
        
        if (IDebugVMConstants.COLUMN_ID__VALUE.equals(columnId)) {
            /*
             *   In order to decide what kind of editor to present we need to know if there are 
             *   mnemonics which can be used to represent the values. If there are then we will
             *   create a Combo editor for them. Otherwise we will just make a normal text cell
             *   editor.  If there are bit groups then the modifier will check the size of  the
             *   value being entered.
             */
            IBitFieldDMData bitFieldData = getSyncRegisterDataAccess().readBitField(element);

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

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IElementEditor#getCellModifier(org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext, java.lang.Object)
     */
    public ICellModifier getCellModifier(IPresentationContext context, Object element) {
        
        /*
         *   In order to decide what kind of modifier to present we need to know if there
         *   are mnemonics which can be used to represent the values. 
         */
        IBitFieldDMData bitFieldData = getSyncRegisterDataAccess().readBitField(element);

        if ( bitFieldData != null && bitFieldData.isWriteable() ) {

            IMnemonic[] mnemonics = bitFieldData.getMnemonics();

            if ( mnemonics != null  && mnemonics.length != 0 ) {
                /*
                 *  Note we are complex COMBO and return the right editor.
                 */
                return new RegisterBitFieldCellModifier(
                    getDMVMProvider(), fFormattedPrefStore, BitFieldEditorStyle.BITFIELDCOMBO, getSyncRegisterDataAccess() );
            }
            else {
                /*
                 *  Text editor even if we need to clamp the value entered.
                 */
                return new RegisterBitFieldCellModifier( 
                    getDMVMProvider(), fFormattedPrefStore, BitFieldEditorStyle.BITFIELDTEXT, getSyncRegisterDataAccess() );
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
            if ( startIdx == -1 || endIdx == -1 ) {
            	return null;
            }
            String remaining = expression.substring(endIdx+1);
            if ( ! remaining.startsWith(".REG(") ) { //$NON-NLS-1$
                return null;
            }
            
            /*
             * Get the register portion.
             */
            startIdx = ".REG(".length(); //$NON-NLS-1$
            endIdx = remaining.indexOf(')', startIdx);
            if ( startIdx == -1 || endIdx == -1 ) {
            	return null;
            }
            remaining = remaining.substring(endIdx+1);
            
            /*
             * Get the bit-field portion.
             */
            if ( ! remaining.startsWith(".BFLD(") ) { //$NON-NLS-1$
                return null;
            }
            startIdx = ".BFLD(".length(); //$NON-NLS-1$
            endIdx = remaining.indexOf(')', startIdx);
            if ( startIdx == -1 || endIdx == -1 ) {
            	return null;
            }
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
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.dd.dsf.debug.internal.provisional.ui.viewmodel.expression.AbstractExpressionVMNode#testElementForExpression(java.lang.Object, org.eclipse.debug.core.model.IExpression, org.eclipse.dd.dsf.concurrent.DataRequestMonitor)
     */
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

    /*
     * (non-Javadoc)
     * @see org.eclipse.dd.dsf.debug.internal.provisional.ui.viewmodel.expression.AbstractExpressionVMNode#associateExpression(java.lang.Object, org.eclipse.debug.core.model.IExpression)
     */
    @Override
    protected void associateExpression(Object element, IExpression expression) {
        if (element instanceof BitFieldVMC) {
            ((BitFieldVMC)element).setExpression(expression);
        }
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.dd.dsf.debug.internal.provisional.ui.viewmodel.expression.IExpressionVMNode#getDeltaFlagsForExpression(org.eclipse.debug.core.model.IExpression, java.lang.Object)
     */
    public int getDeltaFlagsForExpression(IExpression expression, Object event) {
        if (event instanceof ISuspendedDMEvent) {
            return IModelDelta.CONTENT;
        }

        if (event instanceof PropertyChangeEvent && 
            ((PropertyChangeEvent)event).getProperty() == IDebugVMConstants.CURRENT_FORMAT_STORAGE) {
            return IModelDelta.CONTENT;            
        }

        if (event instanceof IMemoryChangedEvent) {
            return IModelDelta.CONTENT;
        }
        
        return IModelDelta.NO_CHANGE;
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.dd.dsf.debug.internal.provisional.ui.viewmodel.expression.IExpressionVMNode#buildDeltaForExpression(org.eclipse.debug.core.model.IExpression, int, java.lang.Object, org.eclipse.dd.dsf.ui.viewmodel.VMDelta, org.eclipse.jface.viewers.TreePath, org.eclipse.dd.dsf.concurrent.RequestMonitor)
     */
    public void buildDeltaForExpression(final IExpression expression, final int elementIdx, final Object event, final VMDelta parentDelta, final TreePath path, final RequestMonitor rm) 
    {
        // Always refresh the contents of the view upon suspended event.
        if (event instanceof ISuspendedDMEvent) {
            parentDelta.setFlags(parentDelta.getFlags() | IModelDelta.CONTENT);
        }         

        rm.done();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.dd.dsf.debug.internal.provisional.ui.viewmodel.expression.IExpressionVMNode#buildDeltaForExpressionElement(java.lang.Object, int, java.lang.Object, org.eclipse.dd.dsf.ui.viewmodel.VMDelta, org.eclipse.dd.dsf.concurrent.RequestMonitor)
     */
    public void buildDeltaForExpressionElement(Object element, int elementIdx, Object event, VMDelta parentDelta, final RequestMonitor rm) 
    {
        // The following events can affect register values, refresh the state 
        // of the expression. 
        if ( event instanceof IRegisterChangedDMEvent ||
             event instanceof IMemoryChangedEvent ||
             (event instanceof PropertyChangeEvent && 
                ((PropertyChangeEvent)event).getProperty() == IDebugVMConstants.CURRENT_FORMAT_STORAGE) )
        {
            parentDelta.addNode(element, IModelDelta.STATE);
        } 

        rm.done();
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IElementMementoProvider#compareElements(org.eclipse.debug.internal.ui.viewers.model.provisional.IElementCompareRequest[])
     */
    private final String MEMENTO_NAME = "BITFIELD_MEMENTO_NAME"; //$NON-NLS-1$
    
    public void compareElements(IElementCompareRequest[] requests) {
        for ( final IElementCompareRequest request : requests ) {
            final String mementoName = request.getMemento().getString(MEMENTO_NAME);
            
            final IBitFieldDMContext regDmc = findDmcInPath(request.getViewerInput(), request.getElementPath(), IBitFieldDMContext.class);
            if (regDmc == null || mementoName == null) {
                request.done();
                continue;
            }
            
            //  Now go get the model data for the single register group found.
            try {
                getSession().getExecutor().execute(new DsfRunnable() {
                    public void run() {
                        final IRegisters regService = getServicesTracker().getService(IRegisters.class);
                        if ( regService != null ) {
                            regService.getBitFieldData(
                                regDmc, 
                                new DataRequestMonitor<IBitFieldDMData>(regService.getExecutor(), null) {
                                    @Override
                                    protected void handleCompleted() {
                                        if ( getStatus().isOK() ) {
                                            // Now make sure the register group is the one we want.
                                            request.setEqual( mementoName.equals( "BitField." + getData().getName() ) ); //$NON-NLS-1$
                                        }
                                        request.done();
                                    }
                                });
                        } else {
                            request.done();
                        }
                    }
                });
            } catch (RejectedExecutionException e) {
                request.done();
            }
        }
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IElementMementoProvider#encodeElements(org.eclipse.debug.internal.ui.viewers.model.provisional.IElementMementoRequest[])
     */
    public void encodeElements(IElementMementoRequest[] requests) {
        for ( final IElementMementoRequest request : requests ) {
            final IBitFieldDMContext regDmc = findDmcInPath(request.getViewerInput(), request.getElementPath(), IBitFieldDMContext.class);
            if (regDmc == null) {
                request.done();
                continue;
            }
            
            //  Now go get the model data for the single register group found.
            try {
                getSession().getExecutor().execute(new DsfRunnable() {
                    public void run() {
                        final IRegisters regService = getServicesTracker().getService(IRegisters.class);
                        if ( regService != null ) {
                            regService.getBitFieldData(
                                regDmc, 
                                new DataRequestMonitor<IBitFieldDMData>(regService.getExecutor(), null) {
                                    @Override
                                    protected void handleCompleted() {
                                        if ( getStatus().isOK() ) {
                                            // Now make sure the register group is the one we want.
                                            request.getMemento().putString(MEMENTO_NAME, "BitField." + getData().getName()); //$NON-NLS-1$
                                        }
                                        request.done();
                                    }
                                });
                        } else {
                            request.done();
                        }
                    }
                });
            } catch (RejectedExecutionException e) {
                request.done();
            }
        }
    }
}
