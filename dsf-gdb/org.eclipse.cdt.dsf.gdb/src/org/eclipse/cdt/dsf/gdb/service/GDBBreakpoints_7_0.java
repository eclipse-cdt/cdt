/*******************************************************************************
 * Copyright (c) 2009, 2010 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ericsson - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.service;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.cdt.dsf.concurrent.CountingRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Immutable;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.AbstractDMContext;
import org.eclipse.cdt.dsf.datamodel.AbstractDMEvent;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControl;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.mi.service.MIBreakpointDMData;
import org.eclipse.cdt.dsf.mi.service.MIBreakpoints;
import org.eclipse.cdt.dsf.mi.service.command.commands.CLIPasscount;
import org.eclipse.cdt.dsf.mi.service.command.commands.CLITrace;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIBreakAfter;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIBreakCondition;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIBreakDelete;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIBreakDisable;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIBreakEnable;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIBreakInsert;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIBreakList;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIBreakWatch;
import org.eclipse.cdt.dsf.mi.service.command.output.CLITraceInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIBreakInsertInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIBreakListInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIBreakpoint;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.service.AbstractDsfService;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleContext;

/**
 * Initial breakpoint service implementation.
 * Implements the IBreakpoints interface.
 * @since 2.1
 */
public class GDBBreakpoints_7_0 extends AbstractDsfService implements IBreakpoints
{
	// Services
	ICommandControl fConnection;

	// Service breakpoints tracking
	// The breakpoints are stored per context and keyed on the back-end breakpoint reference
	private Map<IBreakpointsTargetDMContext, Map<Integer, MIBreakpointDMData>> fBreakpoints =
		new HashMap<IBreakpointsTargetDMContext, Map<Integer, MIBreakpointDMData>>();

	// Error messages
	final String NULL_STRING = ""; //$NON-NLS-1$
	final String UNKNOWN_EXECUTION_CONTEXT    = "Unknown execution context";    //$NON-NLS-1$
	final String UNKNOWN_BREAKPOINT_CONTEXT   = "Unknown breakpoint context";   //$NON-NLS-1$
	final String UNKNOWN_BREAKPOINT_TYPE      = "Unknown breakpoint type";      //$NON-NLS-1$
	final String UNKNOWN_BREAKPOINT           = "Unknown breakpoint";           //$NON-NLS-1$
	final String BREAKPOINT_INSERTION_FAILURE = "Breakpoint insertion failure"; //$NON-NLS-1$
	final String WATCHPOINT_INSERTION_FAILURE = "Watchpoint insertion failure"; //$NON-NLS-1$
	final String TRACEPOINT_INSERTION_FAILURE = "Tracepoint insertion failure"; //$NON-NLS-1$
	final String INVALID_CONDITION            = "Invalid condition";            //$NON-NLS-1$
	final String INVALID_BREAKPOINT_TYPE      = "Invalid breakpoint type";      //$NON-NLS-1$

	
	///////////////////////////////////////////////////////////////////////////
	// Breakpoint Events
	///////////////////////////////////////////////////////////////////////////
	
    static private class BreakpointsChangedEvent extends AbstractDMEvent<IBreakpointsTargetDMContext> implements IBreakpointsChangedEvent {
    	private IBreakpointDMContext[] fEventBreakpoints;
 
		public BreakpointsChangedEvent(IBreakpointDMContext bp) {
			super(DMContexts.getAncestorOfType(bp, IBreakpointsTargetDMContext.class));
			fEventBreakpoints = new IBreakpointDMContext[] { bp };
		}
		public IBreakpointDMContext[] getBreakpoints() {
			return fEventBreakpoints;
		}
    }
    
    static private class BreakpointAddedEvent extends BreakpointsChangedEvent implements IBreakpointsAddedEvent {
    	public BreakpointAddedEvent(IBreakpointDMContext context) {
            super(context);
		}
    }

    static private class BreakpointUpdatedEvent extends BreakpointsChangedEvent implements IBreakpointsUpdatedEvent {
        public BreakpointUpdatedEvent(IBreakpointDMContext context) {
            super(context);
        }
    }

    static private class BreakpointRemovedEvent extends BreakpointsChangedEvent implements IBreakpointsRemovedEvent {
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
    private static final class MIBreakpointDMContext extends AbstractDMContext implements IBreakpointDMContext {

    	// The breakpoint reference
    	private final int fReference;

