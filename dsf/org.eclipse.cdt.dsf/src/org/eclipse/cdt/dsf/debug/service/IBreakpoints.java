/*******************************************************************************
 * Copyright (c) 2006, 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Ericsson - Revisited the API
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.service;

import java.util.Map;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Immutable;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.datamodel.IDMData;
import org.eclipse.cdt.dsf.datamodel.IDMEvent;
import org.eclipse.cdt.dsf.service.IDsfService;

/**
 * Breakpoint service interface
 * 
 * @since 1.0
 */
public interface IBreakpoints extends IDsfService {


    /**
     * Marker interface for a context for which breakpoints can be installed
     */
    public interface IBreakpointsTargetDMContext extends IDMContext {}
    
    /**
     * Specific breakpoint context
     */
    @Immutable
    public interface IBreakpointDMContext extends IDMContext {}
    
    /**
	 * Breakpoint events
	 */
    public interface IBreakpointsChangedEvent extends IDMEvent<IBreakpointsTargetDMContext> {
        public IBreakpointDMContext[] getBreakpoints();        
    }
    
    public interface IBreakpointsAddedEvent extends IBreakpointsChangedEvent {}
    public interface IBreakpointsUpdatedEvent extends IBreakpointsChangedEvent {}
    public interface IBreakpointsRemovedEvent extends IBreakpointsChangedEvent {}
    
    /**
     * Effective breakpoint data as held by the back-end.
     */
    public interface IBreakpointDMData extends IDMData {

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
