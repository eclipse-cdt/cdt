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
 *     Nokia - refactored to work for both GDB and EDC.  Nov. 2009.
 *******************************************************************************/

package org.eclipse.cdt.dsf.debug.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ThreadSafeAndProhibitedFromDsfExecutor;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointDMContext;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointsTargetDMContext;
import org.eclipse.cdt.dsf.internal.DsfPlugin;
import org.eclipse.cdt.dsf.service.AbstractDsfService;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.IBreakpointsListener;
import org.eclipse.debug.core.model.IBreakpoint;
import org.osgi.framework.BundleContext;

/**
/**
 * Breakpoints mediator is a DSF service which synchronizes breakpoints in the 
 * IDE and breakpoints in the debugger.  The IDE breakpoints are managed by the 
 * {@link IBreakpointManager} while the debugger breakpoints are accessed 
 * through the {@link IBreakpoints} service.
 * <p>
 * This class is not intended to be extended by clients.  Instead clients should
 * implement the {@link IBreakpointAttributeTranslator2} interface which is used
 * to translate breakpoint attributes between the IDE and debugger breakpoints.
 * <p>
 * Note: This breakpoint mediator is a second generation implementation that 
 * succeeds {@link BreakpointsMediator}.  This new implementation includes 
 * the following additional features:
 * <ul>
 *   <li> support for multiple target breakpoints for each IDE breakpoint, </li>
 *   <li> support for retrieving the mapping between IDE breakpoints and 
 *        debugger breakpoints,</li>
 *   <li> support for updating IDE breakpoint status based on full target
 *        breakpoint data. </li>
 * </ul>
 * 
 * @see IBreakpointAttributeTranslator2
 * @see BreakpointsMediator
 * 
 * @since 2.1
 */
public class BreakpointsMediator2 extends AbstractDsfService implements IBreakpointsListener
{
	public enum BreakpointEventType {ADDED, REMOVED, MODIFIED}; 	
	
    /**
     * The attribute translator that this service will use to map the platform
     * breakpoint attributes to the corresponding target attributes, and vice
     * versa.
     */
    private IBreakpointAttributeTranslator2 fAttributeTranslator2;

    /**
     * DSF Debug service for creating breakpoints.
     */
    IBreakpoints fBreakpointsService;
    
    /**
     * Platform breakpoint manager
     */
    IBreakpointManager fBreakpointManager;

    /**
     * Object describing the information about a single target breakpoint  
     * corresponding to specific platform breakpoint and breakpoint target 
     * context.
     */
    public interface ITargetBreakpointInfo {

    	/**
    	 * Returns the breakpoint attributes as returned by the attribute translator.
    	 */
    	public Map<String, Object> getAttributes();

    	/**
    	 * Returns the target breakpoint context.  Returns <code>null</code> if the 
    	 * breakpoint failed to install on target. 
    	 */
    	public IBreakpointDMContext getTargetBreakpoint();

    	/**
    	 * Returns the status result of the last breakpoint operation (install/remove). 
    	 */
    	public IStatus getStatus();
    }
    
    private static class TargetBP implements ITargetBreakpointInfo {
    	
    	private Map<String, Object> fAttributes;
    	private IBreakpointDMContext fTargetBPContext;
    	private IStatus fStatus;
    	
    	public TargetBP(Map<String, Object> attrs) {
    		fAttributes = attrs;
    	}
    	
        @Override
    	public Map<String, Object> getAttributes() {
			return fAttributes;
		}
    	
        @Override
    	public IBreakpointDMContext getTargetBreakpoint() {
			return fTargetBPContext;
		}
    	
        @Override
    	public IStatus getStatus() {
			return fStatus;
		}

		public void setTargetBreakpoint(IBreakpointDMContext fTargetBPContext) {
			this.fTargetBPContext = fTargetBPContext;
		}

		public void setStatus(IStatus status) {
			this.fStatus = status;
		}
    }
    
	private class PlatformBreakpointInfo {
		IBreakpoint 		breakpoint;
		boolean 			enabled;
		// All attributes available from UI, including standard and extended ones.
		Map<String, Object>	attributes;
		
		public PlatformBreakpointInfo(IBreakpoint bp, boolean enabled, Map<String, Object> attributes) {
			super();
			breakpoint = bp;
			this.enabled = enabled;
			this.attributes = attributes;
		}
	}

	///////////////////////////////////////////////////////////////////////////
    // Breakpoints tracking
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Holds the set of platform breakpoints with their breakpoint information 
     * structures, per context (i.e. each platform breakpoint is
     * replicated for each execution context).
     * - Context entry added/removed on start/stopTrackingBreakpoints()
     * - Augmented on breakpointAdded()
     * - Modified on breakpointChanged()
     * - Diminished on breakpointRemoved()
     */
	private Map<IBreakpointsTargetDMContext, Map<IBreakpoint, List<ITargetBreakpointInfo>>> fPlatformBPs = 
		new HashMap<IBreakpointsTargetDMContext, Map<IBreakpoint, List<ITargetBreakpointInfo>>>();

	/**
	 * BreakpointsTargetDMContext's that are being removed from {@link #fPlatformBPs}.
	 * See where this is used for more.
	 */
	private List<IBreakpointsTargetDMContext>	fBPTargetDMCsBeingRemoved = new ArrayList<IBreakpoints.IBreakpointsTargetDMContext>();
	
