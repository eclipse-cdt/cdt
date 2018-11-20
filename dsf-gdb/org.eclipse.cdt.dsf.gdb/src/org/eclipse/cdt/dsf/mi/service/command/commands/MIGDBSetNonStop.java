/*******************************************************************************
 * Copyright (c) 2008, 2009 Ericsson and others.
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
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;

/**
 *
 *     -gdb-set non-stop [on | off]
 * @since 1.1
 *
 */
public class MIGDBSetNonStop extends MIGDBSet {
	public MIGDBSetNonStop(ICommandControlDMContext ctx, boolean isSet) {
		super(ctx, new String[] { "non-stop", isSet ? "on" : "off" });//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
	}
}