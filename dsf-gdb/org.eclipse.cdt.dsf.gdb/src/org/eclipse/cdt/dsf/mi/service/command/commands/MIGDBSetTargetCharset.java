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
 * -gdb-set target-charset CHARSET
 *
 * Sets the current target charset to CHARSET. The target charset is the charset used
 * by the char type of the inferior program.
 *
 * @since 4.1
 */
public class MIGDBSetTargetCharset extends MIGDBSet {
	public MIGDBSetTargetCharset(ICommandControlDMContext ctx, String targetCharset) {
		super(ctx, new String[] { "target-charset", targetCharset }); //$NON-NLS-1$
	}
}
