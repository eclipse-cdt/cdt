/*******************************************************************************
 * Copyright (c) 2014 Mentor Graphics and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Mentor Graphics - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.debug.service;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;

/**
 * This interface provides the ability to perform detach on multiple contexts.
 *
 * @since 2.6
 */
public interface IMultiDetach {

	/**
	 * Checks whether it is possible to detach the debugger from at least one
	 * of the specified processes.
	 *
	 * @param dmcs The contexts to detach the debugger from. Each context
	 *             should have {@link IContainerDMContext} as an ancestor.
	 * @param rm Request monitor returning whether there is at least one context
	 *           that can be detached from the debugger.
	 */
	void canDetachDebuggerFromSomeProcesses(IDMContext[] dmcs, DataRequestMonitor<Boolean> rm);

	/**
	 * Checks whether it is possible to detach the debugger from all of the specified processes.
	 *
	 * @param dmc The contexts  to detach the debugger from. Each context
	 *            should have {@link IContainerDMContext} as an ancestor.
	 * @param rm Request monitor returning whether all processes specified by the given contexts
	 *           that can be detached from the debugger.
	 */
	void canDetachDebuggerFromAllProcesses(IDMContext[] dmcs, DataRequestMonitor<Boolean> rm);

	/**
	 * Request to detach debugger from the specified processes. Only contexts
	 * that are in a state that can be detached will be affected, others will be ignored.
	 *
	 * @param dmc The contexts  to detach the debugger from. Each context
	 *            should have {@link IContainerDMContext} as an ancestor.
	 */
	void detachDebuggerFromProcesses(IDMContext[] dmcs, RequestMonitor rm);
}
