/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.dd.dsf.debug.service;

import org.eclipse.dd.dsf.concurrent.RequestMonitor;
import org.eclipse.dd.dsf.datamodel.IDMContext;
import org.eclipse.dd.dsf.service.IDsfService;

/**
 * Breakpoint service interface.  The breakpoint service tracks platform breakpoint
 * objects, and based on those, it manages breakpoints in the debugger back end.
 * The purpose of the service model interface is to allow UI clients to display
 * breakpoint status in more detail and more dynamically than it it possible with
 * just the marker-based breakpoint object.
 */
public interface IBreakpoints extends IDsfService {

    /**
     * Marker interface for a context for which breakpoints can be installed.
     */
    public interface IBreakpointsDMContext extends IDMContext {};
    
    /**
     * Install and begin tracking breakpoints for given context.  The service 
     * will keep installing new breakpoints that appear in the IDE for this 
     * context until {@link #uninstallBreakpoints(IDMContext)} is called for that
     * context.
     * @param dmc Context to start tracking breakpoints for.
     * @param rm Completion callback.
     */
    public void installBreakpoints(IDMContext dmc, RequestMonitor rm);
    
    /**
     * Uninstall and stop tracking breakpoints for the given context.
     * @param dmc Context to start tracking breakpoints for.
     * @param rm Completion callback.
     */
    public void uninstallBreakpoints(IDMContext dmc, RequestMonitor rm);
}

