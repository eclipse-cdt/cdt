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

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.dd.dsf.concurrent.Done;
import org.eclipse.dd.dsf.concurrent.GetDataDone;
import org.eclipse.dd.dsf.ui.viewmodel.AbstractVMProvider;
import org.eclipse.dd.dsf.ui.viewmodel.AbstractVMRootLayoutNode;
import org.eclipse.dd.dsf.ui.viewmodel.IVMLayoutNode;
import org.eclipse.dd.dsf.ui.viewmodel.IVMRootLayoutNode;
import org.eclipse.dd.dsf.ui.viewmodel.VMDelta;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;

/**
 * Layout node for the standard ILaunch object.  This node can only be used at 
 * the root of a hierarchy.  It does not implement the label provider
 * functionality, so the default adapters should be used to retrieve the label.  
 */
@SuppressWarnings("restriction")
public class StandardLaunchRootLayoutNode extends AbstractVMRootLayoutNode 
    implements IVMRootLayoutNode
{
    final private ILaunch fLaunch;
    
    public StandardLaunchRootLayoutNode(AbstractVMProvider provider, ILaunch launch) {
        super(provider);
        fLaunch = launch;
    }

    @Override
    public int getDeltaFlags(Object e) {
        if (e instanceof DebugEvent) {
            DebugEvent de = (DebugEvent)e;
            if (de.getSource() instanceof IProcess && 
                !((IProcess)de.getSource()).getLaunch().equals(fLaunch) ) 
            {
                return IModelDelta.NO_CHANGE;
            } 
            else if (de.getSource() instanceof IDebugElement && 
                     !((IDebugElement)de.getSource()).getLaunch().equals(fLaunch) ) 
            {
                return IModelDelta.NO_CHANGE;
            }
        }
        return super.getDeltaFlags(e);
    }

    public void createDelta(Object event, final GetDataDone<IModelDelta> done) {
        /*
         * Create the root of the delta.  Since the launch object is not at the 
         * root of the view, create the delta with the path to the launch, then
         * pass that to the child layout nodes.
         */
        ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
        List<ILaunch> launchList = Arrays.asList(manager.getLaunches());
        final VMDelta viewRootDelta = new VMDelta(manager, 0, IModelDelta.NO_CHANGE, launchList.size());
        final VMDelta rootDelta = viewRootDelta.addNode(getRootObject(), launchList.indexOf(fLaunch), IModelDelta.NO_CHANGE);

        final Map<IVMLayoutNode,Integer> childNodeDeltas = getChildNodesWithDeltas(event);
        assert childNodeDeltas.size() != 0 : "Caller should make sure that there are deltas for given event."; //$NON-NLS-1$

        callChildNodesToBuildDelta(
            childNodeDeltas, rootDelta, event, 
            new Done() { 
                public void run() {
                    if (isDisposed()) return;
                    if (propagateError(getExecutor(), done, "Failed to create delta.")); //$NON-NLS-1$
                    done.setData(viewRootDelta);
                    getExecutor().execute(done);
                }
            });
    }
    
    public Object getRootObject() {
        return fLaunch;
    }
}
