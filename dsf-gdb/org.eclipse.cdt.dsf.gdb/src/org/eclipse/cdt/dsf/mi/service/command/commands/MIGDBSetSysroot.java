/*******************************************************************************
 * Copyright (c) 2008, 2016  Ericsson and others.
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
 *     Ingenico	- Sysroot with spaces (Bug 497693)
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;

/**
 *
 *     -gdb-set sysroot PATH
 * @since 1.1
 *
 */
public class MIGDBSetSysroot extends MIGDBSet {
	public MIGDBSetSysroot(ICommandControlDMContext ctx, String path) {
		super(ctx, new String[] { "sysroot", path }, x -> new MINoChangeAdjustable(x));//$NON-NLS-1$
	}

	// Using /dev/null is the recommended way to disable sysroot
	public MIGDBSetSysroot(ICommandControlDMContext ctx) {
		this(ctx, "/dev/null"); //$NON-NLS-1$
	}
}
