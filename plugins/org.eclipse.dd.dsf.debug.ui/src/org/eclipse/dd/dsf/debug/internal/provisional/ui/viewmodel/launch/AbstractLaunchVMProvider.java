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
package org.eclipse.dd.dsf.debug.internal.provisional.ui.viewmodel.launch;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.dd.dsf.concurrent.DsfRunnable;
import org.eclipse.dd.dsf.concurrent.RequestMonitor;
import org.eclipse.dd.dsf.concurrent.ThreadSafe;
import org.eclipse.dd.dsf.datamodel.DMContexts;
import org.eclipse.dd.dsf.datamodel.IDMContext;
import org.eclipse.dd.dsf.datamodel.IDMEvent;
import org.eclipse.dd.dsf.debug.internal.provisional.ui.viewmodel.launch.LaunchRootVMNode.LaunchesEvent;
import org.eclipse.dd.dsf.debug.service.IRunControl;
import org.eclipse.dd.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.dd.dsf.debug.service.IRunControl.ISuspendedDMEvent;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.dd.dsf.ui.viewmodel.AbstractVMAdapter;
import org.eclipse.dd.dsf.ui.viewmodel.IRootVMNode;
import org.eclipse.dd.dsf.ui.viewmodel.IVMModelProxy;
import org.eclipse.dd.dsf.ui.viewmodel.datamodel.AbstractDMVMProvider;
import org.eclipse.dd.dsf.ui.viewmodel.update.IVMUpdatePolicy;
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
public class AbstractLaunchVMProvider extends AbstractDMVMProvider 
    implements IDebugEventSetListener, ILaunchesListener2
{
    /**
	 * Delay (in milliseconds) before a full stack trace will be requested.
	 */
	private static final int FRAME_UPDATE_DELAY= 200;
	
    private final Map<IExecutionDMContext,ScheduledFuture<?>> fRefreshStackFramesFutures = new HashMap<IExecutionDMContext,ScheduledFuture<?>>();

	@ThreadSafe
    public AbstractLaunchVMProvider(AbstractVMAdapter adapter, IPresentationContext presentationContext, DsfSession session)
    {
        super(adapter, presentationContext, session);
    }
    
    @Override
	protected IVMUpdatePolicy[] createUpdateModes() {
		return new IVMUpdatePolicy[] { new DelayedStackRefreshUpdatePolicy() };
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
    protected void handleEvent(IVMModelProxy proxyStrategy, final Object event, RequestMonitor rm) {
    	super.handleEvent(proxyStrategy, event, rm);
    	
		if (event instanceof IRunControl.ISuspendedDMEvent) {
    		final IExecutionDMContext exeContext= ((IRunControl.ISuspendedDMEvent) event).getDMContext();
    		ScheduledFuture<?> refreshStackFramesFuture = getRefreshFuture(exeContext);
    		// trigger delayed full stack frame update
    		if (refreshStackFramesFuture != null) {
    			// cancel previously scheduled frame update
    			refreshStackFramesFuture.cancel(false);
    		}

    		refreshStackFramesFuture = getSession().getExecutor().schedule(
	            new DsfRunnable() { 
	                public void run() {
	                    if (getSession().isActive()) {
	                        getExecutor().execute(new Runnable() {
	                            public void run() {
	                                // trigger full stack frame update
	                                ScheduledFuture<?> future= fRefreshStackFramesFutures.get(exeContext);
	                                if (future != null && !isDisposed()) {
	                                    fRefreshStackFramesFutures.remove(exeContext);
	                                    handleEvent(new FullStackRefreshEvent(exeContext));
	                                }
	                            }});
	                    }
	                }
	            },
			    FRAME_UPDATE_DELAY, TimeUnit.MILLISECONDS);
			fRefreshStackFramesFutures.put(exeContext, refreshStackFramesFuture);
    	} else if (event instanceof IRunControl.IResumedDMEvent) {
    		IExecutionDMContext exeContext= ((IRunControl.IResumedDMEvent) event).getDMContext();
    		ScheduledFuture<?> refreshStackFramesFuture= fRefreshStackFramesFutures.get(exeContext);
    		if (refreshStackFramesFuture != null) {
    			// cancel previously scheduled frame update
    			refreshStackFramesFuture.cancel(false);
    			fRefreshStackFramesFutures.remove(exeContext);
    		}
    	}
    }

    /**
     * Returns the future for the given execution context or for any child of the 
     * given execution context.
     */
    private ScheduledFuture<?> getRefreshFuture(IExecutionDMContext execCtx) {
        for (IExecutionDMContext refreshCtx : fRefreshStackFramesFutures.keySet()) {
            if (refreshCtx.equals(execCtx) || DMContexts.isAncestorOf(refreshCtx, execCtx)) {
                return fRefreshStackFramesFutures.remove(refreshCtx);
            }
        }
        return null;
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
