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

import org.eclipse.dd.dsf.concurrent.DataRequestMonitor;
import org.eclipse.dd.dsf.concurrent.RequestMonitor;
import org.eclipse.dd.dsf.datamodel.IDMContext;
import org.eclipse.dd.dsf.datamodel.IDMEvent;
import org.eclipse.dd.dsf.debug.service.IRegisters;
import org.eclipse.dd.dsf.debug.service.IRunControl;
import org.eclipse.dd.dsf.debug.service.IStack;
import org.eclipse.dd.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.dd.dsf.debug.service.IStack.IVariableDMContext;
import org.eclipse.dd.dsf.debug.service.IStack.IVariableDMData;
import org.eclipse.dd.dsf.debug.ui.viewmodel.IDebugVMConstants;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.dd.dsf.ui.viewmodel.AbstractVMProvider;
import org.eclipse.dd.dsf.ui.viewmodel.VMDelta;
import org.eclipse.dd.dsf.ui.viewmodel.dm.AbstractDMVMLayoutNode;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;

@SuppressWarnings("restriction")
public class LocalsLayoutNode extends AbstractDMVMLayoutNode<IVariableDMData> {

    public LocalsLayoutNode(AbstractVMProvider provider, DsfSession session) {
        super(provider, session, IStack.IVariableDMContext.class);
    }
    
    @Override
    protected void updateElementsInSessionThread(final IChildrenUpdate update) {
        if (!checkService(IRegisters.class, null, update)) return;
        
        final IFrameDMContext frameDmc = findDmcInPath(update.getElementPath(), IFrameDMContext.class) ;
        
        if (frameDmc != null) {
            getServicesTracker().getService(IStack.class).getArguments(
                frameDmc,
                new DataRequestMonitor<IVariableDMContext[]>(getSession().getExecutor(), null) { 
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
    protected void fillColumnLabel(IDMContext<IVariableDMData> dmContext, IVariableDMData dmData,
                                   String columnId, int idx, ILabelUpdate update) 
    {
        if (IDebugVMConstants.COLUMN_ID__NAME.equals(columnId)) {
            update.setLabel(dmData.getName(), idx);
        } else if (IDebugVMConstants.COLUMN_ID__VALUE.equals(columnId)) {
            update.setLabel(dmData.getValue(), idx);
        } else {
            update.setLabel("", idx); //$NON-NLS-1$
        }
    }
    
    @Override
    protected int getNodeDeltaFlagsForDMEvent(IDMEvent<?> e) {
        if (e instanceof IRunControl.ISuspendedDMEvent) {
            return IModelDelta.CONTENT;
        }
        return IModelDelta.NO_CHANGE;
    }

    @Override
    protected void buildDeltaForDMEvent(IDMEvent<?> e, VMDelta parent, int nodeOffset, RequestMonitor rm) {
        if (e instanceof IRunControl.ISuspendedDMEvent) {
            // Create a delta that the whole register group has changed.
            parent.addFlags(IModelDelta.CONTENT);
        } 
        
        super.buildDeltaForDMEvent(e, parent, nodeOffset, rm);
    }
}