    	/**
    	 * @param session       the DsfSession for this service
    	 * @param parents       the parent contexts
    	 * @param reference     the DsfMIBreakpoint reference
    	 */
    	public MIBreakpointDMContext(DsfSession session, IDMContext[] parents, int reference) {
            super(session.getId(), parents);
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
            return baseHashCode() + fReference;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return baseToString() + ".reference(" + fReference + ")";  //$NON-NLS-1$//$NON-NLS-2$
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
	public GDBBreakpoints_7_0(DsfSession session) {
		super(session);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.service.AbstractDsfService#initialize(org.eclipse.cdt.dsf.concurrent.RequestMonitor)
	 */
	@Override
	public void initialize(final RequestMonitor rm) {
		super.initialize(new RequestMonitor(getExecutor(), rm) {
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

		// Register this service
		register(new String[] { IBreakpoints.class.getName(),
				 				GDBBreakpoints_7_0.class.getName() },
				new Hashtable<String, String>());

		rm.done();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.service.AbstractDsfService#shutdown(org.eclipse.cdt.dsf.concurrent.RequestMonitor)
	 */
	@Override
	public void shutdown(final RequestMonitor rm) {
		unregister();
		rm.done();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.service.AbstractDsfService#getBundleContext()
	 */
	@Override
	protected BundleContext getBundleContext() {
		return GdbPlugin.getBundleContext();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.debug.service.IBreakpoints#getBreakpoints(org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointsTargetDMContext, org.eclipse.cdt.dsf.concurrent.DataRequestMonitor)
	 */
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
		final Map<Integer, MIBreakpointDMData> breakpointContext = fBreakpoints.get(context);
		if (breakpointContext == null) {
       		drm.setData(new IBreakpointDMContext[0]);
       		drm.done();
			return;
		}

		// Execute the command
		fConnection.queueCommand(new MIBreakList(context),
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
						result[i] = new MIBreakpointDMContext(getSession(), new IDMContext[] { context }, reference);
						breakpointContext.put(reference, breakpoint);
					}
					drm.setData(result);
					drm.done();
				}
			});
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.debug.service.IBreakpoints#getBreakpointDMData(org.eclipse.cdt.dsf.debug.service.IBreakpoints.IDsfBreakpointDMContext, org.eclipse.cdt.dsf.concurrent.DataRequestMonitor)
	 */
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
		Map<Integer, MIBreakpointDMData> contextBreakpoints = fBreakpoints.get(context);
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

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.debug.service.IBreakpoints#insertBreakpoint(org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointsTargetDMContext, java.util.Map, org.eclipse.cdt.dsf.concurrent.DataRequestMonitor)
	 */
	public void insertBreakpoint(IBreakpointsTargetDMContext context, Map<String, Object> attributes, DataRequestMonitor<IBreakpointDMContext> drm) {
		
		// Validate the context
		if (context == null) {
       		drm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, REQUEST_FAILED, UNKNOWN_EXECUTION_CONTEXT, null));
       		drm.done();
			return;
		}

		// Select the breakpoints context map. If it doesn't exist, create it.
		Map<Integer, MIBreakpointDMData> breakpointContext = fBreakpoints.get(context);
		if (breakpointContext == null) {
			breakpointContext = new HashMap<Integer, MIBreakpointDMData>();
			fBreakpoints.put(context, breakpointContext);
		}

		// Validate the breakpoint type
		String type = (String) attributes.get(MIBreakpoints.BREAKPOINT_TYPE);
		if (type == null) {
       		drm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, REQUEST_FAILED, UNKNOWN_BREAKPOINT_TYPE, null));
   			drm.done();
   			return;
		}

		// And go...
		if (type.equals(MIBreakpoints.BREAKPOINT)) {
			addBreakpoint(context, attributes, drm);
		}
		else if (type.equals(MIBreakpoints.WATCHPOINT)) {
			addWatchpoint(context, attributes, drm);
		}
		else if (type.equals(MIBreakpoints.TRACEPOINT)) {
			addTracepoint(context, attributes, drm);
		}
		else {
       		drm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, REQUEST_FAILED, UNKNOWN_BREAKPOINT_TYPE, null));
   			drm.done();
		}
	}

