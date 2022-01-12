/*******************************************************************************
 * Copyright (c) 2010 Ericsson and others.
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

import org.eclipse.cdt.dsf.gdb.service.IGDBTraceControl.ITraceTargetDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;

/**
 * -trace-define-variable
 *
 * Creates trace variable name if it does not exist. If value is specified,
 * sets the initial value of the specified trace variable to that value.
 * Note that the name should start with the `$' character.
 *
 * Available with GDB 7.1
 *
 * @since 3.0
 */
public class MITraceDefineVariable extends MICommand<MIInfo> {
	public MITraceDefineVariable(ITraceTargetDMContext ctx, String varName) {
		this(ctx, varName, ""); //$NON-NLS-1$
	}

	public MITraceDefineVariable(ITraceTargetDMContext ctx, String varName, String varValue) {
		super(ctx, "-trace-define-variable", new String[] { varName, varValue }); //$NON-NLS-1$
	}

}
