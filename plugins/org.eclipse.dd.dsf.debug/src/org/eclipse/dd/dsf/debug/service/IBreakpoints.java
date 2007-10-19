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

import org.eclipse.dd.dsf.concurrent.DataRequestMonitor;
import org.eclipse.dd.dsf.datamodel.IDMContext;
import org.eclipse.dd.dsf.datamodel.IDMData;
import org.eclipse.dd.dsf.datamodel.IDMEvent;
import org.eclipse.dd.dsf.datamodel.IDMService;
import org.eclipse.debug.core.model.IBreakpoint;

/**
 * Breakpoint service interface.  The breakpoint service tracks platform breakpoint
 * objects, and based on those, it manages breakpoints in the debugger back end.
 * The purpose of the service model interface is to allow UI clients to display
 * breakpoint status in more detail and more dynamically than it it possible with
 * just the marker-based breakpoint object.
 */
public interface IBreakpoints extends IDMService {
   
    public enum BreakpointStatus { INSTALLED, FAILED_TO_INSTALL, FILTERED_OUT }
    
    public interface IBreakpointDMContext extends IDMContext {}
    
    public interface IBreakpointDMData extends IDMData {
        IBreakpoint getPlatformBreakpoint();
        BreakpointStatus getStatus();
    }
    
    public interface IBreakpointDMEvent extends IDMEvent<IBreakpointDMContext> {}
    
    public interface IBreakpointInstalledDMEvent extends IBreakpointDMEvent {}
    public interface IBreakpointUninstalledDMEvent extends IBreakpointDMEvent {}
    public interface IBreakpointInstallFailedDMEvent extends IBreakpointDMEvent {}
    
    public interface IBreakpointHitEvent extends IBreakpointDMEvent {}
    
    public void getAllBreakpoints(IDMContext ctx, DataRequestMonitor<IBreakpointDMContext[]> rm);
    public void getBreakpoints(IDMContext ctx, IBreakpoint platformBp, DataRequestMonitor<IBreakpointDMContext[]> rm);
}

