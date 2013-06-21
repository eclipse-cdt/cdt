/*******************************************************************************
 * Copyright (c) 2013 Ericsson and others.
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
 * @since 2.5
 */
public interface IRegisters2 extends IRegisters {
	/**
	 * Get the register names for the given register context array i.e. not complete RegisterDMData
	 * 
	 * @param regDmcArray
	 *            - resolving the individual register names on these contexts
	 * @param rm
	 *            - monitor encapsulating the corresponding register names
	 */
	public void getRegisterNames(IRegisterDMContext[] regDmcArray, final DataRequestMonitor<IRegisterDMData[]> rm);

	/**
	 * Returns a boolean indicating if it is allowed to add a new register group with the given selected context
	 * @param selectionContext
	 * @param rm
	 */
	public void canAddRegisterGroup(final IDMContext selectionContext, final DataRequestMonitor<Boolean> rm);
	
	/**
	 * Add a Register group referencing the given registers
	 * 
	 * @param name
	 * @param registers
	 * @param rm
	 */
	public void addRegisterGroup(final String name, final IRegisterDMContext[] registers, RequestMonitor rm);

	/**
	 * Returns a boolean indicating if it is allowed to edit a register group with the given selected context
	 * @param selectionContext
	 * @param rm
	 */
	public void canEditRegisterGroup(final IDMContext selectionContext, final DataRequestMonitor<Boolean> rm);
	
	/**
	 * Edit the given register group and update its name and associated registers
	 * 
	 * @param group
	 * @param groupName
	 * @param registers
	 * @param rm
	 */
	public void editRegisterGroup(final IRegisterGroupDMContext group, String groupName,
			final IRegisterDMContext[] registers, RequestMonitor rm);

	/**
	 * Remove the given register groups
	 * 
	 * @param groups
	 * @param rm
	 */
	public void removeRegisterGroups(final IRegisterGroupDMContext[] groups, RequestMonitor rm);

	/**
	 * Remove all the user defined register groups and refresh the default one
	 * 
	 * @param rm
	 */
	public void restoreDefaultGroups(final RequestMonitor rm);

	/**
	 * Propose an unused Register group name e.g. needed during the creation of a new group
	 * 
	 * @param rm
	 */
	public void proposeGroupName(final DataRequestMonitor<String> rm);
}
