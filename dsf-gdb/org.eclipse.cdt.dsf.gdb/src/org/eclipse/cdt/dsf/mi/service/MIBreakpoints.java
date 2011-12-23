/*******************************************************************************
 * Copyright (c) 2007, 2010 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ericsson - Initial API and implementation 
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.dsf.concurrent.CountingRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Immutable;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Sequence.Step;
import org.eclipse.cdt.dsf.datamodel.AbstractDMContext;
import org.eclipse.cdt.dsf.datamodel.AbstractDMEvent;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints;
import org.eclipse.cdt.dsf.debug.service.IBreakpointsExtension;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerResumedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerSuspendedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExitedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IResumedDMEvent;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControl;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlShutdownDMEvent;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.mi.service.command.CommandFactory;
import org.eclipse.cdt.dsf.mi.service.command.events.MIWatchpointScopeEvent;
import org.eclipse.cdt.dsf.mi.service.command.output.CLICatchInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIBreakInsertInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIBreakListInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIBreakpoint;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.service.AbstractDsfService;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleContext;

/**
 * Initial breakpoint service implementation.
 * Implements the IBreakpoints interface.
 */
public class MIBreakpoints extends AbstractDsfService implements IBreakpoints, IBreakpointsExtension
{
    /**
     * Breakpoint attributes markers used in the map parameters of insert/updateBreakpoint().
     * All are optional with the possible exception of TYPE. It is the responsibility of the
     * service to ensure that the set of attributes provided is sufficient to create/update
     * a valid breakpoint on the back-end.
     */
    public static final String PREFIX   = "org.eclipse.cdt.dsf.debug.breakpoint"; //$NON-NLS-1$
    
    // General markers
    public static final String BREAKPOINT_TYPE = PREFIX + ".type";      //$NON-NLS-1$
    public static final String BREAKPOINT      = "breakpoint";          //$NON-NLS-1$
    public static final String WATCHPOINT      = "watchpoint";          //$NON-NLS-1$
    public static final String CATCHPOINT      = "catchpoint";          //$NON-NLS-1$
    /** @since 3.0 */
    public static final String TRACEPOINT      = "tracepoint";          //$NON-NLS-1$

    // Basic set of breakpoint attribute markers
    public static final String FILE_NAME     = PREFIX + ".fileName";    //$NON-NLS-1$
    public static final String LINE_NUMBER   = PREFIX + ".lineNumber";  //$NON-NLS-1$
    public static final String FUNCTION      = PREFIX + ".function";    //$NON-NLS-1$
    public static final String ADDRESS       = PREFIX + ".address";     //$NON-NLS-1$
    public static final String CONDITION     = PREFIX + ".condition";   //$NON-NLS-1$
    public static final String IGNORE_COUNT  = PREFIX + ".ignoreCount"; //$NON-NLS-1$
    /** @since 3.0 */
    public static final String PASS_COUNT    = PREFIX + ".passCount"; //$NON-NLS-1$
    public static final String IS_ENABLED    = PREFIX + ".isEnabled";   //$NON-NLS-1$
    /** @since 3.0 */
    public static final String COMMANDS      = PREFIX + ".commands";     //$NON-NLS-1$

    // Basic set of watchpoint attribute markers
    public static final String EXPRESSION    = PREFIX + ".expression";  //$NON-NLS-1$
    public static final String READ          = PREFIX + ".read";        //$NON-NLS-1$
    public static final String WRITE         = PREFIX + ".write";       //$NON-NLS-1$
    
    // Catchpoint properties

	/**
	 * Property that indicates the kind of catchpoint (.e.g, fork call, C++
	 * exception throw). Value is the gdb keyword associated with that type, as
	 * listed in 'help catch'.
	 * 
	 * @since 3.0
	 */
    public static final String CATCHPOINT_TYPE = PREFIX + ".catchpoint_type";       //$NON-NLS-1$

	/**
	 * Property that holds arguments for the catchpoint. Value is an array of
	 * Strings. Never null, but may be empty collection, as most catchpoints are
	 * argument-less.
	 * 
	 * @since 3.0
	 */
    public static final String CATCHPOINT_ARGS = PREFIX + ".catchpoint_args";       //$NON-NLS-1$

	// Services
	private ICommandControl fConnection;
	private IMIRunControl fRunControl;
	private CommandFactory fCommandFactory;

	// Service breakpoints tracking
	// The breakpoints are stored per context and keyed on the back-end breakpoint reference
	private Map<IBreakpointsTargetDMContext, Map<Integer, MIBreakpointDMData>> fBreakpoints =
		new HashMap<IBreakpointsTargetDMContext, Map<Integer, MIBreakpointDMData>>();

	/**
	 * Map tracking which threads are currently suspended on a breakpoint.
	 * @since 3.0
	 */
	private Map<IExecutionDMContext, IBreakpointDMContext[]> fBreakpointHitMap = 
	    new HashMap<IExecutionDMContext, IBreakpointDMContext[]>();
	
	/**
	 * Returns a map of existing breakpoints for the specified context
	 * 
	 * @since 3.0
	 */
	protected Map<Integer, MIBreakpointDMData> getBreakpointMap(IBreakpointsTargetDMContext ctx) {
		return fBreakpoints.get(ctx);
	}
	
	/**
	 * Create an empty breakpoint map and store it with the specific context.
	 * 
	 * @return The newly created empty map.
	 * 
	 * @since 3.0
	 */
	protected Map<Integer, MIBreakpointDMData> createNewBreakpointMap(IBreakpointsTargetDMContext ctx) {
		Map<Integer, MIBreakpointDMData> map = new HashMap<Integer, MIBreakpointDMData>();
		fBreakpoints.put(ctx, map);
		return map;
	}
	
