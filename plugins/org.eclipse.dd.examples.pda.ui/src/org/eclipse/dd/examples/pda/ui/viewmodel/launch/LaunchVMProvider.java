/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Ericsson			  - Modified for new functionality	
 *******************************************************************************/
package org.eclipse.dd.examples.pda.ui.viewmodel.launch;

import java.util.concurrent.RejectedExecutionException;

import org.eclipse.dd.dsf.concurrent.ThreadSafe;
import org.eclipse.dd.dsf.debug.ui.viewmodel.launch.LaunchRootVMNode;
import org.eclipse.dd.dsf.debug.ui.viewmodel.launch.StackFramesVMNode;
import org.eclipse.dd.dsf.debug.ui.viewmodel.launch.StandardProcessVMNode;
import org.eclipse.dd.dsf.debug.ui.viewmodel.launch.LaunchRootVMNode.LaunchesEvent;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.dd.dsf.ui.viewmodel.AbstractVMAdapter;
import org.eclipse.dd.dsf.ui.viewmodel.IRootVMNode;
import org.eclipse.dd.dsf.ui.viewmodel.IVMNode;
import org.eclipse.dd.dsf.ui.viewmodel.datamodel.AbstractDMVMProvider;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchesListener2;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;


/**
 * View Model provider for the Launch (AKA Debug) view.  The PDA debugger is 
 * single-threaded, so there is no need for a debug target element to be visible
 * in the debug view.  Therefore the launch VM provider is configured with three nodes:
 * <ul>
 * <li> LaunchRootVMNode - This is the root of the PDA view model.</li>  
 * <li> ThreadVMNode - Supplies the PDA program element.</li>  
 * <li> StackFramesVMNode - Supplies the stack frame elements.</li>  
 * <li> StandardProcessVMNode - Supplies elements representing the PDA debugger process.</li>
 * </ul> 
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

        // Launch node is a parent to the processes and program nodes.
        IVMNode threadsNode = new PDAProgramVMNode(this, getSession());
        IVMNode processesNode = new StandardProcessVMNode(this);
        addChildNodes(launchNode, new IVMNode[] { threadsNode, processesNode});
        
        // Stack frames node is under the PDA program node.
        IVMNode stackFramesNode = new StackFramesVMNode(this, getSession());
        addChildNodes(threadsNode, new IVMNode[] { stackFramesNode });

        // Register the LaunchVM provider as a listener to debug and launch 
        // events.  These events are used by the launch and processes nodes.
        DebugPlugin.getDefault().addDebugEventListener(this);
        DebugPlugin.getDefault().getLaunchManager().addLaunchListener(this);
    }
    
    
    public void handleDebugEvents(final DebugEvent[] events) {
        if (isDisposed()) return;
        
        // This method may be called on any thread.  Switch to the 
        // view model executor thread before processing.
        try {
            getExecutor().execute(new Runnable() {
                public void run() {
                    if (isDisposed()) return;
    
                    for (final DebugEvent event : events) {
                        handleEvent(event);
                    }
                }});
        } catch (RejectedExecutionException e) {
            // Ignore.  This exception could be thrown if the view model is being 
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
        
        // This method also may be called on any thread.  Switch to the 
        // view model executor thread before processing.
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
            // Ignore.  This exception could be thrown if the view model is being 
            // shut down.  
        }
    }
}