	/**
	 * Mapping of platform breakpoints to all their attributes (standard ones and
	 * extended ones) from UI. This will be used to check what attributes have
	 * changed for a breakpoint when the breakpoint is changed. The map is <br>
	 * 1. augmented in breakpointsAdded(); <br>
	 * 2. updated in breakpointsChanged(); <br>
	 * 3. diminished in breakpointsRemoved();
	 */
	private Map<IBreakpoint, Map<String, Object>> fBreakpointAttributes = 
		new HashMap<IBreakpoint, Map<String, Object>>();
	
	/**
	 * Hold info about a breakpoint events (added, removed, changed) for later
	 * handling.
	 */
	private static class PendingEventInfo {
		PendingEventInfo(BreakpointEventType eventType, PlatformBreakpointInfo bpInfo,
				Collection<IBreakpointsTargetDMContext> bpsTargetDmc, RequestMonitor rm) {
			fEventType = eventType;
			fBPInfo = bpInfo;
			fBPTargetContexts = bpsTargetDmc;
			fRequestMonitor = rm;
			fAttributeDelta = null;
		}
		
		PendingEventInfo(BreakpointEventType eventType, Collection<IBreakpointsTargetDMContext> updateContexts,
				Map<String, Object> attrDelta) {
			fEventType = eventType;
			fBPTargetContexts = updateContexts;
			fAttributeDelta = attrDelta;
			fRequestMonitor = null;
			fBPInfo = null;
		}

		PlatformBreakpointInfo fBPInfo;
		RequestMonitor fRequestMonitor;
		BreakpointEventType fEventType;
		Collection<IBreakpointsTargetDMContext> fBPTargetContexts;
		Map<String, Object>	fAttributeDelta;	// for change event only
	}
	
    /**
     * Due to the very asynchronous nature of DSF, a new breakpoint request can
     * pop up at any time before an ongoing one is completed. The following set
     * is used to store requests until the ongoing operation completes.
     */
	private Set<IBreakpoint> fRunningEvents    = new HashSet<IBreakpoint>();

	private Map<IBreakpoint, LinkedList<PendingEventInfo>> fPendingEvents = 
			new HashMap<IBreakpoint, LinkedList<PendingEventInfo>>();
	
    ///////////////////////////////////////////////////////////////////////////
    // AbstractDsfService    
    ///////////////////////////////////////////////////////////////////////////

