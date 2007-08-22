/**
 * Copyright (c) 2006 Wind River Systems and others. All rights reserved. This program and
 * the accompanying materials are made available under the terms of the Eclipse Public
 * License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Wind River Systems - initial API and implementation
 */

package org.eclipse.dd.dsf.debug.ui.viewmodel.variable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.dd.dsf.concurrent.DataRequestMonitor;
import org.eclipse.dd.dsf.concurrent.DsfExecutor;
import org.eclipse.dd.dsf.concurrent.MultiRequestMonitor;
import org.eclipse.dd.dsf.concurrent.RequestMonitor;
import org.eclipse.dd.dsf.datamodel.DMContexts;
import org.eclipse.dd.dsf.datamodel.IDMContext;
import org.eclipse.dd.dsf.datamodel.IDMService;
import org.eclipse.dd.dsf.debug.service.IExpressions;
import org.eclipse.dd.dsf.debug.service.IFormattedValues;
import org.eclipse.dd.dsf.debug.service.IRunControl;
import org.eclipse.dd.dsf.debug.service.IStack;
import org.eclipse.dd.dsf.debug.service.IExpressions.IExpressionDMContext;
import org.eclipse.dd.dsf.debug.service.IExpressions.IExpressionDMData;
import org.eclipse.dd.dsf.debug.service.IFormattedValues.FormattedValueDMContext;
import org.eclipse.dd.dsf.debug.service.IFormattedValues.FormattedValueDMData;
import org.eclipse.dd.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.dd.dsf.debug.service.IStack.IVariableDMContext;
import org.eclipse.dd.dsf.debug.service.IStack.IVariableDMData;
import org.eclipse.dd.dsf.debug.ui.DsfDebugUIPlugin;
import org.eclipse.dd.dsf.debug.ui.viewmodel.IDebugVMConstants;
import org.eclipse.dd.dsf.debug.ui.viewmodel.expression.AbstractExpressionLayoutNode;
import org.eclipse.dd.dsf.debug.ui.viewmodel.formatsupport.IFormattedValuePreferenceStore;
import org.eclipse.dd.dsf.debug.ui.viewmodel.formatsupport.IFormattedValueVMContext;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.dd.dsf.service.IDsfService;
import org.eclipse.dd.dsf.ui.viewmodel.AbstractVMProvider;
import org.eclipse.dd.dsf.ui.viewmodel.IVMContext;
import org.eclipse.dd.dsf.ui.viewmodel.IVMLayoutNode;
import org.eclipse.dd.dsf.ui.viewmodel.VMDelta;
import org.eclipse.dd.dsf.ui.viewmodel.update.VMCacheManager;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementEditor;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.ui.actions.IWatchExpressionFactoryAdapterExtension;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.widgets.Composite;

@SuppressWarnings({"restriction", "nls"})
public class VariableLayoutNode extends AbstractExpressionLayoutNode<IExpressionDMData> implements IElementEditor {
    
    /** 
     * List of child nodes containing only a reference to this.  This is what enables the view model
     * provider to know about the recursive nature of subexpression nodes.
     */
    private final IVMLayoutNode[] fChildLayoutNodes = { this };
    
    @Override
    public IVMLayoutNode[] getChildLayoutNodes() {
        return fChildLayoutNodes;
    }
    
    @Override
    public int getDeltaFlags(Object e) {
        /*
         * @see buildDelta()
         */
        
        if (e instanceof IRunControl.ISuspendedDMEvent) {
            return IModelDelta.CONTENT;
        }
        
        if ( e instanceof IExpressions.IExpressionChangedDMEvent) {
            return IModelDelta.STATE;
        }
        
        return IModelDelta.NO_CHANGE;
    }

    @Override
    public void buildDelta(final Object event, final VMDelta parentDelta, final int nodeOffset, final RequestMonitor requestMonitor) {
    
        if (event instanceof IRunControl.ISuspendedDMEvent) {
            parentDelta.addFlags(IModelDelta.CONTENT);
        } 
        else if ( event instanceof IExpressions.IExpressionChangedDMEvent) {
            /*
             * Flush the cache.
             */
            VMCacheManager.getVMCacheManager().flush(super.getVMProvider().getPresentationContext());

            /*
             *  Logically one would think that STATE should be specified here. But we specifiy CONTENT
             *  as well so that if there sub expressions which are affected in some way ( such as with
             *  an expanded union then they will show the changes also.
             */
            parentDelta.addNode( createVMContext(((IExpressions.IExpressionChangedDMEvent)event).getDMContext()), IModelDelta.CONTENT | IModelDelta.STATE );
        }
        
        requestMonitor.done();
    }
    