	/**
	 * @param map
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public Object getProperty(Map<String, Object> map, String key, Object defaultValue) {
		Object value = map.get(key);
		return (value != null) ? value : defaultValue;
	}

	/**
	 * @param attributes
	 * @return
	 */
	private String formatLocation(Map<String, Object> attributes) {

		// Unlikely default location
		String location = (String)  getProperty(attributes, MIBreakpoints.ADDRESS, NULL_STRING);

		// Get the relevant parameters
		String  fileName   = (String)  getProperty(attributes, MIBreakpoints.FILE_NAME, NULL_STRING);
		Integer lineNumber = (Integer) getProperty(attributes, MIBreakpoints.LINE_NUMBER, -1);
		String  function   = (String)  getProperty(attributes, MIBreakpoints.FUNCTION,  NULL_STRING);

		// Fix for Bug264721
		if (fileName.contains(" ")) { //$NON-NLS-1$
			fileName = "\"" + fileName + "\"";  //$NON-NLS-1$//$NON-NLS-2$
		}

		if (!fileName.equals(NULL_STRING)) {
			if (lineNumber != -1) {
				location = fileName + ":" + lineNumber; //$NON-NLS-1$
			} else {
				location = fileName + ":" + function;   //$NON-NLS-1$
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
	 * @param drm
	 */
	private void addBreakpoint(final IBreakpointsTargetDMContext context, final Map<String, Object> attributes, final DataRequestMonitor<IBreakpointDMContext> drm)
	{
		// Select the context breakpoints map
		final Map<Integer, MIBreakpointDMData> contextBreakpoints = fBreakpoints.get(context);
		if (contextBreakpoints == null) {
       		drm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, REQUEST_FAILED, UNKNOWN_BREAKPOINT_CONTEXT, null));
       		drm.done();
			return;
		}

		// Extract the relevant parameters (providing default values to avoid potential NPEs)
		String location = formatLocation(attributes);
		if (location.equals(NULL_STRING)) {
       		drm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, REQUEST_FAILED, UNKNOWN_BREAKPOINT_CONTEXT, null));
       		drm.done();
			return;
		}

		Boolean enabled        = (Boolean) getProperty(attributes, MIBreakpoints.IS_ENABLED,        true);
		Boolean isTemporary    = (Boolean) getProperty(attributes, MIBreakpointDMData.IS_TEMPORARY, false);
		Boolean isHardware     = (Boolean) getProperty(attributes, MIBreakpointDMData.IS_HARDWARE,  false);
		String  condition      = (String)  getProperty(attributes, MIBreakpoints.CONDITION,         NULL_STRING);
		Integer ignoreCount    = (Integer) getProperty(attributes, MIBreakpoints.IGNORE_COUNT,      0);
		String  threadId       = (String)  getProperty(attributes, MIBreakpointDMData.THREAD_ID,    "0"); //$NON-NLS-1$
		int     tid            = Integer.parseInt(threadId);

		// The DataRequestMonitor for the add request
		DataRequestMonitor<MIBreakInsertInfo> addBreakpointDRM =
			new DataRequestMonitor<MIBreakInsertInfo>(getExecutor(), drm) {
				@Override
	            protected void handleSuccess() {

	            	// With MI, an invalid location won't generate an error
                	if (getData().getMIBreakpoints().length == 0) {
                   		drm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, REQUEST_FAILED, BREAKPOINT_INSERTION_FAILURE, null));
                   		drm.done();
                   		return;
                    }

