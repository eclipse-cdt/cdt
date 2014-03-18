/*******************************************************************************
 * Copyright (c) 2014 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alvaro Sanchez-Leon (Ericsson) - First API (Bug 235747)
 *******************************************************************************/

package org.eclipse.cdt.dsf.debug.service;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.IDMContext;

/**
 * Service extension of IRegistersmanage user defined Register Groups
 * @since 2.5
 */
public interface IRegisters2 extends IRegisters {
	/**
	 * Get the register data for each register context in the give array
	 * 
	 * @param regDmcArray
	 *            - resolving the individual register data on these contexts
	 * @param rm
	 *            - monitor encapsulating the corresponding registers data
	 */
	public void getRegistersData(IRegisterDMContext[] regDmcArray, DataRequestMonitor<IRegisterDMData[]> rm);

	/** 
	 * Provides a list of register group data associated to the given context
	 * @param ctx Context for the returned data.
	 * @param rm Request completion monitor.
	 */
	public void getRegisterGroupsData(IDMContext ctx, DataRequestMonitor<IRegisterGroupDMData[]> rm );

	/**
	 * Returns a boolean indicating if it is allowed to add a new register group with the given selected context
	 * 
	 * @param rm - monitor encapsulating the response
	 */
	public void canAddRegisterGroup(IDMContext selectionContext, DataRequestMonitor<Boolean> rm);

	/**
	 * Add a Register group referencing the given registers
	 * 
	 * @param containerContext - A context containing a parent group context e.g. IContainerDMContext
	 * @param name - register group name
	 * @param registers - registers part of this new group
	 * @param rm - request monitor
	 */
	public void addRegisterGroup(IDMContext containerContext, String name, IRegisterDMContext[] registers, RequestMonitor rm);

	/**
	 * Returns a boolean indicating if it is allowed to edit the given group
	 * 
	 * @param rm - monitor encapsulating the response
	 */
	public void canEditRegisterGroup(IRegisterGroupDMContext group,
			DataRequestMonitor<Boolean> rm);

	/**
	 * Edit the given register group and update its name and associated registers
	 * 
	 * @param group - group to be edited
	 * @param groupName - new group name
	 * @param registers - registers to be set as part of this group
	 * @param rm - request monitor
	 */
	public void editRegisterGroup(IRegisterGroupDMContext group, String groupName,
			IRegisterDMContext[] registers, RequestMonitor rm);

	/**
	 * Returns a boolean indicating if it is allowed to remove the given registers groups
	 * @param groups
	 * @param rm
	 */
	public void canRemoveRegisterGroups(IRegisterGroupDMContext[] groups,
			DataRequestMonitor<Boolean> rm);

	/**
	 * Remove the given register groups
	 * 
	 * @param groups - groups that shall be removed
	 * @param rm - request monitor
	 */
	public void removeRegisterGroups(IRegisterGroupDMContext[] groups, RequestMonitor rm);

	/**
	 * Returns a boolean indicating if it is allowed to restore to the default groups
	 * @param selectionContext
	 * @param rm
	 */
	public void canRestoreDefaultGroups(IDMContext selectionContext, 
			DataRequestMonitor<Boolean> rm);

	/**
	 * Remove all the user defined register groups and refresh the default one
	 * 
	 * @param rm - request monitor
	 */
	public void restoreDefaultGroups(IDMContext selectionContext, RequestMonitor rm);
	
	/**
	 * Resolve the Register Group containing all the current registers reported by the target
	 * 
	 * @param ctx - context including a common groups parent e.g IContainerDMContext
	 * @param rm - request monitor encapsulating the response of the resolved register group
	 */
	public void findTargetRegisterGroup(IDMContext ctx, DataRequestMonitor<IRegisterGroupDMContext> rm);
	
}
