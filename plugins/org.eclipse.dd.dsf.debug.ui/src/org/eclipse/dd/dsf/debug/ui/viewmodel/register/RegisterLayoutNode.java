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

import org.eclipse.dd.dsf.concurrent.DataRequestMonitor;
import org.eclipse.dd.dsf.concurrent.RequestMonitor;
import org.eclipse.dd.dsf.datamodel.IDMEvent;
import org.eclipse.dd.dsf.datamodel.IDMService;
import org.eclipse.dd.dsf.debug.service.IFormattedValues;
import org.eclipse.dd.dsf.debug.service.IRegisters;
import org.eclipse.dd.dsf.debug.service.IRunControl;
import org.eclipse.dd.dsf.debug.service.IFormattedValues.FormattedValueDMContext;
import org.eclipse.dd.dsf.debug.service.IFormattedValues.FormattedValueDMData;
import org.eclipse.dd.dsf.debug.service.IRegisters.IRegisterChangedDMEvent;
import org.eclipse.dd.dsf.debug.service.IRegisters.IRegisterDMContext;
import org.eclipse.dd.dsf.debug.service.IRegisters.IRegisterDMData;
import org.eclipse.dd.dsf.debug.service.IRegisters.IRegisterGroupDMContext;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.dd.dsf.service.IDsfService;
import org.eclipse.dd.dsf.ui.viewmodel.AbstractVMProvider;
import org.eclipse.dd.dsf.ui.viewmodel.VMDelta;
import org.eclipse.dd.dsf.ui.viewmodel.dm.AbstractDMVMLayoutNode;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;

@SuppressWarnings("restriction")
public class RegisterLayoutNode extends AbstractDMVMLayoutNode<IRegisterDMData> {

    public RegisterLayoutNode(AbstractVMProvider provider, DsfSession session) {
        super(provider, session, IRegisters.IRegisterDMContext.class);
    }
    
    /**
     *  Private data access routine which performs the extra level of data access needed to
     *  get the formatted data value for a specific register.
     */
    private void updateFormattedRegisterValue(final ILabelUpdate update, final int labelIndex, final IRegisterDMContext dmc)
    {
        final IRegisters regService = getServicesTracker().getService(IRegisters.class);
        /*
         *  PREFPAGE : We are using a default format until the preference page is created
         *  
         *  First select the format to be used. This involves checking so see that the preference
         *  page format is supported by the register service. If the format is not supported then 
         *  we will pick the first available format.
         */
        
        final String preferencePageFormatId = IFormattedValues.HEX_FORMAT;
        
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
                    
                    regService.getModelData(
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
                        }
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
            
            final IRegisterDMContext dmc = findDmcInPath(update.getElementPath(), IRegisters.IRegisterDMContext.class);
            
            ((IDMService)getServicesTracker().getService(null, dmc.getServiceFilter())).getModelData(
                dmc, 
                new DataRequestMonitor<IRegisterDMData>(getSession().getExecutor(), null) { 
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
                        if (localColumns == null) localColumns = new String[] { null };
                        
                        boolean weAreExtractingFormattedData = false;
                        
                        for (int idx = 0; idx < localColumns.length; idx++) {
                            if (RegisterColumnPresentation.COL_NAME.equals(localColumns[idx])) {
                                update.setLabel(getData().getName(), idx);
                            } else if (RegisterColumnPresentation.COL_VALUE.equals(localColumns[idx])) {
                                weAreExtractingFormattedData = true;
                            } else if (RegisterColumnPresentation.COL_DESCRIPTION.equals(localColumns[idx])) {
                                update.setLabel(getData().getDescription(), idx);
                            }
                        }
                        
                        if ( ! weAreExtractingFormattedData ) {
                            update.done();
                        } else {
                            for (int idx = 0; idx < localColumns.length; idx++) {
                                if (RegisterColumnPresentation.COL_VALUE.equals(localColumns[idx])) {
                                    updateFormattedRegisterValue(update, idx, dmc);
                                }
                            }
                        }
                    }
                }
            );
        }
    }

    @Override
    protected void updateElementsInSessionThread(final IChildrenUpdate update) {
        final IRegisterGroupDMContext execDmc = findDmcInPath(update.getElementPath(), IRegisterGroupDMContext.class);

        if (execDmc == null) {
            handleFailedUpdate(update);
            return;
        }          
        
        getServicesTracker().getService(IRegisters.class).getRegisters(
            execDmc,
            new DataRequestMonitor<IRegisterDMContext[]>(getSession().getExecutor(), null) { 
                @Override
                public void handleCompleted() {
                    if (!getStatus().isOK()) {
                        handleFailedUpdate(update);
                        return;
                    }
                    fillUpdateWithVMCs(update, getData());
                    update.done();
                }
            });            
    }
    
    @Override
    protected int getNodeDeltaFlagsForDMEvent(IDMEvent<?> e) {
        if (e instanceof IRunControl.ISuspendedDMEvent) {
            return IModelDelta.CONTENT;
        } else if (e instanceof IRegisters.IRegisterChangedDMEvent) {
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
        if (e instanceof IRegisters.IRegisterChangedDMEvent) {
            parent.addNode( new DMVMContext(((IRegisterChangedDMEvent)e).getDMContext()), IModelDelta.STATE );
        } 
        
        super.buildDeltaForDMEvent(e, parent, nodeOffset, rm);
    }
}
