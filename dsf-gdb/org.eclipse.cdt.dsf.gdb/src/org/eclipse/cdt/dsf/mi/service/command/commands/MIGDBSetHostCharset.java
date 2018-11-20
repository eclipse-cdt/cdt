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
 * -gdb-set host-charset CHARSET
 *
 * Sets the current host charset to CHARSET. The host charset is the charset used by gdb.
 *
 * @since 4.1
 */
public class MIGDBSetHostCharset extends MIGDBSet {
	public MIGDBSetHostCharset(ICommandControlDMContext ctx, String hostCharset) {
		super(ctx, new String[] { "host-charset", hostCharset }); //$NON-NLS-1$
	}
}
