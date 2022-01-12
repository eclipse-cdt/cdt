/*******************************************************************************
 * Copyright (c) 2010 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Ericsson - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;

/**
 *
 * unset env [VARNAME]
 *
 * Deletes the environment variable VARNAME for the inferior, not GDB itself.
 * If VARNAME is omitted, all environment variables are deleted.
 *
 * @since 3.0
 */
public class CLIUnsetEnv extends CLICommand<MIInfo> {

	/**
	 * Delete all existing environment variables
	 */
	public CLIUnsetEnv(ICommandControlDMContext ctx) {
		this(ctx, null);
	}

	/**
	 * Delete the environment variable specified by 'name'
	 * If 'name' is null, all variables will be deleted.
	 */
	public CLIUnsetEnv(ICommandControlDMContext ctx, String name) {
		super(ctx, "unset env " + (name != null ? name : "")); //$NON-NLS-1$ //$NON-NLS-2$
	}
}