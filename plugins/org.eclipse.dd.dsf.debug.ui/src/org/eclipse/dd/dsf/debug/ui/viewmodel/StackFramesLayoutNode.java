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

import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.dd.dsf.concurrent.Done;
import org.eclipse.dd.dsf.concurrent.GetDataDone;
import org.eclipse.dd.dsf.datamodel.IDMEvent;
import org.eclipse.dd.dsf.debug.service.IRunControl;
import org.eclipse.dd.dsf.debug.service.IStack;
import org.eclipse.dd.dsf.debug.service.IStepQueueManager;
import org.eclipse.dd.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.dd.dsf.debug.service.IRunControl.StateChangeReason;
import org.eclipse.dd.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.dd.dsf.debug.service.IStack.IFrameDMData;
import org.eclipse.dd.dsf.debug.ui.DsfDebugUIPlugin;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.dd.dsf.ui.viewmodel.DMContextVMLayoutNode;
import org.eclipse.dd.dsf.ui.viewmodel.IVMContext;
import org.eclipse.dd.dsf.ui.viewmodel.VMDelta;
import org.eclipse.debug.internal.ui.viewers.provisional.ILabelRequestMonitor;
import org.eclipse.debug.internal.ui.viewers.provisional.IModelDelta;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.resource.ImageDescriptor;


@SuppressWarnings("restriction")
public class StackFramesLayoutNode extends DMContextVMLayoutNode {
    
    public IVMContext[] fCachedOldFramesVMCs;
    
    public StackFramesLayoutNode(DsfSession session) {
        super(session, IStack.IFrameDMContext.class);
    }
    
    public void hasElements(IVMContext parentVmc, final GetDataDone<Boolean> done) {
        IExecutionDMContext execDmc = findDmcInVmc(parentVmc, IExecutionDMContext.class);
        if (execDmc == null || getServicesTracker().getService(IStack.class) == null || getServicesTracker().getService(IRunControl.class) == null) {
            done.setData(false);
            getExecutor().execute(done);
            return;
        }          
        
        done.setData(getServicesTracker().getService(IStack.class).isStackAvailable(execDmc)); 
        getExecutor().execute(done);
    }

    public void getElements(final IVMContext parentVmc, final GetDataDone<IVMContext[]> done) {
        final IExecutionDMContext execDmc = findDmcInVmc(parentVmc, IExecutionDMContext.class);
        if (execDmc == null || getServicesTracker().getService(IStack.class) == null || getServicesTracker().getService(IRunControl.class) == null) {
            done.setData(new IVMContext[0]);
            getExecutor().execute(done);
            return;
        }          
        
        getServicesTracker().getService(IStack.class).getFrames(
            execDmc, 
            new GetDataDone<IFrameDMContext[]>() { public void run() {
                if (!getStatus().isOK()) {
                    // Failed to retrieve frames.  If we are stepping, we 
                    // might still be able to retrieve just the top stack 
                    // frame, which would still be useful in Debug View.
                    if (getServicesTracker().getService(IRunControl.class).isStepping(execDmc)) {
                        getElementsTopStackFrameOnly(parentVmc, done);
                    } else {
                        propagateError(getExecutor(), done, "Failed retrieving stack frames");
                    }
                    return;
                }
                // Store the VMC element array, in case we need to use it when 
                fCachedOldFramesVMCs = dmcs2vmcs(parentVmc, getData());
                done.setData(fCachedOldFramesVMCs);
                getExecutor().execute(done);
            }});
    }
    
