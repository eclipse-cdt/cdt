/*******************************************************************************
 * Copyright (c) 2000, 2009 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation'
 *     Ericsson             - Updated for DSF
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;

/**
 *      -environment-cd PATHDIR
 *
 *   Set GDB's working directory.
 * @since 1.1
 *
 */
public class MIEnvironmentCD extends MICommand<MIInfo> {
	public MIEnvironmentCD(ICommandControlDMContext ctx, String path) {
		super(ctx, "-environment-cd", new String[] { path }); //$NON-NLS-1$
	}
}