/*******************************************************************************
 * Copyright (c) 2010 Verigy and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Jens Elmenthaler (Verigy) - Added Full GDB pretty-printing support (bug 302121)
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;

/**
 * -var-set-update-range name from to
 *
 * Set the range of children to be returned by future invocations of
 * -var-update.
 *
 * <code>from</code> and <code>to</code> indicate the range of children to
 * report in subsequent -var-update call. If from or to is less than zero, the
 * range is reset and all children will be reported. Otherwise, children
 * starting at from (zero-based) and up to and excluding to will be reported.
 *
 * @since 4.0
 */
public class MIVarSetUpdateRange extends MICommand<MIInfo> {

	/**
	 * @param ctx
	 * @param name The name of the varobj for which the range is set.
	 * @param from Index of the first child to be updated with future -var-update.
	 * @param to One behind the last child to be updated.
	 */
	public MIVarSetUpdateRange(ICommandControlDMContext ctx, String name, int from, int to) {
		super(ctx, "-var-set-update-range", new String[] { name, String.valueOf(from), String.valueOf(to) }); //$NON-NLS-1$
	}
}
