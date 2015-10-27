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
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.datamodel.IDMData;
import org.eclipse.cdt.dsf.datamodel.IDMEvent;
import org.eclipse.cdt.dsf.debug.internal.provisional.service.IExecutionContextTranslator;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;

/**
 * @since 4.9
 */
public interface IGDBGrouping extends IExecutionContextTranslator {

	/**
	 * Interface describing a group to differentiate it from a process.
	 * We should use IContainerDMContext for groups instead, and have a new
	 * interface for processes instead of using IContainer for processes.
	 * We'll do that eventually, but it requires a lot more changes than this approach.
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
    interface IGroupChangedDMContext extends IDMContext {}
	
    /** 
     * An event that the layout of the debug view needs to change. 
     * A user group has been added.   
     */
    interface IGroupAddedEvent extends IDMEvent<IGroupChangedDMContext> {}
    
    /** 
     * An event that the layout of the debug view needs to change. 
     * A user group has been removed.   
     */
    interface IGroupDeletedEvent extends IDMEvent<IGroupChangedDMContext> {}
    
    /** 
     * An event that the layout of the debug view needs to change. 
     * A user group has been modified.   
     */
    interface IGroupModifiedEvent extends IDMEvent<IGroupChangedDMContext> {}
    
	
    void getExecutionData(IGroupDMContext group, DataRequestMonitor<IGroupDMData> rm);
    
    /**
     * Returns the execution contexts for the given container after the translation.
     * 
     * @param containerDmc The container for which the execution contexts will be returned.
     *                     If null, the top-level execution contexts will be returned.
     * @return array of execution contexts belonging to the given container context.
     */
    void getExecutionContexts(IContainerDMContext containerDmc, DataRequestMonitor<IExecutionDMContext[]> rm);
    
    /**
     * Returns all group contexts that directly contain the execution context. 
     * 
     *   @paran execCtx
     *   @return array of group contexts, having the given execution context as direct child
     */
    void getGroupsForExecutionContext(IExecutionDMContext execCtx, DataRequestMonitor<IGroupDMContext[]> rm);
}
