/*******************************************************************************
 * Copyright (c) 2007, 2015 Wind River and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River - Initial API and implementation
 *     Ericsson   - High-level breakpoints integration
 *     Ericsson   - Added breakpoint filter support
 *     Ericsson   - Re-factored the service and put a few comments
 *     Ericsson   - Added Action support
 *     Marc Khouzam (Ericsson) - Fix support for thread filter (Bug 355833)
 *     Marc Khouzam (Ericsson) - Generalize thread filtering logic (Bug 431986)
 *     Marc Khouzam (Ericsson) - Accept multiple calls to startTrackingBreakpoints (Bug 389945)
 *     Marc Khouzam (Ericsson) - Support for dynamic printf (Bug 400628)
 *     Alvaro Sanchez-Leon (Ericsson) - Sometimes breakpoints set and immediately deleted when debugging with GDB (Bug 442394)
 *     Alvaro Sanchez-Leon (Ericsson) - Breakpoint Enable does not work after restarting the application (Bug 456959)
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.RejectedExecutionException;

import org.eclipse.cdt.core.model.ICModelMarker;
import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.breakpointactions.BreakpointActionManager;
import org.eclipse.cdt.debug.core.model.ICAddressBreakpoint;
import org.eclipse.cdt.debug.core.model.ICBreakpoint;
import org.eclipse.cdt.debug.core.model.ICBreakpointExtension;
import org.eclipse.cdt.debug.core.model.ICBreakpointType;
import org.eclipse.cdt.debug.core.model.ICDynamicPrintf;
import org.eclipse.cdt.debug.core.model.ICEventBreakpoint;
import org.eclipse.cdt.debug.core.model.ICFunctionBreakpoint;
import org.eclipse.cdt.debug.core.model.ICLineBreakpoint;
import org.eclipse.cdt.debug.core.model.ICTracepoint;
import org.eclipse.cdt.debug.core.model.ICWatchpoint;
import org.eclipse.cdt.debug.internal.core.breakpoints.BreakpointProblems;
import org.eclipse.cdt.dsf.concurrent.CountingRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.concurrent.ImmediateDataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateExecutor;
import org.eclipse.cdt.dsf.concurrent.ImmediateRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ThreadSafe;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointDMContext;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointDMData;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointsTargetDMContext;
import org.eclipse.cdt.dsf.debug.service.IDsfBreakpointExtension;
import org.eclipse.cdt.dsf.debug.service.IProcesses;
import org.eclipse.cdt.dsf.debug.service.IRunControl;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExitedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IStartedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.ISuspendedDMEvent;
import org.eclipse.cdt.dsf.debug.service.ISourceLookup;
import org.eclipse.cdt.dsf.debug.service.ISourceLookup.ISourceLookupDMContext;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlShutdownDMEvent;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.mi.service.MIBreakpoints.BreakpointAddedEvent;
import org.eclipse.cdt.dsf.mi.service.MIBreakpoints.BreakpointRemovedEvent;
import org.eclipse.cdt.dsf.mi.service.MIBreakpoints.BreakpointUpdatedEvent;
import org.eclipse.cdt.dsf.mi.service.MIBreakpoints.MIBreakpointDMContext;
import org.eclipse.cdt.dsf.mi.service.MIRunControl.SuspendedEvent;
import org.eclipse.cdt.dsf.mi.service.breakpoint.actions.BreakpointActionAdapter;
import org.eclipse.cdt.dsf.mi.service.command.events.IMIDMEvent;
import org.eclipse.cdt.dsf.mi.service.command.events.MIBreakpointHitEvent;
import org.eclipse.cdt.dsf.mi.service.command.events.MIWatchpointScopeEvent;
import org.eclipse.cdt.dsf.mi.service.command.events.MIWatchpointTriggerEvent;
import org.eclipse.cdt.dsf.service.AbstractDsfService;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.gdb.internal.eventbkpts.GdbCatchpoints;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.MultiRule;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointListener;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.IBreakpointManagerListener;
import org.eclipse.debug.core.model.IBreakpoint;
import org.osgi.framework.BundleContext;

import com.ibm.icu.text.MessageFormat;

/**
 * Breakpoint service interface.  The breakpoint service tracks CDT breakpoint
 * objects, and based on those, it manages breakpoints in the debugger back end.
 * 
 * It relies on MIBreakpoints for the actual back-end interface.
 */
public class MIBreakpointsManager extends AbstractDsfService implements IBreakpointManagerListener, IBreakpointListener
{
    /**
     * A listener is notified by {@link MIBreakpointsManager} when 
     * the breakpoints tracking starts or stops. 
     * @since 4.2
     */
    public interface IMIBreakpointsTrackingListener {

    	public void breakpointTrackingStarted(IBreakpointsTargetDMContext bpTargetDMC);
    	
    	public void breakpointTrackingStopped(IBreakpointsTargetDMContext bpTargetDMC);
	}

	// Note: Find a way to import this (careful of circular dependencies)
    public final static String GDB_DEBUG_MODEL_ID = "org.eclipse.cdt.dsf.gdb"; //$NON-NLS-1$

    // Extra breakpoint attributes
    private static final String ATTR_DEBUGGER_PATH = GdbPlugin.PLUGIN_ID + ".debuggerPath";   //$NON-NLS-1$
    private static final String ATTR_THREAD_FILTER = GdbPlugin.PLUGIN_ID + ".threadFilter";   //$NON-NLS-1$
    private static final String ATTR_THREAD_ID     = GdbPlugin.PLUGIN_ID + ".threadID";       //$NON-NLS-1$

    // Services
    ICommandControlService fConnection;
    IRunControl        fRunControl;
    ISourceLookup      fSourceLookup;
    IProcesses         fProcesses;
    IBreakpoints       fBreakpoints;
    IBreakpointManager fBreakpointManager;  // Platform breakpoint manager (not this!)
    BreakpointActionManager fBreakpointActionManager;
    
    ///////////////////////////////////////////////////////////////////////////
    // Breakpoints tracking
    ///////////////////////////////////////////////////////////////////////////

    private String fDebugModelId;
    
    // Holds the set of platform breakpoints with their corresponding back-end
    // breakpoint attributes, per context (i.e. each platform breakpoint is
    // replicated for each execution context).
    // - Context entry added/removed on start/stopTrackingBreakpoints()
    // - Augmented on breakpointAdded()
    // - Modified on breakpointChanged()
    // - Diminished on breakpointRemoved()
    private Map<IBreakpointsTargetDMContext, Map<ICBreakpoint, Map<String, Object>>> fPlatformToAttributesMaps = new HashMap<>();

	/**
	 * Returns the structure that maps each breakpoint target to a map of platform breakpoints 
	 * and their corresponding back-end attributes.
	 * @since 4.7
	 */
	protected Map<IBreakpointsTargetDMContext, Map<ICBreakpoint, Map<String, Object>>> getPlatformToAttributesMaps() {
		return fPlatformToAttributesMaps;
	}

    // Holds the set of target breakpoints, per execution context, and their
    // mapping to the corresponding platform breakpoint. In a given execution
    // context there can only be one platform breakpoint per target breakpoint.
    // Acts as a mapping from target (low-level) BP to the corresponding platform
    // (high-level) BP.
    // Updated when:
    // - We start/stop tracking an execution context
    // - A platform breakpoint is added/removed
    // - A thread filter is applied/removed
    private Map<IBreakpointsTargetDMContext, Map<IBreakpointDMContext, ICBreakpoint>> fBPToPlatformMaps = new HashMap<>();

    /**
	 * Returns the structure that maps each breakpoint target to a map of back-end breakpoints 
	 * and their corresponding platform breakpoint.
	 * @since 4.7
	 */
    protected Map<IBreakpointsTargetDMContext, Map<IBreakpointDMContext, ICBreakpoint>> getBPToPlatformMaps() {
    	return fBPToPlatformMaps;
    }
    
    // Holds the mapping from platform breakpoint to the corresponding target
    // breakpoint(s), per context. There can be multiple back-end BPs for a
    // single platform BP in the case of [1] multiple target contexts, and/or
    // [2] thread filtering.
    // Updated when:
    // - We start/stop tracking an execution context
    // - A platform breakpoint is added/removed
    // - A thread filter is applied/removed
    private Map<IBreakpointsTargetDMContext, Map<ICBreakpoint, Vector<IBreakpointDMContext>>> fPlatformToBPsMaps = new HashMap<>();

    /**
	 * Returns the structure that maps each breakpoint target to a map of platform breakpoints 
	 * and their corresponding vector of back-end breakpoints.
	 * @since 4.7
	 */
    protected Map<IBreakpointsTargetDMContext, Map<ICBreakpoint, Vector<IBreakpointDMContext>>> getPlatformToBPsMaps() {
    	return fPlatformToBPsMaps;
    }

    // Holds the mapping from platform breakpoint to the corresponding target
    // breakpoint threads, per context.
    // Updated when:
    // - We start/stop tracking an execution context
    // - A platform breakpoint is added/removed
    // - A thread filter is applied/removed
    private Map<IBreakpointsTargetDMContext, Map<ICBreakpoint, Set<String>>> fPlatformToBPThreadsMaps = new HashMap<>();

    /**
	 * Returns the structure that maps each breakpoint target to a map of platform breakpoints 
	 * and their corresponding back-end breakpoint thread ids.
	 * @since 4.7
	 */
    protected Map<IBreakpointsTargetDMContext, Map<ICBreakpoint, Set<String>>> getPlatformToBPThreadsMaps() {
    	return fPlatformToBPThreadsMaps;
    }

    // Due to the very asynchronous nature of DSF, a new breakpoint request can
    // pop up at any time before an ongoing one is completed. The following set
    // is used to store requests until the ongoing operation completes.
    private Set<IBreakpoint> fPendingRequests    = new HashSet<>();
    private Set<IBreakpoint> fPendingBreakpoints = new HashSet<>();

    private Map<ICBreakpoint, IMarker> fBreakpointMarkerProblems = new HashMap<>();

    private ListenerList fTrackingListeners = new ListenerList();

    ///////////////////////////////////////////////////////////////////////////
    // String constants
    ///////////////////////////////////////////////////////////////////////////

    private static final String NULL_STRING = ""; //$NON-NLS-1$

    static final String CONTEXT_ALREADY_INITIALIZED  = "Context already initialized";  //$NON-NLS-1$
    static final String INVALID_CONTEXT_TYPE         = "Invalid context type";         //$NON-NLS-1$
    static final String INVALID_CONTEXT              = "Invalid context";              //$NON-NLS-1$

