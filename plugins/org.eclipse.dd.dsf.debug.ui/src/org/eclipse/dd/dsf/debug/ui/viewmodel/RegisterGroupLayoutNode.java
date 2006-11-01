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
package org.eclipse.dd.dsf.debug.ui.viewmodel;

import org.eclipse.dd.dsf.concurrent.Done;
import org.eclipse.dd.dsf.concurrent.GetDataDone;
import org.eclipse.dd.dsf.datamodel.IDMEvent;
import org.eclipse.dd.dsf.debug.service.IRegisters;
import org.eclipse.dd.dsf.debug.service.IRegisters.IRegisterGroupDMContext;
import org.eclipse.dd.dsf.debug.service.IRegisters.IRegisterGroupData;
import org.eclipse.dd.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.dd.dsf.service.IDsfService;
import org.eclipse.dd.dsf.ui.viewmodel.DMContextVMLayoutNode;
import org.eclipse.dd.dsf.ui.viewmodel.IVMContext;
import org.eclipse.dd.dsf.ui.viewmodel.VMDelta;
import org.eclipse.debug.internal.ui.viewers.provisional.ILabelRequestMonitor;

@SuppressWarnings("restriction")
public class RegisterGroupLayoutNode extends DMContextVMLayoutNode {

    public RegisterGroupLayoutNode(DsfSession session) {
        super(session, IRegisters.IRegisterGroupDMContext.class);
    }
    
    public void hasElements( final IVMContext parentVmc , final GetDataDone<Boolean> done ) {
        final IExecutionDMContext execDmc = findDmcInVmc(parentVmc, IExecutionDMContext.class);

        if (execDmc == null || getServicesTracker().getService(IRegisters.class) == null) {
            done.setData(Boolean.FALSE);
            getExecutor().execute(done);
            return;
        }          
        
        getServicesTracker().getService(IRegisters.class).getRegisterGroups(
            execDmc, null,
            new GetDataDone<IRegisterGroupDMContext[]>() { public void run() {
                if (propagateError(getExecutor(), done, "Failed to retrieve register groups")) return;
                done.setData(getData().length != 0);
                getExecutor().execute(done);
            }});
    }

    public void getElements( final IVMContext parentVmc, final GetDataDone<IVMContext[]> done ) {
        final IExecutionDMContext execDmc = findDmcInVmc(parentVmc, IExecutionDMContext.class) ;
        
        if ( execDmc == null || getServicesTracker().getService( IRegisters.class ) == null ) {
            done.setData(new IVMContext[0]);
            getExecutor().execute( done );
            return;
        }          
        
        getServicesTracker().getService(IRegisters.class).getRegisterGroups(
            execDmc, null,
            new GetDataDone<IRegisterGroupDMContext[]>() { public void run() {
                if (propagateError(getExecutor(), done, "Failed to retrieve register groups")) return;
            	done.setData(dmcs2vmcs(parentVmc, getData()));
                getExecutor().execute(done);
            }}); 
    }
    
    public void retrieveLabel( final IVMContext vmc , final ILabelRequestMonitor result ) {
        if (getServicesTracker().getService(IRegisters.class) == null) {
            result.done();
            return;
        }          
        
        final IRegisterGroupDMContext registerGroupDmc = (IRegisterGroupDMContext) ( (DMContextVMContext) vmc ).getDMC() ;
        
        getServicesTracker().getService( IRegisters.class ).getModelData(
            registerGroupDmc, 
            new GetDataDone<IRegisterGroupData>() { 
                public void run() {
                    if (!getStatus().isOK()) {
                        // Some error conditions are expected.
                        assert getStatus().getCode() == IDsfService.INVALID_STATE || getStatus().getCode() == IDsfService.INVALID_HANDLE : getStatus().toString(); 
                        result.setLabels( new String[] { "...", "...", "..." } ) ;
                    } else {
                        result.setLabels(new String[] { getData().getName(), "", getData().getDescription() }); //$NON-NLS-1$
                    }
                    result.done();
                    return;
                }
            }
        ) ;
    }
    
    public boolean hasDeltaFlagsForDMEvent(IDMEvent e) {
        return super.hasDeltaFlagsForDMEvent(e);
    }

    public void buildDeltaForDMEvent(final IDMEvent e, final VMDelta parent, final Done done) {
        super.buildDeltaForDMEvent(e, parent, done);
    }
}
