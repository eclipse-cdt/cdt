/*******************************************************************************
 * Copyright (c) 2011 Ericsson
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Ericsson - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.gdb.service.IGDBTraceControl.ITraceRecordDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.CLITraceDumpInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOutput;

/**
 * GDB tdump CLI command.
 * @since 4.0
 *
 */
public class CLITraceDump extends CLICommand<CLITraceDumpInfo> {

	/**
	 * @param ctx trace context
	 */
	public CLITraceDump(ITraceRecordDMContext ctx) {
		super(ctx, "tdump"); //$NON-NLS-1$
	}

	@Override
	public CLITraceDumpInfo getResult(MIOutput out) {
		return new CLITraceDumpInfo(out);
	}
}