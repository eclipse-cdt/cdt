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
import org.eclipse.dd.dsf.concurrent.DsfRunnable;
import org.eclipse.dd.dsf.concurrent.ImmediateExecutor;
import org.eclipse.dd.dsf.concurrent.RequestMonitor;
import org.eclipse.dd.dsf.datamodel.IDMEvent;
import org.eclipse.dd.dsf.debug.service.IRunControl;
import org.eclipse.dd.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.dd.dsf.debug.service.IRunControl.IExecutionDMData;
import org.eclipse.dd.dsf.debug.service.IRunControl.IResumedDMEvent;
import org.eclipse.dd.dsf.debug.service.IRunControl.ISuspendedDMEvent;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.dd.dsf.ui.concurrent.ViewerDataRequestMonitor;
import org.eclipse.dd.dsf.ui.viewmodel.AbstractVMContext;
import org.eclipse.dd.dsf.ui.viewmodel.IVMNode;
import org.eclipse.dd.dsf.ui.viewmodel.VMDelta;
import org.eclipse.dd.dsf.ui.viewmodel.datamodel.AbstractDMVMNode;
import org.eclipse.dd.dsf.ui.viewmodel.datamodel.AbstractDMVMProvider;
import org.eclipse.dd.examples.pda.PDAPlugin;
import org.eclipse.dd.examples.pda.launch.PDALaunch;
import org.eclipse.dd.examples.pda.service.PDACommandControl;
import org.eclipse.dd.examples.pda.service.PDAProgramDMContext;
import org.eclipse.dd.examples.pda.service.PDAStartedEvent;
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

/**
 * View Model node representing a PDA program. 
 */
