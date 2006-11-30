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

import org.eclipse.dd.dsf.concurrent.Done;
import org.eclipse.dd.dsf.concurrent.GetDataDone;
import org.eclipse.dd.dsf.datamodel.IDMContext;
import org.eclipse.dd.dsf.datamodel.IDMEvent;
import org.eclipse.dd.dsf.debug.service.IRegisters;
import org.eclipse.dd.dsf.debug.service.IRunControl;
import org.eclipse.dd.dsf.debug.service.IRegisters.IRegisterDMContext;
import org.eclipse.dd.dsf.debug.service.IRegisters.IRegisterDMData;
import org.eclipse.dd.dsf.debug.service.IRegisters.IRegisterGroupDMContext;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.dd.dsf.ui.viewmodel.AbstractVMProvider;
import org.eclipse.dd.dsf.ui.viewmodel.IVMContext;
import org.eclipse.dd.dsf.ui.viewmodel.VMDelta;
import org.eclipse.dd.dsf.ui.viewmodel.dm.AbstractDMVMLayoutNode;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;

@SuppressWarnings("restriction")
public class RegisterLayoutNode extends AbstractDMVMLayoutNode<IRegisterDMData> {

    public IVMContext[] fCachedRegisterVMCs;
    
    public RegisterLayoutNode(AbstractVMProvider provider, DsfSession session) {
        super(provider, session, IRegisters.IRegisterDMContext.class);
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
            new GetDataDone<IRegisterDMContext[]>() { 
                public void run() {
                    if (!getStatus().isOK()) {
                        handleFailedUpdate(update);
                    }
                    fillUpdateWithVMCs(update, getData());
                    update.done();
                }
            });            
    }
    
    @Override
    protected void fillColumnLabel(IDMContext<IRegisterDMData> dmContext, IRegisterDMData dmData, String columnId,
                                   int idx, ILabelUpdate update) 
    {
        if (RegisterColumnPresentation.COL_NAME.equals(columnId)) {
            update.setLabel(dmData.getName(), idx);
        } else if (RegisterColumnPresentation.COL_VALUE.equals(columnId)) {
            update.setLabel(dmData.getHexValue(), idx); 
        } else if (RegisterColumnPresentation.COL_DESCRIPTION.equals(columnId)) {
            String size = dmData.getDescription();
            String value = dmData.getHexValue();
            if ("".equals(size)) { //$NON-NLS-1$
                if ( value.contains( "uint64" ) ) {          //$NON-NLS-1$
                    size = "64 bit register" ;               //$NON-NLS-1$
                } else if ( value.contains( "v4_float" ) ) { //$NON-NLS-1$
                    size = "128 bit register" ;              //$NON-NLS-1$
                }
            }
            update.setLabel(size, idx);
        }
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
    protected void buildDeltaForDMEvent(IDMEvent<?> e, VMDelta parent, int nodeOffset, Done done) {
        if (e instanceof IRunControl.ISuspendedDMEvent) {
            // Create a delta that the whole register group has changed.
            parent.addFlags(IModelDelta.CONTENT);
        } 
        if (e instanceof IRegisters.IRegisterChangedDMEvent) {
            parent.addNode( new DMVMContext(((IRegisters.IRegisterChangedDMEvent)e).getDMContext()), IModelDelta.STATE );
        } 
        super.buildDeltaForDMEvent(e, parent, nodeOffset, done);
    }
}
