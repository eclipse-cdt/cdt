/*******************************************************************************
 * Copyright (c) 2014 Mentor Graphics and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Mentor Graphics - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.breakpoints;

import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointDMContext;

/**
 * Clients that want to modify breakpoint's presentation when a breakpoint is installed 
 * or uninstalled on the target can implement this interface and contribute the implementation 
 * using the "org.eclipse.cdt.dsf.gdb.breakpointUpdater" extension point.
 * 
 * @since 4.4
 */
public interface IBreakpointUpdater {

	/**
	 * Callback interface that allows implementations to get the list of 
	 * breakpoints that require an update for the given context.
	 */
	public interface IBreakpointProvider {

		public IBreakpointDMContext[] getBreakpointsToUpdate(IDMContext dmc);
	}

	/**
	 * This method is called when the updater is created.
	 */
	public void initialize(IBreakpointProvider provider);
	
	/**
	 * This method is called when the updater is shut down.
	 */
	public void shutdown();
	
	/**
	 * This method is called when an update for the given breakpoints is required.
	 */
	public void updateBreakpoints(IBreakpointDMContext[] bpDmcs);
}
