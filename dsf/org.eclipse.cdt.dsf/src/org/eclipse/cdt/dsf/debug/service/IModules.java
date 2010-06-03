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

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.datamodel.IDMEvent;
import org.eclipse.cdt.dsf.service.IDsfService;

/**
 * Debugger service representing module handling logic of a debugger.
 * 
 * @since 1.0
 */
public interface IModules extends IDsfService {
    
    /**
     * Symbol context represents the space into which module symbols are loaded.
     * Traditionally symbols are loaded in context of a process, but for other
     * types of debugging, like kernel or no-OS debugging, it's useful to 
     * separate the concept of a symbol context from a process.
     */
    public interface ISymbolDMContext extends IDMContext {}
    
    /**
     * Module context represents a single module that is loaded.
     */
    public interface IModuleDMContext extends IDMContext {}
    
    /**
     * Event indicating a change in the symbol information for given context.
     */
    public interface ModulesChangedDMEvent extends IDMEvent<ISymbolDMContext> {}

    /**
     * Specific event identifying that a new module was loaded into a 
     * symbol context.
     */
    public interface ModuleLoadedDMEvent extends ModulesChangedDMEvent {
        /** Returns context of the module that was loaded */
        IModuleDMContext getLoadedModuleContext();
    }
    
    public interface ModuleUnloadedDMEvent extends ModulesChangedDMEvent {
        /** Returns context of the module that was un-loaded */
        IModuleDMContext getUnloadedModuleContext();
    }

    /** Module information. */
    public interface IModuleDMData {
        String getName();
        String getFile();
        long getTimeStamp();
        String getBaseAddress();
        String getToAddress();
        boolean isSymbolsLoaded();
        long getSize();
    }
    
    /** Line information about a particular address */
    public interface LineInfo  {
        IAddress getAddress();
        String getSourceFile();
        int getStartLine();
        int getStartColumn();
        int getEndLine();
        int getEndColumn();
    }
    
    /** Address information about a particular file/line */
    public interface AddressRange {
        IAddress getStartAddress();
        IAddress getEndAddress();
    }

    void getModuleData(IModuleDMContext dmc, DataRequestMonitor<IModuleDMData> rm);

    /** 
     * Retreives the list of modules loaded in given symbol context. 
     */ 
    void getModules(ISymbolDMContext symCtx, DataRequestMonitor<IModuleDMContext[]> rm);

    /**
     * Calculates the line numbers corresponding to the given address. 
     */
    void calcLineInfo(ISymbolDMContext symCtx, IAddress address, DataRequestMonitor<LineInfo[]> rm);
    
    /**
     * Calculates the addresses corresponding to the given source file location.
     */
    void calcAddressInfo(ISymbolDMContext symCtx, String file, int line, int col, DataRequestMonitor<AddressRange[]> rm);

}
