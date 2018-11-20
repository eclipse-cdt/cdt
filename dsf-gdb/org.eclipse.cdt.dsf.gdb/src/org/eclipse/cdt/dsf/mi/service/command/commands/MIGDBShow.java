/*******************************************************************************
 * Copyright (c) 2013  AdaCore and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Philippe Gil (AdaCore) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;

/**
 *
 *     -gdb-show
 *
 * @since 4.3
 *
 */
public class MIGDBShow<V extends MIInfo> extends MICommand<V> {
	public MIGDBShow(IDMContext ctx, String[] params) {
		super(ctx, "-gdb-show", null, params); //$NON-NLS-1$
	}
}