/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Ericsson 		  - Modified for multi threaded functionality	
 *******************************************************************************/
package org.eclipse.dd.examples.pda.ui.viewmodel.launch;

import java.util.concurrent.RejectedExecutionException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.dd.dsf.concurrent.ConfinedToDsfExecutor;
import org.eclipse.dd.dsf.concurrent.DataRequestMonitor;
import org.eclipse.dd.dsf.concurrent.DsfRunnable;
import org.eclipse.dd.dsf.concurrent.RequestMonitor;
import org.eclipse.dd.dsf.datamodel.IDMContext;
import org.eclipse.dd.dsf.datamodel.IDMEvent;
import org.eclipse.dd.dsf.debug.service.IRunControl;
import org.eclipse.dd.dsf.debug.service.IRunControl.IContainerResumedDMEvent;
import org.eclipse.dd.dsf.debug.service.IRunControl.IContainerSuspendedDMEvent;
import org.eclipse.dd.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.dd.dsf.debug.service.IRunControl.IExecutionDMData;
import org.eclipse.dd.dsf.debug.service.IRunControl.IResumedDMEvent;
import org.eclipse.dd.dsf.debug.service.IRunControl.ISuspendedDMEvent;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.dd.dsf.ui.viewmodel.AbstractVMContext;
import org.eclipse.dd.dsf.ui.viewmodel.IVMAdapter;
import org.eclipse.dd.dsf.ui.viewmodel.IVMNode;
import org.eclipse.dd.dsf.ui.viewmodel.VMDelta;
import org.eclipse.dd.dsf.ui.viewmodel.datamodel.AbstractDMVMNode;
import org.eclipse.dd.dsf.ui.viewmodel.datamodel.AbstractDMVMProvider;
import org.eclipse.dd.examples.pda.PDAPlugin;
import org.eclipse.dd.examples.pda.launch.PDALaunch;
import org.eclipse.dd.examples.pda.service.command.PDACommandControl;
import org.eclipse.dd.examples.pda.service.command.PDAStartedEvent;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenCountUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementLabelProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IHasChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.viewers.TreePath;


