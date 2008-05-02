/*******************************************************************************
 * Copyright (c) 2006 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson			  - Initial API and implementation
 *******************************************************************************/

package org.eclipse.dd.gdb.internal.ui.viewmodel.launch;


import java.util.concurrent.RejectedExecutionException;

import org.eclipse.dd.dsf.concurrent.DataRequestMonitor;
import org.eclipse.dd.dsf.concurrent.DsfRunnable;
import org.eclipse.dd.dsf.concurrent.RequestMonitor;
import org.eclipse.dd.dsf.datamodel.DMContexts;
import org.eclipse.dd.dsf.datamodel.IDMEvent;
import org.eclipse.dd.dsf.debug.service.IRunControl;
import org.eclipse.dd.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.dd.dsf.debug.service.IRunControl.IExitedDMEvent;
import org.eclipse.dd.dsf.debug.service.IRunControl.IStartedDMEvent;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.dd.dsf.ui.viewmodel.VMDelta;
import org.eclipse.dd.dsf.ui.viewmodel.datamodel.AbstractDMVMNode;
import org.eclipse.dd.dsf.ui.viewmodel.datamodel.AbstractDMVMProvider;
import org.eclipse.dd.gdb.internal.provisional.service.GDBRunControl;
import org.eclipse.dd.gdb.internal.provisional.service.GDBRunControl.GDBProcessData;
import org.eclipse.dd.gdb.internal.provisional.service.command.GDBControl;
import org.eclipse.dd.gdb.internal.provisional.service.command.GDBControlDMContext;
import org.eclipse.dd.gdb.internal.provisional.service.command.GDBControl.StartedEvent;
import org.eclipse.dd.mi.service.command.AbstractMIControl;
import org.eclipse.dd.mi.service.command.MIControlDMContext;
import org.eclipse.dd.mi.service.command.events.MIInferiorExitEvent;
import org.eclipse.dd.mi.service.command.events.MIInferiorSignalExitEvent;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementLabelProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;

@SuppressWarnings("restriction")
public class ContainerVMNode extends AbstractDMVMNode 
    implements IElementLabelProvider
{

	public ContainerVMNode(AbstractDMVMProvider provider, DsfSession session) {
        super(provider, session, IRunControl.IExecutionDMContext.class);
	}

	@Override
	protected void updateElementsInSessionThread(IChildrenUpdate update) {
      
      AbstractMIControl controlService = getServicesTracker().getService(AbstractMIControl.class);
      if ( controlService == null ) {
              handleFailedUpdate(update);
              return;
      }
      
      MIControlDMContext containerCtx = controlService.getControlDMContext();
      update.setChild(createVMContext(containerCtx), 0); 
      update.done();
	}

	
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
	
	protected void updateLabelInSessionThread(ILabelUpdate[] updates) {
        for (final ILabelUpdate update : updates) {
        	final GDBRunControl runControl = getServicesTracker().getService(GDBRunControl.class);
            if ( runControl == null ) {
                handleFailedUpdate(update);
                continue;
            }
            
            final GDBControlDMContext dmc = findDmcInPath(update.getViewerInput(), update.getElementPath(), GDBControlDMContext.class);

            String imageKey = null;
            if (runControl.isSuspended(dmc)) {
                imageKey = IDebugUIConstants.IMG_OBJS_THREAD_SUSPENDED;
            } else {
                imageKey = IDebugUIConstants.IMG_OBJS_THREAD_RUNNING;
            }
            update.setImageDescriptor(DebugUITools.getImageDescriptor(imageKey), 0);
            
            runControl.getProcessData(
                dmc, 
                new DataRequestMonitor<GDBProcessData>(getExecutor(), null) { 
					@Override
                    public void handleCompleted() {
                        if (!isSuccess()) {
                            update.done();
                            return;
                        }
                        update.setLabel(getData().getName(), 0);
                        update.done();
                    }
                });
        }
    }

    public int getDeltaFlags(Object e) {
        if(e instanceof IStartedDMEvent || e instanceof IExitedDMEvent) {
            return IModelDelta.CONTENT;
        } else if(e instanceof IRunControl.IContainerResumedDMEvent || 
                  e instanceof IRunControl.IContainerSuspendedDMEvent) 
        {
            return IModelDelta.CONTENT;
        } else if (e instanceof GDBControl.ExitedEvent || 
                   e instanceof MIInferiorExitEvent || 
                   e instanceof MIInferiorSignalExitEvent) 
        {
            return IModelDelta.CONTENT;
        }
        if (e instanceof StartedEvent) {
            return IModelDelta.EXPAND | IModelDelta.SELECT;
        }
        return IModelDelta.NO_CHANGE;
    }

    public void buildDelta(Object e, final VMDelta parentDelta, final int nodeOffset, final RequestMonitor requestMonitor) {
    	if(e instanceof IRunControl.IContainerResumedDMEvent || 
    	   e instanceof IRunControl.IContainerSuspendedDMEvent) 
    	{
            parentDelta.addNode(createVMContext(((IDMEvent<?>)e).getDMContext()), IModelDelta.CONTENT);
        } else if (e instanceof IStartedDMEvent || e instanceof IExitedDMEvent) {
            IContainerDMContext containerCtx = DMContexts.getAncestorOfType(
                ((IDMEvent<?>)e).getDMContext(), IContainerDMContext.class);
            if (containerCtx != null) {
                parentDelta.addNode(createVMContext(containerCtx), IModelDelta.CONTENT);
            }
        } else if (e instanceof GDBControl.ExitedEvent || 
            e instanceof MIInferiorExitEvent || 
            e instanceof MIInferiorSignalExitEvent) 
        {
            parentDelta.addNode(createVMContext(((IDMEvent<?>)e).getDMContext()), IModelDelta.CONTENT);
        } 
        if (e instanceof StartedEvent) {
            parentDelta.addNode(createVMContext(((IDMEvent<?>)e).getDMContext()), IModelDelta.EXPAND | IModelDelta.SELECT);
        }

    	requestMonitor.done();
  	 }
}
