/*******************************************************************************
 * Copyright (c) 2012  Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Marc Khouzam (Ericsson) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;

/**
 * remote get targetfile hostfile
 *    Copy file targetfile from the target system to hostfile on the host system.
 * @since 4.1
 */
public class CLIRemoteGet extends CLIRemote {
	public CLIRemoteGet(ICommandControlDMContext ctx, String targetfile, String hostfile) {
		super(ctx, new String[] { "get", targetfile, hostfile }); //$NON-NLS-1$
	}
}