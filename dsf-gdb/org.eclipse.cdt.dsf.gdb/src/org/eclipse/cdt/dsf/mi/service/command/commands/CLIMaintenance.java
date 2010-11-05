/*******************************************************************************
 * Copyright (c) 2010 Verigy and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jens Elmenthaler (Verigy) - Added Full GDB pretty-printing support (bug 302121)
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;

/**
 * Executes "maintenance" command.
 * @since 4.0
 */
public class CLIMaintenance extends CLICommand<MIInfo> {

	public CLIMaintenance(ICommandControlDMContext ctx, String arguments) {
		super(ctx, "maintenance " + arguments); //$NON-NLS-1$
	}
}
