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

import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;

/**
 * jump LINESPEC
 * jump LOCATION
 *          Resume execution at line LINESPEC or at address given by LOCATION.
 *
 * @since 3.0
 */
public class CLIJump extends CLICommand<MIInfo> {

	public CLIJump(IExecutionDMContext ctx, String location) {
		super(ctx, "jump " + location); //$NON-NLS-1$
	}
}
