/*******************************************************************************
 * Copyright (c) 2015 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc Khouzam (Ericsson) - initial API and implementation (bug 336876)
********************************************************************************/
package org.eclipse.cdt.dsf.gdb.service;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.datamodel.IDMData;
import org.eclipse.cdt.dsf.datamodel.IDMEvent;
import org.eclipse.cdt.dsf.debug.internal.provisional.service.IExecutionContextTranslator;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;

/**
 * @since 5.0
 */
public interface IGDBGrouping extends IExecutionContextTranslator {

	/**
	 * Interface describing a group to differentiate it from a process.
	 * We should use IContainerDMContext for groups instead, and have a new
	 * interface for processes instead of using IContainer for processes.
	 * We'll do that eventually, but it requires a lot more changes than this approach.
	 */
	interface IGroupDMContext extends IContainerDMContext {}
	
	/**
	 *  Model data interface corresponding to IGroupDMContext
	 */
	interface IGroupDMData extends IDMData {
		String getId();
		String getName();
	}
		
    /** 
     * Indicates that a new group has been created.
     */
    interface IGroupCreatedEvent extends IDMEvent<IGroupDMContext> {}
    
    /** 
     * Indicates that a group has been deleted.
     */
    interface IGroupDeletedEvent extends IDMEvent<IGroupDMContext> {}
    
    /** 
     * Indicates that a group's content has been modified. 
     */
    interface IGroupChangedEvent extends IDMEvent<IGroupDMContext> {}
    
	/**
	 * Returns the group execution data that corresponds to a group DM context
	 * 
	 * @param group The group context
	 * @param rm The request monitor
	 */
    void getExecutionData(IGroupDMContext group, DataRequestMonitor<IGroupDMData> rm);
    
    /**
     * Returns the execution contexts for the given container after the translation.
     * 
     * @param containerDmc The container for which the execution contexts will be returned.
     *                     If null, the top-level execution contexts will be returned.
     * @param rm The request monitor
     * @return array of execution contexts belonging to the given container context.
     */
    void getExecutionContexts(IContainerDMContext containerDmc, DataRequestMonitor<IExecutionDMContext[]> rm);
    
    /**
     * Returns contexts for all groups that contains the specified execution context.
     * 
     *   @param wantedCtx The execution context that is looked-for
     *   @param recurse whether to search deeper than the first group level
     *   @param rm The request monitor
     *   @return array of group contexts, containing the given execution context
     */
    void getGroups(IExecutionDMContext wantedCtx, boolean recurse, DataRequestMonitor<IGroupDMContext[]> rm);
    
    /** 
     * Returns whether grouping is active (i.e. at least one group exists)
     */
	void isGroupingEnabled(DataRequestMonitor<Boolean> rm);
}