@SuppressWarnings("restriction")
public class PDAProgramVMNode extends AbstractDMVMNode 
    implements IElementLabelProvider
{
    // View model context representing a terminated PDA program.
    // It's purpose is to show a terminated program in the debug view
    // even after the DSF session is terminated.
    // 
    // Note: this context does not implement the IDMVMContext
    // interfaces, as it does not use an IDMContext as its root.
    // 
    // To implement comparison methods, this contexts uses the
    // VM node object, such that two terminated program contexts
    // from the same instance of VM node will be equal. 
    private static class TerminatedProgramVMContext extends AbstractVMContext {
        TerminatedProgramVMContext(IVMNode node) {
            super(node);
        }
        
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof TerminatedProgramVMContext) {
                TerminatedProgramVMContext context = (TerminatedProgramVMContext)obj;
                return getVMNode().equals(context.getVMNode());
            }
            return false;
        }
        
        @Override
        public int hashCode() {
            return getVMNode().hashCode();
        }
    }
    
    public PDAProgramVMNode(AbstractDMVMProvider provider, DsfSession session) {
        super(provider, session, IExecutionDMContext.class);
    }

    @Override
    public void update(IHasChildrenUpdate[] updates) {
        for (IHasChildrenUpdate update : updates) {
            // Check if the launch is initialized.  PDA program element should 
            // be shown only if the launch has completed initializing.
            PDALaunch launch = findLaunchInPath(update.getElementPath());
            update.setHasChilren(launch != null && launch.isInitialized());
            update.done();
        }        
    }

    @Override
    public void update(IChildrenCountUpdate[] updates) {
        for (IChildrenCountUpdate update : updates) {
            // Check if the launch is initialized.  PDA program element should 
            // be shown only if the launch has completed initializing.
            PDALaunch launch = findLaunchInPath(update.getElementPath());
            if (launch != null && launch.isInitialized()) {
                update.setChildCount(1);
            } else {
                update.setChildCount(0);
            }
            update.done();
        }        
    }

    @Override
    public void update(IChildrenUpdate[] updates) {
        for (IChildrenUpdate update : updates) {
            PDALaunch launch = findLaunchInPath(update.getElementPath());
            if (launch != null && launch.isInitialized() && launch.isShutDown()) {
                // If the debug session has been shut down, add a dummy 
                // VM context representing the PDA thread.
                update.setChild(new TerminatedProgramVMContext(this), 0);
                update.done();
            } else {
                super.update(new IChildrenUpdate[] { update });
            }
        }
    }
    
    @Override
    protected void updateElementsInSessionThread(final IChildrenUpdate update) {
    	// Get the instance of the service.  Note that there is no race condition
    	// in getting the service since this method is called only in the 
    	// service executor thread.
        final PDACommandControl commandControl = getServicesTracker().getService(PDACommandControl.class);

        // Check if the service is available.  If it is not, no elements are 
        // updated.
        if (commandControl == null) {
            handleFailedUpdate(update);
            return;
        }
        
        update.setChild(createVMContext(commandControl.getProgramDMContext()), 0);
        update.done();
    }

    public void update(final ILabelUpdate[] updates) {
        for (final ILabelUpdate update : updates) {
            if (update.getElement() instanceof TerminatedProgramVMContext) {
                // If the element is a terminated program, update the label 
                // in the View Model thread.
                updateTerminatedThreadLabel(update);
            } else {
                // If the element is the PDA Program context, try to switch
                // to the DSF session thread before updating the label.
                try {
                    getSession().getExecutor().execute(new DsfRunnable() {
                        public void run() {
                            updateProgramLabelInSessionThread(update);
                        }});
                } catch (RejectedExecutionException e) {
                    // Acceptable race condition: DSF session terminated.
                    handleFailedUpdate(update);
                }
            }
        }
    }
    
    @ConfinedToDsfExecutor("getSession().getExecutor()")
    private void updateProgramLabelInSessionThread(final ILabelUpdate update) {
        // Get a reference to the run control service.
        final IRunControl runControl = getServicesTracker().getService(IRunControl.class);
        if (runControl == null) {
            handleFailedUpdate(update);
            return;
        }
        
        // Find the PDA program context.
        final PDAProgramDMContext programCtx = 
            findDmcInPath(update.getViewerInput(), update.getElementPath(), PDAProgramDMContext.class);

        // Call service to get current program state
        final boolean isSuspended = runControl.isSuspended(programCtx);

        // Set the program icon based on the running state of the program.
        String imageKey = null;
        if (isSuspended) {
            imageKey = IDebugUIConstants.IMG_OBJS_THREAD_SUSPENDED;
        } else {
            imageKey = IDebugUIConstants.IMG_OBJS_THREAD_RUNNING;
        }
        update.setImageDescriptor(DebugUITools.getImageDescriptor(imageKey), 0);

        // Retrieve the last state change reason 
        runControl.getExecutionData(
            programCtx, 
            new ViewerDataRequestMonitor<IExecutionDMData>(ImmediateExecutor.getInstance(), update) 
            { 
                @Override
                public void handleCompleted(){
                    // If the request failed, fail the udpate. 
                    if (!isSuccess()) {
                        handleFailedUpdate(update);
                        return;
                    }
    
                    // Compose the thread name string.
                    final StringBuilder builder = new StringBuilder(); 
    
                    builder.append("PDA [");
                    builder.append(programCtx.getProgram());
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
        // Retrieve the program name from the launch object in the update path.
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
            return IModelDelta.STATE;
        } 
        if (e instanceof PDAStartedEvent) {
            return IModelDelta.EXPAND | IModelDelta.SELECT;
        }
        return IModelDelta.NO_CHANGE;
    }

    public void buildDelta(Object e, VMDelta parentDelta, int nodeOffset, RequestMonitor rm) {
        if(e instanceof IResumedDMEvent || e instanceof ISuspendedDMEvent) {
            // If a suspended/resumed event is received, just update the 
            // state of the program.  StackFramesVMNode will take care of 
            // refreshing the stack frames.
            parentDelta.addNode(createVMContext(((IDMEvent<?>)e).getDMContext()), IModelDelta.STATE);
        } 
        if (e instanceof PDAStartedEvent) {
            // When debug session is started expand and select the program.
            // If the program hits a breakpoint, the top stack frame will then
            // be selected.
            parentDelta.addNode(createVMContext(((PDAStartedEvent)e).getDMContext()), IModelDelta.EXPAND | IModelDelta.SELECT);            
        }
        rm.done();
  	 }
}