    /**
     * Retrieves teh list of VMC elements for a full stack trace, but with only 
     * the top stack frame being retrieved from the service.  The rest of the 
     * frames are retrieved from the cache or omitted.  
     * @see #getElements(IVMContext, GetDataDone)
     */
    private void getElementsTopStackFrameOnly(final IVMContext parentVmc, final GetDataDone<IVMContext[]> done) {
        final IExecutionDMContext execDmc = findDmcInVmc(parentVmc, IExecutionDMContext.class);

        getServicesTracker().getService(IStack.class).getTopFrame(
            execDmc, 
            new GetDataDone<IFrameDMContext>() { public void run() {
                if (propagateError(getExecutor(), done, "Failed retrieving top stack frame")) return;
                IVMContext topFrameVmc = new DMContextVMContext(parentVmc, getData());
                
                // If there are old frames cached, use them and only substitute the top frame object. Otherwise, create
                // an array of VMCs with just the top frame.
                if (fCachedOldFramesVMCs != null && fCachedOldFramesVMCs.length >= 1) {
                    fCachedOldFramesVMCs[0] = topFrameVmc;
                    done.setData(fCachedOldFramesVMCs);
                } else {
                    done.setData(new IVMContext[] { topFrameVmc });
                }
                getExecutor().execute(done);
            }});
    }
    
    public void retrieveLabel(IVMContext vmc, final ILabelRequestMonitor result) {
        final IExecutionDMContext execDmc = findDmcInVmc(vmc, IExecutionDMContext.class);
        if (execDmc == null || getServicesTracker().getService(IStack.class) == null || getServicesTracker().getService(IRunControl.class) == null) {
            result.done();
            return;
        }          

        String imageKey = null;
        IRunControl rc = getServicesTracker().getService(IRunControl.class);
        if (rc.isSuspended(execDmc) || 
            (rc.isStepping(execDmc) && !getServicesTracker().getService(IStepQueueManager.class).isSteppingTimedOut(execDmc)))
        {
            imageKey = IDebugUIConstants.IMG_OBJS_STACKFRAME;
        } else {
            imageKey = IDebugUIConstants.IMG_OBJS_STACKFRAME_RUNNING;
        }            
        result.setImageDescriptors(new ImageDescriptor[] { DebugUITools.getImageDescriptor(imageKey) });
        
        IFrameDMContext frameDmc = (IFrameDMContext)((DMContextVMContext)vmc).getDMC();
        getServicesTracker().getService(IStack.class).getModelData(
            frameDmc, 
            new GetDataDone<IFrameDMData>() { public void run() {
                // Check if services are still available.
                if (getServicesTracker().getService(IRunControl.class) == null) {
                    result.done();
                    return;
                }

                if (!getStatus().isOK()) {
                    // If failed set a dummy label, and only propagate the 
                    // error if we are not stepping, since that would be a 
                    // common cause of failure.
                    result.setLabels(new String[] { "..." });
                    if (!getServicesTracker().getService(IRunControl.class).isStepping(execDmc)) {
                        MultiStatus status = new MultiStatus(DsfDebugUIPlugin.PLUGIN_ID, 0, "Failed to retrieve stack frame label", null);
                        status.add(getStatus());
                        result.setStatus(status);
                    }
                    result.done();
                    return;
                }
                
                //
                // Finally, if all goes well, set the label.
                //
                StringBuilder label = new StringBuilder();
                
                // Add frame number (if total number of frames in known)
                if (fCachedOldFramesVMCs != null) {
                    label.append(fCachedOldFramesVMCs.length - getData().getLevel());
                }
                
                // Add the function name
                if (getData().getFunction() != null && getData().getFunction().length() != 0) { 
                    label.append(" ");
                    label.append(getData().getFunction());
                    label.append("()");
                }
                
                // Add full file name
                if (getData().getFile() != null && getData().getFile().length() != 0) {
                    label.append(" at ");
                    label.append(getData().getFile());
                }
                
                // Add line number 
                if (getData().getLine() >= 0) {
                    label.append(":");
                    label.append(getData().getLine());
                    label.append(" ");
                }
                
                // Add the address
                label.append(getData().getAddress());
                    
                // Set the label to the result listener
                result.setLabels(new String[] { label.toString() });
                result.done();
            }});
    }