    static final String UNABLE_TO_READ_BREAKPOINT    = "Unable to read initial breakpoint attributes"; //$NON-NLS-1$
    static final String BREAKPOINT_NOT_INSTALLED     = "Breakpoints not installed for given context";  //$NON-NLS-1$
    static final String BREAKPOINT_ALREADY_INSTALLED = "Breakpoint already installed"; //$NON-NLS-1$
    static final String BREAKPOINT_ALREADY_REMOVED   = "Breakpoint already removed";   //$NON-NLS-1$

    static final String INVALID_BREAKPOINT           = "Invalid breakpoint";                    //$NON-NLS-1$
    static final String UNKNOWN_BREAKPOINT           = "Unknown breakpoint";                    //$NON-NLS-1$
    static final String INVALID_PARAMETER            = "Invalid breakpoint parameter(s)";       //$NON-NLS-1$

    static final String NO_DEBUGGER_PATH             = "No debugger path for breakpoint";       //$NON-NLS-1$
    static final String NO_MARKER_FOR_BREAKPOINT     = "No marker associated with breakpoint";  //$NON-NLS-1$

    ///////////////////////////////////////////////////////////////////////////
    // AbstractDsfService
    ///////////////////////////////////////////////////////////////////////////

    /**
     * The service constructor.
     * Performs basic instantiation (method initialize() performs the real
     * service initialization asynchronously).
     * 
     * @param session       the debugging session
     * @param debugModelId  the debugging model
     */
    public MIBreakpointsManager(DsfSession session, String debugModelId) {
        super(session);
        fDebugModelId = debugModelId;
    }

    //-------------------------------------------------------------------------
    // initialize
    //-------------------------------------------------------------------------
    // - Collect references for the services we interact with
    // - Register to interesting events
    // - Obtain the list of platform breakpoints
    // - Register the service for interested parties
    //-------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.eclipse.cdt.dsf.service.AbstractDsfService#initialize(org.eclipse.cdt.dsf.concurrent.RequestMonitor)
     */
    @Override
    public void initialize(final RequestMonitor rm) {
        super.initialize(
            new ImmediateRequestMonitor(rm) {
                @Override
                protected void handleSuccess() {
                    doInitialize(rm);
                }});
    }

    /**
     * @param rm
     */
    private void doInitialize(RequestMonitor rm) {
        
        // Get the required services references from central repository
        fConnection     = getServicesTracker().getService(ICommandControlService.class);
        fRunControl     = getServicesTracker().getService(IRunControl.class);
        fSourceLookup   = getServicesTracker().getService(ISourceLookup.class);
        fBreakpoints    = getServicesTracker().getService(IBreakpoints.class);
        fProcesses      = getServicesTracker().getService(IProcesses.class);
        fBreakpointManager = DebugPlugin.getDefault().getBreakpointManager();
        fBreakpointActionManager = CDebugCorePlugin.getDefault().getBreakpointActionManager();

        // Register to the useful events
        getSession().addServiceEventListener(this, null);
        fBreakpointManager.addBreakpointListener(this);
        fBreakpointManager.addBreakpointManagerListener(this);

        // And register this service
        register(new String[] { MIBreakpointsManager.class.getName() },
                 new Hashtable<String, String>());
        rm.done();
    }

    //-------------------------------------------------------------------------
    // shutdown
    //-------------------------------------------------------------------------
    // - Un-register the service
    // - Stop listening to events
    // - Remove the breakpoints installed by this service
    //
    //  Since we are shutting down, there is no overwhelming need
    //  to keep the maps coherent...
    //-------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.dsf.service.AbstractDsfService#shutdown(org.eclipse.cdt.dsf.concurrent.RequestMonitor)
     */
    @Override
    public void shutdown(final RequestMonitor rm) {

        // Stop accepting requests and events
        unregister();
        getSession().removeServiceEventListener(this);
        fBreakpointManager.removeBreakpointListener(this);
        fBreakpointManager.removeBreakpointManagerListener(this);
        fTrackingListeners.clear();

        // Cleanup the breakpoints that are still installed by the service.
        // Use a counting monitor which will call mom to complete the shutdown
        // after the breakpoints are un-installed (successfully or not).
        CountingRequestMonitor countingRm = new CountingRequestMonitor(getExecutor(), rm) {
            @Override
            protected void handleCompleted() {
                MIBreakpointsManager.super.shutdown(rm);
            }
        };

        List<IBreakpointsTargetDMContext> targetBPKeys = new ArrayList<>(fBPToPlatformMaps.size());
        targetBPKeys.addAll(0, fBPToPlatformMaps.keySet());
        for (IBreakpointsTargetDMContext dmc : targetBPKeys) {
            stopTrackingBreakpoints(dmc, countingRm);
        }
        countingRm.setDoneCount(targetBPKeys.size());
    }

    //-------------------------------------------------------------------------
    // getBundleContext
    //-------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.eclipse.cdt.dsf.service.AbstractDsfService#getBundleContext()
     */
    @Override
    protected BundleContext getBundleContext() {
        return GdbPlugin.getBundleContext();
    }

    ///////////////////////////////////////////////////////////////////////////
    // IBreakpointsManager
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Wrapper around startTrackingBreakpoints() which accepts a containerDmc and sets
     * each breakpoints filter to include that container.  This method should be called
     * instead of startTrackingBreakpoints()
     * 
     * @param containerDmc The container to be added in the bp filter.  This container
     *                     must have the proper IBreakpointsTargetDMContext in its hierarchy.
     * 
	 * @since 4.6
	 */    
	public void startTrackingBpForProcess(final IContainerDMContext containerDmc, final RequestMonitor rm) {
		final IBreakpointsTargetDMContext targetBpDmc = DMContexts.getAncestorOfType(containerDmc,
				IBreakpointsTargetDMContext.class);

		IBreakpoint[] breakpoints = DebugPlugin.getDefault().getBreakpointManager().getBreakpoints(fDebugModelId);
		for (IBreakpoint breakpoint : breakpoints) {
			if (breakpoint instanceof ICBreakpoint && supportsBreakpoint(breakpoint)) {
				setTargetFilter((ICBreakpoint) breakpoint, containerDmc);
			}
		}
		startTrackingBreakpoints(targetBpDmc, rm);
	}
    
    //-------------------------------------------------------------------------
    // startTrackingBreakpoints
    //-------------------------------------------------------------------------
    // - Augment the maps with the new execution context
    // - Install the platform breakpoints on the selected target
    //-------------------------------------------------------------------------