	// Error messages
	/** @since 3.0 */
	public final static String NULL_STRING = ""; //$NON-NLS-1$
	/** @since 3.0 */
	public final static String UNKNOWN_EXECUTION_CONTEXT    = "Unknown execution context";    //$NON-NLS-1$
	/** @since 3.0 */
	public final static String UNKNOWN_BREAKPOINT_CONTEXT   = "Unknown breakpoint context";   //$NON-NLS-1$
	/** @since 3.0 */
	public final static String UNKNOWN_BREAKPOINT_TYPE      = "Unknown breakpoint type";      //$NON-NLS-1$
	/** @since 3.0 */
	public final static String UNKNOWN_BREAKPOINT           = "Unknown breakpoint";           //$NON-NLS-1$
	/** @since 3.0 */
	public final static String BREAKPOINT_INSERTION_FAILURE = "Breakpoint insertion failure"; //$NON-NLS-1$
	/** @since 3.0 */
	public final static String WATCHPOINT_INSERTION_FAILURE = "Watchpoint insertion failure"; //$NON-NLS-1$
	/** @since 3.0 */
	public final static String INVALID_CONDITION            = "Invalid condition";            //$NON-NLS-1$
	/** @since 3.0 */
	public final static String TRACEPOINT_INSERTION_FAILURE = "Tracepoint insertion failure"; //$NON-NLS-1$
	/** @since 3.0 */
	public final static String INVALID_BREAKPOINT_TYPE      = "Invalid breakpoint type";      //$NON-NLS-1$
	/** @since 3.0 */
	public final static String CATCHPOINT_INSERTION_FAILURE = "Catchpoint insertion failure"; //$NON-NLS-1$

	
	///////////////////////////////////////////////////////////////////////////
	// Breakpoint Events
	///////////////////////////////////////////////////////////////////////////
	
    public class BreakpointsChangedEvent extends AbstractDMEvent<IBreakpointsTargetDMContext> implements IBreakpointsChangedEvent {
    	private IBreakpointDMContext[] fEventBreakpoints;
 
		public BreakpointsChangedEvent(IBreakpointDMContext bp) {
			super(DMContexts.getAncestorOfType(bp, IBreakpointsTargetDMContext.class));
			fEventBreakpoints = new IBreakpointDMContext[] { bp };
		}
		@Override
		public IBreakpointDMContext[] getBreakpoints() {
			return fEventBreakpoints;
		}
    }
    
    public class BreakpointAddedEvent extends BreakpointsChangedEvent implements IBreakpointsAddedEvent {
    	public BreakpointAddedEvent(IBreakpointDMContext context) {
            super(context);
		}
    }

    public class BreakpointUpdatedEvent extends BreakpointsChangedEvent implements IBreakpointsUpdatedEvent {
        public BreakpointUpdatedEvent(IBreakpointDMContext context) {
            super(context);
        }
    }

    public class BreakpointRemovedEvent extends BreakpointsChangedEvent implements IBreakpointsRemovedEvent {
        public BreakpointRemovedEvent(IBreakpointDMContext context) {
            super(context);
        }
    }

	///////////////////////////////////////////////////////////////////////////
	// IBreakpointDMContext
	// Used to hold the back-end breakpoint references. The reference can then
	// be used to get the actual DsfMIBreakpoint.
	///////////////////////////////////////////////////////////////////////////
	@Immutable
    public static final class MIBreakpointDMContext extends AbstractDMContext implements IBreakpointDMContext {

    	// The breakpoint reference
    	private final Integer fReference;

    	/**
    	 * @param service       the Breakpoint service
    	 * @param parents       the parent contexts
    	 * @param reference     the DsfMIBreakpoint reference
    	 */
    	public MIBreakpointDMContext(MIBreakpoints service, IDMContext[] parents, int reference) {
            this(service.getSession().getId(), parents, reference);
        }

        /**
         * @param sessionId       session ID
         * @param parents       the parent contexts
         * @param reference     the DsfMIBreakpoint reference
         * 
         * @since 3.0
         */
        public MIBreakpointDMContext(String sessionId, IDMContext[] parents, int reference) {
            super(sessionId, parents);
            fReference = reference;
        }

		/* (non-Javadoc)
    	 * @see org.eclipse.cdt.dsf.debug.service.IBreakpoints.IDsfBreakpointDMContext#getReference()
    	 */
    	public int getReference() {
    		return fReference;
    	}
 
