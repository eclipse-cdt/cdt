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
package org.eclipse.dd.dsf.debug.ui.viewmodel.launch;

import org.eclipse.dd.dsf.concurrent.DoneCollector;
import org.eclipse.dd.dsf.concurrent.DsfExecutor;
import org.eclipse.dd.dsf.concurrent.GetDataDone;
import org.eclipse.dd.dsf.ui.viewmodel.AbstractVMRootLayoutNode;
import org.eclipse.dd.dsf.ui.viewmodel.IVMContext;
import org.eclipse.dd.dsf.ui.viewmodel.IVMLayoutNode;
import org.eclipse.dd.dsf.ui.viewmodel.IVMRootLayoutNode;
import org.eclipse.dd.dsf.ui.viewmodel.VMDelta;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.internal.ui.viewers.provisional.IModelDelta;

/**
 * Layout node for the standard ILaunch object.  This node can only be used at 
 * the root of a hierarchy.  It does not implement the label provider
 * functionality, so the default adapters should be used to retrieve the label.  
 */
@SuppressWarnings("restriction")
public class StandardLaunchRootLayoutNode extends AbstractVMRootLayoutNode 
    implements IVMRootLayoutNode
{
    final private RootVMC<ILaunch> fRootVMC;
    final IVMContext[] fElements;
    
    public StandardLaunchRootLayoutNode(DsfExecutor executor, ILaunch launch) {
        super(executor);
        fRootVMC = new RootVMC<ILaunch>(this, launch);
        fElements = new IVMContext[] {  fRootVMC };
    }

    @Override
    public boolean hasDeltaFlags(Object e) {
        /*
         * Launch deltas for standard platform DebugEvent events are handled by 
         * org.eclipse.debug.internal.ui.viewers.update.LaunchManagerProxy.
         * Here only control the deltas of child nodes.  This is necessary to avoid
         * IProcess layout node from processing events that are intended for a 
         * different launch.  This is not a problem with DMC events, because the 
         * full path of the DMC object is built into the DMC itself.
         */
        if (e instanceof DebugEvent) {
            DebugEvent de = (DebugEvent)e;
            if (de.getSource() instanceof IProcess) {
                return ((IProcess)de.getSource()).getLaunch().equals(getRootVMC().getInputObject()) && super.hasDeltaFlags(e);
            } else if (de.getSource() instanceof IDebugElement) {
                return ((IDebugElement)de.getSource()).getLaunch().equals(getRootVMC().getInputObject()) && super.hasDeltaFlags(e);
            }
        }
        return super.hasDeltaFlags(e);
    }

    public void createDelta(Object event, final GetDataDone<IModelDelta> done) {
        /*
         * Create the root of the delta.  Since the launch object is not at the 
         * root of the view, create the delta with the path to the launch, then
         * pass that to the child layout nodes.
         */
        final VMDelta delta = new VMDelta(DebugPlugin.getDefault().getLaunchManager(), null);
        done.setData(delta);
        final VMDelta rootDelta = delta.addNode(getRootVMC().getInputObject(), getRootVMC());

        final IVMLayoutNode[] childNodes = getChildNodesWithDeltas(event);
        if (childNodes.length == 0) {
            done.setData(delta);
            getExecutor().execute(done);
            return;
        }            

        /* 
         * The execution for this node is not done until all the child nodes
         * are done.  Use the tracker to wait for all children to complete. 
         */
        final DoneCollector doneCollector = new DoneCollector(getExecutor()) { 
            public void run() {
                if (propagateError(getExecutor(), done, "Failed to generate child deltas.")) return; //$NON-NLS-1$
                done.setData(delta);
                getExecutor().execute(done);                                
            }
        };
        for (final IVMLayoutNode childNode : childNodes) {
            childNode.buildDelta(event, rootDelta, doneCollector.addNoActionDone());
        }
    }
    
    public IRootVMC getRootVMC() {
        return fRootVMC;
    }
}
