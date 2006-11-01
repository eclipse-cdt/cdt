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
import org.eclipse.dd.dsf.debug.service.IRunControl;
import org.eclipse.dd.dsf.debug.service.IRegisters.IRegisterDMContext;
import org.eclipse.dd.dsf.debug.service.IRegisters.IRegisterDMData;
import org.eclipse.dd.dsf.debug.service.IRegisters.IRegisterGroupDMContext;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.dd.dsf.service.IDsfService;
import org.eclipse.dd.dsf.ui.viewmodel.DMContextVMLayoutNode;
import org.eclipse.dd.dsf.ui.viewmodel.IVMContext;
import org.eclipse.dd.dsf.ui.viewmodel.VMDelta;
import org.eclipse.debug.internal.ui.viewers.provisional.ILabelRequestMonitor;
import org.eclipse.debug.internal.ui.viewers.provisional.IModelDelta;

@SuppressWarnings("restriction")
public class RegisterLayoutNode extends DMContextVMLayoutNode {

    public IVMContext[] fCachedRegisterVMCs;
    
    public RegisterLayoutNode(DsfSession session) {
        super(session, IRegisters.IRegisterDMContext.class);
    }
    
    public void hasElements( final IVMContext parentVmc , final GetDataDone<Boolean> done ) {
        final IRegisterGroupDMContext registerGroupDmc = findDmcInVmc(parentVmc, IRegisterGroupDMContext.class);

        if (registerGroupDmc == null || getServicesTracker().getService(IRegisters.class) == null) {
            done.setData(Boolean.FALSE);
            getExecutor().execute(done);
            return;
        }          
        
        getServicesTracker().getService( IRegisters.class ).getRegisters(
            registerGroupDmc,
            new GetDataDone<IRegisterDMContext[]>() { 
                public void run() {
                    if (propagateError(getExecutor(), done, "Failed to retrieve registers")) return;
                    done.setData(getData().length != 0);
                    getExecutor().execute(done);
                }
            });            
    }

    public void getElements( final IVMContext parentVmc , final GetDataDone<IVMContext[]> done ) {
        final IRegisterGroupDMContext execDmc = findDmcInVmc(parentVmc, IRegisterGroupDMContext.class);

        if (execDmc == null || getServicesTracker().getService(IRegisters.class) == null) {
            getExecutor().execute(done);
            return;
        }          
        
        getServicesTracker().getService( IRegisters.class ).getRegisters(
            execDmc,
            new GetDataDone<IRegisterDMContext[]>() { public void run() {
                if (propagateError(getExecutor(), done, "Failed to retrieve registers")) return;
            	done.setData( dmcs2vmcs( parentVmc, getData()) );
                getExecutor().execute(done);
            }});            
    }
    
    public void retrieveLabel( final IVMContext vmc , final ILabelRequestMonitor result ) {
   
        if ( getServicesTracker().getService( IRegisters.class ) == null ) {
            result.done();
            return;
        }          
        
        final IRegisterDMContext registerDmc = (IRegisterDMContext) ( (DMContextVMContext) vmc ).getDMC() ;
        
        getServicesTracker().getService( IRegisters.class ).getModelData(
            registerDmc , 
            new GetDataDone<IRegisterDMData>() { 
                public void run() {
                    if ( !getStatus().isOK() ) {
                        assert getStatus().getCode() == IDsfService.INVALID_STATE || getStatus().getCode() == IDsfService.INVALID_HANDLE : getStatus().toString(); 
                        // Some error conditions are expected.
                        result.setLabels( new String[] { "...", "...", "..." } ) ;
                    } else {  
                    	String size = getData().getDescription();
                        String value = getData().getHexValue();
                    	if ("".equals(size)) {
                        	if ( value.contains( "uint64" ) ) {          //$NON-NLS-1$
                        		size = "64 bit register" ;               //$NON-NLS-1$
                        	} else if ( value.contains( "v4_float" ) ) { //$NON-NLS-1$
                        		size = "128 bit register" ;              //$NON-NLS-1$
                        	}
                        }
                        
                        result.setLabels(new String[] { getData().getName(), getData().getHexValue(), size });
                    }
                    
                    result.done() ;
                    return ;
                }
            }
        ) ;
    }
    
    public boolean hasDeltaFlagsForDMEvent( IDMEvent e ) {
        return (e instanceof IRunControl.ISuspendedDMEvent) || super.hasDeltaFlagsForDMEvent(e) ;
    }

    public void buildDeltaForDMEvent( final IDMEvent e, final VMDelta parent, final Done done ) {
        if (e instanceof IRunControl.ISuspendedDMEvent) {
            // Create a delta that the whole register group has changed.
            parent.addFlags(IModelDelta.CONTENT);
        } 
        if (e instanceof IRegisters.IRegisterChangedDMEvent) {
            parent.addNode(new DMContextVMContext(parent.getVMC(), e.getDMContext()), IModelDelta.STATE);
        } 

        super.buildDeltaForDMEvent(e, parent, done);
    }
}
