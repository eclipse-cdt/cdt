/*******************************************************************************
 * Copyright (c) 2007, 2015 Wind River Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Ericsson 		  - Modified for additional features in DSF Reference implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.debug.service.command;

import org.eclipse.cdt.dsf.datamodel.IDMContext;

/**
 * Command interface for creating and manipulating GDB/MI commands
 * for the DSF GDB reference implementation. The command represents
 * the GDB/MI request which will be put on the wire to the GDB
 * backend.
 *
 * @since 1.0
 */

public interface ICommand<V extends ICommandResult> {
	/**
	 * Takes the supplied command and coalesces it with this one.
	 * The result is a new third command which represent the two
	 * original commands.
	 * <br>Note: the result type associated with the returned command may be
	 * different than the result type associated with either of the commands
	 * being coalesced.
	 *
	 * @return newly created command, or null if command cannot be coalesced
	 */
	public ICommand<? extends ICommandResult> coalesceWith(ICommand<? extends ICommandResult> command);

	/**
	 * Returns the context that this command is to be evaluated in.  May be null
	 * if the command does not need to be evaluated in a specific context.
	 */
	public IDMContext getContext();
}