                	// Create a breakpoint object and store it in the map
                	final MIBreakpointDMData newBreakpoint = new MIBreakpointDMData(getData().getMIBreakpoints()[0]);
                	int reference = newBreakpoint.getNumber();
                	if (reference == -1) {
                   		drm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, REQUEST_FAILED, BREAKPOINT_INSERTION_FAILURE, null));
                   		drm.done();
                   		return;
                	}
                	contextBreakpoints.put(reference, newBreakpoint);

               		// Format the return value
               		MIBreakpointDMContext dmc = new MIBreakpointDMContext(getSession(), new IDMContext[] { context }, reference);
               		drm.setData(dmc);

               		// Flag the event
					getSession().dispatchEvent(new BreakpointAddedEvent(dmc), getProperties());

					drm.done();
				}

				@Override
	            protected void handleError() {
               		drm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, REQUEST_FAILED, BREAKPOINT_INSERTION_FAILURE, null));
               		drm.done();
				}
		};

		// Execute the command
		fConnection.queueCommand(
				new MIBreakInsert(context, isTemporary, isHardware, condition, ignoreCount, location, tid, !enabled, false), addBreakpointDRM);
	}
	
	/**
	 * Add a tracepoint
	 * 
	 * @param context
	 * @param breakpoint
	 * @param drm
	 */
	private void addTracepoint(final IBreakpointsTargetDMContext context, final Map<String, Object> attributes, final DataRequestMonitor<IBreakpointDMContext> drm)
	{
		// Select the context breakpoints map
		final Map<Integer, MIBreakpointDMData> contextBreakpoints = fBreakpoints.get(context);
		if (contextBreakpoints == null) {
       		drm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, REQUEST_FAILED, UNKNOWN_BREAKPOINT_CONTEXT, null));
       		drm.done();
			return;
		}

		// Extract the relevant parameters (providing default values to avoid potential NPEs)
		final String location = formatLocation(attributes);
		if (location.equals(NULL_STRING)) {
       		drm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, REQUEST_FAILED, UNKNOWN_BREAKPOINT_CONTEXT, null));
       		drm.done();
			return;
		}

		final String condition = (String)  getProperty(attributes, MIBreakpoints.CONDITION, NULL_STRING);

		fConnection.queueCommand(
				new CLITrace(context, location, condition),
				new DataRequestMonitor<CLITraceInfo>(getExecutor(), drm) {
					@Override
					protected void handleSuccess() {
						final Integer tpReference = getData().getTraceReference();
						if (tpReference == null) {
							drm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, REQUEST_FAILED, BREAKPOINT_INSERTION_FAILURE, null));
							drm.done();
							return;
						}
						
						// The simplest way to convert from the CLITraceInfo to a MIBreakInsertInfo
						// is to list the breakpoints and take the proper output
						fConnection.queueCommand(
								new MIBreakList(context),
								new DataRequestMonitor<MIBreakListInfo>(getExecutor(), drm) {
									@Override
									protected void handleSuccess() {
										MIBreakpoint[] breakpoints = getData().getMIBreakpoints();
										for (MIBreakpoint bp : breakpoints) {
											if (bp.getNumber() == tpReference) {

												// Create a breakpoint object and store it in the map
												final MIBreakpointDMData newBreakpoint = new MIBreakpointDMData(bp);
												int reference = newBreakpoint.getNumber();
												contextBreakpoints.put(reference, newBreakpoint);

												// Format the return value
												MIBreakpointDMContext dmc = new MIBreakpointDMContext(getSession(), new IDMContext[] { context }, reference);
												drm.setData(dmc);

												// Flag the event
												getSession().dispatchEvent(new BreakpointAddedEvent(dmc), getProperties());

												// By default the tracepoint is enabled at creation
												// If it wasn't supposed to be, then disable it right away
												// Also, tracepoints are created with no passcount.
												// We have to set the passcount manually now.
												Map<String,Object> delta = new HashMap<String,Object>();
												delta.put(MIBreakpoints.IS_ENABLED, getProperty(attributes, MIBreakpoints.IS_ENABLED, true));
												delta.put(MIBreakpoints.PASS_COUNT, getProperty(attributes, MIBreakpoints.PASS_COUNT, 0));
												modifyBreakpoint(dmc, delta, drm, false);
												return;
											}
										}
										drm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, REQUEST_FAILED, BREAKPOINT_INSERTION_FAILURE, null));
										drm.done();
									}
								});
					}
				});
	}

	/**
	 * Add a breakpoint of type WATCHPOINT
	 * 
	 * @param context
	 * @param watchpoint
	 * @param drm
	 */
	private void addWatchpoint(final IBreakpointsTargetDMContext context, final Map<String, Object> attributes, final DataRequestMonitor<IBreakpointDMContext> drm)
	{
		// Pick the context breakpoints map
		final Map<Integer, MIBreakpointDMData> contextBreakpoints = fBreakpoints.get(context);
		if (contextBreakpoints == null) {
       		drm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, REQUEST_FAILED, UNKNOWN_BREAKPOINT_CONTEXT, null));
       		drm.done();
			return;
		}

		// Extract the relevant parameters (providing default values to avoid potential NPEs)
		String expression = (String)  getProperty(attributes, MIBreakpoints.EXPRESSION, NULL_STRING);
		boolean isRead    = (Boolean) getProperty(attributes, MIBreakpoints.READ,    false);
		boolean isWrite   = (Boolean) getProperty(attributes, MIBreakpoints.WRITE,   false);

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
               		MIBreakpointDMContext dmc = new MIBreakpointDMContext(getSession(), new IDMContext[] { context }, reference);
               		drm.setData(dmc);

               		// Flag the event
					getSession().dispatchEvent(new BreakpointAddedEvent(dmc), getProperties());

               		// Condition, ignore count and state can not be specified at watchpoint creation time.
               		// Therefore, we have to update the watchpoint if any of these is present
               		Map<String,Object> delta = new HashMap<String,Object>();
               		delta.put(MIBreakpoints.CONDITION,    getProperty(attributes, MIBreakpoints.CONDITION, NULL_STRING));
               		delta.put(MIBreakpoints.IGNORE_COUNT, getProperty(attributes, MIBreakpoints.IGNORE_COUNT, 0 ));
               		delta.put(MIBreakpoints.IS_ENABLED,   getProperty(attributes, MIBreakpoints.IS_ENABLED, true));
               		modifyBreakpoint(dmc, delta, drm, false);
				}

				@Override
	            protected void handleError() {
               		drm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, REQUEST_FAILED, WATCHPOINT_INSERTION_FAILURE, null));
               		drm.done();
				}
			};

			// Execute the command
	        fConnection.queueCommand(new MIBreakWatch(context, isRead, isWrite, expression), addWatchpointDRM);
	}
	
	//-------------------------------------------------------------------------
	// removeBreakpoint
	//-------------------------------------------------------------------------

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.debug.service.IBreakpoints#removeBreakpoint(org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointDMContext, org.eclipse.cdt.dsf.concurrent.RequestMonitor)
	 */
	public void removeBreakpoint(final IBreakpointDMContext dmc, final RequestMonitor rm) {

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
		
		// Validate the target context
        IBreakpointsTargetDMContext context = DMContexts.getAncestorOfType(dmc, IBreakpointsTargetDMContext.class);
		if (context == null) {
			rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, REQUEST_FAILED, UNKNOWN_EXECUTION_CONTEXT, null));
			rm.done();
			return;
		}

		// Pick the context breakpoints map
		final Map<Integer,MIBreakpointDMData> contextBreakpoints = fBreakpoints.get(context);
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

		// Queue the command
		fConnection.queueCommand(
			new MIBreakDelete(context, new int[] { reference }),
			new DataRequestMonitor<MIInfo>(getExecutor(), rm) {
				@Override
				protected void handleSuccess() {
					getSession().dispatchEvent(new BreakpointRemovedEvent(dmc), getProperties());
					contextBreakpoints.remove(reference);
					rm.done();
				}
		});
	}

	// -------------------------------------------------------------------------
	// updateBreakpoint
	//-------------------------------------------------------------------------

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.debug.service.IBreakpoints#updateBreakpoint(org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointDMContext, java.util.Map, org.eclipse.cdt.dsf.concurrent.RequestMonitor)
	 */
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
		final Map<Integer, MIBreakpointDMData> contextBreakpoints = fBreakpoints.get(context);
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

        modifyBreakpoint(breakpointCtx, properties, rm, true);
	}

	/**
	 * @param dmc
	 * @param properties
	 * @param rm
	 * @param generateUpdateEvent
	 */
	private void modifyBreakpoint(final MIBreakpointDMContext breakpointCtx, Map<String, Object> attributes, 
			                      final RequestMonitor rm, final boolean generateUpdateEvent)
	{
		// Use a working copy of the attributes since we are going to tamper happily with them
		Map<String, Object> properties = new HashMap<String, Object>(attributes);
		
		// Retrieve the breakpoint parameters
		// At this point, we know their are OK so there is no need to re-validate
        IBreakpointsTargetDMContext context = DMContexts.getAncestorOfType(breakpointCtx, IBreakpointsTargetDMContext.class);
		final Map<Integer, MIBreakpointDMData> contextBreakpoints = fBreakpoints.get(context);
		final int reference = breakpointCtx.getReference();
		MIBreakpointDMData breakpoint = contextBreakpoints.get(reference);

		// Track the number of change requests
		int numberOfChanges = 0;
        final CountingRequestMonitor countingRm = new CountingRequestMonitor(getExecutor(), rm) {
            @Override
            protected void handleSuccess() {
           		if (generateUpdateEvent)
           			getSession().dispatchEvent(new BreakpointUpdatedEvent(breakpointCtx), getProperties());
        		rm.done();
            }
        };

        // Determine if the breakpoint condition changed
		String conditionAttribute = MIBreakpoints.CONDITION;
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
		String ignoreCountAttribute = MIBreakpoints.IGNORE_COUNT;
		if (properties.containsKey(ignoreCountAttribute)) {
			Integer oldValue =  breakpoint.getIgnoreCount();
			Integer newValue = (Integer) properties.get(ignoreCountAttribute);
			if (newValue == null) newValue = 0;
	        if (!oldValue.equals(newValue)) {
	        	changeIgnoreCount(context, reference, newValue, countingRm);
	        	numberOfChanges++;
	        }
			properties.remove(ignoreCountAttribute);
		}

        // Determine if the tracepoint pass count changed
		String passCountAttribute = MIBreakpoints.PASS_COUNT;
		if (properties.containsKey(passCountAttribute)) {
			Integer oldValue =  breakpoint.getPassCount();
			Integer newValue = (Integer) properties.get(passCountAttribute);
			if (newValue == null) newValue = 0;
	        if (!oldValue.equals(newValue)) {
	        	changePassCount(context, reference, newValue, countingRm);
	        	numberOfChanges++;
	        }
			properties.remove(passCountAttribute);
		}

        // Determine if the breakpoint state changed
		String enableAttribute = MIBreakpoints.IS_ENABLED;
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

//		// Determine if the breakpoint state changed
//		String commandsAttribute = MIBreakpoints.COMMANDS;
//		if (properties.containsKey(commandsAttribute)) {
//			String oldValue = "khouzam"; //TODO
//			String newValue = (String) properties.get(commandsAttribute);
//			if (newValue == null) newValue = NULL_STRING;
//	        if (!oldValue.equals(newValue)) {
//	        	IBreakpointAction[] actions = generateGdbActions(newValue);
//	        	numberOfChanges++;
//	        	changeActions(context, reference, actions, countingRm);
//	        }
//			properties.remove(commandsAttribute);
//		}

		// Set the number of completions required
        countingRm.setDoneCount(numberOfChanges);
	}

