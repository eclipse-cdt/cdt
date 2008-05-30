/*******************************************************************************
 * Copyright (c) 2006, 2008 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.dd.dsf.debug.internal.provisional.ui.viewmodel.launch;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.dd.dsf.concurrent.DataRequestMonitor;
import org.eclipse.dd.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.dd.dsf.debug.internal.ui.DsfDebugUIPlugin;
import org.eclipse.dd.dsf.ui.viewmodel.AbstractVMProvider;
import org.eclipse.dd.dsf.ui.viewmodel.IRootVMNode;
import org.eclipse.dd.dsf.ui.viewmodel.RootVMNode;
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
public class LaunchRootVMNode extends RootVMNode 
    implements IRootVMNode
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
    
    
    public LaunchRootVMNode(AbstractVMProvider provider) {
        super(provider);
    }

    @Override
    public boolean isDeltaEvent(Object rootObject, Object e) {
        if (e instanceof DebugEvent) {
            DebugEvent de = (DebugEvent)e;
            if (de.getSource() instanceof IProcess && 
                !((IProcess)de.getSource()).getLaunch().equals(rootObject) ) 
            {
                return false;
            } 
            else if (de.getSource() instanceof IDebugElement && 
                     !rootObject.equals(((IDebugElement)de.getSource()).getLaunch()))
            {
                return false;
            }
        }
        return super.isDeltaEvent(rootObject, e);
    }
    
    @Override
    public int getDeltaFlags(Object e) {
        int flags = 0;
        if (e instanceof LaunchesEvent) {
            LaunchesEvent le = (LaunchesEvent)e;
            if (le.fType == LaunchesEvent.Type.CHANGED || le.fType == LaunchesEvent.Type.TERMINATED) {
                flags = IModelDelta.STATE | IModelDelta.CONTENT;
            }
        }
        
        return flags;
    }

    @Override
    public void createRootDelta(Object rootObject, Object event, final DataRequestMonitor<VMDelta> rm) {
        if (!(rootObject instanceof ILaunch)) {
            rm.setStatus(new Status(IStatus.ERROR, DsfDebugUIPlugin.PLUGIN_ID, IDsfStatusConstants.INVALID_STATE, "Invalid root element configured with launch root node.", null)); //$NON-NLS-1$
            return;
        }
        
        ILaunch rootLaunch = (ILaunch)rootObject;
        
        /*
         * Create the root of the delta.  Since the launch object is not at the 
         * root of the view, create the delta with the path to the launch.
         */
        ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
        List<ILaunch> launchList = Arrays.asList(manager.getLaunches());
        final VMDelta viewRootDelta = new VMDelta(manager, 0, IModelDelta.NO_CHANGE, launchList.size());
        final VMDelta rootDelta = viewRootDelta.addNode(rootLaunch, launchList.indexOf(rootLaunch), IModelDelta.NO_CHANGE);

        // Generate delta for launch node.
        if (event instanceof LaunchesEvent) {
            LaunchesEvent le = (LaunchesEvent)event;
            for (ILaunch launch : le.fLaunches) {
                if (rootLaunch == launch) {
                    if (le.fType == LaunchesEvent.Type.CHANGED) {
                        rootDelta.setFlags(rootDelta.getFlags() | IModelDelta.STATE | IModelDelta.CONTENT);
                    } else if (le.fType == LaunchesEvent.Type.TERMINATED) {
                        rootDelta.setFlags(rootDelta.getFlags() | IModelDelta.STATE | IModelDelta.CONTENT);
                    }
                }
            }
        } 
        
        rm.setData(rootDelta);
        rm.done();
    }
    
}
