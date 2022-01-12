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
 * -trace-start
 *
 * Starts a tracing experiment. The result of this command does not have any fields.
 *
 * Available with GDB 7.1
 *
 * @since 3.0
 */
public class MITraceStart extends MICommand<MIInfo> {
	public MITraceStart(ITraceTargetDMContext ctx) {
		super(ctx, "-trace-start"); //$NON-NLS-1$
	}
}
