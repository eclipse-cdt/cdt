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
import org.eclipse.cdt.dsf.mi.service.command.output.MITraceStopInfo;

/**
 * -trace-stop
 *
 * Stops a tracing experiment. The result of this command has the same fields
 * as -trace-status, except that the 'supported' and 'running' fields are not output.
 *
 * Available with GDB 7.1
 *
 * @since 3.0
 */
public class MITraceStop extends MICommand<MITraceStopInfo> {
	public MITraceStop(ITraceTargetDMContext ctx) {
		super(ctx, "-trace-stop"); //$NON-NLS-1$
	}

	@Override
	public MITraceStopInfo getResult(MIOutput out) {
		return new MITraceStopInfo(out);
	}
}
