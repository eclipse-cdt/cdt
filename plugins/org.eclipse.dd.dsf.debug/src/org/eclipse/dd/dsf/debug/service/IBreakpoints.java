/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Ericsson - Revisited the API
 *******************************************************************************/
package org.eclipse.dd.dsf.debug.service;

import java.util.Map;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.dd.dsf.concurrent.DataRequestMonitor;
import org.eclipse.dd.dsf.concurrent.Immutable;
import org.eclipse.dd.dsf.concurrent.RequestMonitor;
import org.eclipse.dd.dsf.datamodel.IDMContext;
import org.eclipse.dd.dsf.datamodel.IDMEvent;
import org.eclipse.dd.dsf.service.IDsfService;

/**
 * Breakpoint service interface
 */
public interface IBreakpoints extends IDsfService {

    /**
     * Breakpoint attributes markers used in the map parameters of insert/updateBreakpoint().
     * All are optional with the possible exception of TYPE. It is the responsibility of the
     * service to ensure that the set of attributes provided is sufficient to create/update
     * a valid breakpoint on the back-end.
     */
	// General markers
	public static final String DSFBREAKPOINT   = "org.eclipse.dd.dsf.debug.breakpoint"; //$NON-NLS-1$
	public static final String BREAKPOINT_TYPE = DSFBREAKPOINT + ".type";      //$NON-NLS-1$
	public static final String BREAKPOINT      = "breakpoint";                 //$NON-NLS-1$
	public static final String WATCHPOINT      = "watchpoint";                 //$NON-NLS-1$
	public static final String CATCHPOINT      = "catchpoint";                 //$NON-NLS-1$

	// Basic set of breakpoint attribute markers
	public static final String FILE_NAME     = DSFBREAKPOINT + ".fileName";    //$NON-NLS-1$
	public static final String LINE_NUMBER   = DSFBREAKPOINT + ".lineNumber";  //$NON-NLS-1$
	public static final String FUNCTION      = DSFBREAKPOINT + ".function";    //$NON-NLS-1$
	public static final String ADDRESS       = DSFBREAKPOINT + ".address";     //$NON-NLS-1$
	public static final String CONDITION     = DSFBREAKPOINT + ".condition";   //$NON-NLS-1$
	public static final String IGNORE_COUNT  = DSFBREAKPOINT + ".ignoreCount"; //$NON-NLS-1$
	public static final String IS_ENABLED    = DSFBREAKPOINT + ".isEnabled";   //$NON-NLS-1$

	// Basic set of watchpoint attribute markers
	public static final String EXPRESSION    = DSFBREAKPOINT + ".expression";  //$NON-NLS-1$
	public static final String READ          = DSFBREAKPOINT + ".read";        //$NON-NLS-1$
	public static final String WRITE         = DSFBREAKPOINT + ".write";       //$NON-NLS-1$

    /**
     * Marker interface for a context for which breakpoints can be installed
     */
    public interface IBreakpointsTargetDMContext extends IDMContext {}
    
    /**
     * Specific breakpoint context
     */
    @Immutable
    public interface IBreakpointDMContext extends IDMContext {

    	public IBreakpointsTargetDMContext getTargetContext();
    }
    
    /**
	 * Breakpoint events
	 */
    public interface IBreakpointsChangedEvent extends IDMEvent<IBreakpointDMContext> {}
    
    public interface IBreakpointAddedEvent extends IBreakpointsChangedEvent {
    	public IBreakpointDMContext getAddedBreakpoint();
    }

    public interface IBreakpointUpdatedEvent extends IBreakpointsChangedEvent {
    	public IBreakpointDMContext getUpdatedBreakpoint();
    }

    public interface IBreakpointRemovedEvent extends IBreakpointsChangedEvent {
    	public IBreakpointDMContext getRemovedBreakpoint();
    }
    
    /**
     * Effective breakpoint data as held by the back-end.
     */
    public interface IBreakpointDMData {

    	public String     getBreakpointType();
    	public String     getFileName();
    	public int        getLineNumber();
    	public String     getFunctionName();
    	public IAddress[] getAddresses();
    	public String     getCondition();
    	public int        getIgnoreCount();
    	public boolean    isEnabled();
    	public String     getExpression();
    }

    /**
     * Retrieves the list of breakpoints installed in the context.
     * 
     * Use getBreakpointDMData() to retrieve individual breakpoints.
     * 
     * @param context	the execution context of the breakpoint
     * @param drm		the list of breakpoints in the execution context
     */
	public void getBreakpoints(IBreakpointsTargetDMContext context,
			DataRequestMonitor<IBreakpointDMContext[]> drm);

	/**
     * Retrieves a specific breakpoint from the service.
     * 
     * @param dmc		the breakpoint reference
     * @param drm		the DRM returning the breakpoint data
     */
	public void getBreakpointDMData(IBreakpointDMContext dmc,
			DataRequestMonitor<IBreakpointDMData> drm);

	/**
     * Adds a breakpoint on the target.
     * 
     * The breakpoint context is returned in the DRM. The actual breakpoint
     * object can be later be retrieved using getBreakpoint(bp_context).
     * 
     * E.g.:
     *    IBreakpointDMContext ref = insertBreakpoint(...);
     *    IBreakpointDMData bp = getBreakpointDMData(ref);
     * 
     * If the breakpoint is a duplicate (already set previously), then it is up to
     * the back-end to decide if it is an error or not.
     * 
     * @param context		the execution context of the breakpoint
     * @param attributes	the breakpoint attributes
     * @param drm			the DRM returning the breakpoint reference
     */
	public void insertBreakpoint(IBreakpointsTargetDMContext context,
			Map<String,Object> attributes,
			DataRequestMonitor<IBreakpointDMContext> drm);

	/**
     * Removes the breakpoint on the target.
     * 
     * If the breakpoint doesn't exist, silently ignore it.
     * 
     * @param dmc		the context of the breakpoints to remove
     * @param rm		the asynchronous request monitor
     */
	public void removeBreakpoint(IBreakpointDMContext dmc,
			RequestMonitor rm);

	/**
     * Updates the breakpoint properties on the target.
     * 
     * To add/update/remove a property, simply create a map with
     * the desired value(s) for the given key(s).
     * 
     * Properties that affect the breakpoint nature or location
     * should not be updated. Instead, the breakpoint should be
     * removed then re-inserted.
     * 
     * A null value is used for removal of a property e.g.:
     *    delta.set(some_key, null);
     * 
     * @param delta     the delta properties
     * @param dmc		the context of the breakpoints to modify
     * @param rm		the asynchronous request monitor
     */
	public void updateBreakpoint(IBreakpointDMContext dmc,
			Map<String,Object> delta, RequestMonitor drm);

}