        /* (non-Javadoc)
         * @see org.eclipse.cdt.dsf.datamodel.AbstractDMContext#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            return baseEquals(obj) && (fReference == ((MIBreakpointDMContext) obj).fReference);
        }
        
        /* (non-Javadoc)
         * @see org.eclipse.cdt.dsf.datamodel.AbstractDMContext#hashCode()
         */
        @Override
        public int hashCode() {
            return baseHashCode() + fReference.hashCode();
        }

        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return baseToString() + ".reference(" + fReference + ")";  //$NON-NLS-1$//$NON-NLS-2$*/
        }
    }

    ///////////////////////////////////////////////////////////////////////////
	// AbstractDsfService
	///////////////////////////////////////////////////////////////////////////
	
	/**
	 * The service constructor
	 * 
	 * @param session           The debugging session
	 */
	public MIBreakpoints(DsfSession session) {
		super(session);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.service.AbstractDsfService#initialize(org.eclipse.cdt.dsf.concurrent.RequestMonitor)
	 */
	@Override
	public void initialize(final RequestMonitor rm) {
		super.initialize(new ImmediateRequestMonitor(rm) {
			@Override
			protected void handleSuccess() {
				doInitialize(rm);
			}
		});
	}

	/*
	 * Asynchronous service initialization
	 */
	private void doInitialize(final RequestMonitor rm) {

    	// Get the services references
		fConnection = getServicesTracker().getService(ICommandControl.class);
		fRunControl = getServicesTracker().getService(IMIRunControl.class);

		fCommandFactory = getServicesTracker().getService(IMICommandControl.class).getCommandFactory();

        // Register for the useful events
		getSession().addServiceEventListener(this, null);

		// Register this service
		register(new String[] { IBreakpoints.class.getName(), IBreakpointsExtension.class.getName(), MIBreakpoints.class.getName() },
				new Hashtable<String, String>());

		rm.done();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.service.AbstractDsfService#shutdown(org.eclipse.cdt.dsf.concurrent.RequestMonitor)
	 */
	@Override
	public void shutdown(final RequestMonitor rm) {
		unregister();
		getSession().removeServiceEventListener(this);
		super.shutdown(rm);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.service.AbstractDsfService#getBundleContext()
	 */
	@Override
	protected BundleContext getBundleContext() {
		return GdbPlugin.getBundleContext();
	}

	///////////////////////////////////////////////////////////////////////////
    // IServiceEventListener
    ///////////////////////////////////////////////////////////////////////////

    /**
     * This method is left for API compatibility only.
     * @nooverride This method is not intended to be re-implemented or extended by clients.
     * @noreference This method is not intended to be referenced by clients.
     */
	@DsfServiceEventHandler
	public void eventDispatched(MIWatchpointScopeEvent e) {
	    // When a watchpoint goes out of scope, it is automatically removed from
	    // the back-end. To keep our internal state synchronized, we have to
	    // remove it from our breakpoints map.
	    IBreakpointsTargetDMContext bpContext = DMContexts.getAncestorOfType(e.getDMContext(), IBreakpointsTargetDMContext.class);
	    if (bpContext != null) {
	        Map<Integer, MIBreakpointDMData> contextBps = getBreakpointMap(bpContext);
	        if (contextBps != null) {
	            contextBps.remove(e.getNumber());
	        }
	    }
	}

    /**
     * @since 1.1
     * @nooverride This method is not intended to be re-implemented or extended by clients.
     * @noreference This method is not intended to be referenced by clients.
     */
    @DsfServiceEventHandler
    public void eventDispatched(ICommandControlShutdownDMEvent e) {
    }

	///////////////////////////////////////////////////////////////////////////
	// IBreakpoints interface
	///////////////////////////////////////////////////////////////////////////

	//-------------------------------------------------------------------------
	// getBreakpoints
	//-------------------------------------------------------------------------

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.debug.service.IBreakpoints#getBreakpoints(org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointsTargetDMContext, org.eclipse.cdt.dsf.concurrent.DataRequestMonitor)
	 */
	@Override
	public void getBreakpoints(final IBreakpointsTargetDMContext context, final DataRequestMonitor<IBreakpointDMContext[]> drm)
	{
		// Validate the context
		if (context == null) {
       		drm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, REQUEST_FAILED, UNKNOWN_EXECUTION_CONTEXT, null));
       		drm.done();
			return;
		}

		// Select the breakpoints context map
		// If it doesn't exist then no breakpoint was ever inserted for this breakpoint space.
		// In that case, return an empty list.
		final Map<Integer, MIBreakpointDMData> breakpointContext = getBreakpointMap(context);
		if (breakpointContext == null) {
       		drm.setData(new IBreakpointDMContext[0]);
       		drm.done();
			return;
		}

		// Execute the command
		fConnection.queueCommand(fCommandFactory.createMIBreakList(context),
			new DataRequestMonitor<MIBreakListInfo>(getExecutor(), drm) {
				@Override
				protected void handleSuccess() {
					// Refresh the breakpoints map and format the result
					breakpointContext.clear();
					MIBreakpoint[] breakpoints = getData().getMIBreakpoints();
					IBreakpointDMContext[] result = new IBreakpointDMContext[breakpoints.length];
					for (int i = 0; i < breakpoints.length; i++) {
						MIBreakpointDMData breakpoint = new MIBreakpointDMData(breakpoints[i]);
						int reference = breakpoint.getReference();
						result[i] = new MIBreakpointDMContext(MIBreakpoints.this, new IDMContext[] { context }, reference);
						breakpointContext.put(reference, breakpoint);
					}
					drm.setData(result);
					drm.done();
				}
			});
	}

	//-------------------------------------------------------------------------
	// getBreakpointDMData
	//-------------------------------------------------------------------------

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.debug.service.IBreakpoints#getBreakpointDMData(org.eclipse.cdt.dsf.debug.service.IBreakpoints.IDsfBreakpointDMContext, org.eclipse.cdt.dsf.concurrent.DataRequestMonitor)
	 */
	@Override
	public void getBreakpointDMData(IBreakpointDMContext dmc, DataRequestMonitor<IBreakpointDMData> drm)
	{
		// Validate the breakpoint context
		if (dmc == null) {
       		drm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, REQUEST_FAILED, UNKNOWN_BREAKPOINT_CONTEXT, null));
       		drm.done();
			return;
		}

		// Validate the breakpoint type
		MIBreakpointDMContext breakpoint;
		if (dmc instanceof MIBreakpointDMContext) {
			breakpoint = (MIBreakpointDMContext) dmc;
		}
		else {
       		drm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, REQUEST_FAILED, UNKNOWN_BREAKPOINT_TYPE, null));
       		drm.done();
			return;
		}

		// Validate the target context
		IBreakpointsTargetDMContext context = DMContexts.getAncestorOfType(breakpoint, IBreakpointsTargetDMContext.class);
		if (context == null) {
       		drm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, REQUEST_FAILED, UNKNOWN_EXECUTION_CONTEXT, null));
       		drm.done();
			return;
		}

		// Select the breakpoints context map
		Map<Integer, MIBreakpointDMData> contextBreakpoints = getBreakpointMap(context);
		if (contextBreakpoints == null) {
       		drm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, REQUEST_FAILED, UNKNOWN_BREAKPOINT, null));
       		drm.done();
			return;
		}

		// No need to go to the back-end for this one
		IBreakpointDMData breakpointCopy = new MIBreakpointDMData(contextBreakpoints.get(breakpoint.getReference()));
		drm.setData(breakpointCopy);
		drm.done();
	}

	//-------------------------------------------------------------------------
	// insertBreakpoint
	//-------------------------------------------------------------------------

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.debug.service.IBreakpoints#insertBreakpoint(org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointsTargetDMContext, java.util.Map, org.eclipse.cdt.dsf.concurrent.DataRequestMonitor)
	 */
	@Override
	public void insertBreakpoint(IBreakpointsTargetDMContext context, Map<String, Object> attributes, DataRequestMonitor<IBreakpointDMContext> drm) {
		
		// Validate the context
		if (context == null) {
       		drm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, REQUEST_FAILED, UNKNOWN_EXECUTION_CONTEXT, null));
       		drm.done();
			return;
		}

		// Select the breakpoints context map. If it doesn't exist, create it.
		Map<Integer, MIBreakpointDMData> breakpointContext = getBreakpointMap(context);
		if (breakpointContext == null) {
			breakpointContext = createNewBreakpointMap(context);
		}

		// Validate the breakpoint type
		String type = (String) attributes.get(BREAKPOINT_TYPE);
		if (type == null) {
       		drm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, REQUEST_FAILED, UNKNOWN_BREAKPOINT_TYPE, null));
   			drm.done();
   			return;
		}

		// And go...
		if (type.equals(BREAKPOINT)) {
			addBreakpoint(context, attributes, drm);
		}
		else if (type.equals(WATCHPOINT)) {
			addWatchpoint(context, attributes, drm);
		}
		else if (type.equals(MIBreakpoints.TRACEPOINT)) {
			addTracepoint(context, attributes, drm);
		}
		else if (type.equals(MIBreakpoints.CATCHPOINT)) {
			addCatchpoint(context, attributes, drm);
		}
		else {
       		drm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, REQUEST_FAILED, UNKNOWN_BREAKPOINT_TYPE, null));
   			drm.done();
		}
	}

	/**
     * @since 3.0
     */
	@Override
	public void getExecutionContextBreakpoints(IExecutionDMContext ctx, DataRequestMonitor<IBreakpointDMContext[]> rm) {
	    IBreakpointDMContext[] bps = fBreakpointHitMap.get(ctx);
	    if (bps == null && ctx instanceof IContainerDMContext) {
	        List<IBreakpointDMContext> bpsList = new ArrayList<IBreakpointDMContext>(1); 
	        for (Map.Entry<IExecutionDMContext, IBreakpointDMContext[]> entry : fBreakpointHitMap.entrySet()) {
	            
	            if (DMContexts.isAncestorOf(entry.getKey(), ctx)) {
	                for (IBreakpointDMContext bp : entry.getValue()) {
	                    bpsList.add(bp);
	                }
	            }
	        }
	        bps = bpsList.toArray(new IBreakpointDMContext[bpsList.size()]);
	    } 
	    rm.setData(bps != null ? bps : new IBreakpointDMContext[0]);
	    rm.done();
	}
	
	/**
	 * @param map
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public Object getProperty(Map<String, Object> map, String key, Object defaultValue) {
		return (map.containsKey(key) && (map.get(key) != null)) ? map.get(key) : defaultValue;
	}

	/**
	 * Creates a gdb location string for a breakpoint/watchpoint/tracepoint
	 * given its set of properties.
	 * 
	 * @param attributes
	 * @return
	 * @since 3.0
	 */
	protected String formatLocation(Map<String, Object> attributes) {

		// Unlikely default location
		String location = (String)  getProperty(attributes, ADDRESS, NULL_STRING);

		// Get the relevant parameters
		String  fileName   = (String)  getProperty(attributes, FILE_NAME, NULL_STRING);
		Integer lineNumber = (Integer) getProperty(attributes, LINE_NUMBER, -1);
		String  function   = (String)  getProperty(attributes, FUNCTION,  NULL_STRING);

		// Fix for Bug264721
		if (fileName.contains(" ")) { //$NON-NLS-1$
			fileName = "\"" + fileName + "\"";  //$NON-NLS-1$//$NON-NLS-2$
		}

		// GDB seems inconsistent about allowing parentheses so we must remove them.
		// Bug 336888
		int paren = function.indexOf('(');
		if (paren != -1) {
			function = function.substring(0, paren);
		}

		if (!fileName.equals(NULL_STRING)) {
			// If the function is set it means we want a function breakpoint
			// We must check it first because the line number is still set in this case.
			if (!function.equals(NULL_STRING)) {
				location = fileName + ":" + function; //$NON-NLS-1$
			} else {
				location = fileName + ":" + lineNumber;   //$NON-NLS-1$
			}
		} else if (!function.equals(NULL_STRING)) {
			// function location without source
			location = function;
		} else if (location.length() > 0) {
			// address location
			if (Character.isDigit(location.charAt(0))) {
				// numeric address needs '*' prefix
				location = '*' + location;
			}
		}

		return location;
	}

	/**
	 * Add a breakpoint of type BREAKPOINT
	 * 
	 * @param context
	 * @param breakpoint
	 * @param finalRm
	 * 
 	 * @since 3.0
	 */
	protected void addBreakpoint(final IBreakpointsTargetDMContext context, final Map<String, Object> attributes, final DataRequestMonitor<IBreakpointDMContext> finalRm)
	{
		// Select the context breakpoints map
		final Map<Integer, MIBreakpointDMData> contextBreakpoints = getBreakpointMap(context);
		if (contextBreakpoints == null) {
       		finalRm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, REQUEST_FAILED, UNKNOWN_BREAKPOINT_CONTEXT, null));
       		finalRm.done();
			return;
		}

		// Extract the relevant parameters (providing default values to avoid potential NPEs)
		final String location = formatLocation(attributes);
		if (location.equals(NULL_STRING)) {
       		finalRm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, REQUEST_FAILED, UNKNOWN_BREAKPOINT_CONTEXT, null));
       		finalRm.done();
			return;
		}

		final Boolean isTemporary    = (Boolean) getProperty(attributes, MIBreakpointDMData.IS_TEMPORARY, false);
		final Boolean isHardware     = (Boolean) getProperty(attributes, MIBreakpointDMData.IS_HARDWARE,  false);
		final String  condition      = (String)  getProperty(attributes, CONDITION,    NULL_STRING);
		final Integer ignoreCount    = (Integer) getProperty(attributes, IGNORE_COUNT,          0 );
		final String  threadId       = (String)  getProperty(attributes, MIBreakpointDMData.THREAD_ID,      "0"); //$NON-NLS-1$
		final int     tid            = Integer.parseInt(threadId);

	    final Step insertBreakpointStep = new Step() {
    		@Override
    		public void execute(final RequestMonitor rm) {
    				fConnection.queueCommand(
    					fCommandFactory.createMIBreakInsert(context, isTemporary, isHardware, condition, ignoreCount, location, tid), 
						new DataRequestMonitor<MIBreakInsertInfo>(getExecutor(), rm) {
							@Override
							protected void handleSuccess() {

								// With MI, an invalid location won't generate an error
								if (getData().getMIBreakpoints().length == 0) {
									rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, REQUEST_FAILED, BREAKPOINT_INSERTION_FAILURE, null));
									rm.done();
									return;
								}

								// Create a breakpoint object and store it in the map
								final MIBreakpointDMData newBreakpoint = new MIBreakpointDMData(getData().getMIBreakpoints()[0]);
								int reference = newBreakpoint.getNumber();
								if (reference == -1) {
									rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, REQUEST_FAILED, BREAKPOINT_INSERTION_FAILURE, null));
									rm.done();
									return;
								}
								contextBreakpoints.put(reference, newBreakpoint);

								// Format the return value
								MIBreakpointDMContext dmc = new MIBreakpointDMContext(MIBreakpoints.this, new IDMContext[] { context }, reference);
								finalRm.setData(dmc);

								// Flag the event
								getSession().dispatchEvent(new BreakpointAddedEvent(dmc), getProperties());

								// By default the breakpoint is enabled at creation
								// If it wasn't supposed to be, then disable it right away
								Map<String,Object> delta = new HashMap<String,Object>();
								delta.put(IS_ENABLED, getProperty(attributes, IS_ENABLED, true));
								modifyBreakpoint(dmc, delta, rm, false);
							}

							@Override
							protected void handleError() {
								rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, REQUEST_FAILED, BREAKPOINT_INSERTION_FAILURE, null));
								rm.done();
							}
						});
    		}
		};

		fRunControl.executeWithTargetAvailable(context, new Step[] { insertBreakpointStep }, finalRm);
	}

	/**
	 * Add a tracepoint.  Currently not supported in this version, but only in our GDB 7.0 version.
	 * 
	 * @param context
	 * @param attributes
	 * @param drm
	 * 
	 * @since 3.0
	 */
	protected void addTracepoint(final IBreakpointsTargetDMContext context, final Map<String, Object> attributes, final DataRequestMonitor<IBreakpointDMContext> drm) {
		// Not supported 
   		drm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, REQUEST_FAILED, UNKNOWN_BREAKPOINT_TYPE, null));
		drm.done();

	}

	/**
	 * Add a breakpoint of type WATCHPOINT
	 * 
	 * @param context
	 * @param watchpoint
	 * @param drm
	 * @since 3.0
	 */
	protected void addWatchpoint(final IBreakpointsTargetDMContext context, final Map<String, Object> attributes, final DataRequestMonitor<IBreakpointDMContext> drm)
	{
		// Pick the context breakpoints map
		final Map<Integer, MIBreakpointDMData> contextBreakpoints = getBreakpointMap(context);
		if (contextBreakpoints == null) {
       		drm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, REQUEST_FAILED, UNKNOWN_BREAKPOINT_CONTEXT, null));
       		drm.done();
			return;
		}

		// Extract the relevant parameters (providing default values to avoid potential NPEs)
		String expression = (String)  getProperty(attributes, EXPRESSION, NULL_STRING);
		boolean isRead    = (Boolean) getProperty(attributes, READ,    false);
		boolean isWrite   = (Boolean) getProperty(attributes, WRITE,   false);

		// The DataRequestMonitor for the add request
		DataRequestMonitor<MIBreakInsertInfo> addWatchpointDRM =
			new DataRequestMonitor<MIBreakInsertInfo>(getExecutor(), drm) {
				@Override
	            protected void handleSuccess() {

	            	// With MI, an invalid location won't generate an error
                	if (getData().getMIBreakpoints().length == 0) {
                   		drm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, REQUEST_FAILED, WATCHPOINT_INSERTION_FAILURE, null));
                   		drm.done();
                   		return;
                    }

                	// Create a breakpoint object and store it in the map
                	final MIBreakpointDMData newBreakpoint = new MIBreakpointDMData(getData().getMIBreakpoints()[0]);
                	int reference = newBreakpoint.getNumber();
                	if (reference == -1) {
                   		drm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, REQUEST_FAILED, WATCHPOINT_INSERTION_FAILURE, null));
                   		drm.done();
                   		return;
                	}
                	contextBreakpoints.put(reference, newBreakpoint);

               		// Format the return value
               		IBreakpointDMContext dmc = new MIBreakpointDMContext(MIBreakpoints.this, new IDMContext[] { context }, reference);
               		drm.setData(dmc);

               		// Flag the event
					getSession().dispatchEvent(new BreakpointAddedEvent(dmc), getProperties());

               		// Condition, ignore count and state can not be specified at watchpoint creation time.
               		// Therefore, we have to update the watchpoint if any of these is present
               		Map<String,Object> delta = new HashMap<String,Object>();
               		delta.put(CONDITION,    getProperty(attributes, CONDITION, NULL_STRING));
               		delta.put(IGNORE_COUNT, getProperty(attributes, IGNORE_COUNT, 0 ));
               		delta.put(IS_ENABLED,   getProperty(attributes, IS_ENABLED, true));
               		modifyBreakpoint(dmc, delta, drm, false);
				}

				@Override
	            protected void handleError() {
               		drm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, REQUEST_FAILED, WATCHPOINT_INSERTION_FAILURE, null));
               		drm.done();
				}
			};

			// Execute the command
	        fConnection.queueCommand(fCommandFactory.createMIBreakWatch(context, isRead, isWrite, expression), addWatchpointDRM);
	}

	/**
	 * @since 3.0
	 */
	protected void addCatchpoint(final IBreakpointsTargetDMContext context, final Map<String, Object> attributes, final DataRequestMonitor<IBreakpointDMContext> finalRm) {
		// Select the context breakpoints map
		final Map<Integer, MIBreakpointDMData> contextBreakpoints = getBreakpointMap(context);
		if (contextBreakpoints == null) {
       		finalRm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, REQUEST_FAILED, UNKNOWN_BREAKPOINT_CONTEXT, null));
       		finalRm.done();
			return;
		}

		// Though CDT allows setting a temporary catchpoint, CDT never makes use of it
		assert (Boolean) getProperty(attributes, MIBreakpointDMData.IS_TEMPORARY, false) == false;

		// GDB has no support for hardware catchpoints
		assert (Boolean) getProperty(attributes, MIBreakpointDMData.IS_HARDWARE,  false) == false;

		final String event = (String) getProperty(attributes, CATCHPOINT_TYPE, NULL_STRING);
		final String[] args = (String[]) getProperty(attributes, CATCHPOINT_ARGS, null);

	    final Step insertBreakpointStep = new Step() {
    		@Override
    		public void execute(final RequestMonitor rm) {
    				fConnection.queueCommand(
    					fCommandFactory.createCLICatch(context, event, args == null ? new String[0] : args), 
						new DataRequestMonitor<CLICatchInfo>(getExecutor(), rm) {
							@Override
							protected void handleSuccess() {

								// Sanity check
								MIBreakpoint miBkpt = getData().getMIBreakpoint();
								if (miBkpt == null) {
									rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, REQUEST_FAILED, CATCHPOINT_INSERTION_FAILURE, null));
									rm.done();
									return;
								}

								// Create a breakpoint object and store it in the map
								final MIBreakpointDMData newBreakpoint = new MIBreakpointDMData(miBkpt);
								int reference = newBreakpoint.getNumber();
								if (reference == -1) {
									rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, REQUEST_FAILED, CATCHPOINT_INSERTION_FAILURE, null));
									rm.done();
									return;
								}
								contextBreakpoints.put(reference, newBreakpoint);

								// Format the return value
								MIBreakpointDMContext dmc = new MIBreakpointDMContext(MIBreakpoints.this, new IDMContext[] { context }, reference);
								finalRm.setData(dmc);

								// Flag the event
								getSession().dispatchEvent(new BreakpointAddedEvent(dmc), getProperties());

								// Break/Watch/Catchpoints that are disabled when set are delayed (we
								// don't tell gdb about them until the user enables them). So, we shouldn't 
								// be here if this is a disabled breakpoint
								assert ((Boolean)getProperty(attributes, IS_ENABLED, true)) == true;
								
			               		// Condition, ignore count and cannot be specified at creation time.
			               		// Therefore, we have to update the catchpoint if any of these is present
			               		Map<String,Object> delta = new HashMap<String,Object>();
			               		delta.put(CONDITION,    getProperty(attributes, CONDITION, NULL_STRING));
			               		delta.put(IGNORE_COUNT, getProperty(attributes, IGNORE_COUNT, 0 ));
			               		modifyBreakpoint(dmc, delta, rm, false);
							}

							@Override
							protected void handleError() {
								rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, REQUEST_FAILED, CATCHPOINT_INSERTION_FAILURE, null));
								rm.done();
							}
						});
    		}
		};

		fRunControl.executeWithTargetAvailable(context, new Step[] { insertBreakpointStep }, finalRm);
	}

	//-------------------------------------------------------------------------
	// removeBreakpoint
	//-------------------------------------------------------------------------

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.debug.service.IBreakpoints#removeBreakpoint(org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointDMContext, org.eclipse.cdt.dsf.concurrent.RequestMonitor)
	 */
	@Override
	public void removeBreakpoint(final IBreakpointDMContext dmc, final RequestMonitor finalRm) {

		// Validate the breakpoint context
		if (dmc == null) {
			finalRm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, REQUEST_FAILED, UNKNOWN_BREAKPOINT_CONTEXT, null));
			finalRm.done();
			return;
		}

		// Validate the breakpoint type
		MIBreakpointDMContext breakpointCtx;
		if (dmc instanceof MIBreakpointDMContext) {
			breakpointCtx = (MIBreakpointDMContext) dmc;
		}
		else {
       		finalRm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, REQUEST_FAILED, UNKNOWN_BREAKPOINT_TYPE, null));
       		finalRm.done();
			return;
		}
		
		// Validate the target context
        final IBreakpointsTargetDMContext context = DMContexts.getAncestorOfType(dmc, IBreakpointsTargetDMContext.class);
		if (context == null) {
			finalRm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, REQUEST_FAILED, UNKNOWN_EXECUTION_CONTEXT, null));
			finalRm.done();
			return;
		}

		// Pick the context breakpoints map
		final Map<Integer,MIBreakpointDMData> contextBreakpoints = getBreakpointMap(context);
		if (contextBreakpoints == null) {
			finalRm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, REQUEST_FAILED, UNKNOWN_BREAKPOINT, null));
			finalRm.done();
			return;
		}

		// Validate the breakpoint
		final int reference = breakpointCtx.getReference();
		MIBreakpointDMData breakpoint = contextBreakpoints.get(reference);
		if (breakpoint == null) {
			finalRm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, REQUEST_FAILED, UNKNOWN_BREAKPOINT, null));
			finalRm.done();
			return;
		}

	    final Step deleteBreakpointStep = new Step() {
    		@Override
    		public void execute(final RequestMonitor rm) {
    			// Queue the command
    			fConnection.queueCommand(
    					fCommandFactory.createMIBreakDelete(context, new int[] { reference }),
    					new DataRequestMonitor<MIInfo>(getExecutor(), rm) {
    						@Override
    						protected void handleCompleted() {
    							if (isSuccess()) {
    								getSession().dispatchEvent(new BreakpointRemovedEvent(dmc), getProperties());
    								contextBreakpoints.remove(reference);
    							}
    							rm.done();
    						}
    					});
    		}
	    };
		
		fRunControl.executeWithTargetAvailable(context, new Step[] { deleteBreakpointStep }, finalRm);
	}

	// -------------------------------------------------------------------------
	// updateBreakpoint
	//-------------------------------------------------------------------------

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.debug.service.IBreakpoints#updateBreakpoint(org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointDMContext, java.util.Map, org.eclipse.cdt.dsf.concurrent.RequestMonitor)
	 */
	@Override
	public void updateBreakpoint(IBreakpointDMContext dmc, Map<String, Object> properties, RequestMonitor rm)
	{
		// Validate the breakpoint context
		if (dmc == null) {
       		rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, REQUEST_FAILED, UNKNOWN_BREAKPOINT_CONTEXT, null));
       		rm.done();
			return;
		}

		// Validate the breakpoint type
		MIBreakpointDMContext breakpointCtx;
		if (dmc instanceof MIBreakpointDMContext) {
			breakpointCtx = (MIBreakpointDMContext) dmc;
		}
		else {
			rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, REQUEST_FAILED, UNKNOWN_BREAKPOINT_TYPE, null));
			rm.done();
			return;
		}

		// Validate the context
        IBreakpointsTargetDMContext context = DMContexts.getAncestorOfType(dmc, IBreakpointsTargetDMContext.class);
		if (context == null) {
       		rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, REQUEST_FAILED, UNKNOWN_EXECUTION_CONTEXT, null));
       		rm.done();
			return;
		}

		// Pick the context breakpoints map
		final Map<Integer, MIBreakpointDMData> contextBreakpoints = getBreakpointMap(context);
		if (contextBreakpoints == null) {
       		rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, REQUEST_FAILED, UNKNOWN_BREAKPOINT, null));
       		rm.done();
			return;
		}

		// Validate the breakpoint
		final int reference = breakpointCtx.getReference();
		MIBreakpointDMData breakpoint = contextBreakpoints.get(reference);
		if (breakpoint == null) {
       		rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, REQUEST_FAILED, UNKNOWN_BREAKPOINT, null));
       		rm.done();
			return;
		}

        modifyBreakpoint(dmc, properties, rm, true);
	}

	/**
	 * @param dmc
	 * @param properties
	 * @param rm
	 * @param generateUpdateEvent
     *
	 * @since 3.0
	 */
	protected void modifyBreakpoint(final IBreakpointDMContext dmc, Map<String, Object> attributes, final RequestMonitor rm, final boolean generateUpdateEvent)
	{
		// Use a working copy of the attributes since we are going to tamper happily with them
		Map<String, Object> properties = new HashMap<String, Object>(attributes);
		
		// Retrieve the breakpoint parameters
		// At this point, we know their are OK so there is no need to re-validate
		MIBreakpointDMContext breakpointCtx = (MIBreakpointDMContext) dmc;
        IBreakpointsTargetDMContext context = DMContexts.getAncestorOfType(dmc, IBreakpointsTargetDMContext.class);
		final Map<Integer, MIBreakpointDMData> contextBreakpoints = getBreakpointMap(context);
		final int reference = breakpointCtx.getReference();
		MIBreakpointDMData breakpoint = contextBreakpoints.get(reference);

		// Track the number of change requests
		int numberOfChanges = 0;
        final CountingRequestMonitor countingRm = new CountingRequestMonitor(getExecutor(), rm) {
            @Override
            protected void handleSuccess() {
           		if (generateUpdateEvent)
           			getSession().dispatchEvent(new BreakpointUpdatedEvent(dmc), getProperties());
        		rm.done();
            }
        };

        // Determine if the breakpoint condition changed
		String conditionAttribute = CONDITION;
		if (properties.containsKey(conditionAttribute)) {
			String oldValue = breakpoint.getCondition();
			String newValue = (String) properties.get(conditionAttribute);
			if (newValue == null) newValue = NULL_STRING;
	        if (!oldValue.equals(newValue)) {
	        	changeCondition(context, reference, newValue, countingRm);
	        	numberOfChanges++;
	        }
			properties.remove(conditionAttribute);
		}

        // Determine if the breakpoint ignore count changed
		String ignoreCountAttribute = IGNORE_COUNT;
		if (properties.containsKey(ignoreCountAttribute)) {
			Integer oldValue = breakpoint.getIgnoreCount();
			Integer newValue = (Integer) properties.get(ignoreCountAttribute);
			if (newValue == null) newValue = 0;
	        if (!oldValue.equals(newValue)) {
	        	changeIgnoreCount(context, reference, newValue, countingRm);
	        	numberOfChanges++;
	        }
			properties.remove(ignoreCountAttribute);
		}

        // Determine if the breakpoint state changed
		String enableAttribute = IS_ENABLED;
		if (properties.containsKey(enableAttribute)) {
			Boolean oldValue = breakpoint.isEnabled();
			Boolean newValue = (Boolean) properties.get(enableAttribute);
			if (newValue == null) newValue = false;
	        if (!oldValue.equals(newValue)) {
	        	numberOfChanges++;
				if (newValue)
					enableBreakpoint(context, reference, countingRm);
				else
					disableBreakpoint(context, reference, countingRm);
	        }
			properties.remove(enableAttribute);
		}

		// Set the number of completions required
        countingRm.setDoneCount(numberOfChanges);
	}

	/**
	 * Update the breakpoint condition
	 * 
	 * @param context
	 * @param dmc
	 * @param condition
	 * @param finalRm
	 * 
	 * @since 3.0
	 */
	protected void changeCondition(final IBreakpointsTargetDMContext context,
			final int reference, final String condition, final RequestMonitor finalRm)
	{
		// Pick the context breakpoints map
		final Map<Integer, MIBreakpointDMData> contextBreakpoints = getBreakpointMap(context);
		if (contextBreakpoints == null) {
       		finalRm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, REQUEST_FAILED, UNKNOWN_BREAKPOINT_CONTEXT, null));
       		finalRm.done();
			return;
		}

	    final Step changeConditionStep = new Step() {
    		@Override
    		public void execute(final RequestMonitor rm) {
    			// Queue the command
    			fConnection.queueCommand(
    					fCommandFactory.createMIBreakCondition(context, reference, condition),
    					new DataRequestMonitor<MIInfo>(getExecutor(), rm) {
    						@Override
    						protected void handleSuccess() {
    							MIBreakpointDMData breakpoint = contextBreakpoints.get(reference);
    							if (breakpoint == null) {
    								rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, REQUEST_FAILED, UNKNOWN_BREAKPOINT, null));
    								rm.done();
    								return;
    							}
    							breakpoint.setCondition(condition);
    							rm.done();
    						}

    						// In case of error (new condition could not be installed for whatever reason),
    						// GDB "offers" different behaviours depending on its version: it can either keep
    						// the original condition (the right thing to do) or keep the invalid condition.
    						// Our sole option is to remove the condition in case of error and rely on the
    						// upper layer to re-install the right condition.
    						@Override
    						protected void handleError() {
    							MIBreakpointDMData breakpoint = contextBreakpoints.get(reference);
    							if (breakpoint == null) {
    								rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, REQUEST_FAILED, UNKNOWN_BREAKPOINT, null));
    								rm.done();
    								return;
    							}
    							// Remove invalid condition from the back-end breakpoint
    							breakpoint.setCondition(NULL_STRING);
    							fConnection.queueCommand(
    									fCommandFactory.createMIBreakCondition(context, reference, NULL_STRING),
    									new DataRequestMonitor<MIInfo>(getExecutor(), rm) {
    										@Override
    										// The report the initial problem
    										protected void handleCompleted() {
    											rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, REQUEST_FAILED, INVALID_CONDITION, null));
    											rm.done();
    										}
    									});
    						}
    					});
    		}
	    };
	    
	    fRunControl.executeWithTargetAvailable(context, new Step[] { changeConditionStep }, finalRm);
	}


	/**
	 * Update the breakpoint ignoreCount
	 * 
	 * @param context
	 * @param reference
	 * @param ignoreCount
	 * @param finalRm
	 * 
	 * @since 3.0
	 */
	protected void changeIgnoreCount(final IBreakpointsTargetDMContext context,
			final int reference, final int ignoreCount, final RequestMonitor finalRm)
	{
		// Pick the context breakpoints map
		final Map<Integer, MIBreakpointDMData> contextBreakpoints = getBreakpointMap(context);
		if (contextBreakpoints == null) {
       		finalRm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, REQUEST_FAILED, UNKNOWN_BREAKPOINT_CONTEXT, null));
       		finalRm.done();
			return;
		}

		final Step changeIgnoreCountStep = new Step() {
			@Override
			public void execute(final RequestMonitor rm) {
				// Queue the command
				fConnection.queueCommand(
						fCommandFactory.createMIBreakAfter(context, reference, ignoreCount),
						new DataRequestMonitor<MIInfo>(getExecutor(), rm) {
							@Override
							protected void handleSuccess() {
								MIBreakpointDMData breakpoint = contextBreakpoints.get(reference);
								if (breakpoint == null) {
									rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, REQUEST_FAILED, UNKNOWN_BREAKPOINT, null));
									rm.done();
									return;
								}
								breakpoint.setIgnoreCount(ignoreCount);
								rm.done();
							}
						});
			}
		};

		fRunControl.executeWithTargetAvailable(context, new Step[] { changeIgnoreCountStep }, finalRm);
	}

	/**
	 * Enable the breakpoint
	 * 
	 * @param context
	 * @param reference
	 * @param finalRm
	 * 
	 * @since 3.0
	 */
	protected void enableBreakpoint(final IBreakpointsTargetDMContext context,
			final int reference, final RequestMonitor finalRm)
	{
		// Pick the context breakpoints map
		final Map<Integer, MIBreakpointDMData> contextBreakpoints = getBreakpointMap(context);
		if (contextBreakpoints == null) {
       		finalRm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, REQUEST_FAILED, UNKNOWN_BREAKPOINT_CONTEXT, null));
       		finalRm.done();
			return;
		}

	    final Step enableBreakpointStep = new Step() {
    		@Override
    		public void execute(final RequestMonitor rm) {
    			// Queue the command
    			fConnection.queueCommand(
    					fCommandFactory.createMIBreakEnable(context, new int[] { reference }),
    					new DataRequestMonitor<MIInfo>(getExecutor(), rm) {
    						@Override
    						protected void handleSuccess() {
    							MIBreakpointDMData breakpoint = contextBreakpoints.get(reference);
    							if (breakpoint == null) {
    								rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, REQUEST_FAILED, UNKNOWN_BREAKPOINT, null));
    								rm.done();
    								return;
    							}
    							breakpoint.setEnabled(true);
    							rm.done();
    						}
    					});
    		}
	    };
	    
		fRunControl.executeWithTargetAvailable(context, new Step[] { enableBreakpointStep }, finalRm);
	}

	/**
	 * Disable the breakpoint
	 * 
	 * @param context
	 * @param dmc
	 * @param finalRm
	 * 
	 * @since 3.0
	 */
	protected void disableBreakpoint(final IBreakpointsTargetDMContext context,
			final int reference, final RequestMonitor finalRm)
	{
		// Pick the context breakpoints map
		final Map<Integer, MIBreakpointDMData> contextBreakpoints = getBreakpointMap(context);
		if (contextBreakpoints == null) {
       		finalRm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, REQUEST_FAILED, UNKNOWN_BREAKPOINT_CONTEXT, null));
       		finalRm.done();
			return;
		}

	    final Step disableBreakpointStep = new Step() {
    		@Override
    		public void execute(final RequestMonitor rm) {
    			// Queue the command
    			fConnection.queueCommand(
    					fCommandFactory.createMIBreakDisable(context, new int[] { reference }),
    					new DataRequestMonitor<MIInfo>(getExecutor(), rm) {
    						@Override
    						protected void handleSuccess() {
    							MIBreakpointDMData breakpoint = contextBreakpoints.get(reference);
    							if (breakpoint == null) {
    								rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, REQUEST_FAILED, UNKNOWN_BREAKPOINT, null));
    								rm.done();
    								return;
    							}
    							breakpoint.setEnabled(false);
    							rm.done();
    						}
    					});
    		}
	    };
	    
		fRunControl.executeWithTargetAvailable(context, new Step[] { disableBreakpointStep }, finalRm);
	}

    /**
     * @nooverride This method is not intended to be re-implemented or extended by clients.
     * @noreference This method is not intended to be referenced by clients.
     */
    @DsfServiceEventHandler
    public void eventDispatched(IBreakpointHitDMEvent e) {
        if (e instanceof IContainerSuspendedDMEvent) {
            IExecutionDMContext[] triggeringContexts = ((IContainerSuspendedDMEvent)e).getTriggeringContexts();
            if (triggeringContexts != null) {
                for (IExecutionDMContext ctx : triggeringContexts) {
                    fBreakpointHitMap.put(ctx, e.getBreakpoints());
                }
            } else {
                fBreakpointHitMap.put(e.getDMContext(), e.getBreakpoints());
            }
        } else {
            fBreakpointHitMap.put(e.getDMContext(), e.getBreakpoints());
        }
    }

    /**
     * @nooverride This method is not intended to be re-implemented or extended by clients.
     * @noreference This method is not intended to be referenced by clients.
     */
    @DsfServiceEventHandler
    public void eventDispatched(IResumedDMEvent e) {
        if (e instanceof IContainerResumedDMEvent) {
            clearBreakpointHitForContainer((IContainerDMContext)e.getDMContext());
        } else {
            fBreakpointHitMap.remove(e.getDMContext());
        }
    }

    /**
     * Event handler when a thread is destroyed
     * @nooverride This method is not intended to be re-implemented or extended by clients.
     * @noreference This method is not intended to be referenced by clients.
     */
    @DsfServiceEventHandler
    public void eventDispatched(IExitedDMEvent e) {
        if (e.getDMContext() instanceof IContainerDMContext) {
            clearBreakpointHitForContainer((IContainerDMContext)e.getDMContext());
        } else {
            fBreakpointHitMap.remove(e.getDMContext());
        }
    }

    private void clearBreakpointHitForContainer(IContainerDMContext container) {
        for (Iterator<Map.Entry<IExecutionDMContext, IBreakpointDMContext[]>> itr = fBreakpointHitMap.entrySet().iterator(); itr.hasNext();) {
            if (DMContexts.isAncestorOf(itr.next().getKey(), container)) {
                itr.remove();
            }
        }
        fBreakpointHitMap.remove(container);
    }
    
}
