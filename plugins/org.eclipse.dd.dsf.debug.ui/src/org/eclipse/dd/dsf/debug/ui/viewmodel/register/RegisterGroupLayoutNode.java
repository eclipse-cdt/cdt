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
import org.eclipse.dd.dsf.datamodel.IDMContext;
import org.eclipse.dd.dsf.debug.service.IRegisters;
import org.eclipse.dd.dsf.debug.service.IRegisters.IRegisterGroupDMContext;
import org.eclipse.dd.dsf.debug.service.IRegisters.IRegisterGroupDMData;
import org.eclipse.dd.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.dd.dsf.ui.viewmodel.AbstractVMProvider;
import org.eclipse.dd.dsf.ui.viewmodel.dm.AbstractDMVMLayoutNode;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;

@SuppressWarnings("restriction")
public class RegisterGroupLayoutNode extends AbstractDMVMLayoutNode<IRegisterGroupDMData> {

    public RegisterGroupLayoutNode(AbstractVMProvider provider, DsfSession session) {
        super(provider, session, IRegisters.IRegisterGroupDMContext.class);
    }
    
    @Override
    protected void updateElementsInSessionThread(final IChildrenUpdate update) {
        if (!checkService(IRegisters.class, null, update)) return;
        
        final IExecutionDMContext execDmc = findDmcInPath(update.getElementPath(), IExecutionDMContext.class) ;
        
        if (execDmc != null) {
            getServicesTracker().getService(IRegisters.class).getRegisterGroups(
                execDmc, null,
                new DataRequestMonitor<IRegisterGroupDMContext[]>(getSession().getExecutor(), null) { 
                    @Override
                    public void handleCompleted() {
                        if (!getStatus().isOK()) {
                            update.done();
                            return;
                        }
                        fillUpdateWithVMCs(update, getData());
                        update.done();
                    }}); 
        } else {
            handleFailedUpdate(update);
        }          
        
    }
    
    @Override
    protected void fillColumnLabel(IDMContext<IRegisterGroupDMData> dmContext, IRegisterGroupDMData dmData,
                                   String columnId, int idx, ILabelUpdate update) 
    {
        if (RegisterColumnPresentation.COL_NAME.equals(columnId)) {
            update.setLabel(dmData.getName(), idx);
        } else if (RegisterColumnPresentation.COL_VALUE.equals(columnId)) {
            update.setLabel("", idx); //$NON-NLS-1$
        } else if (RegisterColumnPresentation.COL_DESCRIPTION.equals(columnId)) {
            update.setLabel(dmData.getDescription(), idx);
        }
    }
}
