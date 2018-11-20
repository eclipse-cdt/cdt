/*******************************************************************************
 * Copyright (c) 2009, 2011 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Ericsson - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service;

import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Sequence;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl2;

/**
 * This interface provides methods for RunControl that are not
 * part of the standard DSF IRunControl
 *
 * @since 2.0
 */
public interface IMIRunControl extends IRunControl2 {
	/**
	 * Returns true if the target currently accepting commands.
	 *
	 * @since 4.0
	 */
	public boolean isTargetAcceptingCommands();

	/**
	 * Request that the specified steps be executed by first ensuring the target is available
	 * to receive commands.  Once the specified steps are executed, the target should be
	 * returned to its original availability.
	 *
	 * This is of value for breakpoints commands; e.g., breakpoints need to be inserted
	 * even when the target is running, so this call would suspend the target, insert the
	 * breakpoint, and resume the target again.
	 *
	 * @since 3.0
	 */
	public void executeWithTargetAvailable(IDMContext ctx, Sequence.Step[] stepsToExecute, RequestMonitor rm);

	/**
	 * Generic interface for different possible Run modes such as Non-Stop or All-Stop.
	 * Using this interface allows to extend the list of RunModes.
	 *
	 * @since 4.0
	 */
	public interface IRunMode {
	}

	/**
	 * The run-modes supported by GDB.
	 *
	 * @since 4.0
	 */
	public enum MIRunMode implements IRunMode {
		ALL_STOP, NON_STOP
	}

	/**
	 * Returns the RunMode that is currently being used by this RunControl service.
	 * @returns The current RunMode used by this service.
	 *
	 * @since 4.0
	 */
	public IRunMode getRunMode();
}
