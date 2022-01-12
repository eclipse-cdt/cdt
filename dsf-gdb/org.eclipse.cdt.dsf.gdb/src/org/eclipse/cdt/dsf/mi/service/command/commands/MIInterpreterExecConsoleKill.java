/*******************************************************************************
 * Copyright (c) 2011 Ericsson and others.
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

import org.eclipse.cdt.dsf.mi.service.IMIContainerDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;

/**
 * MIInterpreterExecConsoleKill
 * Send the CLI Kill command for a specific process.
 * @since 4.0
 */
public class MIInterpreterExecConsoleKill extends MIInterpreterExecConsole<MIInfo> {

	public MIInterpreterExecConsoleKill(IMIContainerDMContext ctx) {
		super(ctx, "kill"); //$NON-NLS-1$
	}
}
