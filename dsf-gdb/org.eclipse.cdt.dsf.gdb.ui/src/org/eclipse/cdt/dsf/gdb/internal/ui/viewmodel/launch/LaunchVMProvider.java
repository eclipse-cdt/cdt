/*******************************************************************************
 * Copyright (c) 2006, 2014 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Ericsson			  - Modified for new functionality
 *     Marc Khouzam (Ericsson) - Allow to override this class (Bug 455537)
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.viewmodel.launch;

import java.util.concurrent.RejectedExecutionException;

import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
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
import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
import org.eclipse.cdt.dsf.gdb.service.IGDBTraceControl.ITraceRecordSelectedChangedDMEvent;
import org.eclipse.cdt.dsf.gdb.service.IGDBTraceControl.ITracingStartedDMEvent;
import org.eclipse.cdt.dsf.gdb.service.IGDBTraceControl.ITracingStoppedDMEvent;
import org.eclipse.cdt.dsf.gdb.service.IGDBTraceControl.ITracingSupportedChangeDMEvent;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.ui.viewmodel.AbstractVMAdapter;
import org.eclipse.cdt.dsf.ui.viewmodel.IRootVMNode;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMNode;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunchesListener2;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;


/**
 * 
 */
public class LaunchVMProvider extends AbstractLaunchVMProvider 
    implements IDebugEventSetListener, ILaunchesListener2
{
	
	/**
	 * Indicates that we are currently visualizing trace data.
	 */
	private boolean fTracepointVisualizationModeEnabled;
	
	@ThreadSafe
    public LaunchVMProvider(AbstractVMAdapter adapter, IPresentationContext presentationContext, DsfSession session)
    {
        super(adapter, presentationContext, session);
        
        createNodes();
    }

	protected void createNodes() {
        IRootVMNode launchNode = new LaunchRootVMNode(this);
        setRootNode(launchNode);

        IVMNode processesNode = new ContainerVMNode(this, getSession());
        IVMNode groupsNode = new GroupVMNode(this, getSession());
        IVMNode launchProcessesNode = new GdbStandardProcessVMNode(this);
        IVMNode threadsNode = new ThreadVMNode(this, getSession());
        IVMNode stackFramesNode = new StackFramesVMNode(this, getSession());

        // Allow the launch node to have processes or groups as children, as well as the launch-processes
        // nodes (gdb and inferior nodes)
        addChildNodes(launchNode, new IVMNode[] { processesNode, groupsNode, launchProcessesNode});
        
        // Process nodes can have groups or threads as children
        addChildNodes(processesNode, new IVMNode[] { groupsNode, threadsNode });

        // Group nodes can have other groups, processes or threads as children
//        addChildNodes(groupsNode, new IVMNode[] { processesNode, groupsNode, threadsNode }); This causes an infinite loop somewhere
        addChildNodes(groupsNode, new IVMNode[] { groupsNode, threadsNode });
        
        // Threads can only have stack frames as children
        addChildNodes(threadsNode, new IVMNode[] { stackFramesNode });
    }
    
    @Override
    protected boolean canSkipHandlingEvent(Object newEvent, Object eventToSkip) {
        // Never skip the process lifecycle events.
        if (eventToSkip instanceof ICommandControlInitializedDMEvent ||
            eventToSkip instanceof ICommandControlShutdownDMEvent) 
        {
            return false;
        }
        
        if (eventToSkip instanceof ITracingStartedDMEvent || 
        	eventToSkip instanceof ITracingStoppedDMEvent) 
        {
        	if (newEvent instanceof ITracingStartedDMEvent || 
        		newEvent instanceof ITracingStoppedDMEvent) 
        	{
        		return true;
        	}
        }
        
        if (eventToSkip instanceof ITracingSupportedChangeDMEvent) 
        {
        	if (newEvent instanceof ITracingSupportedChangeDMEvent) 
        	{
        		return true;
        	}
        }
        
        if (eventToSkip instanceof ITraceRecordSelectedChangedDMEvent) {
    		ITraceRecordSelectedChangedDMEvent recordChanged = (ITraceRecordSelectedChangedDMEvent)eventToSkip;
    		if (recordChanged.isVisualizationModeEnabled() == fTracepointVisualizationModeEnabled) {
    			// We only care about this event if it indicates a change of visualization state
    			return true;
    		}
        }
        
        return super.canSkipHandlingEvent(newEvent, eventToSkip);
    }
    
    @Override
    public void handleEvent(Object event, RequestMonitor rm) {
    	if (event instanceof ITracingStartedDMEvent || 
    		event instanceof ITracingStoppedDMEvent ||
    		event instanceof ITracingSupportedChangeDMEvent)
    	{
    		// Refresh the view to trigger a context change, which
    		// will cause command enablement to be refreshed
    		refresh();
    		rm.done();
    		return;
    	}    

    	if (event instanceof ITraceRecordSelectedChangedDMEvent) {
    		ITraceRecordSelectedChangedDMEvent recordChanged = (ITraceRecordSelectedChangedDMEvent)event;
    		// If trace visualization has changed we have to refresh the debug view
    		if (recordChanged.isVisualizationModeEnabled() != fTracepointVisualizationModeEnabled) {
    			fTracepointVisualizationModeEnabled = recordChanged.isVisualizationModeEnabled();
    			
        		// Refresh the view because the set of threads has totally changed.
        		refresh();
        		rm.done();
        		return;
        	}
    	}
    	
    	super.handleEvent(event, rm);
    }

    @Override
    public void refresh() {
        super.refresh();
        try {
            getSession().getExecutor().execute(new DsfRunnable() {
                @Override
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

    //TODO why do we need this?
    // Currently it causes an infinite loop
//    @Override
//    protected IVMModelProxy createModelProxyStrategy(Object rootElement) {
//    	// user groups support
//    	DefaultVMModelProxyStrategy proxy = new DefaultVMModelProxyStrategy(this, rootElement);
//    	proxy.setAllowRecursiveVMNodes(true);
//    	return proxy;
//    }
}
