/*******************************************************************************
 * Copyright (c) 2006, 2008 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Ericsson			  - Modified for new functionality	
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.viewmodel.launch;

import java.util.concurrent.RejectedExecutionException;

import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.concurrent.ThreadSafe;
import org.eclipse.cdt.dsf.debug.service.ICachingService;
import org.eclipse.cdt.dsf.debug.service.IProcesses;
import org.eclipse.cdt.dsf.debug.service.IRunControl;
import org.eclipse.cdt.dsf.debug.service.IStack;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlInitializedDMEvent;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlShutdownDMEvent;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.launch.AbstractLaunchVMProvider;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.launch.LaunchRootVMNode;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.launch.StackFramesVMNode;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.launch.StandardProcessVMNode;
import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.ui.viewmodel.AbstractVMAdapter;
import org.eclipse.cdt.dsf.ui.viewmodel.IRootVMNode;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMNode;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunchesListener2;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;


/**
 * 
 */
@SuppressWarnings("restriction")
public class LaunchVMProvider extends AbstractLaunchVMProvider 
    implements IDebugEventSetListener, ILaunchesListener2
{
	@ThreadSafe
    public LaunchVMProvider(AbstractVMAdapter adapter, IPresentationContext presentationContext, DsfSession session)
    {
        super(adapter, presentationContext, session);
        
        IRootVMNode launchNode = new LaunchRootVMNode(this);
        setRootNode(launchNode);

        // Container node to contain all processes and threads
        IVMNode containerNode = new ContainerVMNode(this, getSession());
        IVMNode processesNode = new StandardProcessVMNode(this);
        addChildNodes(launchNode, new IVMNode[] { containerNode, processesNode});
        
        IVMNode threadsNode = new ThreadVMNode(this, getSession());
        addChildNodes(containerNode, new IVMNode[] { threadsNode });
        
        IVMNode stackFramesNode = new StackFramesVMNode(this, getSession());
        addChildNodes(threadsNode, new IVMNode[] { stackFramesNode });

        
        DebugPlugin.getDefault().addDebugEventListener(this);
        DebugPlugin.getDefault().getLaunchManager().addLaunchListener(this);
    }
    
    @Override
    public void dispose() {
        DebugPlugin.getDefault().removeDebugEventListener(this);
        DebugPlugin.getDefault().getLaunchManager().removeLaunchListener(this);
        super.dispose();
    }
    
    @Override
    protected boolean canSkipHandlingEvent(Object newEvent, Object eventToSkip) {
        // Never skip the process lifecycle events.
        if (eventToSkip instanceof ICommandControlInitializedDMEvent ||
            eventToSkip instanceof ICommandControlShutdownDMEvent) 
        {
            return false;
        }
        return super.canSkipHandlingEvent(newEvent, eventToSkip);
    }

    @Override
    public void refresh() {
        super.refresh();
        try {
            getSession().getExecutor().execute(new DsfRunnable() {
                public void run() {
                    DsfServicesTracker tracker = new DsfServicesTracker(GdbUIPlugin.getBundleContext(), getSession().getId());
                    IProcesses processesService = tracker.getService(IProcesses.class);
                    if (processesService instanceof ICachingService) {
                        ((ICachingService)processesService).flushCache(null);
                    }
                    IStack stackService = tracker.getService(IStack.class);
                    if (stackService instanceof ICachingService) {
                        ((ICachingService)stackService).flushCache(null);
                    }
                    IRunControl runControlService = tracker.getService(IRunControl.class);
                    if (runControlService instanceof ICachingService) {
                        ((ICachingService)runControlService).flushCache(null);
                    }
                    tracker.dispose();
                }
            });
        } catch (RejectedExecutionException e) {
            // Session disposed, ignore.
        }
    }
}
