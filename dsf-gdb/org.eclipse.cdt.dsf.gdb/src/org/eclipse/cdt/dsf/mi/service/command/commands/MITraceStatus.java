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
import org.eclipse.cdt.dsf.mi.service.command.output.MITraceStatusInfo;

/**
 * -trace-status
 *
 * Gets the status of tracing.
 *
 * Available with GDB 7.1
 *
 * @since 3.0
 */
public class MITraceStatus extends MICommand<MITraceStatusInfo> {
	public MITraceStatus(ITraceTargetDMContext ctx) {
		super(ctx, "-trace-status"); //$NON-NLS-1$
	}

	@Override
	public MITraceStatusInfo getResult(MIOutput out) {
		return new MITraceStatusInfo(out);
	}
}
