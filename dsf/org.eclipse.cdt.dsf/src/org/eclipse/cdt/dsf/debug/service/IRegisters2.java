/*******************************************************************************
 * Copyright (c) 2014 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alvaro Sanchez-Leon (Ericsson) - First API (Bug 235747)
 *******************************************************************************/

package org.eclipse.cdt.dsf.debug.service;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.IDMContext;

/**
 * Service extension of IRegisters to manage user defined Register Groups
 * @since 2.6
 */
public interface IRegisters2 extends IRegisters {
	/**
	 * Returns a boolean indicating if it is allowed to add a new register group with the given selected context
	 *
	 * @param rm - monitor encapsulating the response
	 */
	public void canAddRegisterGroup(IDMContext ctx, DataRequestMonitor<Boolean> rm);

	/**
	 * Add a Register group referencing the given registers
	 *
	 * @param ctx - A context containing a parent group context e.g. IContainerDMContext
	 * @param name - register group name
	 * @param registers - registers part of this new group
	 * @param rm - request monitor
	 */
	public void addRegisterGroup(IDMContext ctx, String name, IRegisterDMContext[] registers, RequestMonitor rm);

	/**
	 * Returns a boolean indicating if it is allowed to edit the given group
	 *
	 * @param rm - monitor encapsulating the response
	 */
	public void canEditRegisterGroup(IRegisterGroupDMContext group, DataRequestMonitor<Boolean> rm);

	/**
	 * Edit the given register group and update its name and associated registers
	 *
	 * @param group - group to be edited
	 * @param groupName - new group name or null if name is not to be changed
	 * @param registers - new list of registers for this group or null if the list of registers is not be changed
	 * @param rm - request monitor
	 */
	public void editRegisterGroup(IRegisterGroupDMContext group, String groupName, IRegisterDMContext[] registers,
			RequestMonitor rm);

	/**
	 * Returns a boolean indicating if it is allowed to remove the given registers groups
	 * @param groups - list of register group contexts to be removed
	 * @param rm
	 */
	public void canRemoveRegisterGroups(IRegisterGroupDMContext[] groups, DataRequestMonitor<Boolean> rm);

	/**
	 * Remove the given register groups
	 *
	 * @param groups - groups that shall be removed
	 * @param rm - request monitor
	 */
	public void removeRegisterGroups(IRegisterGroupDMContext[] groups, RequestMonitor rm);

	/**
	 * Returns a boolean indicating if it is allowed to restore to the default groups
	 * @param ctx
	 * @param rm
	 */
	public void canRestoreDefaultGroups(IDMContext ctx, DataRequestMonitor<Boolean> rm);

	/**
	 * Remove all the user defined register groups and restore the default ones to their
	 * original state.
	 *
	 * @param rm - request monitor
	 */
	public void restoreDefaultGroups(IDMContext selectionContext, RequestMonitor rm);

}
