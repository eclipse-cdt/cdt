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
 * Breakpoint service interface.  The breakpoint service tracks platform breakpoint
 * objects, and based on those, it manages breakpoints in the debugger back end.
 */
public interface IBreakpoints extends IDsfService {

    /**
     * Marker interface for a context for which breakpoints can be installed
     */
    public interface IBreakpointsTargetDMContext extends IDMContext {};
    
    /**
     * Specific breakpoint context 
     */
    public interface IDsfBreakpointDMContext extends IDMContext {};
    
    /**
     * Breakpoint structure.
     * Properties are stored in a map.
     */
    public interface IDsfBreakpoint {

    	// Minimal breakpoint properties
    	public static final String DSFBREAKPOINT = "org.eclipse.dd.dsf.debug.service.breakpoint"; //$NON-NLS-1$
    	public static final String FILE_NAME    = DSFBREAKPOINT + ".fileName";    //$NON-NLS-1$
    	public static final String LINE_NUMBER  = DSFBREAKPOINT + ".lineNumber";  //$NON-NLS-1$
    	public static final String FUNCTION     = DSFBREAKPOINT + ".function";    //$NON-NLS-1$
    	public static final String CONDITION    = DSFBREAKPOINT + ".condition";   //$NON-NLS-1$
    	public static final String IGNORE_COUNT = DSFBREAKPOINT + ".ignoreCount"; //$NON-NLS-1$
    	public static final String IS_ENABLED   = DSFBREAKPOINT + ".isEnabled";   //$NON-NLS-1$

    	public Object getReference();

    	public Map<String,Object> getProperties();
    	public Object getProperty(String key, Object defaultValue);

    	public Object setProperty(String key, Object value);
//    	public void setProperties(Map<String,Object> properties);
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
	public void getBreakpointList(IBreakpointsTargetDMContext context,
			DataRequestMonitor<IDsfBreakpointDMContext[]> drm);

	/**
     * Retrieves a specific breakpoint from the service.
     * 
     * @param context	the execution context of the breakpoint
     * @param dmc		the breakpoint reference
     * @return IDsfBreakpoint
     */
	public IDsfBreakpoint getBreakpoint(IBreakpointsTargetDMContext context,
			IDsfBreakpointDMContext dmc);

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
     * @param breakpoint	the breakpoint to insert
     * @param drm			the DRM returning the breakpoint reference
     */
	public void addBreakpoint(IBreakpointsTargetDMContext context, IDsfBreakpoint breakpoint,
			DataRequestMonitor<IDsfBreakpointDMContext> drm);

	/**
     * Removes the breakpoint on the target.
     * 
     * If the breakpoint doesn't exist, silently ignore it.
     * 
     * @param context	the execution context of the breakpoint
     * @param dmc		the reference of breakpoint to remove
     * @param rm		the asynchronous request monitor
     */
	public void removeBreakpoint(IBreakpointsTargetDMContext context,
			IDsfBreakpointDMContext dmc, RequestMonitor rm);

	/**
     * Updates the breakpoint properties on the target.
     * 
     * To add/update/remove a property, simply create a map with
     * the desired value(s) for the given key(s).
     * 
     * A null value is used for removal of a property e.g.:
     *    properties.set(FUNCTION, null);
     * 
     * @param context	the execution context of the breakpoint
     * @param dmc		the reference of breakpoint to remove
     * @param rm		the asynchronous request monitor
     */
	public void updateBreakpoint(IBreakpointsTargetDMContext context,
			IDsfBreakpointDMContext dmc, Map<String,Object> properties,
			DataRequestMonitor<IDsfBreakpointDMContext> drm);
}
