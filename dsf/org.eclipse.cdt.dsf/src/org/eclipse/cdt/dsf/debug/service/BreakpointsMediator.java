/*******************************************************************************
 * Copyright (c) 2007, 2010 Wind River and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River - Initial API and implementation
 *     Ericsson   - Low-level breakpoints integration  
 *******************************************************************************/

package org.eclipse.cdt.dsf.debug.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.RejectedExecutionException;

import org.eclipse.cdt.dsf.concurrent.CountingRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ThreadSafe;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointDMContext;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointsTargetDMContext;
import org.eclipse.cdt.dsf.internal.DsfPlugin;
import org.eclipse.cdt.dsf.service.AbstractDsfService;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointListener;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.IBreakpointManagerListener;
import org.eclipse.debug.core.model.IBreakpoint;
import org.osgi.framework.BundleContext;

/**
 * Breakpoints mediator is a DSF service which synchronizes breakpoints in the 
 * IDE and breakpoints in the debugger.  The IDE breakpoints are managed by the 
 * {@link IBreakpointManager} while the debugger breakpoints are accessed 
 * through the {@link IBreakpoints} service.
 * <p>
 * This class is not intended to be extended by clients.  Instead clients should
 * implement the {@link IBreakpointAttributeTranslator} interface which is used
 * to translate breakpoint attributes between the IDE and debugger breakpoints.
 * <p>
 * Note: This breakpoint mediator implementation has been superseded by a more
 * powerful {@link BreakpointsMediator2} implementation.  
 * 
 * @since 1.0
 * @see IBreakpointAttributeTranslator
 * @see BreakpointsMediator2
 */
public class BreakpointsMediator extends AbstractDsfService implements IBreakpointManagerListener, IBreakpointListener
{

    /**
     * The attribute translator that this service will use to map the platform
     * breakpiont attributes to the corresponding target attributes, and vice
     * versa.
     */
    private IBreakpointAttributeTranslator fAttributeTranslator;

    /**
     * DSF Debug service for creating breakpoints.
     */
    IBreakpoints fBreakpoints;
    
    /**
     * Platform breakpoint manager
     */
    IBreakpointManager fBreakpointManager;
	
    
    ///////////////////////////////////////////////////////////////////////////
    // Breakpoints tracking
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Holds the set of platform breakpoints with their corresponding back-end
     * breakpoint attributes, per context (i.e. each platform breakpoint is
     * replicated for each execution context).
     * - Context entry added/removed on start/stopTrackingBreakpoints()
     * - Augmented on breakpointAdded()
     * - Modified on breakpointChanged()
     * - Diminished on breakpointRemoved()
     */
	private Map<IBreakpointsTargetDMContext, Map<IBreakpoint, List<Map<String, Object>>>> fPlatformBPs = 
		new HashMap<IBreakpointsTargetDMContext, Map<IBreakpoint, List<Map<String, Object>>>>();

    /**
     * Holds the mapping from platform breakpoint to the corresponding target
     * breakpoint(s), per context. There can be multiple back-end BPs for a 
     * single platform BP in the case of [1] multiple target contexts, and/or
     * [2] thread filtering.
     * Updated when:
     * - We start/stop tracking an execution context
     * - A platform breakpoint is added/removed
     * - A thread filter is applied/removed
     */
	private Map<IBreakpointsTargetDMContext, Map<IBreakpoint, List<IBreakpointDMContext>>> fBreakpointDMContexts = 
		new HashMap<IBreakpointsTargetDMContext, Map<IBreakpoint, List<IBreakpointDMContext>>>();

    /**
     * Due to the very asynchronous nature of DSF, a new breakpoint request can
     * pop up at any time before an ongoing one is completed. The following set
     * is used to store requests until the ongoing operation completes.
     */
	private Set<IBreakpoint> fPendingRequests    = new HashSet<IBreakpoint>();
	
	/**
	 * @see fPendingRequests
	 */
	private Set<IBreakpoint> fPendingBreakpoints = new HashSet<IBreakpoint>();
	
    ///////////////////////////////////////////////////////////////////////////
    // AbstractDsfService    
    ///////////////////////////////////////////////////////////////////////////