    public boolean hasDeltaFlagsForDMEvent(IDMEvent e) {
        // This node generates delta if the timers have changed, or if the 
        // label has changed.
        return e instanceof IRunControl.ISuspendedDMEvent || 
               e instanceof IRunControl.IResumedDMEvent ||
               e instanceof IStepQueueManager.ISteppingTimedOutEvent ||
               super.hasDeltaFlagsForDMEvent(e);
    }

    public void buildDeltaForDMEvent(final IDMEvent e, final VMDelta parent, final Done done) {
        if (getServicesTracker().getService(IStack.class) == null || getServicesTracker().getService(IRunControl.class) == null) {
            // Required services have not initialized yet.  Ignore the event.
            super.buildDeltaForDMEvent(e, parent, done);
            return;
        }          
        
        if (e instanceof IRunControl.ISuspendedDMEvent) {
            IRunControl.ISuspendedDMEvent suspendedEvent = (IRunControl.ISuspendedDMEvent)e; 

            // Refresh the whole list of stack frames unless the target is already stepping the next command.  In 
            // which case, the refresh will occur when the stepping sequence slows down or stops.  Trying to
            // refresh the whole stack trace with every step would slow down stepping too much.
            if (!getServicesTracker().getService(IRunControl.class).isStepping(suspendedEvent.getDMContext())) {
                parent.addFlags(IModelDelta.CONTENT);
            }
            
            // Always expand the thread node to show the stack frames.
            parent.addFlags(IModelDelta.EXPAND);

            // Retrieve the list of stack frames, and mark the top frame to be selected.  
            getElementsTopStackFrameOnly(
                parent.getVMC(), 
                new GetDataDone<IVMContext[]>() { public void run() {
                    if (getStatus().isOK() && getData().length != 0) {
                        parent.addNode( getData()[0], IModelDelta.SELECT | IModelDelta.STATE);
                        // If second frame is available repaint it, so that a "..." appears.  This gives a better
                        // impression that the frames are not up-to date.
                        if (getData().length >= 2) {
                            parent.addNode( getData()[1], IModelDelta.STATE);
                        }
                    }                        
                    // Even in case of errors, call super-class to complete building of the delta.
                    StackFramesLayoutNode.super.buildDeltaForDMEvent(e, parent, done);
                }});

        } else if (e instanceof IRunControl.IResumedDMEvent) {
            IRunControl.IResumedDMEvent resumedEvent = (IRunControl.IResumedDMEvent)e; 
            getExecutor().execute(done);
            if (resumedEvent.getReason() == StateChangeReason.STEP) {
                // TODO: Refreshing the state of the top stack frame is only necessary to re-enable the step button.  
                // This is because platform disables the step action every time after it is invoked.  Need to file
                // a bug on this.
                getServicesTracker().getService(IStack.class).getTopFrame(
                    resumedEvent.getDMContext(), 
                    new GetDataDone<IFrameDMContext>() { public void run() {
                        if (getStatus().isOK()) {
                            parent.addNode(new DMContextVMContext(parent.getVMC(), getData()), IModelDelta.STATE);
                        }
                        StackFramesLayoutNode.super.buildDeltaForDMEvent(e, parent, done);
                    }});
                StackFramesLayoutNode.super.buildDeltaForDMEvent(e, parent, done);
            } else {
                // Refresh the list of stack frames only if the run operation is not a step.  Also, clear the list
                // of cached frames.
                parent.addFlags(IModelDelta.CONTENT);
                fCachedOldFramesVMCs = null;
                // Call super-class to build sub-node delta's.
                super.buildDeltaForDMEvent(e, parent, done);
            }
        } else if (e instanceof IStepQueueManager.ISteppingTimedOutEvent) {
            // Repaint the stack frame images to have the running symbol.
            parent.addFlags(IModelDelta.CONTENT);
            super.buildDeltaForDMEvent(e, parent, done);
        } else {
            // Call super-class to build sub-node delta's.
            super.buildDeltaForDMEvent(e, parent, done);
        }
    }
}
