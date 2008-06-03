/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *******************************************************************************/
package org.eclipse.dd.dsf.debug.internal.provisional.ui.viewmodel.launch;

import java.util.concurrent.RejectedExecutionException;

import org.eclipse.dd.dsf.concurrent.DsfRunnable;
import org.eclipse.dd.dsf.concurrent.RequestMonitor;
import org.eclipse.dd.dsf.datamodel.DMContexts;
import org.eclipse.dd.dsf.datamodel.IDMEvent;
import org.eclipse.dd.dsf.debug.service.IRunControl;
import org.eclipse.dd.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.dd.dsf.debug.service.IRunControl.IContainerResumedDMEvent;
import org.eclipse.dd.dsf.debug.service.IRunControl.IContainerSuspendedDMEvent;
import org.eclipse.dd.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.dd.dsf.debug.service.IRunControl.IExitedDMEvent;
import org.eclipse.dd.dsf.debug.service.IRunControl.IStartedDMEvent;
import org.eclipse.dd.dsf.debug.service.StepQueueManager.ISteppingTimedOutEvent;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.dd.dsf.ui.viewmodel.VMDelta;
import org.eclipse.dd.dsf.ui.viewmodel.datamodel.AbstractDMVMNode;
import org.eclipse.dd.dsf.ui.viewmodel.datamodel.AbstractDMVMProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementLabelProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;

/**
 * Abstract implementation of a container view model node.
 * Clients need to implement {@link #updateLabelInSessionThread(ILabelUpdate[])}.
 */
@SuppressWarnings("restriction")
public abstract class AbstractContainerVMNode extends AbstractDMVMNode implements IElementLabelProvider {

	public AbstractContainerVMNode(AbstractDMVMProvider provider, DsfSession session) {
		super(provider, session, IRunControl.IExecutionDMContext.class);
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

    /**
     * Perform the given label updates in the session executor thread.
     * 
     * @param updates  the pending label updates
     * @see {@link #update(ILabelUpdate[])
     */
	protected abstract void updateLabelInSessionThread(ILabelUpdate[] updates);

	public int getDeltaFlags(Object e) {
        if (e instanceof IContainerResumedDMEvent && 
            ((IContainerResumedDMEvent)e).getReason() != IRunControl.StateChangeReason.STEP) 
        {
            return IModelDelta.CONTENT;            
        } else if (e instanceof IContainerSuspendedDMEvent) {
        	// no change, update happens on FullStackRefreshEvent
            return IModelDelta.NO_CHANGE;
        } else if (e instanceof FullStackRefreshEvent) {
        	return IModelDelta.CONTENT;
	    } else if (e instanceof ISteppingTimedOutEvent &&
                   ((ISteppingTimedOutEvent)e).getDMContext() instanceof IContainerDMContext)
	    {
           return IModelDelta.CONTENT;            
	    } else if (e instanceof IExitedDMEvent) {
	        return IModelDelta.CONTENT;
	    } else if (e instanceof IStartedDMEvent) {
	    	if (((IStartedDMEvent) e).getDMContext() instanceof IContainerDMContext) {
	    		return IModelDelta.EXPAND | IModelDelta.SELECT;
	    	} else {
		        return IModelDelta.CONTENT;
	    	}
	    }
	    return IModelDelta.NO_CHANGE;
	}

	public void buildDelta(Object e, final VMDelta parentDelta, final int nodeOffset, final RequestMonitor requestMonitor) {
		if(e instanceof IContainerResumedDMEvent &&
           ((IContainerResumedDMEvent)e).getReason() != IRunControl.StateChangeReason.STEP) 
		{
	        parentDelta.addNode(createVMContext(((IDMEvent<?>)e).getDMContext()), IModelDelta.CONTENT);
		} else if (e instanceof IContainerSuspendedDMEvent) {
        	// do nothing
		} else if (e instanceof FullStackRefreshEvent) {
			parentDelta.addNode(createVMContext(((IDMEvent<?>)e).getDMContext()), IModelDelta.CONTENT);
		} else if (e instanceof ISteppingTimedOutEvent &&
                   ((ISteppingTimedOutEvent)e).getDMContext() instanceof IContainerDMContext)
		{
            parentDelta.addNode(createVMContext(((IDMEvent<?>)e).getDMContext()), IModelDelta.CONTENT);
            // Workaround for bug 233730: we need to add a separate delta node for the state flag in 
            // order to trigger an update of the run control actions.
            parentDelta.addNode(createVMContext(((IDMEvent<?>)e).getDMContext()), IModelDelta.STATE);
		} else if (e instanceof IExitedDMEvent) {
	    	IExecutionDMContext exeContext= ((IExitedDMEvent) e).getDMContext();
			if (exeContext instanceof IContainerDMContext) {
	    		parentDelta.setFlags(parentDelta.getFlags() |  IModelDelta.CONTENT);
	    	} else {
		        IContainerDMContext containerCtx = DMContexts.getAncestorOfType(exeContext, IContainerDMContext.class);
		        if (containerCtx != null) {
		            parentDelta.addNode(createVMContext(containerCtx), IModelDelta.CONTENT);
		        }
	    	}
	    } else if (e instanceof IStartedDMEvent) {
	    	IExecutionDMContext exeContext= ((IStartedDMEvent) e).getDMContext();
			if (exeContext instanceof IContainerDMContext) {
		        parentDelta.addNode(createVMContext(exeContext), IModelDelta.EXPAND | IModelDelta.SELECT);
			} else {
				IContainerDMContext containerCtx = DMContexts.getAncestorOfType(exeContext, IContainerDMContext.class);
				if (containerCtx != null) {
					parentDelta.addNode(createVMContext(containerCtx), IModelDelta.CONTENT);
				}
			}
	    }
	
		requestMonitor.done();
	 }

}