@SuppressWarnings("restriction")
public class ThreadVMNode extends AbstractDMVMNode 
    implements IElementLabelProvider
{
    private class TerminatedThreadVMContext extends AbstractVMContext {
        TerminatedThreadVMContext(IVMAdapter adapter, IVMNode node) {
            super(adapter, node);
        }
        
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof TerminatedThreadVMContext) {
                TerminatedThreadVMContext context = (TerminatedThreadVMContext)obj;
                return getVMNode().equals(context.getVMNode());
            }
            return false;
        }
        
        @Override
        public int hashCode() {
            return getVMNode().hashCode();
        }
    }
    
    public ThreadVMNode(AbstractDMVMProvider provider, DsfSession session) {
        super(provider, session, IExecutionDMContext.class);
    }

    @Override
    public void update(IHasChildrenUpdate[] updates) {
        for (IHasChildrenUpdate update : updates) {
            update.setHasChilren(true);
            update.done();
        }        
    }

    @Override
    public void update(IChildrenCountUpdate[] updates) {
        for (IChildrenCountUpdate update : updates) {
            update.setChildCount(1);
            update.done();
        }        
    }

    @Override
    public void update(IChildrenUpdate[] updates) {
        for (IChildrenUpdate update : updates) {
            PDALaunch launch = findLaunchInPath(update.getElementPath());
            if (launch != null && launch.isInitialized() && launch.isShutDown()) {
                // If the debug session has been shut down.  We cannot retrieve the 
                // DM context representing the thread.  Instead add a dummy 
                // "terminated" PDA thread.
                update.setChild(new TerminatedThreadVMContext(getVMProvider().getVMAdapter(), this), 0);
                update.done();
            } else {
                super.update(new IChildrenUpdate[] { update });
            }
        }
    }
    
    @Override
    protected void updateElementsInSessionThread(final IChildrenUpdate update) {
    	if (!checkService(PDACommandControl.class, null, update)) return;
        final PDACommandControl commandControl = getServicesTracker().getService(PDACommandControl.class);
         
        update.setChild(createVMContext(commandControl.getDMContext()), 0);
        update.done();
    }

    public void update(final ILabelUpdate[] updates) {
        for (final ILabelUpdate update : updates) {
            if (update.getElement() instanceof TerminatedThreadVMContext) {
                updateTerminatedThreadLabel(update);
            } else {
                try {
                    getSession().getExecutor().execute(new DsfRunnable() {
                        public void run() {
                            updateActiveThreadLabelInSessionThread(update);
                        }});
                } catch (RejectedExecutionException e) {
                    handleFailedUpdate(update);
                }
            }
        }
    }
    
    @ConfinedToDsfExecutor("getSession().getExecutor()")
    private void updateActiveThreadLabelInSessionThread(final ILabelUpdate update) {
        if (!checkService(IRunControl.class, null, update)) return;
        final IRunControl runControl = getServicesTracker().getService(IRunControl.class);
        
        final IExecutionDMContext dmc = findDmcInPath(update.getViewerInput(), update.getElementPath(), IExecutionDMContext.class);

        String imageKey = null;
        if (getServicesTracker().getService(IRunControl.class).isSuspended(dmc)) {
            imageKey = IDebugUIConstants.IMG_OBJS_THREAD_SUSPENDED;
        } else {
            imageKey = IDebugUIConstants.IMG_OBJS_THREAD_RUNNING;
        }
        update.setImageDescriptor(DebugUITools.getImageDescriptor(imageKey), 0);

        final boolean isSuspended = runControl.isSuspended(dmc);
        
        // Find the Reason for the State
        runControl.getExecutionData(dmc, 
                new DataRequestMonitor<IExecutionDMData>(getSession().getExecutor(), null) { 
            @Override
            public void handleCompleted(){
                if (!getStatus().isOK()) {
                    handleFailedUpdate(update);
                    return;
                }

                final StringBuilder builder = new StringBuilder(); 

                builder.append("PDA [");
                builder.append(getProgramName(update));
                builder.append("]");
                
                if(isSuspended) {
                    builder.append(" (Suspended"); 
                } else {
                    builder.append(" (Running"); 
                }
                // Reason will be null before ContainerSuspendEvent is fired
                if(getData().getStateChangeReason() != null) {
                    builder.append(" : "); 
                    builder.append(getData().getStateChangeReason());
                }
                builder.append(")"); 
                update.setLabel(builder.toString(), 0);
                update.done();
            }
        });        
    }
    
    private void updateTerminatedThreadLabel(ILabelUpdate update) {
        update.setLabel("<terminated> PDA [" + getProgramName(update) + "]", 0);
        update.setImageDescriptor(DebugUITools.getImageDescriptor(IDebugUIConstants.IMG_OBJS_DEBUG_TARGET_TERMINATED), 0);
        update.done();
    }

    private String getProgramName(IViewerUpdate update) {
        String program = "unknown program";
        ILaunch launch = findLaunchInPath(update.getElementPath());
        if (launch != null) {
            try {
                program = launch.getLaunchConfiguration().getAttribute(PDAPlugin.ATTR_PDA_PROGRAM, program);
            } catch (CoreException e) {
                // Ignore, label will revert to default.
            }
        } 
        return program;
    }
    
    private PDALaunch findLaunchInPath(TreePath path) {
        for (int i = 0; i < path.getSegmentCount(); i++) {
            if (path.getSegment(i) instanceof PDALaunch) {
                return (PDALaunch)path.getSegment(i);
            }
        }
        return null;
    }
    
    public int getDeltaFlags(Object e) {
        if(e instanceof IResumedDMEvent || e instanceof ISuspendedDMEvent) {
            return IModelDelta.CONTENT;
        } 
        if (e instanceof PDAStartedEvent) {
            return IModelDelta.EXPAND;
        }
        return IModelDelta.NO_CHANGE;
    }

    public void buildDelta(Object e, VMDelta parentDelta, int nodeOffset, RequestMonitor rm) {
        if(e instanceof IContainerResumedDMEvent) {
            IDMContext triggeringContext = ((IContainerResumedDMEvent)e).getTriggeringContext();
            if (triggeringContext != null) {
                parentDelta.addNode(createVMContext(triggeringContext), IModelDelta.CONTENT);
            }
        } else if (e instanceof IContainerSuspendedDMEvent) {
            IDMContext triggeringContext = ((IContainerSuspendedDMEvent)e).getTriggeringContext();
            if (triggeringContext != null) {
                parentDelta.addNode(createVMContext(triggeringContext), IModelDelta.CONTENT);
            }
        } else if(e instanceof IResumedDMEvent || e instanceof ISuspendedDMEvent) {
            parentDelta.addNode(createVMContext(((IDMEvent<?>)e).getDMContext()), IModelDelta.CONTENT);
        } 
        if (e instanceof PDAStartedEvent) {
            parentDelta.addNode(createVMContext(((PDAStartedEvent)e).getDMContext()), IModelDelta.EXPAND);            
        }
        rm.done();
  	 }
}