    private final IFormattedValuePreferenceStore fFormattedPrefStore;
    
    private final SyncVariableDataAccess fSyncVariableDataAccess;
    
    public class VariableExpressionVMC extends DMVMContext implements IFormattedValueVMContext, IVariable {
        
        private IExpression fExpression;
        
        public VariableExpressionVMC(IDMContext<?> dmc) {
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
                return fVariableExpressionFactory;
            } else {
                return super.getAdapter(adapter);
            }
        }
        
        @Override
        public boolean equals(Object other) {
            if (other instanceof VariableExpressionVMC && super.equals(other)) {
                VariableExpressionVMC otherGroup = (VariableExpressionVMC)other;
                return (otherGroup.fExpression == null && fExpression == null) ||
                       (otherGroup.fExpression != null && otherGroup.fExpression.equals(fExpression));
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
    
    protected class VariableExpressionFactory implements IWatchExpressionFactoryAdapterExtension {

        public boolean canCreateWatchExpression(IVariable variable) {
            return variable instanceof VariableExpressionVMC;
        }

        public String createWatchExpression(IVariable variable) throws CoreException {
            
            VariableExpressionVMC exprVmc = (VariableExpressionVMC) variable;
            
            IExpressionDMContext exprDmc = DMContexts.getAncestorOfType(exprVmc.getDMC(), IExpressionDMContext.class);
            if (exprDmc != null) {
                return exprDmc.getQualifiedExpression();
            }

            return null;     
        }
    }

    final protected VariableExpressionFactory fVariableExpressionFactory = new VariableExpressionFactory();

    public VariableLayoutNode(IFormattedValuePreferenceStore prefStore, AbstractVMProvider provider,
                                    DsfSession session, SyncVariableDataAccess syncVariableDataAccess) {
        super(provider, session, IExpressions.IExpressionDMContext.class);
        fFormattedPrefStore = prefStore;
        fSyncVariableDataAccess = syncVariableDataAccess;
    }

    @Override
    protected IVMContext createVMContext(IDMContext<IExpressionDMData> dmc) {
        return new VariableExpressionVMC(dmc);
    }
    
    /**
     * We override this method because we now need to perform an extra level of data fetch to get the
     * formatted value of the expression.
     * 
     * @return void
     */
    @Override
    protected void updateLabelInSessionThread(ILabelUpdate[] updates) {
        for (final ILabelUpdate update : updates) {
            
            final IExpressionDMContext dmc = findDmcInPath(update.getElementPath(), IExpressions.IExpressionDMContext.class);
            
            VMCacheManager.getVMCacheManager().getCache(update.getPresentationContext())
				.getModelData((IDMService)getServicesTracker().getService(null, dmc.getServiceFilter()),
				dmc, 
                new DataRequestMonitor<IExpressionDMData>(getSession().getExecutor(), null) { 
                    @Override
                    protected void handleCompleted() {
                        // Check that the request was evaluated and data is still valid.  The request could
                        // fail if the state of the  service changed during the request, but the view model
                        // has not been updated yet.
                        
                        if (!getStatus().isOK() || !getData().isValid()) {
                            assert getStatus().isOK() || 
                                   getStatus().getCode() != IDsfService.INTERNAL_ERROR || 
                                   getStatus().getCode() != IDsfService.NOT_SUPPORTED;
                            
                            /*
                             *  Instead of just failing this outright we are going to attempt to do more here.
                             *  Failing it outright causes the view to display ... for all columns in the line
                             *  and this is uninformative about what is happening. It will be very common that
                             *  one or more variables at that given instance in time are not evaluatable. They
                             *  may be out of scope and will come back into scope later.
                             */
                            String[] localColumns = update.getPresentationContext().getColumns();
                            if (localColumns == null)
                                localColumns = new String[] { IDebugVMConstants.COLUMN_ID__NAME };
                            
                            for (int idx = 0; idx < localColumns.length; idx++) {
                                if (IDebugVMConstants.COLUMN_ID__NAME.equals(localColumns[idx])) {
                                    update.setLabel(dmc.getExpression(), idx);
                                } else if (IDebugVMConstants.COLUMN_ID__TYPE.equals(localColumns[idx])) {
                                    update.setLabel("", idx);
                                } else if (IDebugVMConstants.COLUMN_ID__VALUE.equals(localColumns[idx])) {
                                    update.setLabel("Error : " + getStatus().getMessage(), idx);
                                } else if (IDebugVMConstants.COLUMN_ID__DESCRIPTION.equals(localColumns[idx])) {
                                    update.setLabel("", idx);
                                } else if (IDebugVMConstants.COLUMN_ID__EXPRESSION.equals(localColumns[idx])) {
                                    update.setLabel(dmc.getExpression(), idx);
                                }
                            }
                            
                            update.done();
                            return;
                        }
                        
                        // If columns are configured, extract the selected values for each understood column.
                        // First, we fill all of those columns which can be filled without extra data mining.
                        // We also note if we  do have to do extra data mining.  Any columns need to set the
                        // processing flag so we know we have further work to do.  If there are more columns
                        // which need data extraction they need to be added in both "for" loops.

                        String[] localColumns = update.getPresentationContext().getColumns();
                        if (localColumns == null)
                            localColumns = new String[] { IDebugVMConstants.COLUMN_ID__NAME };
                        
                        boolean weAreExtractingFormattedData = false;
                        
                        for (int idx = 0; idx < localColumns.length; idx++) {
                            if (IDebugVMConstants.COLUMN_ID__NAME.equals(localColumns[idx])) {
                                update.setLabel(getData().getName(), idx);
                            } else if (IDebugVMConstants.COLUMN_ID__TYPE.equals(localColumns[idx])) {
                                update.setLabel(getData().getTypeName(), idx);
                            } else if (IDebugVMConstants.COLUMN_ID__VALUE.equals(localColumns[idx])) {
                                weAreExtractingFormattedData = true;
                            } else if (IDebugVMConstants.COLUMN_ID__DESCRIPTION.equals(localColumns[idx])) {
                                update.setLabel("", idx);
                            } else if (IDebugVMConstants.COLUMN_ID__EXPRESSION.equals(localColumns[idx])) {
                                update.setLabel(getData().getName(), idx);
                            }
                        }
                        
                        if ( ! weAreExtractingFormattedData ) {
                            update.done();
                        } else {
                            for (int idx = 0; idx < localColumns.length; idx++) {
                                if (IDebugVMConstants.COLUMN_ID__VALUE.equals(localColumns[idx])) {
                                    updateFormattedExpressionValue(update, idx, dmc);
                                }
                            }
                        }
                    }
                },
                getExecutor()
            );
        }
    }

    
    /**
     *  Private data access routine which performs the extra level of data access needed to
     *  get the formatted data value for a specific register.
     */
    private void updateFormattedExpressionValue(final ILabelUpdate update, final int labelIndex,
                                                final IExpressionDMContext dmc)
    {
        final IExpressions expressionService = getServicesTracker().getService(IExpressions.class);
        /*
         *  First select the format to be used. This involves checking so see that the preference
         *  page format is supported by the register service. If the format is not supported then 
         *  we will pick the first available format.
         */
        final IPresentationContext context  = update.getPresentationContext();
        final String preferencePageFormatId = fFormattedPrefStore.getCurrentNumericFormat(context) ;
        
        expressionService.getAvailableFormattedValues(
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
                    String   finalFormatId = IFormattedValues.NATURAL_FORMAT;
                    boolean  requestedFormatIsSupported = false;
                    
                    for ( String fId : formatIds ) {
                        if ( preferencePageFormatId.equals(fId) ) {
                            // The desired format is supported.

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
                            // Expression service does not support any format.
                            
                            handleFailedUpdate(update);
                            return;
                        }
                    }
                    
                    /*
                     *  Format has been validated. Get the formatted value.
                     */
                    final FormattedValueDMContext valueDmc = expressionService.getFormattedValue(dmc, finalFormatId);
                    
                    VMCacheManager.getVMCacheManager().getCache(update.getPresentationContext())
        				.getModelData(expressionService,
                        valueDmc, 
                        new DataRequestMonitor<FormattedValueDMData>(getSession().getExecutor(), null) {
                            @Override
                            public void handleCompleted() {
                                if (!getStatus().isOK()) {
                                    handleFailedUpdate(update);
                                    return;
                                }

                                // Fill the label/column with the properly formatted data value.
                                update.setLabel(getData().getFormattedValue(), labelIndex);
                                
                                // Color based on change history
                                FormattedValueDMData oldData = (FormattedValueDMData) VMCacheManager.getVMCacheManager()
                                    .getCache(VariableLayoutNode.this.getVMProvider().getPresentationContext())
                                    .getArchivedModelData(valueDmc);
                                if (oldData != null && !oldData.getFormattedValue().equals(getData().getFormattedValue())) {
                                    update.setBackground(
                                        DebugUIPlugin.getPreferenceColor(
                                            IInternalDebugUIConstants.PREF_CHANGED_VALUE_BACKGROUND).getRGB(),
                                        labelIndex);
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
    
    public CellEditor getCellEditor(IPresentationContext context, String columnId, Object element, Composite parent) {
        if (IDebugVMConstants.COLUMN_ID__VALUE.equals(columnId)) {
            return new TextCellEditor(parent);
        }
        else if (IDebugVMConstants.COLUMN_ID__EXPRESSION.equals(columnId)) {
            return new TextCellEditor(parent);
        } 

        return null;
    }

    public ICellModifier getCellModifier(IPresentationContext context, Object element) {
        return new VariableLayoutValueCellModifier(fFormattedPrefStore, fSyncVariableDataAccess);
    }
    
    @Override
    protected void getElementForExpressionPart(IChildrenUpdate update, String expressionPartText, DataRequestMonitor<Object> rm) {
        /*
         * Create a valid DMC for this entered expression.
         */
        final IFrameDMContext frameDmc = findDmcInPath(update.getElementPath(), IFrameDMContext.class);
        final IExpressions expressionService = getServicesTracker().getService(IExpressions.class);

        IExpressionDMContext expressionDMC = expressionService.createExpression(frameDmc, expressionPartText);
        
        /*
         *  Now create the valid VMC which wrappers it.
         */
        IVMContext vmc = createVMContext(expressionDMC);
        rm.setData(vmc);
        rm.done();
    }
    
    @Override
    protected void associateExpression(Object element, IExpression expression) {
        if (element instanceof VariableExpressionVMC) {
            ((VariableExpressionVMC)element).setExpression(expression);
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
    protected void testContextForExpression(Object element, String expression, DataRequestMonitor<Boolean> rm) {
        /*
         * Since we are overriding "getElementForExpression" we do not need to do anything here. But
         * we are forced to supply this routine because it is abstract in the extending class.
         */
    }

    public int getExpressionLength(String expression) {
        /*
         *  Since we are overriding "getElementForExpression" we do not need to do anything here.
         *  We just assume the entire expression is for us.
         */
        return expression.length() ;
    }

    @Override
    protected void updateElementsInSessionThread(final IChildrenUpdate update) {
        // Get the data model context object for the current node in the hierarchy.
        
        final IExpressionDMContext expressionDMC = findDmcInPath(update.getElementPath(), IExpressionDMContext.class);
        
        if ( expressionDMC != null ) {
            getSubexpressionsUpdateElementsInSessionThread( update );
        }
        else {
            getLocalsUpdateElementsInSessionThread( update );
        }
    }
    
    private void getSubexpressionsUpdateElementsInSessionThread(final IChildrenUpdate update) {

        final IExpressionDMContext expressionDMC = findDmcInPath(update.getElementPath(), IExpressionDMContext.class);
        
        if ( expressionDMC != null ) {

            // Get the services we need to use.
            
            final IExpressions expressionService = getServicesTracker().getService(IExpressions.class);
            
            if (expressionService == null) {
                handleFailedUpdate(update);
                return;
            }

            final DsfExecutor dsfExecutor = getSession().getExecutor();
            
            // Call IExpressions.getSubExpressions() to get an Iterable of IExpressionDMContext objects representing
            // the sub-expressions of the expression represented by the current expression node.
            
            final DataRequestMonitor<Iterable<IExpressionDMContext>> rm =
                new DataRequestMonitor<Iterable<IExpressionDMContext>>(dsfExecutor, null) {
                    @Override
                    public void handleCompleted() {
                        if (!getStatus().isOK()) {
                            handleFailedUpdate(update);
                            return;
                        }
                        
                        // Fill the update with the the IExpressionDMContext objects returned by
                        // IExpressions.getSubExpressions().
                        
                        List<IExpressionDMContext> subExpressionDMCList = (List<IExpressionDMContext>)getData();
                        IExpressionDMContext[] subExpressionDMCArray = new IExpressionDMContext[subExpressionDMCList.size()];
                        Iterator<IExpressionDMContext> iter = subExpressionDMCList.iterator();

                        int i = 0;
                        while (iter.hasNext()) {
                            subExpressionDMCArray[i++] = iter.next();
                        }

                        fillUpdateWithVMCs(update, subExpressionDMCArray);
                        update.done();
                    }
            };

            // Make the asynchronous call to IExpressions.getSubExpressions().  The results are processed in the
            // DataRequestMonitor.handleCompleted() above.

            expressionService.getSubExpressions(expressionDMC, rm);
        }
    }
    
    private void getLocalsUpdateElementsInSessionThread(final IChildrenUpdate update) {

        final IFrameDMContext frameDmc = findDmcInPath(update.getElementPath(), IFrameDMContext.class);

        // Get the services we need to use.
        
        final IExpressions expressionService = getServicesTracker().getService(IExpressions.class);
        final IStack stackFrameService = getServicesTracker().getService(IStack.class);
        
        if ( frameDmc == null || expressionService == null || stackFrameService == null) {
            handleFailedUpdate(update);
            return;
        }

        final DsfExecutor dsfExecutor = getSession().getExecutor();
        
        // Call IStack.getLocals() to get an array of IVariableDMContext objects representing the local
        // variables in the stack frame represented by frameDmc.
         
        final DataRequestMonitor<IVariableDMContext[]> rm =
            new DataRequestMonitor<IVariableDMContext[]>(dsfExecutor, null) {
                @Override
                public void handleCompleted() {
                    if (!getStatus().isOK()) {
                        handleFailedUpdate(update);
                        return;
                    }
                    
                    // For each IVariableDMContext object returned by IStack.getLocals(), call
                    // MIStackFrameService.getModelData() to get the IVariableDMData object.  This requires
                    // a MultiRequestMonitor object.
                    
                    // First, get the data model context objects for the local variables.
                    
                    IVariableDMContext[] localsDMCs = getData();
                    
                    if (localsDMCs == null) {
                        handleFailedUpdate(update);
                        return;
                    }
                    
                    if ( localsDMCs.length == 0 ) {
                        // There are no locals so just complete the request
                        update.done();
                        return;
                    }
                    
                    // Create a List in which we store the DM data objects for the local variables.  This is
                    // necessary because there is no MultiDataRequestMonitor. :)
                    
                    final List<IVariableDMData> localsDMData = new ArrayList<IVariableDMData>();
                    
                    // Create the MultiRequestMonitor to handle completion of the set of getModelData() calls.
                    
                    final MultiRequestMonitor<DataRequestMonitor<IVariableDMData>> mrm =
                        new MultiRequestMonitor<DataRequestMonitor<IVariableDMData>>(dsfExecutor, null) {
                            @Override
                            public void handleCompleted() {
                                // Now that all the calls to getModelData() are complete, we create an
                                // IExpressionDMContext object for each local variable name, saving them all
                                // in an array.

                                if (!getStatus().isOK()) {
                                    handleFailedUpdate(update);
                                    return;
                                }
         
                                IExpressionDMContext[] expressionDMCs = new IExpressionDMContext[localsDMData.size()];
                                
                                int i = 0;
                                
                                for (IVariableDMData localDMData : localsDMData) {
                                    expressionDMCs[i++] = expressionService.createExpression(frameDmc, localDMData.getName());
                                }

                                // Lastly, we fill the update from the array of view model context objects
                                // that reference the ExpressionDMC objects for the local variables.  This is
                                // the last code to run for a given call to updateElementsInSessionThread().
                                // We can now leave anonymous-inner-class hell.

                                fillUpdateWithVMCs(update, expressionDMCs);
                                update.done();
                            }
                    };
                    
                    // Perform a set of getModelData() calls, one for each local variable's data model
                    // context object.  In the handleCompleted() method of the DataRequestMonitor, add the
                    // IVariableDMData object to the localsDMData List for later processing (see above).
                    
                    for (IVariableDMContext localDMC : localsDMCs) {
                        DataRequestMonitor<IVariableDMData> rm =
                            new DataRequestMonitor<IVariableDMData>(dsfExecutor, null) {
                                @Override
                                public void handleCompleted() {
                                    localsDMData.add(getData());
                                    mrm.requestMonitorDone(this);
                                }
                        };
                        
                        mrm.add(rm);
                        
                        VMCacheManager.getVMCacheManager().getCache(VariableLayoutNode.this.getVMProvider().getPresentationContext())
                            .getModelData(stackFrameService, localDMC, rm, getExecutor());
                    }
                }
        };

        // Make the asynchronous call to IStack.getLocals().  The results are processed in the
        // DataRequestMonitor.handleCompleted() above.

        stackFrameService.getLocals(frameDmc, rm);
    }
}
