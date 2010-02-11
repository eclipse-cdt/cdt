/*******************************************************************************
 * Copyright (c) 2010 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
