/*******************************************************************************
 * Copyright (c) 2000, 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
public class MIEnvironmentCD extends MICommand <MIInfo> 
{
	public MIEnvironmentCD(ICommandControlDMContext ctx, String path) {
		super(ctx, "-environment-cd", new String[]{path}); //$NON-NLS-1$
	}
}