	/**
	 * The service constructor
	 * 
	 * @param session
	 * @param debugModelId
	 */
	public BreakpointsMediator(DsfSession session, IBreakpointAttributeTranslator attributeTranslator) {
        super(session);
        fAttributeTranslator = attributeTranslator;
	}

    @Override
    public void initialize(final RequestMonitor rm) {
        // - Collect references for the services we interact with
        // - Register to interesting events
        // - Obtain the list of platform breakpoints   
        // - Register the service for interested parties
        super.initialize(
            new RequestMonitor(getExecutor(), rm) { 
                @Override
                protected void handleSuccess() {
                    doInitialize(rm);
                }});
    }

    /**
     * Asynchronous service initialization 
     * 
     * @param requestMonitor
     */
    private void doInitialize(RequestMonitor rm) {
    	
    	// Get the services references
        fBreakpoints  = getServicesTracker().getService(IBreakpoints.class);
        fBreakpointManager = DebugPlugin.getDefault().getBreakpointManager();
        fAttributeTranslator.initialize(this);

        // Register to the useful events
        fBreakpointManager.addBreakpointListener(this);
        fBreakpointManager.addBreakpointManagerListener( this );

        // Register this service
        register(new String[] { BreakpointsMediator.class.getName() },
				 new Hashtable<String, String>());

        rm.done();
    }

    @Override
    public void shutdown(final RequestMonitor rm) {
        // - Un-register the service
        // - Stop listening to events
        // - Remove the breakpoints installed by this service
        // 
        //  Since we are shutting down, there is no overwhelming need
        //  to keep the maps coherent...

        // Stop accepting requests and events
    	unregister();
        fBreakpointManager.removeBreakpointListener(this);
        fBreakpointManager.removeBreakpointManagerListener( this );
        fAttributeTranslator.dispose();

        // Cleanup the breakpoints that are still installed by the service.
        // Use a counting monitor which will call mom to complete the shutdown
        // after the breakpoints are un-installed (successfully or not).
        CountingRequestMonitor countingRm = new CountingRequestMonitor(getExecutor(), rm) {
            @Override
            protected void handleCompleted() {
                BreakpointsMediator.super.shutdown(rm);
            }
        };

        // We have to make a copy of the fPlatformBPs keys because uninstallBreakpoints()
        // modifies the map as it walks through it.
        List<IBreakpointsTargetDMContext> platformBPKeysCopy = new ArrayList<IBreakpointsTargetDMContext>(fPlatformBPs.size());
        platformBPKeysCopy.addAll(0, fPlatformBPs.keySet());
        for (IBreakpointsTargetDMContext dmc : platformBPKeysCopy) {
            stopTrackingBreakpoints(dmc, countingRm);
        }
        countingRm.setDoneCount(platformBPKeysCopy.size());
    }
    
	@Override
    protected BundleContext getBundleContext() {
        return DsfPlugin.getBundleContext();
    }

    ///////////////////////////////////////////////////////////////////////////
    // IBreakpointsManager
    ///////////////////////////////////////////////////////////////////////////


