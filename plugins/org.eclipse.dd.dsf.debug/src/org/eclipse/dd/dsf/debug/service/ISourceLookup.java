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

import org.eclipse.dd.dsf.concurrent.GetDataDone;
import org.eclipse.dd.dsf.service.IDsfService;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;

/**
 * Service for mapping debugger paths to host paths.  This service is needed
 * primarily by other services that need to access source-path mappings, such
 * as the breakpoints service.  For UI components, the platform source lookup
 * interfaces could be sufficient.
 */
public interface ISourceLookup extends IDsfService {
    
    /**
     * Context needed for debuggers that debug multiple processes.
     */
    public interface ISourceLookupContext {}

    public interface ISourceLookupResult {
        Object getSourceObject();
        ISourceContainer getMatchingContainer();
    }
    
    public interface IDebuggerPathLookupResult {
        String getDebuggerPath();
        ISourceContainer getMatchingContainer();
    }
    
    /** Returns the source lookup context for the given modules context. */
    ISourceLookupContext getContextForSymbolContext(IModules.ISymbolDMContext symCtx);
    
    /** 
     * Initializes the given context with the given list of source lookup 
     * containers.
     */ 
    void initializeSourceContainers(ISourceLookupContext ctx, ISourceContainer[] containers);
    
    /**
     * Retrieves the host source object for given debugger path string.
     */
    void getSource(ISourceLookupContext srcCtx, String debuggerPath, boolean searchDuplicates, GetDataDone<ISourceLookupResult[]> done);
    
    /**
     * Retrieves the debugger path string(s) for given host source object.
     */
    void getDebuggerPath(ISourceLookupContext srcCtx, Object source, boolean searchDuplicates, GetDataDone<IDebuggerPathLookupResult[]> done);
}
