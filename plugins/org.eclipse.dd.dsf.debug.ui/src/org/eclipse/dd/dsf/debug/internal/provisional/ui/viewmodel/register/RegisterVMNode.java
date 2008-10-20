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

import java.util.ArrayList;
import java.util.concurrent.RejectedExecutionException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.dd.dsf.concurrent.DataRequestMonitor;
import org.eclipse.dd.dsf.concurrent.DsfExecutor;
import org.eclipse.dd.dsf.concurrent.DsfRunnable;
import org.eclipse.dd.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.dd.dsf.concurrent.ImmediateExecutor;
import org.eclipse.dd.dsf.concurrent.MultiRequestMonitor;
import org.eclipse.dd.dsf.concurrent.RequestMonitor;
import org.eclipse.dd.dsf.datamodel.DMContexts;
import org.eclipse.dd.dsf.datamodel.IDMContext;
import org.eclipse.dd.dsf.debug.internal.provisional.ui.viewmodel.IDebugVMConstants;
import org.eclipse.dd.dsf.debug.internal.provisional.ui.viewmodel.expression.AbstractExpressionVMNode;
import org.eclipse.dd.dsf.debug.internal.provisional.ui.viewmodel.numberformat.IFormattedValuePreferenceStore;
import org.eclipse.dd.dsf.debug.internal.provisional.ui.viewmodel.numberformat.IFormattedValueVMContext;
import org.eclipse.dd.dsf.debug.internal.ui.DsfDebugUIPlugin;
import org.eclipse.dd.dsf.debug.service.IFormattedValues;
import org.eclipse.dd.dsf.debug.service.IRegisters;
import org.eclipse.dd.dsf.debug.service.IFormattedValues.FormattedValueDMContext;
import org.eclipse.dd.dsf.debug.service.IFormattedValues.FormattedValueDMData;
import org.eclipse.dd.dsf.debug.service.IMemory.IMemoryChangedEvent;
import org.eclipse.dd.dsf.debug.service.IRegisters.IRegisterChangedDMEvent;
import org.eclipse.dd.dsf.debug.service.IRegisters.IRegisterDMContext;
import org.eclipse.dd.dsf.debug.service.IRegisters.IRegisterDMData;
import org.eclipse.dd.dsf.debug.service.IRegisters.IRegisterGroupDMData;
import org.eclipse.dd.dsf.debug.service.IRegisters.IRegistersChangedDMEvent;
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
import org.eclipse.debug.internal.ui.viewers.model.provisional.IHasChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.actions.IWatchExpressionFactoryAdapter2;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.swt.widgets.Composite;

