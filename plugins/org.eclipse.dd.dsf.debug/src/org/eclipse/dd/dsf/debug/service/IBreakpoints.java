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

import org.eclipse.dd.dsf.concurrent.DataRequestMonitor;
import org.eclipse.dd.dsf.concurrent.RequestMonitor;
import org.eclipse.dd.dsf.datamodel.IDMContext;
import org.eclipse.dd.dsf.service.IDsfService;

/**
 * Breakpoint service interface
 */
public interface IBreakpoints extends IDsfService {

    /**
     * Marker interface for a context for which breakpoints can be installed
     */
    public interface IBreakpointsTargetDMContext extends IDMContext {};
    
    /**
     * Specific breakpoint context
     */
    public interface IDsfBreakpointDMContext extends IDMContext {

    	// Get the execution context
    	IBreakpointsTargetDMContext getTargetContext();

    	// Get the breakpoint reference
    	Object getReference();
    };
    
    /**
     * Breakpoint structure
     */
    public interface IDsfBreakpoint {

    	// Breakpoint types
    	public static enum IDsfBreakpointNature { BREAKPOINT, WATCHPOINT, CATCHPOINT, TRACEPOINT };

    	// Retrieve the breakpoint nature
    	public IDsfBreakpointNature getNature();

    	// Retrieve the breakpoint set of properties
    	public Map<String,Object> getProperties();

    	// Retrieve a single breakpoint property
    	public Object getProperty(String key, Object defaultValue);

    	// Update a single breakpoint property
    	public void setProperty(String key, Object value);
    };

    /**
     * Refreshes the list of breakpoints from the [context] and returns the list 
     * of references.
     * 
     * Use getBreakpoint() to retrieve individual breakpoints.
     * 
     * @param context	the execution context of the breakpoint
     * @param drm		the list of breakpoints in the execution context
     */
	public void getBreakpoints(IBreakpointsTargetDMContext context,
			DataRequestMonitor<IDsfBreakpointDMContext[]> drm);

	/**
     * Retrieves a specific breakpoint from the service.
     * 
     * @param dmc		the breakpoint reference
     * @param drm		the DRM returning the breakpoint data
     */
	public void getBreakpointDMData(IDsfBreakpointDMContext dmc,
			DataRequestMonitor<IDsfBreakpoint> drm);

	/**
     * Adds a breakpoint on the target.
     * 
     * The breakpoint reference is returned in the DRM. The actual breakpoint
     * object can be later be retrieved using getBreakpoint(reference).
     * 
     * E.g.:
     *    IDsfBreakpointDMContext ref = addBreakpoint(...);
     *    IDsfBreakpoint bp = getBreakpoint(ref);
     * 
     * If the breakpoint is a duplicate (already set previously), then it is up to
     * the back-end to decide if it is an error or not.
     * 
     * @param context		the execution context of the breakpoint
     * @param breakpoint	the breakpoint
     * @param drm			the DRM returning the breakpoint reference
     */
	public void insertBreakpoint(IBreakpointsTargetDMContext context,
			IDsfBreakpoint breakpoint,
			DataRequestMonitor<IDsfBreakpointDMContext> drm);

	/**
     * Removes the breakpoint on the target.
     * 
     * If the breakpoint doesn't exist, silently ignore it.
     * 
     * @param dmc		the context of the breakpoints to remove
     * @param rm		the asynchronous request monitor
     */
	public void removeBreakpoint(IDsfBreakpointDMContext dmc,
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
     *    properties.set(some_key, null);
     * 
     * @param delta     the delta properties
     * @param dmc		the context of the breakpoints to modify
     * @param rm		the asynchronous request monitor
     */
	public void updateBreakpoint(IDsfBreakpointDMContext dmc,
			Map<String,Object> delta, RequestMonitor drm);

}
