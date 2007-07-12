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
package org.eclipse.dd.dsf.debug.ui.viewmodel.variable;

import java.util.Iterator;
import java.util.List;

import org.eclipse.dd.dsf.concurrent.DataRequestMonitor;
import org.eclipse.dd.dsf.concurrent.DsfExecutor;
import org.eclipse.dd.dsf.concurrent.RequestMonitor;
import org.eclipse.dd.dsf.datamodel.IDMEvent;
import org.eclipse.dd.dsf.datamodel.IDMService;
import org.eclipse.dd.dsf.debug.service.IExpressions;
import org.eclipse.dd.dsf.debug.service.IFormattedValues;
import org.eclipse.dd.dsf.debug.service.IRunControl;
import org.eclipse.dd.dsf.debug.service.IExpressions.IExpressionDMContext;
import org.eclipse.dd.dsf.debug.service.IExpressions.IExpressionDMData;
import org.eclipse.dd.dsf.debug.service.IFormattedValues.FormattedValueDMContext;
import org.eclipse.dd.dsf.debug.service.IFormattedValues.FormattedValueDMData;
import org.eclipse.dd.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.dd.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.dd.dsf.debug.ui.viewmodel.IDebugVMConstants;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.dd.dsf.service.IDsfService;
import org.eclipse.dd.dsf.ui.viewmodel.AbstractVMProvider;
import org.eclipse.dd.dsf.ui.viewmodel.IVMLayoutNode;
import org.eclipse.dd.dsf.ui.viewmodel.VMDelta;
import org.eclipse.dd.dsf.ui.viewmodel.dm.AbstractDMVMLayoutNode;
import org.eclipse.dd.dsf.ui.viewmodel.update.VMCacheManager;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;

@SuppressWarnings("restriction")
public class VariableSubExpressionsLayoutNode extends AbstractDMVMLayoutNode<IExpressionDMData> {
    
    @SuppressWarnings("unused")
    private SyncVariableDataAccess fSyncVariableDataAccess;

    public VariableSubExpressionsLayoutNode(AbstractVMProvider provider, DsfSession session, SyncVariableDataAccess syncVariableDataAccess) {
        super(provider, session, IExpressions.IExpressionDMContext.class);
        fSyncVariableDataAccess = syncVariableDataAccess;
    }
    
    /** 
     * List of child nodes containing only a reference to this.  This is what enables the view model
     * provider to know about the recursive nature of subexpression nodes.
     */
    private final IVMLayoutNode[] fChildLayoutNodes = { this };
    
    @Override
    public IVMLayoutNode[] getChildLayoutNodes() {
        return fChildLayoutNodes;
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
                            handleFailedUpdate(update);
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
                                update.setLabel("", idx); //$NON-NLS-1$
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
         *  PREFPAGE : We are using a default format until the preference page is created
         *  
         *  First select the format to be used. This involves checking so see that the preference
         *  page format is supported by the register service. If the format is not supported then 
         *  we will pick the first available format.
         */
        
        final String preferencePageFormatId = IFormattedValues.NATURAL_FORMAT;
        
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
                    FormattedValueDMContext valueDmc = expressionService.getFormattedValue(dmc, finalFormatId);
                    
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

                                /*
                                 *  Fill the label/column with the properly formatted data value.
                                 */
                                update.setLabel(getData().getFormattedValue(), labelIndex);
                                update.done();
                            }
                        },
                        getExecutor()
                    );
                }
            }
        );
    }
    
    @Override
    protected void updateElementsInSessionThread(final IChildrenUpdate update) {
        // Get the data model context object for the current node in the hierarchy.
        
        final IExpressionDMContext expressionDMC = findDmcInPath(update.getElementPath(), IExpressionDMContext.class);

        // ISSUE: Do we need to explicitly get the IExecutionDMContext and ISymbolDMContext since they
        // should be in the parent chain of the IFrameDMContext object?
        
        final IExecutionDMContext execDmc = findDmcInPath(update.getElementPath(), IExecutionDMContext.class);
        final IFrameDMContext frameDmc = findDmcInPath(update.getElementPath(), IFrameDMContext.class);
        //final ISymbolDMContext symbolDmc = findDmcInPath(update.getElementPath(), ISymbolDMContext.class);

        // Get the services we need to use.
        
        final IExpressions expressionService = getServicesTracker().getService(IExpressions.class);
        
        if (execDmc == null || frameDmc == null || expressionService == null) {
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
    
    @Override
    protected int getNodeDeltaFlagsForDMEvent(IDMEvent<?> e) {
        if (e instanceof IRunControl.ISuspendedDMEvent) {
            return IModelDelta.CONTENT;
        } 
        else if (e instanceof IExpressions.IExpressionChangedDMEvent) {
            /*
             * Flush the cache.
             */
            VMCacheManager.getVMCacheManager().flush(super.getVMProvider().getPresentationContext());
            
            /*
             *  Logically one would think that STATE should be specified here. But we specifiy CONTENT
             *  as well so that if there sub expressions which are affected in some way ( such as with
             *  an expanded union then they will show the changes also.
             */
            return IModelDelta.CONTENT | IModelDelta.STATE;
        }
        
        return IModelDelta.NO_CHANGE;
    }

    @Override
    protected void buildDeltaForDMEvent(IDMEvent<?> e, VMDelta parent, int nodeOffset, RequestMonitor requestMonitor) {
        if (e instanceof IRunControl.ISuspendedDMEvent) {
            // Create a delta that the whole register group has changed.
            parent.addFlags(IModelDelta.CONTENT);
        } 
        else if (e instanceof IExpressions.IExpressionChangedDMEvent) {
            parent.addNode( createVMContext(((IExpressions.IExpressionChangedDMEvent)e).getDMContext()), IModelDelta.CONTENT | IModelDelta.STATE );
        }
        
        super.buildDeltaForDMEvent(e, parent, nodeOffset, requestMonitor);
    }
}
