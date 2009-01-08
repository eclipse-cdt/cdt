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
package org.eclipse.cdt.dsf.debug.ui.viewmodel.launch;

import java.util.concurrent.RejectedExecutionException;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.datamodel.IDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerResumedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerSuspendedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExitedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IStartedDMEvent;
import org.eclipse.cdt.dsf.debug.service.StepQueueManager.ISteppingTimedOutEvent;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.SteppingController.SteppingTimedOutEvent;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMContext;
import org.eclipse.cdt.dsf.ui.viewmodel.VMDelta;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.AbstractDMVMNode;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.AbstractDMVMProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementLabelProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;

/**
 * Abstract implementation of a container view model node.
 * Clients need to implement {@link #updateLabelInSessionThread(ILabelUpdate[])}.
 * 
 * @since 1.1
 */
@SuppressWarnings("restriction")
public abstract class AbstractContainerVMNode extends AbstractDMVMNode implements IElementLabelProvider {

	public AbstractContainerVMNode(AbstractDMVMProvider provider, DsfSession session) {
		super(provider, session, IRunControl.IContainerDMContext.class);
	}

	public void update(final ILabelUpdate[] updates) {
	    try {
	        getSession().getExecutor().execute(new DsfRunnable() {
	            public void run() {
	                for (final ILabelUpdate update : updates) {
	                    updateLabelInSessionThread(update);
	                }
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
	protected abstract void updateLabelInSessionThread(ILabelUpdate update);

    @Override
    public void getContextsForEvent(VMDelta parentDelta, Object e, final DataRequestMonitor<IVMContext[]> rm) {
        super.getContextsForEvent(parentDelta, e, rm);
    }
            
	public int getDeltaFlags(Object e) {
        IDMContext dmc = e instanceof IDMEvent<?> ? ((IDMEvent<?>)e).getDMContext() : null;

	    if (e instanceof IContainerResumedDMEvent) {
            if (((IContainerResumedDMEvent)e).getReason() != IRunControl.StateChangeReason.STEP) 
            {
                return IModelDelta.CONTENT;
            }
        } else if (e instanceof IContainerSuspendedDMEvent) {
            return IModelDelta.NO_CHANGE;
        } else if (e instanceof FullStackRefreshEvent) {
            if (dmc instanceof IContainerDMContext) {
                return IModelDelta.CONTENT;
            }
	    } else if (e instanceof SteppingTimedOutEvent) {
	        if (dmc instanceof IContainerDMContext) 
	        {
	            return IModelDelta.CONTENT;
	        }
	    } else if (e instanceof ISteppingTimedOutEvent) {
	        if (dmc instanceof IContainerDMContext) 
	        {
	            return IModelDelta.CONTENT;
	        }
	    } else if (e instanceof IExitedDMEvent) {
	        return IModelDelta.CONTENT;
	    } else if (e instanceof IStartedDMEvent) {
	    	if (dmc instanceof IContainerDMContext) {
	    		return IModelDelta.EXPAND | IModelDelta.SELECT;
	    	} else {
		        return IModelDelta.CONTENT;
	    	}
	    }
	    return IModelDelta.NO_CHANGE;
	}

	public void buildDelta(Object e, final VMDelta parentDelta, final int nodeOffset, final RequestMonitor requestMonitor) {
	    IDMContext dmc = e instanceof IDMEvent<?> ? ((IDMEvent<?>)e).getDMContext() : null;
	    
		if(e instanceof IContainerResumedDMEvent) {
            // Container resumed: 
		    // - If not stepping, update the container and the execution 
		    // contexts under it.  
		    // - If stepping, do nothing to avoid too many updates.  If a 
		    // time-out is reached before the step completes, the 
		    // ISteppingTimedOutEvent will trigger a full refresh.
		    if (((IContainerResumedDMEvent)e).getReason() != IRunControl.StateChangeReason.STEP) 
		    {
    	        parentDelta.addNode(createVMContext(((IDMEvent<?>)e).getDMContext()), IModelDelta.CONTENT);
		    } 
		} else if (e instanceof IContainerSuspendedDMEvent) {
            // Container suspended.  Do nothing here to give the stack the 
		    // priority in updating. The container and threads will update as 
		    // a result of FullStackRefreshEvent. 
		} else if (e instanceof FullStackRefreshEvent) {
		    // Full-stack refresh event is generated following a suspended event 
		    // and a fixed delay.  If the suspended event was generated for the 
		    // container refresh the whole container.
		    if (dmc instanceof IContainerDMContext) {
		        parentDelta.addNode(createVMContext(dmc), IModelDelta.CONTENT);
		    }
		} else if (e instanceof SteppingTimedOutEvent) {
		    // Stepping time-out indicates that a step operation is taking 
		    // a long time, and the view needs to be refreshed to show 
		    // the user that the program is running.
		    // If the step was issued for the whole container refresh
		    // the whole container.
		    if (dmc instanceof IContainerDMContext) {
	            parentDelta.addNode(createVMContext(dmc), IModelDelta.CONTENT);
		    }
		} else if (e instanceof ISteppingTimedOutEvent) {
		    // Stepping time-out indicates that a step operation is taking 
		    // a long time, and the view needs to be refreshed to show 
		    // the user that the program is running.
		    // If the step was issued for the whole container refresh
		    // the whole container.
		    if (dmc instanceof IContainerDMContext) {
	            parentDelta.addNode(createVMContext(dmc), IModelDelta.CONTENT);
		    }
		} else if (e instanceof IExitedDMEvent) {
		    // An exited event could either be for a thread within a container
		    // or for the container itself.  
		    // If a container exited, refresh the parent element so that the 
		    // container may be removed.
		    // If a thread exited within a container, refresh that container.
			if (dmc instanceof IContainerDMContext) {
	    		parentDelta.setFlags(parentDelta.getFlags() |  IModelDelta.CONTENT);
	    	} else {
		        IContainerDMContext containerCtx = DMContexts.getAncestorOfType(dmc, IContainerDMContext.class);
		        if (containerCtx != null) {
		            parentDelta.addNode(createVMContext(containerCtx), IModelDelta.CONTENT);
		        }
	    	}
	    } else if (e instanceof IStartedDMEvent) {
            // A started event could either be for a thread within a container
            // or for the container itself.  
            // If a container started, issue an expand and select event to 
	        // show the threads in the new container. 
	        // Note: the EXPAND flag implies refreshing the parent element.
			if (dmc instanceof IContainerDMContext) {
		        parentDelta.addNode(createVMContext(dmc), IModelDelta.EXPAND | IModelDelta.SELECT);
			} else {
				IContainerDMContext containerCtx = DMContexts.getAncestorOfType(dmc, IContainerDMContext.class);
				if (containerCtx != null) {
					parentDelta.addNode(createVMContext(containerCtx), IModelDelta.CONTENT);
				}
			}
	    }
	
		requestMonitor.done();
	 }

}
