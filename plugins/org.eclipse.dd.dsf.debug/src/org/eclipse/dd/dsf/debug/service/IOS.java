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

import org.eclipse.dd.dsf.concurrent.Done;
import org.eclipse.dd.dsf.concurrent.GetDataDone;
import org.eclipse.dd.dsf.datamodel.IDMContext;
import org.eclipse.dd.dsf.datamodel.IDMData;
import org.eclipse.dd.dsf.datamodel.IDMService;

/**
 * Provides generic access to operating system objects and methods to 
 * manipulate those objects.  This is a much more extensive interface than
 * the NativeProcesses service but for simple debugging, it serves the same 
 * purpose:  to list/create/terminate processes and attach debugger to them.
 *
 * @see INativeProcesses
 */
public interface IOS extends IDMService {
    
    /**
     * Context object for the whole OS, for debuggers that support 
     * debugging multiple targets/cores simultaneously.
     */
    public interface IOSDMContext extends IDMContext<IOSDMData> {}

    /**
     * Data object describing OS info
     */
    public interface IOSDMData extends IDMData {
        String getName();
        String getDescription();
        String getVersion();        
    }

    /**
     * Context for a OS object type, such as process, kernel task, semaphore, etc.
     */
    public interface IObjectTypeDMContext extends IDMContext<IObjectTypeDMData> {}
    
    /**
     * Description data for a OS object type.
     */
    public interface IObjectTypeDMData extends IDMData {
        String getName();
        String getDescription();
        String getSingularName();
        String getPluralName();
        boolean hasExecutionContext();
        boolean hasModulesContext();
        boolean hasMemoryContext();
    }

    /**
     * OS object context.
     */
    public interface IObjectDMContext extends IDMContext<IObjectDMData> {}

    /**
     * Description data for an OS object.
     */
    public interface IObjectDMData extends IDMData {
        String getName();
        String getID();
        boolean canAttachDebugger();
        boolean isDebuggerAttached(); 
        IRunControl.IExecutionDMContext getExecutionDMC();
        IModules.ISymbolDMContext getSymbolDMC();
        IMemory.IMemoryContext getMemoryContext();
    }
    
    /**
     * Retrieves list of OS object types.
     * @param os OS context.
     * @param parent Optional parent type.
     * @param done Return token.
     */
    public void getObjectTypes(IOSDMContext os, IObjectTypeDMContext parent, GetDataDone<IObjectTypeDMContext[]> done);
    
    /**
     * Retrieves list of OS objects for given type.
     * @param os OS context.
     * @param type The object type.
     * @param parent Optional parent of the requested objects.
     * @param done Return token.
     */
    public void getObjects(IOSDMContext os, IObjectTypeDMContext type, IObjectDMContext parent, GetDataDone<IObjectDMContext[]> done);
    
    /**
     * Attaches the debugger to given OS object context.
     */
    public void attachDebuggerToObject(IObjectDMContext objectDmc, Done done);
}
