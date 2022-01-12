/*******************************************************************************
 * Copyright (c) 2007, 2016 Ericsson and others.
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

import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointsTargetDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;

/**
 *
 *  -break-condition NUMBER EXPR
 *
 *  Breakpoint NUMBER will stop the program only if the condition in
 *  EXPR is true.  The condition becomes part of the `-break-list' output
 *  (see the description of the DsfMIBreakList).
 */

public class MIBreakCondition extends MICommand<MIInfo> {
	/*
	 * MICommand wraps a parameter with double quotes if it contains a space.
	 * However, GDB does not want quotes around a condition.
	 * To avoid the double quotes, we create our own adjustable parameter.
	 * It is important to send the breakpoint and condition as parameters because
	 * MI can insert flags such as --thread-group between the command and the
	 * parameters.  If we make the entire output be the command, then the
	 * --thread-group flag will end up at the end, and the syntax will not be valid.
	 *
	 * See bug 213076 for more information.
	 */
	/** @since 5.0 */
	public MIBreakCondition(IBreakpointsTargetDMContext ctx, String breakpoint, String condition) {
		super(ctx, "-break-condition"); //$NON-NLS-1$

		setParameters(new Adjustable[] { new MIStandardParameterAdjustable(breakpoint),
				new MINoChangeAdjustable(condition) });
	}
}
