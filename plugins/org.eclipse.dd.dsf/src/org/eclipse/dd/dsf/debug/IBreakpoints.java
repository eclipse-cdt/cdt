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
package org.eclipse.dd.dsf.debug;

import org.eclipse.dd.dsf.concurrent.GetDataDone;
import org.eclipse.dd.dsf.debug.IRunControl.IExecutionDMC;
import org.eclipse.dd.dsf.model.IDataModelContext;
import org.eclipse.dd.dsf.model.IDataModelData;
import org.eclipse.dd.dsf.model.IDataModelEvent;
import org.eclipse.dd.dsf.service.IDsfService;
import org.eclipse.debug.core.model.IBreakpoint;

/**
 * Breakpoint service interface.  The breakpoint service tracks platform breakpoint
 * objects, and based on those, it manages breakpoints in the debugger back end.
 * The purpose of the service model interface is to allow UI clients to display
 * breakpoint status in more detail and more dynamically than it it possible with
 * just the marker-based breakpoint object.
 */
public interface IBreakpoints extends IDsfService {
   
    public enum BreakpointStatus { INSTALLED, FAILED_TO_INSTALL, FILTERED_OUT }
    
    public interface IBreakpointDMC extends IDataModelContext<IBreakpointData> {}
    
    public interface IBreakpointData extends IDataModelData {
        IBreakpoint getPlatformBreakpoint();
        BreakpointStatus getStatus();
    }
    
    public interface IBreakpointEvent extends IDataModelEvent<IBreakpointDMC> {}
    
    public interface IBreakpointInstalledEvent extends IBreakpointEvent {}
    public interface IBreakpointUninstalledEvent extends IBreakpointEvent {}
    public interface IBreakpointInstallFailedEvent extends IBreakpointEvent {}
    
    public interface IBreakpointHitEvent extends IBreakpointEvent {}
    
    public void getAllBreakpoints(IExecutionDMC execDmc, GetDataDone<IBreakpointDMC[]> done);
    public void getBreakpoints(IExecutionDMC execDmc, IBreakpoint platformBp, GetDataDone<IBreakpointDMC[]> done);
}