    /**
     * Install and begin tracking breakpoints for given context.  The service
     * will keep installing new breakpoints that appear in the IDE for this
     * context until {@link #uninstallBreakpoints(IDMContext)} is called for that
     * context.
     * @param dmc Context to start tracking breakpoints for.
     * @param rm Completion callback.
     */
    public void startTrackingBreakpoints(final IBreakpointsTargetDMContext dmc, final RequestMonitor rm) {

        // Validate the execution context
        if (dmc == null) {
            rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, REQUEST_FAILED, INVALID_CONTEXT, null));
            rm.done();
            return;
        }

        Map<ICBreakpoint,Map<String, Object>> platformBPs = fPlatformToAttributesMaps.get(dmc);
        Map<ICBreakpoint, Vector<IBreakpointDMContext>> breakpointIDs = fPlatformToBPsMaps.get(dmc);
        Map<IBreakpointDMContext, ICBreakpoint> targetIDs = fBPToPlatformMaps.get(dmc);
        Map<ICBreakpoint, Set<String>> threadIDs = fPlatformToBPThreadsMaps.get(dmc);
        if ((platformBPs != null) || (breakpointIDs != null) || (targetIDs != null) || (threadIDs != null)) {
        	// If the maps already contains this context we can simply ignore this request.
        	// This happens when we start or attach to another process with GDB >= 7.4
            assert platformBPs != null && breakpointIDs != null && targetIDs != null && threadIDs != null;
            rm.done();
            return;
        }

        // Create entries in the breakpoint tables for the new context. These entries should only
        // be removed when this service stops tracking breakpoints for the given context.
        fPlatformToAttributesMaps.put(dmc, new HashMap<ICBreakpoint, Map<String, Object>>());
        fPlatformToBPsMaps.put(dmc, new HashMap<ICBreakpoint, Vector<IBreakpointDMContext>>());
        fBPToPlatformMaps.put(dmc, new HashMap<IBreakpointDMContext, ICBreakpoint>());
        fPlatformToBPThreadsMaps.put(dmc, new HashMap<ICBreakpoint, Set<String>>());

        // Install the platform breakpoints (stored in fPlatformBPs) on the target.
        new Job("DSF BreakpointsManager: Install initial breakpoints on target") { //$NON-NLS-1$
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                // Submit the runnable to plant the breakpoints on dispatch thread.
                getExecutor().submit(new Runnable() {
                	@Override
                	public void run() {
                        installInitialBreakpoints(dmc, new RequestMonitor(ImmediateExecutor.getInstance(), rm) {
                        	@Override
                        	protected void handleSuccess() {
                        		// Notify breakpoints tracking listeners that the tracking is started.
                        		for (Object o : fTrackingListeners.getListeners()) {
                        			((IMIBreakpointsTrackingListener)o).breakpointTrackingStarted(dmc);
                        		}
                    			rm.done();
                        	};
                        });
                    }
                });

                return Status.OK_STATUS;
            }
        }.schedule();
    }

    /**
     * Installs the breakpoints that existed prior to the activation of this
     * execution context.
     * 
     * @param dmc
     * @param initialPlatformBPs
     * @param rm
     */
    private void installInitialBreakpoints(final IBreakpointsTargetDMContext dmc, final RequestMonitor rm)
    {
        // Retrieve the set of platform breakpoints for this context
        final Map<ICBreakpoint,Map<String, Object>> platformBPs = fPlatformToAttributesMaps.get(dmc);
        if (platformBPs == null) {
            rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR, INVALID_CONTEXT, null));
            rm.done();
            return;
        }

        // Read current breakpoints from platform and copy their augmented
        // attributes into the local reference map
        try {
            IBreakpoint[] breakpoints = DebugPlugin.getDefault().getBreakpointManager().getBreakpoints(fDebugModelId);
            for (IBreakpoint breakpoint : breakpoints) {
                if (supportsBreakpoint(breakpoint)) {
                    Map<String, Object> attributes = breakpoint.getMarker().getAttributes();
                    attributes.put(ATTR_DEBUGGER_PATH, NULL_STRING);
                    attributes.put(ATTR_THREAD_FILTER, extractThreads(dmc, (ICBreakpoint) breakpoint));
                    attributes.put(ATTR_THREAD_ID, NULL_STRING);
                    platformBPs.put((ICBreakpoint) breakpoint, attributes);
                }
            }
        } catch (CoreException e) {
            IStatus status = new Status(
                IStatus.ERROR, GdbPlugin.PLUGIN_ID, REQUEST_FAILED, UNABLE_TO_READ_BREAKPOINT, e);
            rm.setStatus(status);
            rm.done();
        }

        // Install the individual breakpoints on the dispatcher thread
        // Requires a counting monitor to know when we are done
        final CountingRequestMonitor countingRm = new CountingRequestMonitor(getExecutor(), rm);
        countingRm.setDoneCount(platformBPs.size());

        for (final ICBreakpoint breakpoint : platformBPs.keySet()) {
            final Map<String, Object> attributes = platformBPs.get(breakpoint);
            // Upon determining the debuggerPath, the breakpoint is installed
            determineDebuggerPath(dmc, attributes, new RequestMonitor(getExecutor(), countingRm) {
                @Override
                protected void handleSuccess() {                	
                	// Must install breakpoints right away, even if disabled, so that
                	// we can find out if they apply to this target (Bug 389070)
               		installBreakpoint(dmc, breakpoint, attributes, countingRm);
                }
            });
        }
    }

    //-------------------------------------------------------------------------
    // stopTrackingBreakpoints
    //-------------------------------------------------------------------------
    // - Remove the target breakpoints for the given execution context
    // - Update the maps
    //-------------------------------------------------------------------------

    /**
     * Uninstall and stop tracking breakpoints for the given context.
     * @param dmc Context to start tracking breakpoints for.
     * @param rm Completion callback.
     */
    public void stopTrackingBreakpoints(final IBreakpointsTargetDMContext dmc, final RequestMonitor rm) {

        // Validate the context
        if (dmc == null) {
            rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR, INVALID_CONTEXT, null));
            rm.done();
            return;
        }

        // Retrieve the set of platform breakpoints for this context
        final Map<ICBreakpoint,Map<String, Object>> platformBPs = fPlatformToAttributesMaps.get(dmc);
        if (platformBPs == null) {
            rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR, INVALID_CONTEXT, null));
            rm.done();
            return;
        }

        // Un-install the individual breakpoints on the dispatcher thread
        // (requires a counting monitor to know when we are done).
        // On completion (success or failure), update the maps.
        final CountingRequestMonitor countingRm = new CountingRequestMonitor(getExecutor(), rm) {
            @Override
            protected void handleCompleted() {
                fPlatformToAttributesMaps.remove(dmc);
                fPlatformToBPsMaps.remove(dmc);
                fBPToPlatformMaps.remove(dmc);
                fPlatformToBPThreadsMaps.remove(dmc);
        		// Notify breakpoints tracking listeners that the tracking is stopped.
        		for (Object o : fTrackingListeners.getListeners()) {
        			((IMIBreakpointsTrackingListener)o).breakpointTrackingStopped(dmc);
        		}
                rm.done();
            }
        };
        countingRm.setDoneCount(platformBPs.size());

        for (final ICBreakpoint breakpoint : platformBPs.keySet()) {
            uninstallBreakpoint(dmc, breakpoint,
                new RequestMonitor(getExecutor(), countingRm) {
                    @Override
                    protected void handleCompleted() {
                        countingRm.done();
                    }
                });
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Back-end interface functions
    ///////////////////////////////////////////////////////////////////////////

    //-------------------------------------------------------------------------
    // installBreakpoint
    //-------------------------------------------------------------------------

    /**
     * Install a platform breakpoint on the back-end. For a given context, a
     * platform breakpoint can resolve into multiple back-end breakpoints when
     * threads are taken into account or if multiple breakpoints are created
     * on the target using the console.
     * 
     * @param dmc
     * @param breakpoint
     * @param attributes
     * @param rm
     */
    private void installBreakpoint(IBreakpointsTargetDMContext dmc, final ICBreakpoint breakpoint,
        final Map<String, Object> attributes, final RequestMonitor rm)
    {
        // Retrieve the breakpoint maps
        final Map<ICBreakpoint,Map<String,Object>> platformBPs = fPlatformToAttributesMaps.get(dmc);
        assert platformBPs != null;

        final Map<ICBreakpoint, Vector<IBreakpointDMContext>> breakpointIDs = fPlatformToBPsMaps.get(dmc);
        assert breakpointIDs != null;

        final Map<IBreakpointDMContext, ICBreakpoint> targetBPs = fBPToPlatformMaps.get(dmc);
        assert targetBPs != null;

        final Map<ICBreakpoint, Set<String>> threadsIDs = fPlatformToBPThreadsMaps.get(dmc);
        assert threadsIDs != null;

        // Ensure the breakpoint has a valid debugger source path
        if (breakpoint instanceof ICLineBreakpoint 
        	&& !(breakpoint instanceof ICAddressBreakpoint)
        	&& !(breakpoint instanceof ICFunctionBreakpoint)) {
            String debuggerPath = (String) attributes.get(ATTR_DEBUGGER_PATH);
            if (debuggerPath == null || debuggerPath == NULL_STRING) {
                rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, REQUEST_FAILED, NO_DEBUGGER_PATH, null));
                rm.done();
                return;
            }
        }

        // A back-end breakpoint needs to be installed for each specified thread
        final Set<String> threads = getThreads(attributes);

        // Update the breakpoint state when all back-end breakpoints have been installed
        final CountingRequestMonitor installRM = new CountingRequestMonitor(getExecutor(), rm) {
            @Override
            protected void handleCompleted() {
                // Store the platform breakpoint
                platformBPs.put(breakpoint, attributes);
                rm.done();
            }
        };
        installRM.setDoneCount(threads.size());

        // Install the back-end breakpoint(s)
        for (final String thread : threads) {
            DataRequestMonitor<IBreakpointDMContext> drm =
                new DataRequestMonitor<IBreakpointDMContext>(getExecutor(), installRM) {
                    @Override
                    protected void handleSuccess() {
                        // Add the new back-end breakpoint to the map
                        Vector<IBreakpointDMContext> list = breakpointIDs.get(breakpoint);
                        if (list == null)
                            list = new Vector<IBreakpointDMContext>();
                        IBreakpointDMContext targetBP = getData();
                        list.add(targetBP);
                        breakpointIDs.put(breakpoint, list);

                        // Add the reverse mapping
                        targetBPs.put(targetBP, breakpoint);

                        // And update the corresponding thread list
                        Set<String> thrds = threadsIDs.get(breakpoint);
                        if (thrds == null)
                            thrds = new HashSet<String>();
                        thrds.add(thread);
                        threadsIDs.put(breakpoint, thrds);

                        // Reset the thread (is it necessary?)
                        attributes.put(ATTR_THREAD_ID, NULL_STRING);

                        // Remove breakpoint problem marker (if any)
                        removeBreakpointProblemMarker(breakpoint);

                        // Check for a pending breakpoint before showing that it was properly installed
                        fBreakpoints.getBreakpointDMData(targetBP, new DataRequestMonitor<IBreakpointDMData>(getExecutor(), null) {
                        	@Override
                        	protected void handleCompleted() {
                        		boolean pending = false;
                        		if (isSuccess()) {
                        			IBreakpointDMData data = getData();
                        			if (data instanceof MIBreakpointDMData) {
                        				pending = ((MIBreakpointDMData)data).isPending();
                        			}
                        		}
                        		// Finally, update the platform breakpoint to show it was installed, unless we have a pending breakpoint
                        		if (!pending) {
                        			try {
                        				breakpoint.incrementInstallCount();
                        			} catch (CoreException e) {
                        			}
                        		}
                        		installRM.done();                        		
                        	}
                        });
                    }

                    @Override
                    protected void handleError() {
                    	String detailedMessage;
                    	if (getStatus().getException() != null &&
                    			getStatus().getException().getMessage() != null) {
                    		detailedMessage = getStatus().getException().getMessage();
                    	} else {
                    		detailedMessage = getStatus().getMessage();
                    	}
                    	String description = (detailedMessage == null) ? 
                    			Messages.Breakpoint_attribute_problem :
                    			MessageFormat.format(Messages.Breakpoint_attribute_detailed_problem, new Object[] { detailedMessage});

                        addBreakpointProblemMarker(breakpoint, description, IMarker.SEVERITY_WARNING);
                        installRM.done();
                    }
                };

            // Convert the breakpoint attributes for the back-end
            attributes.put(ATTR_THREAD_ID, thread);
            Map<String,Object> targetAttributes = convertToTargetBreakpoint(breakpoint, attributes);
        	// Must install breakpoint right away, even if disabled, so that
        	// we can find out if it applies to this target (Bug 389070)
            fBreakpoints.insertBreakpoint(dmc, targetAttributes, drm);
        }
    }

    private void addBreakpointProblemMarker(final ICBreakpoint breakpoint, final String description, final int severity) {

        new Job("Add Breakpoint Problem Marker") { //$NON-NLS-1$
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                
                if (breakpoint instanceof ICLineBreakpoint) {
                	// If we have already have a problem marker on this breakpoint
                	// we should remove it first.
                    IMarker marker = fBreakpointMarkerProblems.remove(breakpoint);
                    if (marker != null) {
                        try {
                            marker.delete();
                        } catch (CoreException e) {
                        }
                	}

                    ICLineBreakpoint lineBreakpoint = (ICLineBreakpoint) breakpoint;
                    try {
                        // Locate the workspace resource via the breakpoint marker
                        IMarker breakpoint_marker = lineBreakpoint.getMarker();
                        IResource resource = breakpoint_marker.getResource();

                        // Add a problem marker to the resource
                        IMarker problem_marker = resource.createMarker(BreakpointProblems.BREAKPOINT_PROBLEM_MARKER_ID);
                        int line_number = lineBreakpoint.getLineNumber();
                        String sourceHandle = lineBreakpoint.getSourceHandle();
                        problem_marker.setAttribute(IMarker.LOCATION,    String.valueOf(line_number));
                        problem_marker.setAttribute(IMarker.MESSAGE,     description);
                        problem_marker.setAttribute(IMarker.SEVERITY,    severity);
                        problem_marker.setAttribute(IMarker.LINE_NUMBER, line_number);
                        if (sourceHandle != null)
                            problem_marker.setAttribute(ICModelMarker.C_MODEL_MARKER_EXTERNAL_LOCATION, sourceHandle);

                        // And save the baby
                        fBreakpointMarkerProblems.put(breakpoint, problem_marker);
                    } catch (CoreException e) {
                    }
                }
                return Status.OK_STATUS;
            }
        }.schedule();
    }

    private void removeBreakpointProblemMarker(final ICBreakpoint breakpoint) {

        new Job("Remove Breakpoint Problem Marker") { //$NON-NLS-1$
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                
                IMarker marker = fBreakpointMarkerProblems.remove(breakpoint);
                if (marker != null) {
                    try {
                        marker.delete();
                    } catch (CoreException e) {
                    }
                }

                return Status.OK_STATUS;
            }
        }.schedule();
    }

    //-------------------------------------------------------------------------
    // uninstallBreakpoint
    //-------------------------------------------------------------------------

    /**
     * Un-install an individual breakpoint on the back-end. For one platform
     * breakpoint in a given execution context, there could be multiple
     * corresponding back-end breakpoints (one per thread).
     * 
     * @param dmc
     * @param breakpoint
     * @param rm
     * 
     * @since 4.2
     */
    public void uninstallBreakpoint(final IBreakpointsTargetDMContext dmc,
            final ICBreakpoint breakpoint, final RequestMonitor rm)
    {
        // Retrieve the breakpoint maps
        final Map<ICBreakpoint,Map<String,Object>> platformBPs = fPlatformToAttributesMaps.get(dmc);
        assert platformBPs != null;

        final Map<ICBreakpoint, Vector<IBreakpointDMContext>> breakpointIDs = fPlatformToBPsMaps.get(dmc);
        assert breakpointIDs != null;

        final Map<IBreakpointDMContext, ICBreakpoint> targetBPs = fBPToPlatformMaps.get(dmc);
        assert targetBPs != null;

        final Map<ICBreakpoint, Set<String>> threadsIDs = fPlatformToBPThreadsMaps.get(dmc);
        assert threadsIDs != null;

        // Remove all relevant target filters
        // Note that this call is important if a breakpoint is removed directly
        // from the gdb console, or else we will try to re-install it (bug 433044)
       	removeAllTargetFilters(dmc, breakpoint);

        // Remove breakpoint problem marker (if any)
        removeBreakpointProblemMarker(breakpoint);

        // Minimal validation
        if (!platformBPs.containsKey(breakpoint) || !breakpointIDs.containsKey(breakpoint) || !targetBPs.containsValue(breakpoint)) {
            rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR, BREAKPOINT_ALREADY_REMOVED, null));
            rm.done();
            return;
        }

        // Remove completion monitor
        // Upon completion, update the mappings
        final CountingRequestMonitor removeRM = new CountingRequestMonitor(getExecutor(), rm) {
            @Override
            protected void handleSuccess() {
                // Update the mappings
                platformBPs.remove(breakpoint);
                threadsIDs.remove(breakpoint);
                fPendingRequests.remove(breakpoint);

				rm.done();
            }
        };

        // Remove the back-end breakpoints
        // Remove the entry from the breakpointIDs map right away to indicate that we have already
        // taken care of this breakpoint.  This avoids race conditions with the shutdown
        // which would also try to decrement the install count (bug 344635)
        Vector<IBreakpointDMContext> list = breakpointIDs.remove(breakpoint);
        int count = 0;
        if (list != null) {       			
            for (final IBreakpointDMContext bp : list) {
            	targetBPs.remove(bp);
            	decrementInstallCount(bp, breakpoint, new RequestMonitor(getExecutor(), removeRM) {
                	@Override
                	protected void handleCompleted() {
                		fBreakpoints.removeBreakpoint(bp, removeRM); 
                	}
                });
            }
            count = list.size();
            list.clear();  // probably not necessary
        }
        removeRM.setDoneCount(count);
    }

    
    private void decrementInstallCount(IBreakpointDMContext targetDmc, final ICBreakpoint breakpoint, final RequestMonitor rm) {
        fBreakpoints.getBreakpointDMData(targetDmc, new DataRequestMonitor<IBreakpointDMData>(getExecutor(), rm) {
        	@Override
        	protected void handleCompleted() {
        		boolean pending = false;
        		if (isSuccess()) {
        			IBreakpointDMData data = getData();
        			if (data instanceof MIBreakpointDMData) {
        				pending = ((MIBreakpointDMData)data).isPending();
        			}
        		}
        		// Finally, update the platform breakpoint to show it was un-installed.
        		// But we don't do this for pending breakpoints since they were
        		// not marked as installed.
        		if (!pending) {
        			try {
        				breakpoint.decrementInstallCount();
        			} catch (CoreException e) {
        			}
        		}
        		rm.done();
        	}
        });
    }
    //-------------------------------------------------------------------------
    // modifyBreakpoint
    //-------------------------------------------------------------------------

    /**
     * Modify a platform breakpoint which can translate to quite a few updates
     * on the target...
     * 
     * @param dmc
     * @param breakpoint
     * @param attributes
     * @param oldValues
     * @param rm
     */
    private void modifyBreakpoint(final IBreakpointsTargetDMContext dmc, final ICBreakpoint breakpoint,
            final Map<String,Object> attributes, final IMarkerDelta oldValues, final RequestMonitor rm)
    {
        // Retrieve the breakpoint maps
        final Map<ICBreakpoint,Map<String,Object>> platformBPs = fPlatformToAttributesMaps.get(dmc);
        assert platformBPs != null;

        final Map<ICBreakpoint, Vector<IBreakpointDMContext>> breakpointIDs = fPlatformToBPsMaps.get(dmc);
        assert breakpointIDs != null;

        final Map<IBreakpointDMContext, ICBreakpoint> targetBPs = fBPToPlatformMaps.get(dmc);
        assert targetBPs != null;

        final Map<ICBreakpoint, Set<String>> threadsIDs = fPlatformToBPThreadsMaps.get(dmc);
        assert threadsIDs != null;

        boolean filtered = isBreakpointEntirelyFiltered(dmc, breakpoint);
        
        if (filtered && !platformBPs.containsKey(breakpoint)) {
            rm.done();
            return;
        }

        // Check if the breakpoint is installed:
        // the installation might have failed; in this case, we try to install it again because
        // some attribute might have changed which will make the install succeed.
        if (!breakpointIDs.containsKey(breakpoint) && !targetBPs.containsValue(breakpoint)) {
        	if (!filtered) {
                attributes.put(ATTR_DEBUGGER_PATH, NULL_STRING);
                attributes.put(ATTR_THREAD_FILTER, extractThreads(dmc, breakpoint));
                attributes.put(ATTR_THREAD_ID, NULL_STRING);
                determineDebuggerPath(dmc, attributes, new RequestMonitor(getExecutor(), rm) {
                    @Override
                    protected void handleSuccess() {
                      	installBreakpoint(dmc, breakpoint, attributes, rm);
                    }
                });
        	}
        	else {
                rm.done();
        	}
       		return;
        }
        
        if (filtered) {
        	uninstallBreakpoint(dmc, breakpoint, rm );
        	return;
        }

        // Get the original breakpoint attributes
        final Map<String,Object> original_attributes = platformBPs.get(breakpoint);
        if (original_attributes == null) {
            rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_HANDLE, INVALID_BREAKPOINT, null));
            rm.done();
            return;
        }

        // Determine the attributes delta
        final Map<String,Object> oldAttributes = new HashMap<String,Object>(original_attributes);
        oldAttributes.put(ATTR_THREAD_FILTER, threadsIDs.get(breakpoint));

        final Set<String> newThreads = extractThreads(dmc, breakpoint);
        Map<String,Object> newAttributes = new HashMap<String,Object>(attributes);
        newAttributes.put(ATTR_THREAD_FILTER, newThreads);

        final Map<String,Object> attributesDelta = determineAttributesDelta(oldAttributes, newAttributes);

        // Get the list of back-end breakpoints
        final Vector<IBreakpointDMContext> oldTargetBPs = new Vector<IBreakpointDMContext>(breakpointIDs.get(breakpoint));
        if (oldTargetBPs.isEmpty()) {
            rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_HANDLE, INVALID_BREAKPOINT, null));
            rm.done();
            return;
        }

        // We're all set for the breakpoint update.
        //
        // The path for a simple update is straightforward:
        // - For each back-end BP corresponding to a platform BP
        //   - Send an update command to the back-end
        //   - If the operation succeeded, update the data structures
        //   - If the operation failed, try to roll-back
        //
        // In cases where the the back-end breakpoint cannot be
        // simply updated (e.g. thread filter modification), the old
        // breakpoint has to be removed and new one(s) inserted.
        //
        // The path for such an update is:
        // - Install the updated breakpoint
        // - In the operation succeeded
        //   - Remove the old breakpoint(s)
        //   - Perform any pending update

        // Update completion monitor
        final CountingRequestMonitor updateRM = new CountingRequestMonitor(getExecutor(), rm) {
            @Override
            protected void handleSuccess() {
                // Success: simply store the new attributes
                platformBPs.put(breakpoint, attributes);
                rm.done();
            }

            @Override
            protected void handleError() {
                // Reset the breakpoint attributes. This will trigger a
                // breakpoint change event and the correct delta will be
                // computed, resulting in a correctly restored breakpoint
                // at the back-end.
                rollbackAttributes(breakpoint, oldValues);
                platformBPs.put(breakpoint, attributes);

                rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, REQUEST_FAILED, INVALID_PARAMETER, null));
                rm.done();
            }
        };

        // Everything OK: remove the old back-end breakpoints
        final Vector<IBreakpointDMContext> newTargetBPs = new Vector<IBreakpointDMContext>();
        final CountingRequestMonitor removeRM = new CountingRequestMonitor(getExecutor(), rm) {
            @Override
            protected void handleSuccess() {
                // All right! Save the new list and perform the final update
                Map<ICBreakpoint, Vector<IBreakpointDMContext>> breakpointIDs = fPlatformToBPsMaps.get(dmc);
                if (breakpointIDs == null) {
                    rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, REQUEST_FAILED, INVALID_BREAKPOINT, null));
                    rm.done();
                    return;
                }
                breakpointIDs.put(breakpoint, newTargetBPs);
                for (IBreakpointDMContext ref : newTargetBPs) {
                    fBreakpoints.updateBreakpoint(ref, attributesDelta, updateRM);
                }
                updateRM.setDoneCount(newTargetBPs.size());
            }};

        // New back-end breakpoints insertion monitor
        // Holds the list of new back-end breakpoint contexts of the platform breakpoint
        final DataRequestMonitor<Vector<IBreakpointDMContext>> insertRM =
            new DataRequestMonitor<Vector<IBreakpointDMContext>>(getExecutor(), null) {

                @Override
                // In theory, we could have had a partial success and the original threads
                // list would be invalid. We think it is highly unlikely so we assume that
                // either everything went fine or else everything failed.
                protected void handleSuccess() {
                    // Get the list of new back-end breakpoints contexts
                    newTargetBPs.addAll(getData());
                    for (IBreakpointDMContext newRef : newTargetBPs)
                    	targetBPs.put(newRef, breakpoint);
                    threadsIDs.put(breakpoint, newThreads);
                    for (final IBreakpointDMContext ref : oldTargetBPs) {
                    	targetBPs.remove(ref);
                    	decrementInstallCount(ref, breakpoint, // A tad early but it should work...
                    			              new RequestMonitor(getExecutor(), removeRM) {
                    		@Override
                    		protected void handleCompleted() {
                    			fBreakpoints.removeBreakpoint(ref, removeRM);
                    		}
                        }); 
                    }
                    removeRM.setDoneCount(oldTargetBPs.size());
                }
                
                @Override
                protected void handleError() {
                    // Keep the old threads list and reset the attributes
                    // (bad attributes are the likely cause of failure)
                    updateRM.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, REQUEST_FAILED, INVALID_PARAMETER, null));
                    updateRM.setDoneCount(0);
                }
        };

        // If the changes in the breakpoint attributes justify it, install a
        // new set of back-end breakpoint(s) and then update them
        if (needsResinstallation(attributesDelta)) {
            reinstallBreakpoint(dmc, breakpoint, attributes, newThreads, insertRM);
        }
        else {
            // Update the back-end breakpoint(s) state
            for (IBreakpointDMContext ref : oldTargetBPs) {
                fBreakpoints.updateBreakpoint(ref, attributesDelta, updateRM);
            }
            updateRM.setDoneCount(oldTargetBPs.size());
        }
    }

    /**
     * Re-install the back-end breakpoints
     * 
     * @param context       the target context
     * @param breakpoint    the platform breakpoint
     * @param attributes    breakpoint augmented attributes
     * @param threads       list of threads where breakpoint is to be installed
     * @param drm           will contain the list of successfully installed back-end breakpoints
     */
    private void reinstallBreakpoint(final IBreakpointsTargetDMContext context, final ICBreakpoint breakpoint,
            final Map<String,Object> attributes, Set<String> threads, final DataRequestMonitor<Vector<IBreakpointDMContext>> drm)
    {
        // Our new list of back-end breakpoints. Built as we go.
        final Vector<IBreakpointDMContext> breakpointList = new Vector<IBreakpointDMContext>();

        // Counting monitor for the new back-end breakpoints to install
        // Once we're done, return the new list of back-end breakpoints contexts
        final CountingRequestMonitor installRM = new CountingRequestMonitor(getExecutor(), drm) {
            @Override
            protected void handleSuccess() {
                // Report whatever we have managed to install
                // It is very likely installation either succeeded or failed for all
                drm.setData(breakpointList);
                drm.done();
            }
        };
        installRM.setDoneCount(threads.size());

        // And install the new back-end breakpoints
        for (String thread : threads) {
            // Convert the breakpoint attributes for the back-end
            // Refresh the set of attributes at each iteration just in case...
            Map<String,Object> attrs = convertToTargetBreakpoint(breakpoint, attributes);
            // Tracepoints and dynamic printf are not affected by "skip-all"
            if (!(breakpoint instanceof ICTracepoint) && !(breakpoint instanceof ICDynamicPrintf)
            		&& !fBreakpointManager.isEnabled()) {
                attrs.put(MIBreakpoints.IS_ENABLED, false);
            }

            attrs.put(MIBreakpointDMData.THREAD_ID, thread);

            // Then install the spiked breakpoint
            fBreakpoints.insertBreakpoint(context, attrs,
                new DataRequestMonitor<IBreakpointDMContext>(getExecutor(), installRM) {
                    @Override
                    protected void handleSuccess() {
                        // Add the new back-end breakpoint context to the list
                        breakpointList.add(getData());
                        
                        // Check for a pending breakpoint before showing that it was properly installed
                        fBreakpoints.getBreakpointDMData(getData(), new DataRequestMonitor<IBreakpointDMData>(getExecutor(), null) {
                        	@Override
                        	protected void handleCompleted() {
                        		boolean pending = false;
                        		if (isSuccess()) {
                        			IBreakpointDMData data = getData();
                        			if (data instanceof MIBreakpointDMData) {
                        				pending = ((MIBreakpointDMData)data).isPending();
                        			}
                        		}
                        		// Finally, update the platform breakpoint to show it was installed, unless we have a pending breakpoint
                        		if (!pending) {
                        			try {
                        				breakpoint.incrementInstallCount();
                        			} catch (CoreException e) {
                        			}
                        		}
                        		installRM.done();                        		
                        	}
                        });
                    }

                    @Override
                    protected void handleError() {
                        // Add the new back-end breakpoint context to the list
                        installRM.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, REQUEST_FAILED, INVALID_PARAMETER, null));
                        installRM.done();
                    }
            });
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // IBreakpointManagerListener implementation
    ///////////////////////////////////////////////////////////////////////////

    /* (non-Javadoc)
     * @see org.eclipse.debug.core.IBreakpointManagerListener#breakpointManagerEnablementChanged(boolean)
     */
	@Override
    public void breakpointManagerEnablementChanged(boolean enabled) {

        // Only modify enabled breakpoints
        for (IBreakpointsTargetDMContext context : fPlatformToBPsMaps.keySet()) {
            for (ICBreakpoint breakpoint : fPlatformToBPsMaps.get(context).keySet()) {
                try {
                    // Note that Tracepoints and dynamic printf are not affected by "skip-all"
                    if (!(breakpoint instanceof ICTracepoint) && !(breakpoint instanceof ICDynamicPrintf)
                    		&& breakpoint.isEnabled()) {
                        for (IBreakpointDMContext ref : fPlatformToBPsMaps.get(context).get(breakpoint)) {
                            Map<String,Object> delta = new HashMap<String,Object>();
                            delta.put(MIBreakpoints.IS_ENABLED, enabled);
                            fBreakpoints.updateBreakpoint(ref, delta, new RequestMonitor(getExecutor(), null));
                        }
                    }
                } catch (CoreException e) {
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // IBreakpointListener implementation
    ///////////////////////////////////////////////////////////////////////////

    /* (non-Javadoc)
     * @see org.eclipse.debug.core.IBreakpointListener#breakpointAdded(org.eclipse.debug.core.model.IBreakpoint)
     */
    @ThreadSafe
	@Override
    public void breakpointAdded(final IBreakpoint breakpoint) {

        if (supportsBreakpoint(breakpoint)) {
            try {
                // Retrieve the breakpoint attributes
                final Map<String, Object> attrs = breakpoint.getMarker().getAttributes();

                getExecutor().execute(new DsfRunnable() {
                	@Override
                    public void run() {
                        // For a new breakpoint, the first thing we do is set the target filter to all existing processes.
                		// We will need this when it is time to install the breakpoint.
                		// We fetch the processes from our IProcess service to be generic (bug 431986)
                		fProcesses.getProcessesBeingDebugged(fConnection.getContext(), new ImmediateDataRequestMonitor<IDMContext[]>() {
                			@Override
                			protected void handleCompleted() {
                				if (isSuccess()) {
									try {
										IDsfBreakpointExtension filterExtension = getFilterExtension((ICBreakpoint)breakpoint);
										for (IDMContext dmc : getData()) {
											IContainerDMContext containerDmc = DMContexts.getAncestorOfType(dmc, IContainerDMContext.class);
											assert containerDmc != null;
                                    		if (filterExtension.getThreadFilters(containerDmc) == null) {
                                    			// Do this only if there wasn't already an entry, or else we would
                                    			// erase the content of that previous entry.
                                    			// There can be an entry already when a thread-specific breakpoint is created
                                    			// from the MIBreakpointsSynchronizer (through the gdb console).  In that case the
                                    			// platform bp gets created, and the targetFilter gets set by MIBreakpointsSynchronizer
                                    			// before the call to breakpointAdded() is made and we get to here.
                                    			// Bug 433339
                                    			filterExtension.setTargetFilter(containerDmc);
                                    		}
										}
									} catch (CoreException e1) {
										// Error setting target filter, just skip altogether
									}
                				}

                				// Now we can install the bp for all target contexts
                				final CountingRequestMonitor countingRm = new CountingRequestMonitor(getExecutor(), null) {
                					@Override
                					protected void handleCompleted() {
                						// Log any error when creating the breakpoint
                						if (getStatus().getSeverity() == IStatus.ERROR) {
                							GdbPlugin.getDefault().getLog().log(getStatus());
                						}

                					}
                				};
                				countingRm.setDoneCount(fPlatformToAttributesMaps.size());

                				for (final IBreakpointsTargetDMContext dmc : fPlatformToAttributesMaps.keySet()) {
                					determineDebuggerPath(dmc, attrs,
                							new RequestMonitor(getExecutor(), countingRm) {
                						@Override
                						protected void handleSuccess() {
                							installBreakpoint(dmc, (ICBreakpoint) breakpoint,
                									attrs, countingRm);
                						}
                					});
                				}

                			}
                		});
                	}
                });
            } catch (CoreException e) {
            } catch (RejectedExecutionException e) {
            }
        }
    }

    /**
     * @param bp
     * @return
     * @throws CoreException
     * @since 4.7
     */
    protected IDsfBreakpointExtension getFilterExtension(ICBreakpoint bp) throws CoreException {
        return (IDsfBreakpointExtension) bp.getExtension(GDB_DEBUG_MODEL_ID, ICBreakpointExtension.class);
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.core.IBreakpointListener#breakpointChanged(org.eclipse.debug.core.model.IBreakpoint, org.eclipse.core.resources.IMarkerDelta)
     */
	@Override
    public void breakpointChanged(final IBreakpoint breakpoint, final IMarkerDelta delta) {

        if (supportsBreakpoint(breakpoint)) {

            try {
                // Retrieve the breakpoint attributes
                final Map<String, Object> attrs = breakpoint.getMarker().getAttributes();
                // Tracepoints and dynamic printf are not affected by "skip-all"
                if (!(breakpoint instanceof ICTracepoint) && !(breakpoint instanceof ICDynamicPrintf) 
                		&& !fBreakpointManager.isEnabled()) {
                    attrs.put(ICBreakpoint.ENABLED, false);
                }

                // Modify the breakpoint in all the target contexts
                getExecutor().execute( new DsfRunnable() {
                	@Override
                    public void run() {

                        // If the breakpoint is currently being updated, queue the request and exit
                        if (fPendingRequests.contains(breakpoint)) {
                            fPendingBreakpoints.add(breakpoint);
                            return;
                        }

                        // Keep track of the updates
                        final CountingRequestMonitor countingRm = new CountingRequestMonitor(getExecutor(), null) {
                            @Override
                            protected void handleCompleted() {

                                if (!isSuccess()) {
                                    if (getStatus().getSeverity() == IStatus.ERROR) {
                                        GdbPlugin.getDefault().getLog().log(getStatus());
                                    }
                                }

                                // Indicate that the pending request has completed
                                fPendingRequests.remove(breakpoint);

                                // Process the next pending update for this breakpoint
                                if (fPendingBreakpoints.contains(breakpoint)) {
                                    fPendingBreakpoints.remove(breakpoint);
                                    breakpointChanged(breakpoint, delta);
                                }
                            }
                        };
                        countingRm.setDoneCount(fPlatformToAttributesMaps.size());

                        // Mark the breakpoint as being updated and go
                        fPendingRequests.add(breakpoint);
                        
                        // Modify the breakpoint in all the execution contexts
                        for (final IBreakpointsTargetDMContext dmc : fPlatformToAttributesMaps.keySet()) {
                            determineDebuggerPath(dmc, attrs,
                                new RequestMonitor(getExecutor(), countingRm) {
                                    @Override
                                    protected void handleSuccess() {
                                        modifyBreakpoint(dmc, (ICBreakpoint) breakpoint, attrs, delta, countingRm);
                                    }
                                 });
                        }
                    }
                });
            } catch (CoreException e) {
            } catch (RejectedExecutionException e) {
            }
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.core.IBreakpointListener#breakpointRemoved(org.eclipse.debug.core.model.IBreakpoint, org.eclipse.core.resources.IMarkerDelta)
     */
	@Override
    public void breakpointRemoved(final IBreakpoint breakpoint, IMarkerDelta delta) {

        if (supportsBreakpoint(breakpoint)) {
            try {
                getExecutor().execute(new DsfRunnable() {
                	@Override
                    public void run() {
                        CountingRequestMonitor countingRm = new CountingRequestMonitor(getExecutor(), null) {
                            @Override
                            protected void handleError() {
                                if (getStatus().getSeverity() == IStatus.ERROR) {
                                    GdbPlugin.getDefault().getLog().log(getStatus());
                                }
                            }
                        };
                        countingRm.setDoneCount(fPlatformToAttributesMaps.size());

                        // Remove the breakpoint in all the execution contexts
                        for (IBreakpointsTargetDMContext dmc : fPlatformToAttributesMaps.keySet()) {
                            if (fPlatformToAttributesMaps.get(dmc).containsKey(breakpoint)) {
                                uninstallBreakpoint(dmc, (ICBreakpoint) breakpoint, countingRm);
                            }
                        }
                    }
                });
            } catch (RejectedExecutionException e) {
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // IServiceEventListener
    ///////////////////////////////////////////////////////////////////////////

    //-------------------------------------------------------------------------
    // Breakpoints
    //-------------------------------------------------------------------------

    @DsfServiceEventHandler
    public void eventDispatched(BreakpointAddedEvent e) {
    	// Nothing to do - already handled by breakpointAdded()
    }

    @DsfServiceEventHandler
    public void eventDispatched(BreakpointUpdatedEvent e) {
    	// Nothing to do - already handled by breakpointChanged()
    }

    @DsfServiceEventHandler
    public void eventDispatched(BreakpointRemovedEvent e) {
    	// Nothing to do - already handled by breakpointRemoved()
    }

    /*
     * When a watchpoint goes out of scope, it is automatically removed from
     * the back-end. To keep our internal state synchronized, we have to
     * remove it from our breakpoints maps.
     * Unfortunately, GDB doesn't generate the correct event...
     */
    @DsfServiceEventHandler
    public void eventDispatched(MIWatchpointScopeEvent e) {
    }

    //-------------------------------------------------------------------------
    // Breakpoint actions
    //-------------------------------------------------------------------------

	/** @since 4.2 */
	@DsfServiceEventHandler 
    public void eventDispatched(ISuspendedDMEvent e) {
		assert e instanceof IMIDMEvent;
		if (e instanceof IMIDMEvent) {
			Object miEvent = ((IMIDMEvent)e).getMIEvent();

			if (miEvent instanceof MIBreakpointHitEvent) {
				// This covers catchpoints, too
				MIBreakpointHitEvent evt = (MIBreakpointHitEvent)miEvent;
				performBreakpointAction(evt.getDMContext(), evt.getNumber());
				return;
			}

			if (miEvent instanceof MIWatchpointTriggerEvent) {
				MIWatchpointTriggerEvent evt = (MIWatchpointTriggerEvent)miEvent;
				performBreakpointAction(evt.getDMContext(), evt.getNumber());
				return;
			}
		}
	}
	
	/**
	 * @deprecated Replaced by the generic {@link #eventDispatched(ISuspendedDMEvent)}
	 */
	@Deprecated
	@DsfServiceEventHandler
    public void eventDispatched(SuspendedEvent e) {
	}

    private void performBreakpointAction(final IDMContext context, int number) {
        // Identify the platform breakpoint
        final ICBreakpoint breakpoint = findPlatformBreakpoint(number);

        if (breakpoint != null ) {
        	// Perform the actions asynchronously (otherwise we can have a deadlock...)
        	new Job("Breakpoint action") { //$NON-NLS-1$
        		{ setSystem(true); }
        		@Override
        		protected IStatus run(IProgressMonitor monitor) {
        			fBreakpointActionManager.executeActions(breakpoint, new BreakpointActionAdapter(getExecutor(), getServicesTracker(), context));
        			return Status.OK_STATUS;
        		};
        	}.schedule();
        }
    }

    // Helper function to locate the platform breakpoint corresponding
    // to the target breakpoint/watchpoint that was just hit

    // FIXME: (Bug228703) Need a way to identify the correct context where the BP was hit
    private ICBreakpoint findPlatformBreakpoint(int targetBreakpointID) {
        Set<IBreakpointsTargetDMContext> targets = fBPToPlatformMaps.keySet();
        for (IBreakpointsTargetDMContext target : targets) {
            Map<IBreakpointDMContext, ICBreakpoint> bps = fBPToPlatformMaps.get(target);
            Set<IBreakpointDMContext> contexts = bps.keySet();
            for (IBreakpointDMContext context : contexts) {
                if (context instanceof MIBreakpointDMContext) {
                    MIBreakpointDMContext ctx = (MIBreakpointDMContext) context;
                    if (ctx.getReference() == targetBreakpointID) {
                        return bps.get(context);
                    }
                }
            }
        }
        return null;
    }

    /**
     * Returns a platform breakpoint corresponding to a given target breakpoint. 
     * 
     * @since 3.0
     */
    public IBreakpoint findPlatformBreakpoint(IBreakpointDMContext bpContext) {
        if (bpContext instanceof MIBreakpointDMContext) {
        	IBreakpointsTargetDMContext targetCtx = DMContexts.getAncestorOfType(bpContext, IBreakpointsTargetDMContext.class);
        	if (targetCtx != null) {
                Map<IBreakpointDMContext, ICBreakpoint> bps = fBPToPlatformMaps.get(targetCtx);
                if (bps != null)
                	return bps.get(bpContext);
        	}
        }
        return null;
    }
    
    //-------------------------------------------------------------------------
    // Process/thread start/exit
    //-------------------------------------------------------------------------
    /**
     * @noreference This method is not intended to be referenced by clients.
     * @since 4.4
     */
    @DsfServiceEventHandler
    public void eventDispatched(IStartedDMEvent e) {
    }  
    
    
    /**
	 * @since 4.7
	 */
    protected void setTargetFilter(ICBreakpoint breakpoint, IContainerDMContext containerDmc) {
    	try {
    		IDsfBreakpointExtension filterExt = getFilterExtension(breakpoint);
    		if (filterExt.getThreadFilters(containerDmc) == null) {
    			// Do this only if there wasn't already an entry, or else we would
    			// erase the content of that previous entry.
    			// This could theoretically happen if the targetFilter is set by
    			// someone else, before this method is called.
    			// Bug 433339
    			filterExt.setTargetFilter(containerDmc);
    		}

    	} catch (CoreException e) {
    	}
    }
    
    /**
     * @noreference This method is not intended to be referenced by clients.
     * @since 4.4
     */
    @DsfServiceEventHandler
    public void eventDispatched(IExitedDMEvent e) {
		// original code moved to API removeTargetFilter (Bug 456959)
    }

	/**
	 * Remove process from the thread filtering of all breakpoints
	 * @since 4.6
	 */
	public void removeTargetFilter(IContainerDMContext containerDMC) {
		// We must get the list of breakpoints from the platform because our different
		// maps might already have been cleaned up
		IBreakpoint[] allBreakpoints = DebugPlugin.getDefault().getBreakpointManager().getBreakpoints(fDebugModelId);
		for (IBreakpoint bp : allBreakpoints) {
			if (supportsBreakpoint(bp)) {
				removeTargetFilter((ICBreakpoint) bp, containerDMC);
			}
		}
	}
    
    private void removeTargetFilter(ICBreakpoint breakpoint, IContainerDMContext containerDmc) {
    	try {
    		IDsfBreakpointExtension filterExt = getFilterExtension(breakpoint);
   			filterExt.removeTargetFilter(containerDmc);
    	} catch (CoreException e) {
    	}
    }

    private void removeAllTargetFilters(IBreakpointsTargetDMContext bpTargetDmc, ICBreakpoint breakpoint) {
    	try {
    		IDsfBreakpointExtension filterExt = getFilterExtension(breakpoint);
   			IContainerDMContext[] targets = filterExt.getTargetFilters();
   			for (IContainerDMContext target : targets) {
   				if (bpTargetDmc.equals(target) || DMContexts.isAncestorOf(target, bpTargetDmc)) {
   					filterExt.removeTargetFilter(target);
   				}
   			}
    	} catch (CoreException e) {
    	}
    }

    //-------------------------------------------------------------------------
    // Session exit
    //-------------------------------------------------------------------------

    /**
     * @since 1.1
     * @nooverride This method is not intended to be re-implemented or extended by clients.
     * @noreference This method is not intended to be referenced by clients.
     */
    @DsfServiceEventHandler
    public void eventDispatched(ICommandControlShutdownDMEvent e) {
        terminated();
    }

    private void terminated() {
    	// Reset the breakpoint install count
    	for (IBreakpointsTargetDMContext ctx : fPlatformToAttributesMaps.keySet()) {
    		Map<ICBreakpoint, Map<String, Object>> breakpoints = fPlatformToAttributesMaps.get(ctx);
            clearBreakpointStatus(breakpoints.keySet().toArray(new ICBreakpoint[breakpoints.size()]), ctx);
    	}
    	// This will prevent Shutdown() from trying to remove bps from a
    	// backend that has already shutdown
        fPlatformToAttributesMaps.clear();
    }

    /**
     * @param bps
     */
    private void clearBreakpointStatus(final ICBreakpoint[] bps, final IBreakpointsTargetDMContext ctx)
    {
        IWorkspaceRunnable wr = new IWorkspaceRunnable() {
        	@Override
            public void run(IProgressMonitor monitor) throws CoreException {
            	// For every platform breakpoint that has at least one target breakpoint installed
            	// we must decrement the install count, for every target breakpoint.
            	// Note that we cannot simply call resetInstallCount() because another
            	// launch may be using the same platform breakpoint.
            	Map<ICBreakpoint, Vector<IBreakpointDMContext>> breakpoints = fPlatformToBPsMaps.get(ctx);
            	for (ICBreakpoint breakpoint : breakpoints.keySet()) {
            		Vector<IBreakpointDMContext> targetBps = breakpoints.get(breakpoint);
            		for (IBreakpointDMContext targetBp : targetBps) {
                        decrementInstallCount(targetBp, breakpoint, new RequestMonitor(getExecutor(), null));
            		}
                }
            }
        };

        // Create the scheduling rule to clear all bp planted.
        ISchedulingRule rule = null;
        List<ISchedulingRule> markerRules = new ArrayList<ISchedulingRule>();
        for (ICBreakpoint bp : bps) {
            IMarker marker = bp.getMarker();
            if (marker != null) {
                ISchedulingRule markerRule =
                    ResourcesPlugin.getWorkspace().getRuleFactory().markerRule(
                            marker.getResource());
                if (markerRule == null) {
                    markerRules = null;
                    break;
                } else {
                    markerRules.add(markerRule);
                }
            }
        }
        if (markerRules != null) {
            rule = MultiRule.combine(markerRules.toArray(new ISchedulingRule[markerRules.size()]));
        }

        try {
        	// Will run the workspace runnable on the current thread, which
        	// is the DSF executor.
            ResourcesPlugin.getWorkspace().run(wr, rule, 0, null);
        } catch (CoreException e) {
        	GdbPlugin.getDefault().getLog().log(e.getStatus());
        }

        new Job("Clear Breakpoints Status") { //$NON-NLS-1$
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                // Clear any problem markers
                for (IMarker marker : fBreakpointMarkerProblems.values()) {
                	if (marker != null) {
                		try {
							marker.delete();
						} catch (CoreException e) {
						}
                	}
                }
                fBreakpointMarkerProblems.clear();
                
                return Status.OK_STATUS;
            }
        }.schedule();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Support functions
    ///////////////////////////////////////////////////////////////////////////

	/**
	 * Indicates if the platform breakpoint object [bp] is one we can deal with.
	 * For now, it boils down to whether it's a CDT Breakpoint (an
	 * ICBreakpoint). DSF can supports other (custom) types of breakpoints, but
	 * DSF-GDB is tied to ICBreakpoint.
	 * 
	 * @param bp the platform breakpoint
	 * @return true if we support it; false otherwise
	 * @since 4.7
	 */
    protected boolean supportsBreakpoint(IBreakpoint bp) {
        if (bp instanceof ICBreakpoint && bp.getModelIdentifier().equals(fDebugModelId)) {
            IMarker marker = bp.getMarker();
            if (marker != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * determineDebuggerPath
     * 
     * Adds the path to the source file to the set of attributes
     * (for the debugger).
     * 
     * @param dmc
     * @param attributes
     * @param rm
     */
    private void determineDebuggerPath(IBreakpointsTargetDMContext dmc,
            final Map<String, Object> attributes, final RequestMonitor rm)
    {
        String hostPath = (String) attributes.get(ICBreakpoint.SOURCE_HANDLE);

        if (hostPath != null) {

            ISourceLookupDMContext srcDmc = DMContexts.getAncestorOfType(dmc, ISourceLookupDMContext.class);
            if (srcDmc != null) {
                fSourceLookup.getDebuggerPath(srcDmc, hostPath,
                    new DataRequestMonitor<String>(getExecutor(), rm) {
                        @Override
                        protected void handleSuccess() {
                            attributes.put(ATTR_DEBUGGER_PATH, adjustDebuggerPath(getData()));
                            rm.done();
                        }
                    });
            } else {
                // Source lookup not available for given context, use the host
                // path for the debugger path.
                attributes.put(ATTR_DEBUGGER_PATH, adjustDebuggerPath(hostPath));
                rm.done();
            }
        } else {
            // Some types of breakpoints do not require a path
            // (e.g. watchpoints)
            rm.done();
        }
    }

	/**
	 * For some platforms (MinGW) the debugger path needs to be adjusted to work 
	 * with earlier GDB versions. 
	 * See https://bugs.eclipse.org/bugs/show_bug.cgi?id=232415
	 * 
	 * @param path
	 *            the absolute path to the source file
	 * @return the adjusted path provided by the breakpoints service.
	 */
    String adjustDebuggerPath(String path) {
    	return (fBreakpoints instanceof IMIBreakpointPathAdjuster) ? 
    			((IMIBreakpointPathAdjuster)fBreakpoints).adjustDebuggerPath(path) : path;
    }

    /**
     * Determine the set of modified attributes.
     * Elementary set operations in full action :-)
     * 
     * @param oldAttributes
     * @param newAttributes
     * @return
     */
    private Map<String, Object> determineAttributesDelta(Map<String, Object> oldAttributes, Map<String, Object> newAttributes) {

        Map<String, Object> delta = new HashMap<String,Object>();

        Set<String> oldKeySet = oldAttributes.keySet();
        Set<String> newKeySet = newAttributes.keySet();

        Set<String> commonKeys  = new HashSet<String>(newKeySet); commonKeys.retainAll(oldKeySet);
        Set<String> addedKeys   = new HashSet<String>(newKeySet); addedKeys.removeAll(oldKeySet);
        Set<String> removedKeys = new HashSet<String>(oldKeySet); removedKeys.removeAll(newKeySet);

        // Add the modified attributes
        for (String key : commonKeys) {
            if (!(oldAttributes.get(key).equals(newAttributes.get(key))))
                delta.put(key, newAttributes.get(key));
        }

        // Add the new attributes
        for (String key : addedKeys) {
            delta.put(key, newAttributes.get(key));
        }

        // Remove the deleted attributes
        for (String key : removedKeys) {
            delta.put(key, null);
        }

        return convertedAttributes(delta);
    }

    /**
     * Converts ICBreakpoint attributes to IBreakpoints attributes.
     * 
     * @param cdt_attributes
     * @return
     */
    private Map<String, Object> convertedAttributes(Map<String, Object> cdt_attributes) {

        Map<String,Object> result = new HashMap<String,Object>();

        // IBreakpoint attributes
        if (cdt_attributes.containsKey(ATTR_DEBUGGER_PATH))
            result.put(MIBreakpoints.FILE_NAME, cdt_attributes.get(ATTR_DEBUGGER_PATH));
            
        if (cdt_attributes.containsKey(IMarker.LINE_NUMBER))
            result.put(MIBreakpoints.LINE_NUMBER, cdt_attributes.get(IMarker.LINE_NUMBER));
            
        if (cdt_attributes.containsKey(BreakpointActionManager.BREAKPOINT_ACTION_ATTRIBUTE))
            result.put(MIBreakpoints.COMMANDS, cdt_attributes.get(BreakpointActionManager.BREAKPOINT_ACTION_ATTRIBUTE));

        // ICLineBreakpoint attributes
        if (cdt_attributes.containsKey(ICLineBreakpoint.FUNCTION))
            result.put(MIBreakpoints.FUNCTION, cdt_attributes.get(ICLineBreakpoint.FUNCTION));
            
        if (cdt_attributes.containsKey(ICLineBreakpoint.ADDRESS))
            result.put(MIBreakpoints.ADDRESS, cdt_attributes.get(ICLineBreakpoint.ADDRESS));
            
        // ICBreakpoint attributes
        if (cdt_attributes.containsKey(ICBreakpoint.CONDITION))
            result.put(MIBreakpoints.CONDITION, cdt_attributes.get(ICBreakpoint.CONDITION));
            
        if (cdt_attributes.containsKey(ICBreakpoint.IGNORE_COUNT))
            result.put(MIBreakpoints.IGNORE_COUNT, cdt_attributes.get(ICBreakpoint.IGNORE_COUNT));

        if (cdt_attributes.containsKey(ICTracepoint.PASS_COUNT))
            result.put(MIBreakpoints.PASS_COUNT, cdt_attributes.get(ICTracepoint.PASS_COUNT));

        if (cdt_attributes.containsKey(ICBreakpoint.ENABLED))
            result.put(MIBreakpoints.IS_ENABLED, cdt_attributes.get(ICBreakpoint.ENABLED));

        if (cdt_attributes.containsKey(ICBreakpointType.TYPE))
            result.put(MIBreakpoints.BREAKPOINT_TYPE, cdt_attributes.get(ICBreakpointType.TYPE));

        // ICWatchpoint attributes
        if (cdt_attributes.containsKey(ICWatchpoint.EXPRESSION))
            result.put(MIBreakpoints.EXPRESSION, cdt_attributes.get(ICWatchpoint.EXPRESSION));

        if (cdt_attributes.containsKey(ICWatchpoint.READ))
            result.put(MIBreakpoints.READ, cdt_attributes.get(ICWatchpoint.READ));
        
        if (cdt_attributes.containsKey(ICWatchpoint.WRITE))
            result.put(MIBreakpoints.WRITE, cdt_attributes.get(ICWatchpoint.WRITE));

        // Threads
        if (cdt_attributes.containsKey(ATTR_THREAD_FILTER))
            result.put(ATTR_THREAD_FILTER, cdt_attributes.get(ATTR_THREAD_FILTER));
        
        // For IDynamicPrintf
        if (cdt_attributes.containsKey(ICDynamicPrintf.PRINTF_STRING))
        	result.put(MIBreakpoints.PRINTF_STRING, cdt_attributes.get(ICDynamicPrintf.PRINTF_STRING));

        return result;
    }

    /**
     * Figure out the corresponding number of back-end breakpoints
     * Even though the thread IDs are usually integers, they are
     * stored as strings in CBreakpoints.
     * 
     * @param attributes
     * @return
     */
    @SuppressWarnings("unchecked")
    private Set<String> getThreads(Map<String, Object> attributes) {
        Set<String> threads = (Set<String>) attributes.get(ATTR_THREAD_FILTER);
        if (threads == null) {
            threads = new HashSet<String>();
            threads.add("0");    // Thread 0 means all threads //$NON-NLS-1$
        }
        return threads;
    }

    /**
     * Get the list of threads from the breakpoint's thread filtering mechanism
     * 
     * @param breakpoint
     * @return
     */
    private Set<String> extractThreads(IBreakpointsTargetDMContext bpTargetDmc, ICBreakpoint breakpoint) {
        Set<String> results = new HashSet<String>();

        if (supportsThreads(breakpoint)) {
        	List<IExecutionDMContext[]> threads = new ArrayList<IExecutionDMContext[]>(1);

        	try {
        		// Retrieve all existing targets.
        		// Note that these targets can be from different debugging sessions since
        		// they are associated with the platform breakpoint.
        		IDsfBreakpointExtension filterExtension = getFilterExtension(breakpoint);
        		IContainerDMContext[] procTargets = filterExtension.getTargetFilters();

        		// Extract the thread IDs
        		for (IContainerDMContext procDmc : procTargets) {
        			// Look for a target/process that belongs to our session
        			if (procDmc.equals(bpTargetDmc) || DMContexts.isAncestorOf(procDmc, bpTargetDmc)) {
        				IExecutionDMContext[] threadFilters = filterExtension.getThreadFilters(procDmc);
        				if (threadFilters == null) {
        					// The breakpoint applies to the entire process.
        					// For GDB < 7.4, we set the thread to 0 to indicate that the breakpoint
        					// is global for this process.
        					// For GDB >= 7.4, things are more complicated.  There will be one bp for all
        					// processes, so by setting the thread to 0, the breakpoint will apply
        					// to all threads of all processes.  We don't have a choice as there is no 
        					// way to tell GDB to apply to all threads (including any new ones that will
        					// be created) for a single process.
        					// So, in this case, if the bp applies to all threads of one process, it will 
        					// automatically apply to all threads of all processes
        					results.add("0"); //$NON-NLS-1$    
        					return results;
        				} else {
        					threads.add(threadFilters);
        				}
        			}
        		}
        	} catch (CoreException e) {
        		// Error with the thread filtering.  Default to all threads.
        		results.add("0"); //$NON-NLS-1$    
        		return results;
        	}
        	
        	// If there are no threads to filter on, it means the bp applies to the entire process.
        	if (threads.isEmpty()) {
    			results.add("0"); //$NON-NLS-1$    
    			return results;
    		}

        	for (IExecutionDMContext[] targetThreads : threads) {
        		if (targetThreads != null) {
        			for (IExecutionDMContext thread : targetThreads) {
        				if (thread instanceof IMIExecutionDMContext) {
        					results.add(Integer.toString(((IMIExecutionDMContext)thread).getThreadId()));
        				} else {
        					// If any of the threads is not an IMIExecutionDMContext,
        					// we don't support thread filters at all.
        					results.clear();
        					results.add("0"); //$NON-NLS-1$    
                			return results;
        				}
        			}
        		} else {
        			// Should not happen
        			assert false;
         		}
        	}
        } else {
        	results.add("0"); //$NON-NLS-1$
        }

        return results;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Non-generic (MI-specific) functions
    ///////////////////////////////////////////////////////////////////////////

	/**
	 * Create a collection of DSF-GDB specific breakpoint properties given a
	 * platform/CDT breakpoint object and its properties. Basically, this
	 * determines the set of MI-specific properties to be used in installing the
	 * given breakpoint.
	 * 
	 * @param breakpoint
	 *            the platform breakpoint object; was created by CDT
	 * @param attributes
	 *            the breakpoint's properties. By allowing this to be passed in
	 *            (rather than us calling
	 *            IBreakpoint#getMarker()#getProperties()), we allow the caller
	 *            to specify additional/modified properties.
	 * @return a property bag containing the corresponding DSF-GDB properties
	 */
    protected Map<String,Object> convertToTargetBreakpoint(ICBreakpoint breakpoint, Map<String,Object> attributes) {

        Map<String, Object> properties = new HashMap<String, Object>();

        if (breakpoint instanceof ICWatchpoint) {
            properties.put(MIBreakpoints.BREAKPOINT_TYPE, MIBreakpoints.WATCHPOINT);
            properties.put(MIBreakpoints.EXPRESSION,      attributes.get(ICWatchpoint.EXPRESSION));
            properties.put(MIBreakpoints.READ,            attributes.get(ICWatchpoint.READ));
            properties.put(MIBreakpoints.WRITE,           attributes.get(ICWatchpoint.WRITE));
        }
        else if (breakpoint instanceof ICLineBreakpoint) {
            properties.put(MIBreakpoints.BREAKPOINT_TYPE, MIBreakpoints.BREAKPOINT);
            properties.put(MIBreakpoints.FILE_NAME,       attributes.get(ATTR_DEBUGGER_PATH));
            properties.put(MIBreakpoints.LINE_NUMBER,     attributes.get(IMarker.LINE_NUMBER));
            properties.put(MIBreakpoints.FUNCTION,        attributes.get(ICLineBreakpoint.FUNCTION));
            properties.put(MIBreakpoints.ADDRESS,         attributes.get(ICLineBreakpoint.ADDRESS));
            properties.put(MIBreakpoints.COMMANDS,        attributes.get(BreakpointActionManager.BREAKPOINT_ACTION_ATTRIBUTE));
            
            if (breakpoint instanceof ICTracepoint) {
            	// A tracepoint is a LineBreakpoint, but needs its own type
                properties.put(MIBreakpoints.BREAKPOINT_TYPE, MIBreakpoints.TRACEPOINT);
                properties.put(MIBreakpoints.PASS_COUNT, attributes.get(ICTracepoint.PASS_COUNT));
            } else if (breakpoint instanceof ICDynamicPrintf) {
            	// A DynamicPrintf is a LineBreakpoint, but needs its own type
            	properties.put(MIBreakpoints.BREAKPOINT_TYPE, MIBreakpoints.DYNAMICPRINTF);
            	properties.put(MIBreakpoints.PRINTF_STRING, attributes.get(ICDynamicPrintf.PRINTF_STRING));
            }
        }
        else if (breakpoint instanceof ICEventBreakpoint) {
        	properties.put(MIBreakpoints.BREAKPOINT_TYPE, MIBreakpoints.CATCHPOINT);
        	properties.put(MIBreakpoints.CATCHPOINT_TYPE, GdbCatchpoints.eventToGdbCatchpointKeyword((String)attributes.get(ICEventBreakpoint.EVENT_TYPE_ID)));

        	String arg = (String)attributes.get(ICEventBreakpoint.EVENT_ARG);
        	String[] args;
        	if ((arg != null) && (arg.length() != 0)) {
        		args = new String[1];
        		args[0] = arg;
        	}
        	else {
        		args = new String[0];
        	}
    		properties.put(MIBreakpoints.CATCHPOINT_ARGS, args);
        }
        else {
        	assert false : "platform breakpoint is of an unexpected type: " + breakpoint.getClass().getName(); //$NON-NLS-1$
        }

        // Common fields
        properties.put(MIBreakpoints.CONDITION,           attributes.get(ICBreakpoint.CONDITION));
        properties.put(MIBreakpoints.IGNORE_COUNT,        attributes.get(ICBreakpoint.IGNORE_COUNT));
        properties.put(MIBreakpoints.IS_ENABLED,          attributes.get(ICBreakpoint.ENABLED));
        properties.put(MIBreakpointDMData.THREAD_ID,      attributes.get(ATTR_THREAD_ID));

        // checks for the breakpoint type, and adds the hardware/temporary flags 
        Object breakpointType = attributes.get(ICBreakpointType.TYPE);
        if(breakpointType != null) {
        	if(breakpointType instanceof Integer) {
	            boolean isHardware = ((Integer) breakpointType & ICBreakpointType.HARDWARE) == ICBreakpointType.HARDWARE;
	            boolean isTemporary = ((Integer) breakpointType & ICBreakpointType.TEMPORARY) == ICBreakpointType.TEMPORARY;
	            properties.put(MIBreakpointDMData.IS_HARDWARE, isHardware);
	            properties.put(MIBreakpointDMData.IS_TEMPORARY, isTemporary);
        	}
        }

        // Adjust for "skip-all"
        // Tracepoints and dynamic printf are not affected by "skip-all"
        if (!(breakpoint instanceof ICTracepoint) && !(breakpoint instanceof ICDynamicPrintf) 
        		&& !fBreakpointManager.isEnabled()) {
            properties.put(MIBreakpoints.IS_ENABLED, false);
        }

        return properties;
    }

    /**
     * Determine if the modified attributes necessitate
     * a breakpoint removal/re-installation
     * 
     * @param delta
     * @return
     */
    protected boolean needsResinstallation(Map<String,Object> delta) {
        
        // Check if there is any modified attribute
        if (delta == null)
            return false;

        // Check the "critical" attributes
        if (delta.containsKey(ATTR_DEBUGGER_PATH)            // File name
        ||  delta.containsKey(MIBreakpoints.LINE_NUMBER)     // Line number
        ||  delta.containsKey(MIBreakpoints.BREAKPOINT_TYPE)     // breakpoint type
        ||  delta.containsKey(MIBreakpoints.FUNCTION)        // Function name
        ||  delta.containsKey(MIBreakpoints.ADDRESS)         // Absolute address
        ||  delta.containsKey(ATTR_THREAD_FILTER)            // Thread ID
        ||  delta.containsKey(MIBreakpoints.EXPRESSION)      // Watchpoint expression
        ||  delta.containsKey(MIBreakpoints.READ)            // Watchpoint type
        ||  delta.containsKey(MIBreakpoints.WRITE)           // Watchpoint type
        ||  delta.containsKey(MIBreakpoints.PRINTF_STRING)) {// Dprintf string
            return true;
        }

         return false;
    }

    /**
     * @param breakpoint
     * @param oldValues
     */
    protected void rollbackAttributes(ICBreakpoint breakpoint, IMarkerDelta oldValues) {

        try {
            String new_condition = breakpoint.getCondition();
            if (new_condition == null)
                new_condition = NULL_STRING;
            String old_condition = (oldValues != null) ? oldValues.getAttribute(ICBreakpoint.CONDITION, NULL_STRING) : NULL_STRING;
            if (!old_condition.equals(new_condition)) {
                breakpoint.setCondition(old_condition);
            }
            else {
                breakpoint.setCondition(NULL_STRING);
            }
        } catch (CoreException e) {
        }
    }

    /**
     * Indicates if the back-end supports multiple threads for
     * this type of breakpoint
     * 
     * @param breakpoint
     */
    protected boolean supportsThreads(ICBreakpoint breakpoint) {

        return !(breakpoint instanceof ICWatchpoint);
    }

    /**
     * Returns whether the breakpoint is filtered for the given target.
     */
    private boolean isBreakpointEntirelyFiltered(IBreakpointsTargetDMContext bpTargetDmc, ICBreakpoint breakpoint) {    	
    	try {
    		IContainerDMContext[] procTargets = getFilterExtension(breakpoint).getTargetFilters();
    		for (IContainerDMContext procDmc : procTargets) {
    			if (procDmc.equals(bpTargetDmc) || DMContexts.isAncestorOf(procDmc, bpTargetDmc)) {
    				return false;
    			}
    		}
    	} catch (CoreException e) {
    	}
    	return true;
    }

    /**
	 * @since 4.2
	 */
    public void addBreakpointsTrackingListener(IMIBreakpointsTrackingListener listener) {
    	fTrackingListeners.add(listener);
    }

    /**
	 * @since 4.2
	 */
    public void removeBreakpointsTrackingListener(IMIBreakpointsTrackingListener listener) {
    	fTrackingListeners.remove(listener);
    }
}