	/**
     * Install and begin tracking breakpoints for given context.  The service 
     * will keep installing new breakpoints that appear in the IDE for this 
     * context until {@link #uninstallBreakpoints(IDMContext)} is called for that
     * context.
     * @param dmc Context to start tracking breakpoints for.
     * @param rm Completion callback.
     */
    public void startTrackingBreakpoints(IBreakpointsTargetDMContext dmc, final RequestMonitor rm) {
        // - Augment the maps with the new execution context
        // - Install the platform breakpoints on the selected target

    	// Validate the context
        final IBreakpointsTargetDMContext breakpointsDmc = DMContexts.getAncestorOfType(dmc, IBreakpointsTargetDMContext.class);
        if (breakpointsDmc == null) {
            rm.setStatus(new Status(IStatus.ERROR, DsfPlugin.PLUGIN_ID, INTERNAL_ERROR, "Invalid context type", null)); //$NON-NLS-1$
            rm.done();            
            return;
        }
            
        // Make sure a mapping for this execution context does not already exist
		Map<IBreakpoint, List<Map<String, Object>>> platformBPs = fPlatformBPs.get(dmc);
		if (platformBPs != null) {
            rm.setStatus(new Status(IStatus.ERROR, DsfPlugin.PLUGIN_ID, INTERNAL_ERROR, "Context already initialized", null)); //$NON-NLS-1$
            rm.done();            
            return;
		}

        // Create entries in the breakpoint tables for the new context. These entries should only
        // be removed when this service stops tracking breakpoints for the given context.
        fPlatformBPs.put(breakpointsDmc, new HashMap<IBreakpoint, List<Map<String, Object>>>());
		fBreakpointDMContexts.put(breakpointsDmc, new HashMap<IBreakpoint, List<IBreakpointDMContext>>());

        // Install the platform breakpoints (stored in fPlatformBPs) on the target.
		// We need to use a background thread for this operation because we are 
		// accessing the resources system to retrieve the breakpoint attributes.
		// Accessing the resources system potentially requires using global locks.
		// Also we will be calling IBreakpointAttributeTranslator which is prohibited
		// from being called on the session executor thread.
		new Job("MI Debugger: Install initial breakpoint list.") { //$NON-NLS-1$
            { setSystem(true); }

			// Get the stored breakpoints from the platform BreakpointManager
			// and install them on the target
        	@Override
            protected IStatus run(IProgressMonitor monitor) {
                // Read initial breakpoints from platform.  Copy the breakpoint attributes into a local map.
                // Note that we cannot write data into fPlatformBPs table here directly because we are not
                // executing on the dispatch thread.
                final Map<IBreakpoint, List<Map<String, Object>>> initialPlatformBPs = 
                    new HashMap<IBreakpoint, List<Map<String, Object>>>();
                try {
                	// Get the stored breakpoint list from the platform BreakpointManager
                    IBreakpoint[] bps = DebugPlugin.getDefault().getBreakpointManager().getBreakpoints();
                    // Single out the installable breakpoints...
                    for (IBreakpoint bp : bps) {
                    	if (fAttributeTranslator.supportsBreakpoint(bp)) {
	                        // Retrieve the breakpoint attributes
                    		List<Map<String, Object>> attrsArray = 
                    		    fAttributeTranslator.getBreakpointAttributes(bp, fBreakpointManager.isEnabled());
	                        // Store it for now (will be installed on the dispatcher thread)
                            initialPlatformBPs.put(bp, attrsArray);
                        }
                    }
                } catch (CoreException e) {
                    IStatus status = new Status(
                        IStatus.ERROR, DsfPlugin.PLUGIN_ID, REQUEST_FAILED, "Unable to read initial breakpoint attributes", e); //$NON-NLS-1$
                    rm.setStatus(status);
                    rm.done();
                    return status;
                }
                
                // Submit the runnable to plant the breakpoints on dispatch thread.
                getExecutor().submit(new Runnable() {
                    @Override
                	public void run() {
                		installInitialBreakpoints(breakpointsDmc, initialPlatformBPs, rm);
                	}
                });

                return Status.OK_STATUS;
            }
        }.schedule();    
    }