@SuppressWarnings("restriction")
public class RegisterVMNode extends AbstractExpressionVMNode 
    implements IElementEditor, IElementLabelProvider, IElementMementoProvider
{
    protected class RegisterVMC extends DMVMContext
        implements IFormattedValueVMContext
    {
        private IExpression fExpression;
        public RegisterVMC(IDMContext dmc) {
            super(dmc);
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
            if (other instanceof RegisterVMC && super.equals(other)) {
                RegisterVMC otherReg = (RegisterVMC)other;
                return (otherReg.fExpression == null && fExpression == null) ||
                (otherReg.fExpression != null && otherReg.fExpression.equals(fExpression));
            }
            return false;
        }

        @Override
        public int hashCode() {
            return super.hashCode() + (fExpression != null ? fExpression.hashCode() : 0);
        }

        public IFormattedValuePreferenceStore getPreferenceStore() {
            return fFormattedPrefStore;
        }
    }

    protected class RegisterExpressionFactory implements IWatchExpressionFactoryAdapter2 {

        public boolean canCreateWatchExpression(Object element) {
            return element instanceof RegisterVMC;
        }

        /**
         * Expected format: GRP( GroupName ).REG( RegisterName )
         */
        public String createWatchExpression(Object element) throws CoreException {
            IRegisterGroupDMData groupData = getSyncRegisterDataAccess().getRegisterGroupDMData(element);
            IRegisterDMData registerData = getSyncRegisterDataAccess().getRegisterDMData(element);
            
            if (groupData != null && registerData != null) { 
            	StringBuffer exprBuf = new StringBuffer();
            	
            	exprBuf.append("GRP( ");  exprBuf.append(groupData.getName());    exprBuf.append(" )"); //$NON-NLS-1$ //$NON-NLS-2$
            	exprBuf.append(".REG( "); exprBuf.append(registerData.getName()); exprBuf.append(" )"); //$NON-NLS-1$ //$NON-NLS-2$
                
                return exprBuf.toString();
            }

            return null;            
        }
    }

    private IWatchExpressionFactoryAdapter2 fRegisterExpressionFactory = null; 
    final private SyncRegisterDataAccess fSyncRegisterDataAccess; 
    private final IFormattedValuePreferenceStore fFormattedPrefStore;

    public RegisterVMNode(IFormattedValuePreferenceStore prefStore, AbstractDMVMProvider provider, DsfSession session, SyncRegisterDataAccess syncDataAccess) {
        super(provider, session, IRegisterDMContext.class);
        fSyncRegisterDataAccess = syncDataAccess;
        fFormattedPrefStore = prefStore;
    }

    @Override
    public String toString() {
        return "RegisterVMNode(" + getSession().getId() + ")";  //$NON-NLS-1$ //$NON-NLS-2$
    }
    
    protected SyncRegisterDataAccess getSyncRegisterDataAccess() {
        return fSyncRegisterDataAccess;
    }

    public IFormattedValuePreferenceStore getPreferenceStore() {
        return fFormattedPrefStore;
    }
    
    /**
     * @since 1.1
     */
    public IWatchExpressionFactoryAdapter2 getWatchExpressionFactory() {
    	if ( fRegisterExpressionFactory == null ) {
    		fRegisterExpressionFactory = new RegisterExpressionFactory();
    	}
    	return fRegisterExpressionFactory;
    }
    
    /*
     * This class is used to hold the associated information needed to finally get the
     * formatted value for a register DMC.  It starts out with the basic set  sans the
     * actual formatted register DMC.  Once found this is added to the information.
     */
    private class QueuedValueUpdate {
    	
    	ILabelUpdate fUpdate;
    	int fIndex ;
    	IRegisterDMContext fDmc;
    	FormattedValueDMContext fValueDmc = null;
    	
    	public QueuedValueUpdate( ILabelUpdate update, int index , IRegisterDMContext dmc ) {
    		fUpdate = update;
        	fIndex = index;
        	fDmc = dmc;
    	}
    	
    	public ILabelUpdate getUpdate() { return fUpdate; }
    	public int getIndex() { return fIndex; }
    	public IRegisterDMContext getDmc() { return fDmc; }
    	
    	public void setValueDmc( FormattedValueDMContext dmc ) { fValueDmc = dmc; }
    	public FormattedValueDMContext getValueDmc() { return fValueDmc; }
    }
                                                 
    private void retrieveAllFormattedDataValues( final ArrayList<QueuedValueUpdate> updates ) {
    	
    	final IRegisters regService = getServicesTracker().getService(IRegisters.class);
        if ( regService == null ) {
        	for ( final QueuedValueUpdate up : updates ) {
        		handleFailedUpdate(up.getUpdate());
        	}
            return;
        }
        
    	for ( final QueuedValueUpdate up : updates ) {
    		
    		final ILabelUpdate update = up.getUpdate();
    		final int idx = up.getIndex();
    		final FormattedValueDMContext valueDmc = up.getValueDmc();
    		
    		getDMVMProvider().getModelData(
    			RegisterVMNode.this, 
    			update, 
    			regService, 
    			valueDmc,
    			new ViewerDataRequestMonitor<FormattedValueDMData>(getSession().getExecutor(), update) {
    				@Override
    				public void handleCompleted() {
    					if (!isSuccess()) {
    						if (getStatus().getCode() == IDsfStatusConstants.INVALID_STATE) {
    							update.setLabel("...", idx); //$NON-NLS-1$
    						} else {
    							update.setLabel("Error: " + getStatus().getMessage(), idx); //$NON-NLS-1$
    						}
    						update.setFontData(JFaceResources.getFontDescriptor(IInternalDebugUIConstants.VARIABLE_TEXT_FONT).getFontData()[0], idx);
    						update.done();
    						return;
    					}
    					/*
    					 *  Fill the label/column with the properly formatted data value.
    					 */
    					update.setLabel(getData().getFormattedValue(), idx);

    					// color based on change history
   						FormattedValueDMData oldData = (FormattedValueDMData) getDMVMProvider().getArchivedModelData(RegisterVMNode.this, update, valueDmc);
   						if(oldData != null && !oldData.getFormattedValue().equals(getData().getFormattedValue())) {
   							update.setBackground(DebugUIPlugin.getPreferenceColor(IInternalDebugUIConstants.PREF_CHANGED_VALUE_BACKGROUND).getRGB(), idx);
   						}
   						update.setFontData(JFaceResources.getFontDescriptor(IInternalDebugUIConstants.VARIABLE_TEXT_FONT).getFontData()[0], idx);
   						update.done();
   					}
   				}, 
   				getSession().getExecutor()
    		);
    	}
    }
    
    /**
     *  Private data access routine which performs the extra level of data access needed to
     *  get the formatted data value for a specific register.
     */
    private void getFormattedDmcForReqister( final ILabelUpdate update, final IRegisterDMContext dmc, final DataRequestMonitor<FormattedValueDMContext> rm)
    {
    	final IRegisters regService = getServicesTracker().getService(IRegisters.class);
        if ( regService == null ) {
        	rm.setStatus(new Status(IStatus.ERROR, DsfDebugUIPlugin.PLUGIN_ID, IDsfStatusConstants.NOT_SUPPORTED, "", null)); //$NON-NLS-1$
            rm.done();
            return;
        }
        
        /*
         *  First select the format to be used. This involves checking so see that the preference
         *  page format is supported by the register service. If the format is not supported then 
         *  we will pick the first available format.
         */
        final IPresentationContext context  = update.getPresentationContext();
        final String preferencePageFormatId = getPreferenceStore().getCurrentNumericFormat(context) ;
            
        regService.getAvailableFormats(
            dmc,
            new DataRequestMonitor<String[]>(getSession().getExecutor(), rm) {
                @Override
                public void handleSuccess() {
                    /*
                     *  See if the desired format is supported.
                     */
                    String[] formatIds = getData();
                    String   finalFormatId = IFormattedValues.NATURAL_FORMAT;
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
                            handleFailure();
                            return;
                        }
                    }
                    
                    /*
                     *  Format has been validated. Return it.
                     */
                    rm.setData(regService.getFormattedValueContext(dmc, finalFormatId));
                    rm.done();
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
     *  Updates the labels which are controlled by the column being requested.
     */
    protected void updateLabelInSessionThread(final ILabelUpdate[] updates) {
    	
    	/*
    	 * This list represents all the QUEUED requests for formatted DMCs. This allows us to issue the
    	 * requests for the data in the same dispatch cycle. Thus the lower level services is given its
    	 * best chance to coalesce the registers in to a single request.
    	 */
    	final ArrayList<QueuedValueUpdate> valueUpdatesToProcess = new ArrayList<QueuedValueUpdate>();
    	
    	final DsfExecutor dsfExecutor = getSession().getExecutor();
    	final MultiRequestMonitor<RequestMonitor> mrm =
            new MultiRequestMonitor<RequestMonitor>(dsfExecutor, null) {
                @Override
                public void handleCompleted() {
                    if (!isSuccess()) {
                  	    for ( ILabelUpdate up : updates ) {
                    	   handleFailedUpdate(up);
                    	}
                        return;
                    }
                    
                    /*
                     * We have all of the formatted DMCs. Go issue the requests for the formatted data
                     * in a single dispatch cycle.
                     */
                    retrieveAllFormattedDataValues( valueUpdatesToProcess );
                }
            };
        /*
         * Process each update request, creating a QUEUE of requests which need further processing
         * for the formatted values. 
         */
        for (final ILabelUpdate update : updates) {
        	
            final IRegisterDMContext dmc = findDmcInPath(update.getViewerInput(), update.getElementPath(), IRegisterDMContext.class);
            if ( dmc == null ) {
            	handleFailedUpdate(update);
                continue;
            }
            
            IRegisters regService = getServicesTracker().getService(IRegisters.class);
            if ( regService == null ) {
            	handleFailedUpdate(update);
                continue;
            }
            
            getDMVMProvider().getModelData(
                this, 
                update, 
                regService,
        		dmc,             
        		new ViewerDataRequestMonitor<IRegisterDMData>(getSession().getExecutor(), update) { 
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
                     * If columns are configured, extract the selected values for each understood column.  First we fill all
                     * of those columns which can be filled without  the extra data mining.  We also note, if we  do have to 
                     * datamine. Any columns need to set the processing flag so we know we have further work to do. If there 
                     * are more columns which need data extraction they need to be added in both "for" loops.
                     */
                    String[] localColumns = update.getColumnIds();
                    if (localColumns == null) localColumns = new String[] { IDebugVMConstants.COLUMN_ID__NAME }; 
                    
                    boolean allFieldsProcessed = true;
                    
                    for (int idx = 0; idx < localColumns.length; idx++) {
                    	update.setFontData(JFaceResources.getFontDescriptor(IInternalDebugUIConstants.VARIABLE_TEXT_FONT).getFontData()[0], idx);
                        if (IDebugVMConstants.COLUMN_ID__NAME.equals(localColumns[idx])) {
                            update.setLabel(getData().getName(), idx);
                            update.setImageDescriptor(DebugPluginImages.getImageDescriptor(IDebugUIConstants.IMG_OBJS_REGISTER), idx);
                        } else if (IDebugVMConstants.COLUMN_ID__VALUE.equals(localColumns[idx])) {
                        	allFieldsProcessed = false;
                        	/*
                        	 * Create an entry which holds all related data and add it to the list to process
                        	 * when all the formatted DMCs are gathered.
                        	 */
                        	final QueuedValueUpdate valueUpdate = new QueuedValueUpdate(update,idx,dmc);
                        	valueUpdatesToProcess.add(valueUpdate);
                        	
                        	/*
                        	 * Fetch the associated formatted DMC for this field. This is added to the multi-request
                        	 * monitor so they can all be gathered and processed in a single set.
                        	 */
                        	DataRequestMonitor<FormattedValueDMContext> rm = new DataRequestMonitor<FormattedValueDMContext>(dsfExecutor, null) {
                        		@Override
                        		public void handleCompleted() {
                        			valueUpdate.setValueDmc(getData());
                        			mrm.requestMonitorDone(this);
                        		}
                        	};

                        	mrm.add(rm);
                        	getFormattedDmcForReqister(update, dmc, rm);
                        } else if (IDebugVMConstants.COLUMN_ID__TYPE.equals(localColumns[idx])) {
                            IRegisterDMData data = getData();
                            String typeStr      = "Unsigned"; //$NON-NLS-1$
                            String ReadAttrStr  = "ReadNone"; //$NON-NLS-1$
                            String WriteAddrStr = "WriteNone"; //$NON-NLS-1$
                            
                            if ( data.isFloat() ) { typeStr = "Floating Point"; } //$NON-NLS-1$ 
                            
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
                    
                    if ( allFieldsProcessed ) {
                        update.done();
                    } 
                }
            },
            getSession().getExecutor());
        }
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.dd.dsf.ui.viewmodel.datamodel.AbstractDMVMNode#update(org.eclipse.debug.internal.ui.viewers.model.provisional.IHasChildrenUpdate[])
     */
    @Override
    public void update(IHasChildrenUpdate[] updates) {
        // As an optimization, always indicate that register groups have 
        // children.
        for (IHasChildrenUpdate update : updates) {
            update.setHasChilren(true);
            update.done();
        }
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.dd.dsf.ui.viewmodel.datamodel.AbstractDMVMNode#updateElementsInSessionThread(org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate)
     */
    @Override
    protected void updateElementsInSessionThread(final IChildrenUpdate update) {
        
        IRegisters regService = getServicesTracker().getService(IRegisters.class);
        
        if ( regService == null ) {
        	handleFailedUpdate(update);
            return;
        }
        
        regService.getRegisters(
            createCompositeDMVMContext(update),
            new ViewerDataRequestMonitor<IRegisterDMContext[]>(getSession().getExecutor(), update) { 
                @Override
                public void handleCompleted() {
                    if (!isSuccess()) {
                        handleFailedUpdate(update);
                        return;
                    }
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
        return new RegisterVMC(dmc);
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.dd.dsf.ui.viewmodel.IVMNode#getDeltaFlags(java.lang.Object)
     */
    public int getDeltaFlags(Object e) {
        if ( e instanceof ISuspendedDMEvent || 
             e instanceof IMemoryChangedEvent ||
             e instanceof IRegistersChangedDMEvent ||
             (e instanceof PropertyChangeEvent &&
              ((PropertyChangeEvent)e).getProperty() == IDebugVMConstants.CURRENT_FORMAT_STORAGE) ) 
        {
            return IModelDelta.CONTENT;
        } 
        
        if (e instanceof IRegisterChangedDMEvent) {
            return IModelDelta.STATE;
        }
        
        return IModelDelta.NO_CHANGE;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.dd.dsf.ui.viewmodel.IVMNode#buildDelta(java.lang.Object, org.eclipse.dd.dsf.ui.viewmodel.VMDelta, int, org.eclipse.dd.dsf.concurrent.RequestMonitor)
     */
    public void buildDelta(Object e, VMDelta parentDelta, int nodeOffset, RequestMonitor rm) {
        // The following events can affect any register's values, 
        // refresh the contents of the parent element (i.e. all the registers). 
        if ( e instanceof ISuspendedDMEvent || 
             e instanceof IMemoryChangedEvent ||
             e instanceof IRegistersChangedDMEvent ||
             (e instanceof PropertyChangeEvent &&
              ((PropertyChangeEvent)e).getProperty() == IDebugVMConstants.CURRENT_FORMAT_STORAGE) ) 
        {
            // Create a delta that the whole register group has changed.
            parentDelta.setFlags(parentDelta.getFlags() | IModelDelta.CONTENT);
        } 
        
        if (e instanceof IRegisterChangedDMEvent) {
            parentDelta.addNode( createVMContext(((IRegisterChangedDMEvent)e).getDMContext()), IModelDelta.STATE );
        } 
        
        rm.done();
    }

    /**
     * Expected format: GRP( GroupName ).REG( RegisterName )
     *              or: $RegisterName
     */
    public boolean canParseExpression(IExpression expression) {
        return parseExpressionForRegisterName(expression.getExpressionText()) != null;
    }
    
    private String parseExpressionForRegisterName(String expression) {
    	if (expression.startsWith("GRP(")) { //$NON-NLS-1$
    		/*
    		 * Get the group portion.
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
            String regName = remaining.substring(startIdx,endIdx);
            return regName.trim();
        }
    	else if ( expression.startsWith("$") ) { //$NON-NLS-1$
    		/*
    		 * At this point I am leaving this code here to represent the register case. To do this
    		 * correctly would be to use the findRegister function and upgrade the register service
    		 * to deal with registers that  do not have a specified group parent context.  I do not
    		 * have the time for this right now.  So by saying we do not handle this the Expression
    		 * VM node will take it and pass it to the debug engine  as a generic expression.  Most
    		 * debug engines ( GDB included )  have an inherent knowledge  of the core registers as
    		 * part of their expression evaluation  and will respond with a flat value for the reg.
    		 * This is not totally complete in that you should be able to express  a register which
    		 * has bit fields for example and the bit fields should be expandable in the expression
    		 * view. With this method it will just appear to have a single value and no sub-fields.
    		 * I will file a defect/enhancement  for this to mark it.  This comment will act as the
    		 * place-holder for the future work.
    		 */
    		return null;
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
        final IRegisterDMContext dmc = DMContexts.getAncestorOfType(((IDMVMContext)element).getDMContext(), IRegisterDMContext.class);
        if (dmc == null) {
            rm.setStatus(new Status(IStatus.ERROR, DsfDebugUIPlugin.PLUGIN_ID, IDsfStatusConstants.INVALID_HANDLE, "Invalid context", null)); //$NON-NLS-1$
            rm.done();
            return;
        }
        
        final String regName = parseExpressionForRegisterName(expression.getExpressionText());
        try {
            getSession().getExecutor().execute(new DsfRunnable() {
                public void run() {
                    IRegisters registersService = getServicesTracker().getService(IRegisters.class);
                    if (registersService != null) {
                        registersService.getRegisterData(
                            dmc, 
                            new DataRequestMonitor<IRegisterDMData>(ImmediateExecutor.getInstance(), rm) {
                                @Override
                                protected void handleSuccess() {
                                    rm.setData( getData().getName().equals(regName) );
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
        if (element instanceof RegisterVMC) {
            ((RegisterVMC)element).setExpression(expression);
        }
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.dd.dsf.debug.internal.provisional.ui.viewmodel.expression.IExpressionVMNode#getDeltaFlagsForExpression(org.eclipse.debug.core.model.IExpression, java.lang.Object)
     */
    public int getDeltaFlagsForExpression(IExpression expression, Object event) {
        if ( event instanceof IRegisterChangedDMEvent ||
             event instanceof IMemoryChangedEvent ||
             (event instanceof PropertyChangeEvent && 
               ((PropertyChangeEvent)event).getProperty() == IDebugVMConstants.CURRENT_FORMAT_STORAGE) )
        {
            return IModelDelta.STATE;
        }
        
        if (event instanceof IRegistersChangedDMEvent ||
            event instanceof ISuspendedDMEvent)
        {
            return IModelDelta.CONTENT;
        }

        return IModelDelta.NO_CHANGE;
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.dd.dsf.debug.internal.provisional.ui.viewmodel.expression.IExpressionVMNode#buildDeltaForExpression(org.eclipse.debug.core.model.IExpression, int, java.lang.Object, org.eclipse.dd.dsf.ui.viewmodel.VMDelta, org.eclipse.jface.viewers.TreePath, org.eclipse.dd.dsf.concurrent.RequestMonitor)
     */
    public void buildDeltaForExpression(IExpression expression, int elementIdx, Object event, VMDelta parentDelta, 
        TreePath path, RequestMonitor rm) 
    {
        // If the register definition has changed, refresh all the 
        // expressions in the expression manager.  This is because some 
        // expressions that were previously invalid, may now represent new 
        // registers.
        if (event instanceof IRegistersChangedDMEvent) {
        	parentDelta.setFlags(parentDelta.getFlags() | IModelDelta.CONTENT);
        }

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
     * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IElementEditor#getCellEditor(org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext, java.lang.String, java.lang.Object, org.eclipse.swt.widgets.Composite)
     */
    public CellEditor getCellEditor(IPresentationContext context, String columnId, Object element, Composite parent) {
        if (IDebugVMConstants.COLUMN_ID__EXPRESSION.equals(columnId)) {
            return new TextCellEditor(parent);
        } 
        else if (IDebugVMConstants.COLUMN_ID__VALUE.equals(columnId)) {
          /*
           *   See if the register is writable and if so we will created a
           *   cell editor for it.
           */
          IRegisterDMData regData = getSyncRegisterDataAccess().readRegister(element);

          if ( regData != null && regData.isWriteable() ) {
              return new TextCellEditor(parent);
          }
      }
      return null;
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IElementEditor#getCellModifier(org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext, java.lang.Object)
     */
    public ICellModifier getCellModifier(IPresentationContext context, Object element) {
        return new RegisterCellModifier( 
            getDMVMProvider(), fFormattedPrefStore, getSyncRegisterDataAccess() );
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IElementMementoProvider#compareElements(org.eclipse.debug.internal.ui.viewers.model.provisional.IElementCompareRequest[])
     */
    private final String MEMENTO_NAME = "REGISTER_MEMENTO_NAME"; //$NON-NLS-1$
    
    public void compareElements(IElementCompareRequest[] requests) {
        for ( final IElementCompareRequest request : requests ) {
            final IRegisterDMContext regDmc = findDmcInPath(request.getViewerInput(), request.getElementPath(), IRegisterDMContext.class);
            final String mementoName = request.getMemento().getString(MEMENTO_NAME);
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
                            regService.getRegisterData(
                                regDmc, 
                                new DataRequestMonitor<IRegisterDMData>(regService.getExecutor(), null) {
                                    @Override
                                    protected void handleCompleted() {
                                        if ( getStatus().isOK() ) {
                                            // Now make sure the register group is the one we want.
                                            request.setEqual( mementoName.equals( "Register." + getData().getName() ) ); //$NON-NLS-1$
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
            final IRegisterDMContext regDmc = findDmcInPath(request.getViewerInput(), request.getElementPath(), IRegisterDMContext.class);
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
                            regService.getRegisterData(
                                regDmc, 
                                new DataRequestMonitor<IRegisterDMData>(regService.getExecutor(), null) {
                                    @Override
                                    protected void handleCompleted() {
                                        if ( getStatus().isOK() ) {
                                            // Now make sure the register group is the one we want.
                                            request.getMemento().putString(MEMENTO_NAME, "Register." + getData().getName()); //$NON-NLS-1$
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
