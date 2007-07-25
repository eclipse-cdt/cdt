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

import org.eclipse.dd.dsf.concurrent.RequestMonitor;
import org.eclipse.dd.dsf.concurrent.DataRequestMonitor;
import org.eclipse.dd.dsf.ui.viewmodel.AbstractVMProvider;
import org.eclipse.dd.dsf.ui.viewmodel.VMRootLayoutNode;
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
public class StandardLaunchRootLayoutNode extends VMRootLayoutNode 
    implements IVMRootLayoutNode
{
    public static class LaunchesEvent {
        public enum Type { ADDED, REMOVED, CHANGED, TERMINATED }
        public final ILaunch[] fLaunches;
        public final Type fType;
        
        public LaunchesEvent(ILaunch[] launches, Type type) {
            fLaunches = launches;
            fType = type;
        }            
    }
    
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
                     !fLaunch.equals(((IDebugElement)de.getSource()).getLaunch()))
            {
                return IModelDelta.NO_CHANGE;
            }
        }
        int flags = 0;
        if (e instanceof LaunchesEvent) {
            LaunchesEvent le = (LaunchesEvent)e;
            for (ILaunch launch : le.fLaunches) {
                if (fLaunch == launch) {
                    if (le.fType == LaunchesEvent.Type.CHANGED) {
                        flags = IModelDelta.STATE | IModelDelta.CONTENT;
                    } else if (le.fType == LaunchesEvent.Type.TERMINATED) {
                        flags = IModelDelta.STATE | IModelDelta.CONTENT;
                    }
                }
            }
        }
        
        return flags | super.getDeltaFlags(e);
    }

    @Override
    public void createDelta(Object event, final DataRequestMonitor<IModelDelta> rm) {
        /*
         * Create the root of the delta.  Since the launch object is not at the 
         * root of the view, create the delta with the path to the launch, then
         * pass that to the child layout nodes.
         */
        ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
        List<ILaunch> launchList = Arrays.asList(manager.getLaunches());
        final VMDelta viewRootDelta = new VMDelta(manager, 0, IModelDelta.NO_CHANGE, launchList.size());
        final VMDelta rootDelta = viewRootDelta.addNode(getRootObject(), launchList.indexOf(fLaunch), IModelDelta.NO_CHANGE);

        // Generate delta for launch node.
        if (event instanceof LaunchesEvent) {
            LaunchesEvent le = (LaunchesEvent)event;
            for (ILaunch launch : le.fLaunches) {
                if (fLaunch == launch) {
                    if (le.fType == LaunchesEvent.Type.CHANGED) {
                        rootDelta.addFlags(IModelDelta.STATE | IModelDelta.CONTENT);
                    } else if (le.fType == LaunchesEvent.Type.TERMINATED) {
                        rootDelta.addFlags(IModelDelta.STATE | IModelDelta.CONTENT);
                    }
                }
            }
        } 
        
        // Call the child nodes to generate their delta.
        Map<IVMLayoutNode,Integer> childNodeDeltas = getChildNodesWithDeltaFlags(event);
        if (childNodeDeltas.size() != 0) {
            callChildNodesToBuildDelta(
                childNodeDeltas, rootDelta, event, 
                new RequestMonitor(getExecutor(), rm) { 
                    @Override
                    public void handleOK() {
                        if (isDisposed()) return;
                        rm.setData(viewRootDelta);
                        rm.done();
                    }
                });
        } else {
            rm.setData(viewRootDelta);
            rm.done();
        }
    }
    
    @Override
    public Object getRootObject() {
        return fLaunch;
    }
}
