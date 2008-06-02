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
package org.eclipse.dd.gdb.internal.ui.viewmodel.launch;

import java.util.concurrent.RejectedExecutionException;

import org.eclipse.dd.dsf.concurrent.ThreadSafe;
import org.eclipse.dd.dsf.datamodel.DMContexts;
import org.eclipse.dd.dsf.datamodel.IDMContext;
import org.eclipse.dd.dsf.datamodel.IDMEvent;
import org.eclipse.dd.dsf.debug.internal.provisional.ui.viewmodel.launch.LaunchRootVMNode;
import org.eclipse.dd.dsf.debug.internal.provisional.ui.viewmodel.launch.StackFramesVMNode;
import org.eclipse.dd.dsf.debug.internal.provisional.ui.viewmodel.launch.StandardProcessVMNode;
import org.eclipse.dd.dsf.debug.internal.provisional.ui.viewmodel.launch.LaunchRootVMNode.LaunchesEvent;
import org.eclipse.dd.dsf.debug.service.IRunControl.ISuspendedDMEvent;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.dd.dsf.ui.viewmodel.AbstractVMAdapter;
import org.eclipse.dd.dsf.ui.viewmodel.IRootVMNode;
import org.eclipse.dd.dsf.ui.viewmodel.IVMNode;
import org.eclipse.dd.dsf.ui.viewmodel.datamodel.AbstractDMVMProvider;
import org.eclipse.dd.gdb.internal.provisional.service.command.GDBControl.GDBExitedEvent;
import org.eclipse.dd.gdb.internal.provisional.service.command.GDBControl.GDBStartedEvent;
import org.eclipse.dd.mi.service.command.MIInferiorProcess.InferiorExitedDMEvent;
import org.eclipse.dd.mi.service.command.MIInferiorProcess.InferiorStartedDMEvent;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchesListener2;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;


/**
 * 
 */
@SuppressWarnings("restriction")
public class LaunchVMProvider extends AbstractDMVMProvider 
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
    
    
    public void handleDebugEvents(final DebugEvent[] events) {
        if (isDisposed()) return;
        
        // We're in session's executor thread.  Re-dispach to VM Adapter 
        // executor thread and then call root layout node.
        try {
            getExecutor().execute(new Runnable() {
                public void run() {
                    if (isDisposed()) return;
    
                    for (final DebugEvent event : events) {
                        handleEvent(event);
                    }
                }});
        } catch (RejectedExecutionException e) {
            // Ignore.  This exception could be thrown if the provider is being 
            // shut down.  
        }
    }

    @Override
    public void dispose() {
        DebugPlugin.getDefault().removeDebugEventListener(this);
        DebugPlugin.getDefault().getLaunchManager().removeLaunchListener(this);
        super.dispose();
    }
    
    public void launchesAdded(ILaunch[] launches) {
        handleLaunchesEvent(new LaunchesEvent(launches, LaunchesEvent.Type.ADDED)); 
    }
    
    public void launchesRemoved(ILaunch[] launches) {
        handleLaunchesEvent(new LaunchesEvent(launches, LaunchesEvent.Type.REMOVED)); 
    }
    
    public void launchesChanged(ILaunch[] launches) {
        handleLaunchesEvent(new LaunchesEvent(launches, LaunchesEvent.Type.CHANGED)); 
    }
    
    public void launchesTerminated(ILaunch[] launches) {
        handleLaunchesEvent(new LaunchesEvent(launches, LaunchesEvent.Type.TERMINATED)); 
    }
    
    private void handleLaunchesEvent(final LaunchesEvent event) {
        if (isDisposed()) return;
        
        // We're in session's executor thread.  Re-dispach to VM Adapter 
        // executor thread and then call root layout node.
        try {
            getExecutor().execute(new Runnable() {
                public void run() {
                    if (isDisposed()) return;
    
                    IRootVMNode rootLayoutNode = getRootVMNode();
                    if (rootLayoutNode != null && rootLayoutNode.getDeltaFlags(event) != 0) {
                        handleEvent(event);
                    }
                }});
        } catch (RejectedExecutionException e) {
            // Ignore.  This exception could be thrown if the provider is being 
            // shut down.  
        }
    }
    
    @Override
    protected boolean canSkipHandlingEvent(Object newEvent, Object eventToSkip) {
        // Never skip the process lifecycle events.
        if (eventToSkip instanceof InferiorExitedDMEvent || 
            eventToSkip instanceof InferiorStartedDMEvent ||
            eventToSkip instanceof GDBStartedEvent ||
            eventToSkip instanceof GDBExitedEvent) 
        {
            return false;
        }
        

        // To optimize view performance when stepping rapidly, skip events that came 
        // before the last suspended events.  However, the debug view can get suspended
        // events for different threads, so make sure to skip only the events if they
        // were in the same hierarchy as the last suspended event.
        if (newEvent instanceof ISuspendedDMEvent && eventToSkip instanceof IDMEvent<?>) {
            IDMContext newEventDmc = ((IDMEvent<?>)newEvent).getDMContext();
            IDMContext eventToSkipDmc = ((IDMEvent<?>)eventToSkip).getDMContext();
            
            if (newEventDmc.equals(eventToSkipDmc) || DMContexts.isAncestorOf(eventToSkipDmc, newEventDmc)) {
                return true;
            }
        }
        
        return false;
    }

}
