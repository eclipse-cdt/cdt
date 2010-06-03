/*******************************************************************************
 * Copyright (c) 2006, 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.service;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.datamodel.IDMEvent;
import org.eclipse.cdt.dsf.service.IDsfService;

/**
 * Service for mapping debugger paths to host paths.  This service is needed
 * primarily by other services that need to access source-path mappings, such
 * as the breakpoints service.  For UI components, the platform source lookup
 * interfaces could be sufficient.
 * 
 * @since 1.0
 */
public interface ISourceLookup extends IDsfService {

    public interface ISourceLookupDMContext extends IDMContext {}
        
    public interface ISourceLookupChangedDMEvent extends IDMEvent<ISourceLookupDMContext> {}
    
    /**
     * Retrieves the host source object for given debugger path string.
     */
    void getSource(ISourceLookupDMContext ctx, String debuggerPath, DataRequestMonitor<Object> rm);
    
    /**
     * Retrieves the debugger path string for given host source object.
     */
    void getDebuggerPath(ISourceLookupDMContext ctx, Object source, DataRequestMonitor<String> rm);
}
