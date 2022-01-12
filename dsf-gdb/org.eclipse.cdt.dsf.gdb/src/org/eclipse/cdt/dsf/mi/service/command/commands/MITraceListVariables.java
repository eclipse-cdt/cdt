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
import org.eclipse.cdt.dsf.mi.service.command.output.MIOutput;
import org.eclipse.cdt.dsf.mi.service.command.output.MITraceListVariablesInfo;

/**
 * -trace-list-variables
 *
 * Return a table of all defined trace variables. Each element of the table has the
 * following fields:
 *   'name'    The name of the trace variable. This field is always present.
 *   'initial' The initial value. This is a 64-bit signed integer. This field is always present.
 *   'current' The value the trace variable has at the moment. This is a 64-bit signed integer.
 *             This field may is absent if the current value is not defined, for example if
 *             the trace was never run, or is presently running.
 *
 * Available with GDB 7.1
 *
 * @since 3.0
 */
public class MITraceListVariables extends MICommand<MITraceListVariablesInfo> {

	public MITraceListVariables(ITraceTargetDMContext ctx) {
		super(ctx, "-trace-list-variables"); //$NON-NLS-1$
	}

	@Override
	public MITraceListVariablesInfo getResult(MIOutput out) {
		return new MITraceListVariablesInfo(out);
	}
}
