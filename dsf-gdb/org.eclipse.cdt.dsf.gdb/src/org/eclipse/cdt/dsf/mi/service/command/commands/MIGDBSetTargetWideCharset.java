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
 * -gdb-set target-wide-charset CHARSET
 *
 * Sets the current target wide charset to CHARSET. The target wide charset is the charset
 * used by the wchar_t type of the inferior program.
 *
 * Available with gdb 7.0
 *
 * @since 4.1
 */
public class MIGDBSetTargetWideCharset extends MIGDBSet {
	public MIGDBSetTargetWideCharset(ICommandControlDMContext ctx, String targetWideCharset) {
		super(ctx, new String[] { "target-wide-charset", targetWideCharset }); //$NON-NLS-1$
	}
}
