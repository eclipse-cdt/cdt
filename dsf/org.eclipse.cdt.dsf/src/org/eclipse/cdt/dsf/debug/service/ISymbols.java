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
import org.eclipse.cdt.dsf.datamodel.IDMData;
import org.eclipse.cdt.dsf.datamodel.IDMEvent;
import org.eclipse.cdt.dsf.service.IDsfService;

/**
 * Service for accessing debugger symbols.  This service builds on the Modules
 * service, but not all debuggers provide access for parsing symbols so this
 * service is separated.
 * 
 * @see IModules
 * @since 1.0
 */
public interface ISymbols extends IDsfService {
    public interface ISymbolObjectDMContext extends IDMContext {}
    
    /**
     * Data about a debug symbol.  
     */
    public interface ISymbolObjectDMData extends IDMData {
        String getName();
        String getTypeName();
        String getFilepath();
    }

    /**
     * Indicates that the list of symbol objects is changed.  Parsing debug 
     * symbols can be a long running operation (order of 10's of seconds or 
     * minutes), so it is useful for the service to provide access to the data
     * even while it's still parsing.  This event may be issued periodically
     * by the service to indicate that a section of debug symbols has been 
     * parsed.
     */
    public interface ISymbolDataChangedDMEvent extends IDMEvent<IModules.ISymbolDMContext> {}
    
    /**
     * Retrieves the list of symbols.
     * @param symCtx Symbols context to retrieve symbols for.
     * @param rm Request completion monitor.  The return value is an iterator (rather than 
     * array) since there could be a very large number of symbols returned.
     */
    public void getSymbols(IModules.ISymbolDMContext symCtx, DataRequestMonitor<Iterable<ISymbolObjectDMContext>> rm);
}