    /**
     * Installs the breakpoints that existed prior to the activation of this
     * breakpoints context.
     */
    private void installInitialBreakpoints(final IBreakpointsTargetDMContext dmc,
            Map<IBreakpoint, List<Map<String, Object>>> initialPlatformBPs,
            RequestMonitor rm)
    {
        // Retrieve the set of platform breakpoints for this context
        Map<IBreakpoint, List<Map<String, Object>>> platformBPs = fPlatformBPs.get(dmc);
        if (platformBPs == null) {
            rm.setStatus(new Status(IStatus.ERROR, DsfPlugin.PLUGIN_ID, INVALID_HANDLE, "Invalid context", null)); //$NON-NLS-1$
            rm.done();
            return;
        }

        // Install the individual breakpoints on the executor thread
        // Requires a counting monitor to know when we're done
        final CountingRequestMonitor countingRm = new CountingRequestMonitor(getExecutor(), rm);
        countingRm.setDoneCount(initialPlatformBPs.size());

        for (final IBreakpoint bp : initialPlatformBPs.keySet()) {
            final List<Map<String, Object>> attrs = initialPlatformBPs.get(bp);
            // Upon determining the debuggerPath, the breakpoint is installed
            installBreakpoint(dmc, bp, attrs, new RequestMonitor(getExecutor(), countingRm));
        }
    }

    
    public void stopTrackingBreakpoints(final IBreakpointsTargetDMContext dmc, final RequestMonitor rm) {
        // - Remove the target breakpoints for the given execution context
        // - Update the maps

    	// Remove the breakpoints for given DMC from the internal maps.
        Map<IBreakpoint, List<Map<String, Object>>> platformBPs = fPlatformBPs.get(dmc);
        if (platformBPs == null) {
            rm.setStatus(new Status(IStatus.ERROR, DsfPlugin.PLUGIN_ID, INTERNAL_ERROR, "Breakpoints not installed for given context", null)); //$NON-NLS-1$
            rm.done();
            return;
        }

        // Uninstall the individual breakpoints on the executor thread
        // Requires a counting monitor to know when we're done
        final CountingRequestMonitor countingRm = new CountingRequestMonitor(getExecutor(), rm);
        countingRm.setDoneCount(platformBPs.size());

        for (final IBreakpoint bp : platformBPs.keySet()) {
            uninstallBreakpoint(dmc, bp, 
                new RequestMonitor(getExecutor(), countingRm) {
                    @Override
                    protected void handleCompleted() {
                        // After the breakpoint is removed from target.  Call the attribute 
                        // translator to refresh breakpoint status based on the new target 
                        // breakpoint status. 
                        new Job("Breakpoint status update") { //$NON-NLS-1$
                            { setSystem(true); }
                            @Override
                            protected IStatus run(IProgressMonitor monitor) {
                                fAttributeTranslator.updateBreakpointStatus(bp);
                                return Status.OK_STATUS;
                            };
                        }.schedule();

                        countingRm.done();
                    }
                });
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Back-end interface functions
    ///////////////////////////////////////////////////////////////////////////

	/**
	 * Install a new platform breakpoint on the back-end. A platform breakpoint
	 * can resolve into multiple back-end breakpoints when threads are taken
	 * into account.
	 *  
	 * @param dmc
	 * @param breakpoint
	 * @param attrsList
	 * @param rm
	 */
	private void installBreakpoint(IBreakpointsTargetDMContext dmc, final IBreakpoint breakpoint,
			final List<Map<String, Object>> attrsList, final RequestMonitor rm)
	{
    	// Retrieve the set of breakpoints for this context
        final Map<IBreakpoint, List<Map<String, Object>>> platformBPs = fPlatformBPs.get(dmc);
        assert platformBPs != null;

        final Map<IBreakpoint, List<IBreakpointDMContext>> breakpointIDs = fBreakpointDMContexts.get(dmc);
        assert breakpointIDs != null; // fBreakpointIds should be updated in parallel with fPlatformBPs 

        // Ensure the breakpoint is not already installed
        if (platformBPs.containsKey(breakpoint)) {
            rm.setStatus(new Status(IStatus.ERROR, DsfPlugin.PLUGIN_ID, INVALID_STATE, "Breakpoint already installed", null)); //$NON-NLS-1$
            rm.done();
            return;
        }

        // Update the breakpoint status when all back-end breakpoints have been installed
    	final CountingRequestMonitor installRM = new CountingRequestMonitor(getExecutor(), rm) {
			@Override
			protected void handleCompleted() {
				// Store the platform breakpoint
				platformBPs.put(breakpoint, attrsList);
                new Job("Breakpoint status update") { //$NON-NLS-1$
                    { setSystem(true); }
                    @Override
                    protected IStatus run(IProgressMonitor monitor) {
                        fAttributeTranslator.updateBreakpointStatus(breakpoint);
                        return Status.OK_STATUS;
                    };
                }.schedule();
		        rm.done();
			}
		};

        // A back-end breakpoint needs to be installed for each specified attributes map.
		installRM.setDoneCount(attrsList.size());

		// Install the back-end breakpoint(s)
		for (Map<String, Object> attrs : attrsList) {
            fBreakpoints.insertBreakpoint(
                dmc, attrs, 
				new DataRequestMonitor<IBreakpointDMContext>(getExecutor(), installRM) {
				@Override
                protected void handleCompleted() {
                    List<IBreakpointDMContext> list = breakpointIDs.get(breakpoint);
                    if (list == null) {
                        list = new LinkedList<IBreakpointDMContext>();
                        breakpointIDs.put(breakpoint, list);
                    }
                    
                    if (isSuccess()) {
						// Add the breakpoint back-end mapping
						list.add(getData());
					} else {
                        // TODO (bug 219841): need to add breakpoint error status tracking
                        // in addition to fBreakpointDMContexts.
					}
					installRM.done();
                }
            });
		}
	}

    /**
     * Un-install an individual breakpoint on the back-end. For one platform
     * breakpoint, there could be multiple corresponding back-end breakpoints.
     * 
     * @param dmc
     * @param breakpoint
     * @param rm
     */
    private void uninstallBreakpoint(final IBreakpointsTargetDMContext dmc, final IBreakpoint breakpoint, 
        final RequestMonitor rm)
    {
  		// Remove completion monitor
    	CountingRequestMonitor removeRM = new CountingRequestMonitor(getExecutor(), rm) {
			@Override
			protected void handleCompleted() {
		    	// Remove the attributes mapping 
		        Map<IBreakpoint, List<Map<String, Object>>> platformBPs = fPlatformBPs.get(dmc);
		        if (platformBPs == null) {
		            rm.setStatus(new Status(IStatus.ERROR, DsfPlugin.PLUGIN_ID, INVALID_HANDLE, "Invalid context", null)); //$NON-NLS-1$
		            rm.done();
		            return;
		        }
		        platformBPs.remove(breakpoint);

				// Remove the back-end mapping
		        Map<IBreakpoint, List<IBreakpointDMContext>> breakpointIDs = fBreakpointDMContexts.get(dmc);
		        if (breakpointIDs == null) {
		            rm.setStatus(new Status(IStatus.ERROR, DsfPlugin.PLUGIN_ID, INVALID_HANDLE, "Invalid breakpoint", null)); //$NON-NLS-1$
		            rm.done();
		            return;
		        }
		        breakpointIDs.get(breakpoint).clear();
		        breakpointIDs.remove(breakpoint);

		        // Update breakpoint status
                new Job("Breakpoint status update") { //$NON-NLS-1$
                    { setSystem(true); }
                    @Override
                    protected IStatus run(IProgressMonitor monitor) {
                        fAttributeTranslator.updateBreakpointStatus(breakpoint);
                        return Status.OK_STATUS;
                    };
                }.schedule();

		        rm.done();
			}
		};

		// Remove the back-end breakpoints
		Map<IBreakpoint, List<IBreakpointDMContext>> breakpointIDs = fBreakpointDMContexts.get(dmc);
        if (breakpointIDs == null) {
            rm.setStatus(new Status(IStatus.ERROR, DsfPlugin.PLUGIN_ID, INVALID_HANDLE, "Invalid breakpoint", null)); //$NON-NLS-1$
            rm.done();
            return;
        }

        List<IBreakpointDMContext> list = breakpointIDs.get(breakpoint);
        int count = 0;
        if (list != null) {
            for (IBreakpointDMContext bp : list) {
                fBreakpoints.removeBreakpoint(bp, removeRM);
            }
            count = list.size();
        }
        removeRM.setDoneCount(count);
    }
	
	/**
	 * Modify an individual breakpoint
	 * 
	 * @param context
	 * @param breakpoint
	 * @param attributes
	 * @param rm
	 * @throws CoreException
	 */
	private void modifyBreakpoint(final IBreakpointsTargetDMContext context, final IBreakpoint breakpoint,
			final List<Map<String, Object>> newAttrsList0, final IMarkerDelta oldValues, final RequestMonitor rm)
	{
	    // This method uses several lists to track the changed breakpoints:
	    // commonAttrsList - attributes which have not changed 
	    // oldAttrsList - attributes for the breakpoint before the change
	    // newAttrsList - attributes for the breakpoint after the change
	    // oldBpContexts - target-side breakpoints from before the change
	    // newBpContexts - target-side breakpoints after the change
	    // attrDeltasList - changes in the attributes for each attribute map in 
	    //     oldAttrsList and newAttrsList
	    
    	// Get the maps
        final Map<IBreakpoint, List<Map<String, Object>>> platformBPs = fPlatformBPs.get(context);
        final Map<IBreakpoint, List<IBreakpointDMContext>> breakpointIDs = fBreakpointDMContexts.get(context);
        if (platformBPs == null || breakpointIDs == null) {
            rm.setStatus(new Status(IStatus.ERROR, DsfPlugin.PLUGIN_ID, INVALID_HANDLE, "Invalid context", null)); //$NON-NLS-1$
            rm.done();
            return;
        }

    	// Get the original breakpoint attributes
        final List<Map<String, Object>> oldAttrsList0 = platformBPs.get(breakpoint);
        if (oldAttrsList0 == null) {
            rm.setStatus(new Status(IStatus.ERROR, DsfPlugin.PLUGIN_ID, INVALID_HANDLE, "Invalid breakpoint", null)); //$NON-NLS-1$
            rm.done();
            return;
        }

        // Get the list of corresponding back-end breakpoints 
        final List<IBreakpointDMContext> oldBpContexts = new ArrayList<IBreakpointDMContext>(breakpointIDs.get(breakpoint));
        if (oldBpContexts == null) {
            rm.setStatus(new Status(IStatus.ERROR, DsfPlugin.PLUGIN_ID, INVALID_HANDLE, "Invalid breakpoint", null)); //$NON-NLS-1$
            rm.done();
            return;
        }

        // Calculate the list of attributes maps that have not changed.  
        // Immediately add these to the list of new breakpoint contexts,
        // and remove them from further breakpoint attribute comparisons.
        final List<Map<String, Object>> commonAttrsList = getCommonAttributeMaps(newAttrsList0, oldAttrsList0);
        final List<IBreakpointDMContext> newBpContexts = new ArrayList<IBreakpointDMContext>(commonAttrsList.size());

        final List<Map<String, Object>> newAttrsList = new ArrayList<Map<String, Object>>(newAttrsList0);
        newAttrsList.removeAll(commonAttrsList);
        
        List<Map<String, Object>> oldAttrsList = new ArrayList<Map<String, Object>>(oldAttrsList0);
        for (int i = 0; i < oldAttrsList.size(); i++) {
            if (commonAttrsList.contains(oldAttrsList.get(i))) {
                if (oldBpContexts.size() > i) {
                    newBpContexts.add(oldBpContexts.remove(i));
                }
            }
        }
        oldAttrsList.removeAll(commonAttrsList);
        
        // Create a list of attribute changes.  The lenghth of this list will
        // always be max(oldAttrList.size(), newAttrsList.size()), padded with
        // null's if oldAttrsList was longer.
        final List<Map<String, Object>> attrDeltasList = getAttributesDeltas(oldAttrsList, newAttrsList);
        
        // Create the request monitor that will be called when all
        // modifying/inserting/removing is complete.
        final CountingRequestMonitor countingRM = new CountingRequestMonitor(getExecutor(), rm) {
            @Override
            protected void handleCompleted() {
                // Save the new list of breakpoint contexts and attributes 
                breakpointIDs.put(breakpoint, newBpContexts);
                newAttrsList.addAll(commonAttrsList);
                platformBPs.put(breakpoint, newAttrsList);
                
                // Update breakpoint status.  updateBreakpointStatus() cannot
                // be called on the executor thread, so we need to 
                // use a Job.
                new Job("Breakpoint status update") { //$NON-NLS-1$
                    { setSystem(true); }
                    @Override
                    protected IStatus run(IProgressMonitor monitor) {
                        fAttributeTranslator.updateBreakpointStatus(breakpoint);
                        return Status.OK_STATUS;
                    };
                }.schedule();
                
                super.handleCompleted();
            }
        };
        
        // Set the count, if could be zero if no breakpoints have actually changed.
        countingRM.setDoneCount(attrDeltasList.size());
        
        // Process the changed breakpoints.
        for (int i = 0; i < attrDeltasList.size(); i++) {
            if (attrDeltasList.get(i) == null) {
                // The list of new attribute maps was shorter than the old.
                // Remove the corresponding target-side bp.
                fBreakpoints.removeBreakpoint(oldBpContexts.get(i), countingRM);
            } else if ( i >= oldBpContexts.size()) {
                // The list of new attribute maps was longer, just insert
                // the new breakpoint
                final Map<String, Object> attrs = newAttrsList.get(i);
                fBreakpoints.insertBreakpoint(
                    context, attrs, 
                    new DataRequestMonitor<IBreakpointDMContext>(getExecutor(), countingRM) {
                        @Override
                        protected void handleSuccess() {
                            newBpContexts.add(getData());
                            countingRM.done();
                        }
                    });
            } else if ( !fAttributeTranslator.canUpdateAttributes(oldBpContexts.get(i), attrDeltasList.get(i)) ) {
                // The attribute translator tells us that the debugger cannot modify the 
                // breakpoint to change the given attributes.  Remove the breakpoint
                // and insert a new one.
                final Map<String, Object> attrs = newAttrsList.get(i);
                fBreakpoints.removeBreakpoint(
                    oldBpContexts.get(i), 
                    new RequestMonitor(getExecutor(), countingRM) {
                        @Override
                        protected void handleCompleted() {
                            fBreakpoints.insertBreakpoint(
                                context, attrs,
                                new DataRequestMonitor<IBreakpointDMContext>(getExecutor(), countingRM) {
                                    @Override
                                    protected void handleCompleted() {
                                        if (isSuccess()) { 
                                            newBpContexts.add(getData());
                                        } else {
                                            // TODO (bug 219841): need to add breakpoint error status tracking
                                            // in addition to fBreakpointDMContexts.
                                        }
                                        countingRM.done();
                                    }
                                });
                        }
                    });
            } else {
                // The back end can modify the breakpoint.  Update the breakpoint with the 
                // new attributes.
                final IBreakpointDMContext bpCtx = oldBpContexts.get(i); 
                fBreakpoints.updateBreakpoint(
                    oldBpContexts.get(i), newAttrsList.get(i), 
                    new RequestMonitor(getExecutor(), countingRM) {
                        @Override
                        protected void handleSuccess() {
                            newBpContexts.add(bpCtx);
                            countingRM.done();
                        }
                    });
            }
        }
	} 
	
	private List<Map<String, Object>> getCommonAttributeMaps(List<Map<String, Object>> array1, List<Map<String, Object>> array2) 
	{
	    List<Map<String, Object>> intersection = new LinkedList<Map<String, Object>>();
	    List<Map<String, Object>> list2 = new ArrayList<Map<String, Object>>(array2);
	    for (Map<String, Object> array1Map : array1) {
	        if (list2.remove(array1Map)) {
	            intersection.add(array1Map);
	        }
	    }
	    return intersection;
	}
	
	/**
	 * Determine the set of modified attributes
	 * 
	 * @param oldAttributes
	 * @param newAttributes
	 * @return
	 */
	private List<Map<String, Object>> getAttributesDeltas(List<Map<String, Object>> oldAttributesList, List<Map<String, Object>> newAttributesList) {
	    List<Map<String, Object>> deltas = new ArrayList<Map<String, Object>>(oldAttributesList.size());
	    
	    // Go through the bp attributes common to the old and the new lists and calculate
	    // their deltas.
	    for (int i = 0; i < oldAttributesList.size() && i < newAttributesList.size(); i++) {
	        Map<String, Object> oldAttributes = oldAttributesList.get(i); 
	        Map<String, Object> newAttributes = newAttributesList.get(i);
    	    
	        Map<String, Object> delta = new HashMap<String, Object>();
    
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
    		deltas.add(delta);
	    } 
	    
	    // Add all the new attributes as deltas
	    for (int i = deltas.size(); i < newAttributesList.size(); i++) {
	        deltas.add(newAttributesList.get(i));
	    }
	    
        // For any old attribute Maps that were removed, insert a null value in the deltas list.
        for (int i = deltas.size(); i < oldAttributesList.size(); i++) {
            deltas.add(null);
        }
	    
	    return deltas;
	}

    ///////////////////////////////////////////////////////////////////////////
    // IBreakpointManagerListener implementation
    ///////////////////////////////////////////////////////////////////////////

    @Override
	public void breakpointManagerEnablementChanged(boolean enabled) {
		for (IBreakpoint breakpoint : fBreakpointManager.getBreakpoints()) {
		    breakpointChanged(breakpoint, null);
		}
	}

	@ThreadSafe
    @Override
	public void breakpointAdded(final IBreakpoint breakpoint) {
		if (fAttributeTranslator.supportsBreakpoint(breakpoint)) {
			try {
                // Retrieve the breakpoint attributes
        		final List<Map<String, Object>> attrsArray = 
                    fAttributeTranslator.getBreakpointAttributes(breakpoint, fBreakpointManager.isEnabled());

                getExecutor().execute(new DsfRunnable() {
                    @Override
					public void run() {
					    //TODO pp: need to track pending requests 
					    
						final CountingRequestMonitor countingRm = new CountingRequestMonitor(getExecutor(), null) {
							@Override
							protected void handleError() {
								if (getStatus().getSeverity() == IStatus.ERROR) {
									DsfPlugin.getDefault().getLog().log(getStatus());
								}
							}
						};
						countingRm.setDoneCount(fPlatformBPs.size());

						// Install the breakpoint in all the execution contexts
						for (final IBreakpointsTargetDMContext dmc : fPlatformBPs.keySet()) {
						    installBreakpoint(dmc, breakpoint, attrsArray, new RequestMonitor(getExecutor(), countingRm));
						}
					}
				});
			} catch (CoreException e) {
                DsfPlugin.getDefault().getLog().log(e.getStatus());
			} catch (RejectedExecutionException e) {
			}
		}

	}
	
    ///////////////////////////////////////////////////////////////////////////
    // IBreakpointListener implementation
    ///////////////////////////////////////////////////////////////////////////

    @Override
	public void breakpointChanged(final IBreakpoint breakpoint, final IMarkerDelta delta) {
		if (fAttributeTranslator.supportsBreakpoint(breakpoint)) {
			try {
                // Retrieve the breakpoint attributes
        		final List<Map<String, Object>> attrsArray = 
        		    fAttributeTranslator.getBreakpointAttributes(breakpoint, fBreakpointManager.isEnabled());

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
			                            DsfPlugin.getDefault().getLog().log(getStatus());
			                        }
		                    	}

		                    	// Indicate that the pending request has completed
		                    	fPendingRequests.remove(breakpoint);

		                    	// Process the next pending update for this breakpoint
		                    	if (fPendingBreakpoints.contains(breakpoint)) {
		                    		fPendingBreakpoints.remove(breakpoint);
		                    		new Job("Deferred breakpoint changed job") { //$NON-NLS-1$
		                                { setSystem(true); }
		                                @Override
                                        protected IStatus run(IProgressMonitor monitor) {
		                                    breakpointChanged(breakpoint, delta);
		                                    return Status.OK_STATUS;
		                                };
		                    		}.schedule();
		                    	}
		                    }
		                };
		                countingRm.setDoneCount(fPlatformBPs.size());

		                // Mark the breakpoint as being updated and go
		                fPendingRequests.add(breakpoint);
		                
		                // Modify the breakpoint in all the execution contexts
		                for (final IBreakpointsTargetDMContext dmc : fPlatformBPs.keySet()) {
		                    modifyBreakpoint(dmc, breakpoint, attrsArray, delta, new RequestMonitor(getExecutor(), countingRm));
		                }
		            }
		        });
		    } catch (CoreException e) {
                DsfPlugin.getDefault().getLog().log(e.getStatus());
		    } catch (RejectedExecutionException e) {
		    }
		}

	}

    @Override
	public void breakpointRemoved(final IBreakpoint breakpoint, IMarkerDelta delta) {

    	if (fAttributeTranslator.supportsBreakpoint(breakpoint)) {
            try {
                getExecutor().execute(new DsfRunnable() {
                    @Override
                	public void run() {
                        //TODO pp: need to track pending requests 

                		CountingRequestMonitor countingRm = new CountingRequestMonitor(getExecutor(), null) {
                			@Override
                			protected void handleError() {
                				if (getStatus().getSeverity() == IStatus.ERROR) {
                					DsfPlugin.getDefault().getLog().log(getStatus());
                				}
                			}
                		};
                		countingRm.setDoneCount(fPlatformBPs.size());

                		// Remove the breakpoint in all the execution contexts
                		for (IBreakpointsTargetDMContext dmc : fPlatformBPs.keySet()) {
                			if (fPlatformBPs.get(dmc).remove(breakpoint) != null) {
                				uninstallBreakpoint(dmc, breakpoint, countingRm);
                			} else {
                				// Breakpoint not installed for given context, do nothing.
                			}
                		}
                	}
                });
            } catch (RejectedExecutionException e) {
            }
    	}
		
	}
}
