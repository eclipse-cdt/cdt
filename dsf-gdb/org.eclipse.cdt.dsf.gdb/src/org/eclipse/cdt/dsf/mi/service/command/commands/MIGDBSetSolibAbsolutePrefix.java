/*******************************************************************************
 * Copyright (c) 2010 Mentor Graphics Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Anna Dushistova (Mentor Graphics) - initial API and implementation
 * Ingenico	- Sysroot with spaces (Bug 497693)
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;

/**
 *
 *     -gdb-set solib-absolute-prefix PATH
 * @since 4.0
 *
 */
public class MIGDBSetSolibAbsolutePrefix extends MIGDBSet {

	public MIGDBSetSolibAbsolutePrefix(ICommandControlDMContext ctx, String prefix) {
		super(ctx, new String[] { "solib-absolute-prefix", prefix }, x -> new MINoChangeAdjustable(x)); //$NON-NLS-1$
	}

}