//	private IBreakpointAction[] generateGdbActions(String actionStr) {
//		String[] actionNames = actionStr.split(",");
//		IBreakpointAction[] actions = new IBreakpointAction[actionNames.length];
//		
//		for (int i = 0; i < actionNames.length; i++) {
//			BreakpointActionManager actionManager = CDebugCorePlugin.getDefault().getBreakpointActionManager();
//			actions[i] = actionManager.findTracepointAction(actionNames[i]);
//		}
//		return actions;
//	}
	/**
	 * Update the breakpoint condition
	 * 
	 * @param context
	 * @param dmc
	 * @param condition
	 * @param rm
	 */
	private void changeCondition(final IBreakpointsTargetDMContext context,
			final int reference, final String condition, final RequestMonitor rm)
	{
		// Pick the context breakpoints map
		final Map<Integer, MIBreakpointDMData> contextBreakpoints = fBreakpoints.get(context);
		if (contextBreakpoints == null) {
       		rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, REQUEST_FAILED, UNKNOWN_BREAKPOINT_CONTEXT, null));
       		rm.done();
			return;
		}

		// Queue the command
		fConnection.queueCommand(
			new MIBreakCondition(context, reference, condition),
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
		    				new MIBreakCondition(context, reference, NULL_STRING),
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

//	private void changeActions(final IBreakpointsTargetDMContext context,
//			final int reference, final IBreakpointAction[] actions, final RequestMonitor rm)
//	{
//		// Pick the context breakpoints map
//		final Map<Integer, MIBreakpointDMData> contextBreakpoints = fBreakpoints.get(context);
//		if (contextBreakpoints == null) {
//       		rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, REQUEST_FAILED, UNKNOWN_BREAKPOINT_CONTEXT, null));
//       		rm.done();
//			return;
//		}
//
//		// We only do this for tracepoints
//		
//		ArrayList<String> actionStrings = new ArrayList<String>();
//		for (int i = 0; i< actions.length; i++) {
//			IBreakpointAction action = actions[i];
//			if (action != null) {
//				actionStrings.add(action.toString());
//			}
//		}
//		// Queue the command
//		//TODO should we use a cache?
//		fConnection.queueCommand(
//			new MIBreakCommands(context, reference, actionStrings.toArray(new String[0])),
//		    new DataRequestMonitor<MIInfo>(getExecutor(), rm) {
//		        @Override
//		        protected void handleSuccess() {
////		        	MIBreakpointDMData breakpoint = contextBreakpoints.get(reference);
////		        	if (breakpoint == null) {
////		        		rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, REQUEST_FAILED, UNKNOWN_BREAKPOINT, null));
////                   		rm.done();
////                   		return;
////		        	}
////			        breakpoint.setCondition(condition);
//		            rm.done();
//		        }
//			});
//	}


	/**
	 * Update the breakpoint ignoreCount
	 * 
	 * @param context
	 * @param reference
	 * @param ignoreCount
	 * @param rm
	 */
	private void changeIgnoreCount(IBreakpointsTargetDMContext context,
			final int reference, final int ignoreCount, final RequestMonitor rm)
	{
		// Pick the context breakpoints map
		final Map<Integer, MIBreakpointDMData> contextBreakpoints = fBreakpoints.get(context);
		if (contextBreakpoints == null) {
       		rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, REQUEST_FAILED, UNKNOWN_BREAKPOINT_CONTEXT, null));
       		rm.done();
			return;
		}
		
		final MIBreakpointDMData breakpoint = contextBreakpoints.get(reference);
		if (breakpoint.getBreakpointType().equals(MIBreakpoints.TRACEPOINT)) {
			// Ingorecount is not supported for tracepoints
       		rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, REQUEST_FAILED, INVALID_BREAKPOINT_TYPE, null));
       		rm.done();
			return;			
		}

		// Queue the command
		fConnection.queueCommand(
			new MIBreakAfter(context, reference, ignoreCount),
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
	
	/**
	 * Update the tracepoint passCount
	 * 
	 * @param context
	 * @param reference
	 * @param ignoreCount
	 * @param rm
	 */
	private void changePassCount(IBreakpointsTargetDMContext context,
			final int reference, final int ignoreCount, final RequestMonitor rm)
	{
		// Pick the context breakpoints map
		final Map<Integer, MIBreakpointDMData> contextBreakpoints = fBreakpoints.get(context);
		if (contextBreakpoints == null) {
       		rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, REQUEST_FAILED, UNKNOWN_BREAKPOINT_CONTEXT, null));
       		rm.done();
			return;
		}
		
		final MIBreakpointDMData breakpoint = contextBreakpoints.get(reference);
		if (breakpoint.getBreakpointType().equals(MIBreakpoints.TRACEPOINT) == false) {
			// Passcount is just for tracepoints
       		rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, REQUEST_FAILED, INVALID_BREAKPOINT_TYPE, null));
       		rm.done();
			return;			
		}

		// Queue the command
		fConnection.queueCommand(
			new CLIPasscount(context, reference, ignoreCount),
		    new DataRequestMonitor<MIInfo>(getExecutor(), rm) {
		        @Override
		        protected void handleSuccess() {
		        	if (breakpoint == null) {
		        		rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, REQUEST_FAILED, UNKNOWN_BREAKPOINT, null));
                   		rm.done();
                   		return;
		        	}
			        breakpoint.setPassCount(ignoreCount);
		            rm.done();
		        }
		});
	}

	/**
	 * Enable the breakpoint
	 * 
	 * @param context
	 * @param reference
	 * @param rm
	 */
	private void enableBreakpoint(IBreakpointsTargetDMContext context,
			final int reference, final RequestMonitor rm)
	{
		// Pick the context breakpoints map
		final Map<Integer, MIBreakpointDMData> contextBreakpoints = fBreakpoints.get(context);
		if (contextBreakpoints == null) {
       		rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, REQUEST_FAILED, UNKNOWN_BREAKPOINT_CONTEXT, null));
       		rm.done();
			return;
		}

		// Queue the command
		fConnection.queueCommand(
			new MIBreakEnable(context, new int[] { reference }),
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

	/**
	 * Disable the breakpoint
	 * 
	 * @param context
	 * @param dmc
	 * @param rm
	 */
	private void disableBreakpoint(IBreakpointsTargetDMContext context,
			final int reference, final RequestMonitor rm)
	{
		// Pick the context breakpoints map
		final Map<Integer, MIBreakpointDMData> contextBreakpoints = fBreakpoints.get(context);
		if (contextBreakpoints == null) {
       		rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, REQUEST_FAILED, UNKNOWN_BREAKPOINT_CONTEXT, null));
       		rm.done();
			return;
		}

		// Queue the command
		fConnection.queueCommand(
			new MIBreakDisable(context, new int[] { reference }),
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
}
