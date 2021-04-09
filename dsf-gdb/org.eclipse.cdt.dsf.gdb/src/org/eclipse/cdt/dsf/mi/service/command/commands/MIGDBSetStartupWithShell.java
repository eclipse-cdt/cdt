/*******************************************************************************
 * Copyright (c) 2021 Kichwa Coders Canada Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;

/**
 *
 * -gdb-set startup-with-shell [on | off]
 *
 * Available with GDB 7.7 in GDB and 8.1 for gdbserver
 *
 * @since 6.3
 *
 */
public class MIGDBSetStartupWithShell extends MIGDBSet {
	public MIGDBSetStartupWithShell(ICommandControlDMContext ctx, boolean enable) {
		super(ctx, new String[] { "startup-with-shell", enable ? "on" : "off" });//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
	}
}