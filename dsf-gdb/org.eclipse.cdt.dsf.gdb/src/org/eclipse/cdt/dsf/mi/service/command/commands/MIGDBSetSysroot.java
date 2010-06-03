/*******************************************************************************
 * Copyright (c) 2008, 2009  Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ericsson - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;

/**
 * 
 *     -gdb-set sysroot PATH
 * @since 1.1
 * 
 */
public class MIGDBSetSysroot extends MIGDBSet 
{
	public MIGDBSetSysroot(ICommandControlDMContext ctx, String path) {
		super(ctx, new String[] {"sysroot", path});//$NON-NLS-1$
	}
	
	// Using /dev/null is the recommended way to disable sysroot
	public MIGDBSetSysroot(ICommandControlDMContext ctx) {
		this(ctx, "/dev/null"); //$NON-NLS-1$ 
	}
}
