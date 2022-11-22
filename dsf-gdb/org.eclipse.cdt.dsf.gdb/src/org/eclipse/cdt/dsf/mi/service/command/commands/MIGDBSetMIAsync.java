/*******************************************************************************
 * Copyright (c) 2009, 2022 Ericsson and others.
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
 * -gdb-set mi-async [on | off]
 *
 * @since 7.0
 */
public class MIGDBSetMIAsync extends MIGDBSet {
	public MIGDBSetMIAsync(ICommandControlDMContext ctx, boolean isSet) {
		super(ctx, new String[] { "mi-async", isSet ? "on" : "off" });//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
	}
}