	/**
	 * The service constructor
	 * 
	 * @param session
	 * @param debugModelId
	 */
	public BreakpointsMediator2(DsfSession session, IBreakpointAttributeTranslator2 attributeTranslator) {
        super(session);
        fAttributeTranslator2 = attributeTranslator;
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
        fBreakpointsService  = getServicesTracker().getService(IBreakpoints.class);
        fBreakpointManager = DebugPlugin.getDefault().getBreakpointManager();
        fAttributeTranslator2.initialize(this);

        // Register to the useful events
        fBreakpointManager.addBreakpointListener(this);

        // Register this service
        register(new String[] { BreakpointsMediator2.class.getName() },
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
        fAttributeTranslator2.dispose();

        // Cleanup the breakpoints that are still installed by the service.
        // Use a counting monitor which will call mom to complete the shutdown
        // after the breakpoints are un-installed (successfully or not).
        CountingRequestMonitor countingRm = new CountingRequestMonitor(getExecutor(), rm) {
            @Override
            protected void handleCompleted() {
                BreakpointsMediator2.super.shutdown(rm);
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

    protected String getPluginID() {
        return DsfPlugin.PLUGIN_ID;
    }

    protected Plugin getPlugin() {
        return DsfPlugin.getDefault();
    }

	/**
     * Install and begin tracking breakpoints for given context.  The service 
     * will keep installing new breakpoints that appear in the IDE for this 
     * context until {@link #stopTrackingBreakpoints} is called for that
     * context.
     * @param dmc Context to start tracking breakpoints for.
     * @param rm Completion callback.
     */
    public void startTrackingBreakpoints(final IBreakpointsTargetDMContext dmc, final RequestMonitor rm) {
        // - Augment the maps with the new execution context
        // - Install the platform breakpoints on the selected target
            
        // Make sure a mapping for this execution context does not already exist
		Map<IBreakpoint, List<ITargetBreakpointInfo>> platformBPs = fPlatformBPs.get(dmc);
		if (platformBPs != null) {
            rm.setStatus(new Status(IStatus.ERROR, getPluginID(), INTERNAL_ERROR, "Context already initialized", null)); //$NON-NLS-1$
            rm.done();            
            return;
		}

        // Create entries in the breakpoint tables for the new context. These entries should only
        // be removed when this service stops tracking breakpoints for the given context.
        fPlatformBPs.put(dmc, new HashMap<IBreakpoint, List<ITargetBreakpointInfo>>());

        // Install the platform breakpoints (stored in fPlatformBPs) on the target.
		// We need to use a background thread for this operation because we are 
		// accessing the resources system to retrieve the breakpoint attributes.
		// Accessing the resources system potentially requires using global locks.
		// Also we will be calling some IBreakpointAttributeTranslator2 methods 
        // that are prohibited from being called on the session executor thread.
		new Job("Install initial breakpoint list.") { //$NON-NLS-1$
            { setSystem(true); }

			// Get the stored breakpoints from the platform BreakpointManager
			// and install them on the target
        	@Override
            protected IStatus run(IProgressMonitor monitor) {
        		doBreakpointsAdded(DebugPlugin.getDefault().getBreakpointManager().getBreakpoints(), dmc, rm);
                return Status.OK_STATUS;
            }
        }.schedule();    
    }

    /**
     * Remove and stop installing breakpoints for the given breakpoints target context.
     * @param dmc Context to stop tracking breakpoints for.
     * @param rm Completion callback.
     */
    public void stopTrackingBreakpoints(final IBreakpointsTargetDMContext dmc, final RequestMonitor rm) {
        // - Remove the target breakpoints for the given DMC
    	// - Remove the given DMC from the internal maps.
        //
        Map<IBreakpoint, List<ITargetBreakpointInfo>> platformBPs = fPlatformBPs.get(dmc);
        if (platformBPs == null) {
            rm.setStatus(new Status(IStatus.INFO /* NOT error */, getPluginID(), INTERNAL_ERROR, "Breakpoints not installed for given context", null)); //$NON-NLS-1$
            rm.done();
            return;
        }
        
        if (platformBPs.size() == 0) {
            fPlatformBPs.remove(dmc);	// dmc tracked but no bps installed for it.
            rm.done();
            return;
        }

        // The stopTrackingBreakpoints() may be called twice for the same DMC 
        // on debugger termination (one on process death and one on debugger shutdown).
        // This is to prevent double killing.
        if (fBPTargetDMCsBeingRemoved.contains(dmc)) { // "stop" is already underway
        	rm.done();
        	return;
        }
        
        fBPTargetDMCsBeingRemoved.add(dmc);
        
        // Just remove the IBreakpoints installed for the "dmc".
        final IBreakpoint[] bps = platformBPs.keySet().toArray(new IBreakpoint[platformBPs.size()]);
        
		new Job("Uninstall target breakpoints list.") { //$NON-NLS-1$
            { setSystem(true); }

        	@Override
            protected IStatus run(IProgressMonitor monitor) {
        		doBreakpointsRemoved(bps, dmc, new RequestMonitor(getExecutor(), rm){
					@Override
					protected void handleCompleted() {
						// Regardless of success or failure in removing the breakpoints,
						// we should stop tracking breakpoints for the "dmc" by removing it
						// from the map.
						fPlatformBPs.remove(dmc);
						fBPTargetDMCsBeingRemoved.remove(dmc);
						
						super.handleCompleted();
					}});
                return Status.OK_STATUS;
            }
        }.schedule();    
    }
    
    /**
     * Find target breakpoints installed in the given context that are resolved 
     * from the given platform breakpoint.
     *  
     * @param dmc - context
     * @param platformBp - platform breakpoint
     * @return array of target breakpoints. 
     */
    public ITargetBreakpointInfo[] getTargetBreakpoints(IBreakpointsTargetDMContext dmc, IBreakpoint platformBp) {
    	assert getExecutor().isInExecutorThread();
    	
        Map<IBreakpoint, List<ITargetBreakpointInfo>> platformBPs = fPlatformBPs.get(dmc);

        if (platformBPs != null)
        {
        	List<ITargetBreakpointInfo> bpInfo = platformBPs.get(platformBp);
            if (bpInfo != null) {
            	return bpInfo.toArray(new ITargetBreakpointInfo[bpInfo.size()]);
            }
        }
        return null;
    }
    
    /**
     * Find the platform breakpoint that's mapped to the given target breakpoint.
     * 
     * @param dmc - context of the target breakpoint, can be null.
     * @param bp - target breakpoint
     * @return platform breakpoint. null if not found. 
     */
    public IBreakpoint getPlatformBreakpoint(IBreakpointsTargetDMContext dmc, IBreakpointDMContext bp) {
    	assert getExecutor().isInExecutorThread();

    	for (IBreakpointsTargetDMContext bpContext : fPlatformBPs.keySet()) {
    		if (dmc != null && !dmc.equals(bpContext))
    			continue;
    		
	        Map<IBreakpoint, List<ITargetBreakpointInfo>> platformBPs = fPlatformBPs.get(bpContext);
	
	        if (platformBPs != null && platformBPs.size() > 0)
	        {
	            for(Map.Entry<IBreakpoint, List<ITargetBreakpointInfo>> e: platformBPs.entrySet())
	            {
	                // Stop at the first occurrence
	            	for (ITargetBreakpointInfo tbp : e.getValue())
	            		if(tbp.getTargetBreakpoint().equals(bp))
	            			return e.getKey();
	            }    
	        }
    	}

    	return null;
    }
    
    ///////////////////////////////////////////////////////////////////////////
    // Back-end interface functions
    ///////////////////////////////////////////////////////////////////////////

	/**
	 * Install a new platform breakpoint on the back-end. A platform breakpoint
	 * can resolve into multiple back-end breakpoints, e.g. when threads are taken
	 * into account.
	 *  
	 * @param dmc
	 * @param breakpoint
	 * @param attrsList - list of attribute map, each mapping to a potential target BP.
	 * @param rm
	 */
	private void installBreakpoint(IBreakpointsTargetDMContext dmc, final IBreakpoint breakpoint,
			final List<Map<String, Object>> attrsList, final DataRequestMonitor<List<TargetBP>> rm)
	{
    	// Retrieve the set of breakpoints for this context
        final Map<IBreakpoint, List<ITargetBreakpointInfo>> platformBPs = fPlatformBPs.get(dmc);
        assert platformBPs != null;

        // Ensure the breakpoint is not already installed
        assert !platformBPs.containsKey(breakpoint);

        final ArrayList<TargetBP> targetBPsAttempted = new ArrayList<TargetBP>(attrsList.size());
        for (int i = 0; i < attrsList.size(); i++) {
        	targetBPsAttempted.add(new TargetBP(attrsList.get(i)));
        }
        
        final ArrayList<ITargetBreakpointInfo> targetBPsInstalled = new ArrayList<ITargetBreakpointInfo>(attrsList.size());

        // Update the breakpoint status when all back-end breakpoints have been installed
    	final CountingRequestMonitor installRM = new CountingRequestMonitor(getExecutor(), rm) {
			@Override
			protected void handleCompleted() {
				// Store successful targetBPs with the platform breakpoint
				if (targetBPsInstalled.size() > 0)
					platformBPs.put(breakpoint, targetBPsInstalled);
				
				// Store all targetBPs, success or failure, in the rm.
				rm.setData(targetBPsAttempted);
		        rm.done();
			}
		};

        // A back-end breakpoint needs to be installed for each specified attributes map.
		installRM.setDoneCount(attrsList.size());

		// Install the back-end breakpoint(s)
		for (int _i = 0; _i < attrsList.size(); _i++) {
			final int i = _i;
            fBreakpointsService.insertBreakpoint(
                dmc, attrsList.get(i), 
				new DataRequestMonitor<IBreakpointDMContext>(getExecutor(), installRM) {
				@Override
                protected void handleCompleted() {
					TargetBP targetBP = targetBPsAttempted.get(i);
                    if (isSuccess()) {
						// Add the breakpoint back-end mapping
                    	targetBP.setTargetBreakpoint(getData());
                    	
                    	targetBPsInstalled.add(targetBP);
					} 
                    targetBP.setStatus(getStatus());
                    
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
	 *            the context for which to remove the breakpoint.
	 * @param breakpoint
	 * @param drm
	 *            contains list of Target breakpoints that are removed
	 *            regardless of success or failure in the removal.
	 */
    private void uninstallBreakpoint(final IBreakpointsTargetDMContext dmc, final IBreakpoint breakpoint, 
        final DataRequestMonitor<List<ITargetBreakpointInfo>> drm)
    {
		// Remove the back-end breakpoints
		final Map<IBreakpoint, List<ITargetBreakpointInfo>> platformBPs = fPlatformBPs.get(dmc);
        if (platformBPs == null) {
            drm.setStatus(new Status(IStatus.ERROR, getPluginID(), INVALID_HANDLE, "Invalid breakpoint", null)); //$NON-NLS-1$
            drm.done();
            return;
        }

        final List<ITargetBreakpointInfo> bpList = platformBPs.get(breakpoint);
        assert bpList != null;

        // Only try to remove those targetBPs that are successfully installed.
        
  		// Remove completion monitor
    	final CountingRequestMonitor countingRm = new CountingRequestMonitor(getExecutor(), drm) {
			@Override
			protected void handleCompleted() {
				platformBPs.remove(breakpoint);

		        // Complete the request monitor.
		        drm.setData(bpList);
		        drm.done();
			}
		};

        int count = 0;
        for (int i = 0; i < bpList.size(); i++) {
        	final ITargetBreakpointInfo bp = bpList.get(i);
        	if (bp.getTargetBreakpoint() != null) {
        		fBreakpointsService.removeBreakpoint(
        				bp.getTargetBreakpoint(), 
        				new RequestMonitor(getExecutor(), countingRm) {
        					@Override
        					protected void handleCompleted() {
        						// Remember result of the removal, success or failure.
        				        ((TargetBP)bp).setStatus(getStatus());
        				        if (isSuccess()) {
        				            ((TargetBP)bp).setTargetBreakpoint(null);
        				        } 
        				        countingRm.done();
        					}
        				});
        		count++;
        	} else {
        	    ((TargetBP)bp).setStatus(Status.OK_STATUS);
        	}
        }
        countingRm.setDoneCount(count);
    }
	
    ///////////////////////////////////////////////////////////////////////////
    // IBreakpointManagerListener implementation
    ///////////////////////////////////////////////////////////////////////////

    /**
     * @noreference This method is not intended to be referenced by clients.
     */
    @ThreadSafeAndProhibitedFromDsfExecutor("getExecutor()")
    @Override
	public void breakpointsAdded(final IBreakpoint[] bps) {
		doBreakpointsAdded(bps, null, null);
	}
	
	protected void doBreakpointsAdded(final IBreakpoint[] bps, final IBreakpointsTargetDMContext bpsTargetDmc, final RequestMonitor rm) {
		// Collect attributes (which will access system resource)
		// in non DSF dispatch thread.
		//
		final PlatformBreakpointInfo[] bpsInfo = collectBreakpointsInfo(bps);
		
		// Nothing to do
		if (bpsInfo.length == 0) {
			if (rm != null) {
				rm.done();
			}
			return;
		}

		try {
            getExecutor().execute(new DsfRunnable() {
                @Override
				public void run() {
					Collection<IBreakpointsTargetDMContext> dmcs = new ArrayList<IBreakpointsTargetDMContext>();
					if (bpsTargetDmc == null)
						dmcs.addAll(fPlatformBPs.keySet());
					else
						dmcs.add(bpsTargetDmc);

					doBreakpointsAddedInExecutor(bpsInfo, dmcs, rm);
				}
			});
		} catch (RejectedExecutionException e) {
			IStatus status = new Status(IStatus.ERROR, getPluginID(), IDsfStatusConstants.INTERNAL_ERROR, "Request for monitor: '" + toString() + "' resulted in a rejected execution exception.", e);//$NON-NLS-1$ //$NON-NLS-2$
			if (rm != null) {
				rm.setStatus(status);
				rm.done();
			} else {
				getPlugin().getLog().log(status); 
			}
		}
	}
	
	/**
	 * Collect breakpoint info. This method must not be called in DSF dispatch thread.
	 * @param bps
	 * @return
	 */
    private PlatformBreakpointInfo[] collectBreakpointsInfo(IBreakpoint[] bps) {
		List<PlatformBreakpointInfo> bpsInfo = new ArrayList<PlatformBreakpointInfo>(bps.length);
		
		for (IBreakpoint bp : bps) {
			if (bp.getMarker() == null)
				continue;
			
			if (fAttributeTranslator2.supportsBreakpoint(bp)) {
				try {
	        		Map<String, Object> attrs = fAttributeTranslator2.getAllBreakpointAttributes(bp, fBreakpointManager.isEnabled());
	        		boolean enabled = bp.isEnabled() && fBreakpointManager.isEnabled();
					bpsInfo.add(new PlatformBreakpointInfo(bp, enabled, attrs));
				} catch (CoreException e) {
					getPlugin().getLog().log(e.getStatus());
				}
			}
		}
		
		return bpsInfo.toArray(new PlatformBreakpointInfo[bpsInfo.size()]);
	}

	private void doBreakpointsAddedInExecutor(PlatformBreakpointInfo[] bpsInfo, Collection<IBreakpointsTargetDMContext> bpTargetDMCs, final RequestMonitor rm) {
		final Map<IBreakpoint, Map<IBreakpointsTargetDMContext, ITargetBreakpointInfo[]>> eventBPs =  
			new HashMap<IBreakpoint, Map<IBreakpointsTargetDMContext, ITargetBreakpointInfo[]>>(bpsInfo.length, 1);
		
        CountingRequestMonitor processPendingCountingRm = new CountingRequestMonitor(getExecutor(), rm) {
                @Override
                protected void handleCompleted() {
                	processPendingRequests();
                	for (Map.Entry<IBreakpoint, Map<IBreakpointsTargetDMContext, ITargetBreakpointInfo[]>> eventEntry : eventBPs.entrySet()) {
                	    for (Map.Entry<IBreakpointsTargetDMContext, ITargetBreakpointInfo[]> bpEntry : eventEntry.getValue().entrySet()) {
                	        fPlatformBPs.get(bpEntry.getKey()).put(eventEntry.getKey(), Arrays.asList( bpEntry.getValue() ));
                	    }
                	}
                	fireUpdateBreakpointsStatus(eventBPs, BreakpointEventType.ADDED);
                	if (rm != null)
                		// don't call this if "rm" is null as this will 
                		// log errors if any and pack Eclipse error 
                		// log view with errors meaningless to user. 
                		super.handleCompleted();
                }
            };	            	
        int processPendingCountingRmCount = 0;
    	
    	for (final PlatformBreakpointInfo bpinfo : bpsInfo) {
    		final Map<IBreakpointsTargetDMContext, ITargetBreakpointInfo[]> targetBPs = 
    			new HashMap<IBreakpointsTargetDMContext, ITargetBreakpointInfo[]>(fPlatformBPs.size(), 1);
    		eventBPs.put(bpinfo.breakpoint, targetBPs);	
    	
			// Remember the new attributes of the bp in our global buffer,
			// even if we cannot or fail to install the bp.
			fBreakpointAttributes.put(bpinfo.breakpoint, bpinfo.attributes);
    		
			if (fRunningEvents.contains(bpinfo.breakpoint)) {
				PendingEventInfo pendingEvent = new PendingEventInfo(BreakpointEventType.ADDED, bpinfo, bpTargetDMCs, processPendingCountingRm);
				processPendingCountingRmCount++;
				updatePendingRequest(bpinfo.breakpoint, pendingEvent);
				continue;
			}
			
			processPendingCountingRmCount++;

            // Mark the breakpoint as being updated and go
            fRunningEvents.add(bpinfo.breakpoint);

    		final CountingRequestMonitor bpTargetsCountingRm = new CountingRequestMonitor(getExecutor(), processPendingCountingRm) {
				@Override
				protected void handleCompleted() {
                	// Indicate that the running event has completed
                	fRunningEvents.remove(bpinfo.breakpoint);
                	super.handleCompleted();
				}
    		};

			int bpTargetsCountingRmCount = 0;

			// Install the breakpoint in all the execution contexts
			for (final IBreakpointsTargetDMContext dmc : bpTargetDMCs) {
				
                // Now ask lower level to set the bp.
				//
				// if the breakpoint is disabled, ask back-end if it can set (and manage)
				// disabled breakpoint. If not, just bail out.
				//
				if (! bpinfo.enabled) {
					Map<String, Object> attr = new HashMap<String, Object>(1);
					attr.put(IBreakpoint.ENABLED, Boolean.FALSE);
					Map<String, Object> targetEnablementAttr = fAttributeTranslator2.convertAttributes(attr);
					if (! fAttributeTranslator2.canUpdateAttributes(bpinfo.breakpoint, dmc, targetEnablementAttr)) {
						// bail out. Continue with the next dmc & breakpoint.
						continue;
					}
				}
            	
				// Now do the real work.
				//
				fAttributeTranslator2.resolveBreakpoint(dmc, bpinfo.breakpoint, bpinfo.attributes,
						new DataRequestMonitor<List<Map<String,Object>>>(getExecutor(), bpTargetsCountingRm){
							@Override
							protected void handleSuccess() {
								installBreakpoint(
							    		dmc, bpinfo.breakpoint, getData(), 
							    		new DataRequestMonitor<List<TargetBP>>(getExecutor(), bpTargetsCountingRm) {
							    			@Override
											protected void handleSuccess() {
							    				targetBPs.put(dmc, getData().toArray(new ITargetBreakpointInfo[getData().size()]));
							    				super.handleSuccess();
							    			};
							    		});
							}});
				
				bpTargetsCountingRmCount++;
			}
			
			bpTargetsCountingRm.setDoneCount(bpTargetsCountingRmCount);
    	}
    	
    	processPendingCountingRm.setDoneCount(processPendingCountingRmCount);	            	
	}

    /**
     * @noreference This method is not intended to be referenced by clients.
     */
	@ThreadSafeAndProhibitedFromDsfExecutor("getExecutor()")
    @Override
	public void breakpointsChanged(IBreakpoint[] bps, IMarkerDelta[] deltas) {
		if (fAttributeTranslator2 == null)
			return;
		
		final PlatformBreakpointInfo[] bpsInfo = collectBreakpointsInfo(bps);
		
		if (bpsInfo.length == 0) 
			return; // nothing to do
		
		try {
	        getExecutor().execute( new DsfRunnable() { 
	            @Override
	            public void run() {
	            	Map<String, Object> tmp = new HashMap<String, Object>(1);
					tmp.put(IBreakpoint.ENABLED, true);
					final String targetEnablementKey = fAttributeTranslator2.convertAttributes(tmp).keySet().iterator().next();

					for (PlatformBreakpointInfo bpinfo : bpsInfo) {
						/*
						 * We cannot depend on "deltas" for attribute change.
						 * For instance, delta can be null when extended
						 * attributes (e.g. breakpoint thread filter for GDB)
						 * are changed.
						 */
						Map<String, Object> newAttrs = bpinfo.attributes;
						Map<String, Object> oldAttrs = fBreakpointAttributes.get(bpinfo.breakpoint);
						
						// remember the new attributes.
						fBreakpointAttributes.put(bpinfo.breakpoint, newAttrs);
		
						if (oldAttrs == null)
							continue;
						
						final Map<String, Object> attrDelta = getAttributesDelta(oldAttrs, newAttrs);
						if (attrDelta.size() == 0) 
							continue;

						final List<IBreakpointsTargetDMContext> reinstallContexts = new ArrayList<IBreakpointsTargetDMContext>();
						
						List<IBreakpointsTargetDMContext> updateContexts = new ArrayList<IBreakpointsTargetDMContext>();
						
						// Now change the breakpoint for each known context.
						//
						for (final IBreakpointsTargetDMContext btContext : fPlatformBPs.keySet()) {
							
							if (! fAttributeTranslator2.canUpdateAttributes(bpinfo.breakpoint, btContext, attrDelta)) {
								// backend cannot handle at least one of the platform BP attribute change,
								// we'll re-install the bp.
								reinstallContexts.add(btContext);
							}
							else {
								// Backend claims it can handle the attributes change, let it do it.
								updateContexts.add(btContext);
							}
							
						}

						final PlatformBreakpointInfo[] oneBPInfo = new PlatformBreakpointInfo[] {bpinfo};
						IBreakpoint[] oneBP = new IBreakpoint[] {bpinfo.breakpoint};

						if (reinstallContexts.size() > 0) {
							// Check if it's only enablement change (user click enable/disable 
							// button or "Skip all breakpoints" button), which is common operation.
							//
							if (attrDelta.size() == 1 && attrDelta.containsKey(targetEnablementKey)) { // only enablement changed.	
								if (bpinfo.enabled)  {
									// change from disable to enable. Install the bp.
									doBreakpointsAddedInExecutor(oneBPInfo, reinstallContexts, null);
								}
								else {
									// change from enable to disable. Remove the bp.
									doBreakpointsRemovedInExecutor(oneBP,  reinstallContexts, null);
								}
							}
							else {
								doBreakpointsRemovedInExecutor(oneBP, reinstallContexts, new RequestMonitor(getExecutor(), null) {
									// What should we do if removal of some or all targetBP fails ? 
									// Go on with the installation of new targetBPs and let clients (i.e. AttributeTranslators) 
									// handle the errors.
									@Override
									protected void handleCompleted() {
										doBreakpointsAddedInExecutor(oneBPInfo, reinstallContexts, null);
									}});
							}
						}
						
						if (updateContexts.size() > 0)
							modifyTargetBreakpoints(bpinfo.breakpoint, updateContexts, attrDelta);
	            	}
	            }
	        });
	    } catch (RejectedExecutionException e) {
			getPlugin().getLog().log(new Status(IStatus.ERROR, getPluginID(), IDsfStatusConstants.INTERNAL_ERROR, "Request for monitor: '" + toString() + "' resulted in a rejected execution exception.", e)); //$NON-NLS-1$ //$NON-NLS-2$
	    }

	}

	/**
	 * For the given platform BP, ask the backend to modify all its target BPs
	 * with the given attribute change. <br>
	 * This must be called in DSF executor thread.
	 * 
	 * @param bp
	 * @param updateContexts 
	 * 			  target contexts in which to do the modification.
	 * @param targetAttrDelta
	 *            target-recognizable attribute(s) with new values.
	 */
	private void modifyTargetBreakpoints(final IBreakpoint bp, Collection<IBreakpointsTargetDMContext> updateContexts, Map<String, Object> targetAttrDelta) {
		// If the breakpoint is currently being updated, queue the request and exit
    	if (fRunningEvents.contains(bp)) {
    		PendingEventInfo pendingEvent = new PendingEventInfo(BreakpointEventType.MODIFIED, updateContexts, targetAttrDelta);
    		updatePendingRequest(bp, pendingEvent);
			return;
    	}
		
    	CountingRequestMonitor modifyTargetBPCRM = new CountingRequestMonitor(getExecutor(), null) {
			@Override
			protected void handleCompleted() {
				fRunningEvents.remove(bp);
			}};
			
    	int targetBPCount = 0;
    	
    	fRunningEvents.add(bp);
    	
		for (IBreakpointsTargetDMContext context : updateContexts) {
			List<ITargetBreakpointInfo> targetBPs = fPlatformBPs.get(context).get(bp);
			if (targetBPs != null) {
				for (ITargetBreakpointInfo tbp : targetBPs) {
					// this must be an installed breakpoint.
					assert (tbp.getTargetBreakpoint() != null);
					
					targetBPCount++;
					fBreakpointsService.updateBreakpoint(tbp.getTargetBreakpoint(), targetAttrDelta, modifyTargetBPCRM);
				}
			}
		}
		
		modifyTargetBPCRM.setDoneCount(targetBPCount);
	}

	/**
	 * @noreference This method is not intended to be referenced by clients.
	 */
    @ThreadSafeAndProhibitedFromDsfExecutor("getExecutor()")
    @Override
	public void breakpointsRemoved(final IBreakpoint[] bps, IMarkerDelta delta[]) {
		getExecutor().execute(new DsfRunnable() {
		    @Override
			public void run() {
				for (IBreakpoint bp : bps)
					fBreakpointAttributes.remove(bp);
			}
		});
		
		doBreakpointsRemoved(bps, null, null);
	}
	
	private void doBreakpointsRemoved(final IBreakpoint[] bps, final IBreakpointsTargetDMContext bpsTargetDmc, final RequestMonitor rm) {
	
		final List<IBreakpoint> bpCandidates = new ArrayList<IBreakpoint>();
		
		for (int i = 0; i < bps.length; i++) {
			IBreakpoint bp = bps[i];
			
			if (fAttributeTranslator2.supportsBreakpoint(bp)) {
				bpCandidates.add(bp);
			}
		}
		
		if (bpCandidates.isEmpty()) { // nothing to do
			if (rm != null)
				rm.done();
			return;
		}
		
		try {
	        getExecutor().execute(new DsfRunnable() {
	            @Override
	        	public void run() {
					Collection<IBreakpointsTargetDMContext> contexts = new ArrayList<IBreakpointsTargetDMContext>();
					if (bpsTargetDmc == null)
						contexts.addAll(fPlatformBPs.keySet());
					else
						contexts.add(bpsTargetDmc);

					doBreakpointsRemovedInExecutor(bpCandidates.toArray(new IBreakpoint[bpCandidates.size()]), contexts, rm);
	        	}
	        });
        } catch (RejectedExecutionException e) {
			IStatus status = new Status(IStatus.ERROR, getPluginID(), IDsfStatusConstants.INTERNAL_ERROR, 
					"Request for monitor: '" + toString() + "' resulted in a rejected execution exception.", e);//$NON-NLS-1$ //$NON-NLS-2$
			if (rm != null) {
				rm.setStatus(status);
				rm.done();
			} else {
				getPlugin().getLog().log(status); 
			}
        }
	}
	
	private void doBreakpointsRemovedInExecutor(IBreakpoint[] bpCandidates, 
			Collection<IBreakpointsTargetDMContext> targetContexts, final RequestMonitor rm) {
		
		final Map<IBreakpoint, Map<IBreakpointsTargetDMContext, ITargetBreakpointInfo[]>> eventBPs =  
			new HashMap<IBreakpoint, Map<IBreakpointsTargetDMContext, ITargetBreakpointInfo[]>>(bpCandidates.length, 1);

		CountingRequestMonitor processPendingCountingRm = new CountingRequestMonitor(getExecutor(), rm) {
			@Override
			protected void handleCompleted() {
				processPendingRequests();
            	fireUpdateBreakpointsStatus(eventBPs, BreakpointEventType.REMOVED);
                for (Map.Entry<IBreakpoint, Map<IBreakpointsTargetDMContext, ITargetBreakpointInfo[]>> eventEntry : eventBPs.entrySet()) {
                    for (IBreakpointsTargetDMContext bpTarget : eventEntry.getValue().keySet()) {
                        fPlatformBPs.get(bpTarget).remove(eventEntry.getKey());
                    }
                }
            	if (rm != null)
            		// don't call this if "rm" is null as this will 
            		// log errors if any and pack Eclipse error 
            		// log view with errors meaningless to user. 
            		super.handleCompleted();
			}
		};
		int processPendingCountingRmCount = 0;
		
		for (final IBreakpoint breakpoint : bpCandidates) {

			// If the breakpoint is currently being updated, queue the request and exit
        	if (fRunningEvents.contains(breakpoint)) {
        		PendingEventInfo pendingEvent = new PendingEventInfo(BreakpointEventType.REMOVED, null, targetContexts, processPendingCountingRm);
                processPendingCountingRmCount++;
        		updatePendingRequest(breakpoint, pendingEvent);
				continue;	// handle next breakpoint
        	}

            processPendingCountingRmCount++;

            final Map<IBreakpointsTargetDMContext, ITargetBreakpointInfo[]> targetBPs = 
    			new HashMap<IBreakpointsTargetDMContext, ITargetBreakpointInfo[]>(fPlatformBPs.size(), 1);
    		eventBPs.put(breakpoint, targetBPs);	
	
    		CountingRequestMonitor bpTargetsCountingRM = new CountingRequestMonitor(getExecutor(), processPendingCountingRm) {
				@Override
				protected void handleCompleted() {
					// Indicate that the running event has completed
                	fRunningEvents.remove(breakpoint);
                	super.handleCompleted();
				}
			};
            
			int bpTargetsCoutingRMCount = 0;

        	// Mark the breakpoint as being updated and go
            fRunningEvents.add(breakpoint);

    		// Remove the breakpoint in all the execution contexts
    		for (final IBreakpointsTargetDMContext dmc : targetContexts) {
    			
    			if (fPlatformBPs.get(dmc).containsKey(breakpoint)) {		// there are targetBPs installed 
    				// now do time-consuming part of the work.
    				
    				uninstallBreakpoint(
    						dmc, breakpoint,
    						new DataRequestMonitor<List<ITargetBreakpointInfo>>(getExecutor(), bpTargetsCountingRM) {
				    			@Override
								protected void handleSuccess() {
				    				targetBPs.put(dmc, getData().toArray(new ITargetBreakpointInfo[getData().size()]));
				    				super.handleSuccess();
				    			};
    						});
    				bpTargetsCoutingRMCount++;
    			} else {
    				// Breakpoint not installed for given context, do nothing.
    			}
    		}
    		
    		bpTargetsCountingRM.setDoneCount(bpTargetsCoutingRMCount);
		}
		
		processPendingCountingRm.setDoneCount(processPendingCountingRmCount);
	}

	private void updatePendingRequest(IBreakpoint breakpoint, PendingEventInfo pendingEvent) {
		LinkedList<PendingEventInfo> pendingEventsList = fPendingEvents.get(breakpoint);
		if (pendingEventsList == null) {
			pendingEventsList = new LinkedList<PendingEventInfo>();
			fPendingEvents.put(breakpoint, pendingEventsList);
		}
		if (pendingEventsList.size() > 0 &&
				pendingEventsList.getLast().fEventType == BreakpointEventType.MODIFIED) {
			pendingEventsList.removeLast();
		}
		pendingEventsList.add(pendingEvent);
	}
	
	private void processPendingRequests() {
		/*
		 * This will process only first pending request for each breakpoint,
		 * whose RequestMonitor (see "processPendingCountingRm" in such methods as 
		 * doBreakpointsRemovedInExecutor()) will invoke this method again.   
		 */
		if (fPendingEvents.isEmpty()) return;  // Nothing to do
		
		// Make a copy to avoid ConcurrentModificationException
		// as we are deleting element in the loop.
		Set<IBreakpoint> bpsInPendingEvents = new HashSet<IBreakpoint>(fPendingEvents.keySet()); 
		for (IBreakpoint bp : bpsInPendingEvents) {
	    	if (! fRunningEvents.contains(bp)) {
				LinkedList<PendingEventInfo> eventInfoList = fPendingEvents.get(bp);

		    	// Process the first pending request for this breakpoint
		   		PendingEventInfo eventInfo = eventInfoList.removeFirst();
	
				if (eventInfoList.isEmpty())
					fPendingEvents.remove(bp);
	
				switch (eventInfo.fEventType) {
				case ADDED:
					doBreakpointsAddedInExecutor(new PlatformBreakpointInfo[] {eventInfo.fBPInfo}, eventInfo.fBPTargetContexts, eventInfo.fRequestMonitor);
					break;
				case MODIFIED:
					modifyTargetBreakpoints(bp, eventInfo.fBPTargetContexts, eventInfo.fAttributeDelta);
					break;
				case REMOVED:
					doBreakpointsRemovedInExecutor(new IBreakpoint[]{bp}, eventInfo.fBPTargetContexts, eventInfo.fRequestMonitor);
					break;
				}
	    	}
		}
	}
	
	private void fireUpdateBreakpointsStatus(final Map<IBreakpoint, Map<IBreakpointsTargetDMContext, ITargetBreakpointInfo[]>> eventBPs, final BreakpointEventType eventType) {
        // Update breakpoint status
        new Job("Breakpoint status update") { //$NON-NLS-1$
            { setSystem(true); }
            @Override
            protected IStatus run(IProgressMonitor monitor) {
               	fAttributeTranslator2.updateBreakpointsStatus(eventBPs, eventType);
                return Status.OK_STATUS;
            };
        }.schedule();
		
	}

    /**
     * Determine the set of modified attributes.
     * 
     * @param oldAttributes old map of attributes.
     * @param newAttributes new map of attributes.
     * @return new and changed attribute in the new map. May be empty indicating the two maps are equal.
     */
    private Map<String, Object> getAttributesDelta(Map<String, Object> oldAttributes, Map<String, Object> newAttributes) {

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

        return delta;
    }
}
