/*******************************************************************************
 * Copyright (c) 2010, 2011 Texas Instruments, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Dobrin Alexiev (Texas Instruments) - initial API and implementation (bug 336876)
********************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.provisional.service;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.datamodel.IDMData;
import org.eclipse.cdt.dsf.datamodel.IDMEvent;
import org.eclipse.cdt.dsf.debug.internal.provisional.service.IExecutionContextTranslator;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.mi.service.IMIExecutionDMContext;

/**
 * EXPERIMENTAL. This class or interface has been added as part
 * of a work in progress. There is no guarantee that this API will work or that
 * it will remain the same.
 * 
 * @since 4.0
 * @noimplement This interface is not intended to be implemented by clients.
 * @experimental
 */
public interface IMIExecutionContextTranslator extends IExecutionContextTranslator {

	/**
	 * Interface describing a group to differenciate it from a process.
	 * We should use IContainerDMContext for groups instead, and have a new
	 * interface for processes instead of using Icontainer for processes.
	 * We'll do that eventually, but it requires a lot more changes than this approach.
	 *
	 */
	interface IGroupDMContext extends IContainerDMContext {}
	interface IGroupDMData extends IDMData {
		String getId();
		String getName();
	}
	
	/**
	 * The DM context of layout change of event. 
	 * Contains information about the layout change. 
	 */
    public interface IContainerLayoutDMContext extends IDMContext {}
	
    /** 
     * An event that the layout of the debug view has changed. 
     * A user group has been added or removed.   
     */
    public interface IContainerLayoutChangedEvent extends IDMEvent<IContainerLayoutDMContext> {
    }
	
    void getExecutionData(IGroupDMContext group, DataRequestMonitor<IGroupDMData> rm);
    
    /**
     * Creates containers path that represents all parent containers  
     * between the top container and the thread.
     * 
     * The original GDB implementation has one container at the top and multiple thread 
     * as children of that container. 
	 * Once we support multilevel containment we need a way to insert all containers 
	 * between the top container and the threads. 
	 * This method is providing that in a hard coded way. 
	 * Once we have user group we’ll change its implementation to consider the user groups.
	 * 
	 * For example if there are two user groups it will set up the 
	 * parent child relationship: Container 1 - User group 1 - Thread 1. 
     * 
     * @param containerDmc The parent process debugging context
     * @param threadDmc The parent thread context
     */
	public IContainerDMContext createContainerPath(IContainerDMContext containerDmc, IMIExecutionDMContext threadDmc);
	
	/**
	 * Returns the children containers of the given container.
	 * @return the children containers of the given container.
	 */
    public IContainerDMContext[] getChildContainers(IContainerDMContext container);

    /**
     * Returns the execution contexts for the given container after the translation.
     * @return array of execution contexts belonging to the given container context.
     */
    public void getExecutionContexts(IContainerDMContext containerDmc, DataRequestMonitor<IExecutionDMContext[]> rm);
    
    /**
     * Clients should cache this object instead of the IExectionDMContext. 
     * When the layout of the debug view changes new IExectionDMContext are being created
     * to reflect properly the new execution context hierarchy. 
     */
    public IDMContext getStableContext(IExecutionDMContext executionDMContext);
    
    /**
     * Returns a list of all children of a given container DM context.
     * The list includes both IContainerDMContext and IExecutionDMContext. 
     */
	public void getExecutionAndContainerContexts(IContainerDMContext containerDmc, DataRequestMonitor<IExecutionDMContext[]> rm);
}
