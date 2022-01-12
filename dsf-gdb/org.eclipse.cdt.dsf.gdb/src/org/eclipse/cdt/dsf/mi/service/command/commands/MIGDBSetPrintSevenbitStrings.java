/*******************************************************************************
 * Copyright (c) 2012 Mathias Kunter and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Mathias Kunter       - Initial API and implementation
*******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;

/**
 *
 * -gdb-set print sevenbit-strings [on | off]
 *
 * When on, gdb displays any eight-bit characters (in strings or character values) using
 * the octal escape notation \nnn. When off, prints full eight-bit characters.
 *
 * @since 4.1
 */
public class MIGDBSetPrintSevenbitStrings extends MIGDBSet {
	public MIGDBSetPrintSevenbitStrings(ICommandControlDMContext ctx, boolean enable) {
		super(ctx, new String[] { "print", "sevenbit-strings", enable ? "on" : "off" }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	}
}
