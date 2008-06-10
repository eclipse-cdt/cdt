/*******************************************************************************
 * Copyright (c) 2008  Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ericsson - Initial API and implementation
 *******************************************************************************/
package org.eclipse.dd.mi.service.command.commands;

import org.eclipse.dd.mi.service.command.MIControlDMContext;

/**
 * 
 *     -gdb-set sysroot PATH
 * 
 */
public class MIGDBSetSysroot extends MIGDBSet 
{
	public MIGDBSetSysroot(MIControlDMContext ctx, String path) {
		super(ctx, new String[] {"sysroot", path});//$NON-NLS-1$
	}
	
	// Using /dev/null is the recommended way to disable sysroot
	public MIGDBSetSysroot(MIControlDMContext ctx) {
		this(ctx, "/dev/null"); //$NON-NLS-1$ 
	}
}
