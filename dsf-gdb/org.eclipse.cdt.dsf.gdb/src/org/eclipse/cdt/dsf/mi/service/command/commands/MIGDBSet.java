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

import java.util.function.Function;

import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;

/**
 *
 *     -gdb-set
 *
 */
public class MIGDBSet extends MICommand<MIInfo> {
	public MIGDBSet(IDMContext ctx, String[] params) {
		super(ctx, "-gdb-set", null, params); //$NON-NLS-1$
	}

	/**
	 * @since 5.2
	 */
	public MIGDBSet(IDMContext ctx, String[] params, Function<String, Adjustable> paramToAdjustable) {
		super(ctx, "-gdb-set", null, params, paramToAdjustable); //$NON-NLS-1$
	}
}