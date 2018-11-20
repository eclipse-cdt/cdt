/*******************************************************************************
 * Copyright (c) 2014 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Marc Khouzam (Ericsson) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.MIGDBVersionInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOutput;

/**
 *
 *     -gdb-version
 *
 * @since 4.6
 *
 */
public class MIGDBVersion extends MICommand<MIGDBVersionInfo> {
	private static final String COMMAND = "-gdb-version"; //$NON-NLS-1$

	public MIGDBVersion(ICommandControlDMContext ctx) {
		super(ctx, COMMAND);
	}

	@Override
	public MIInfo getResult(MIOutput out) {
		return new MIGDBVersionInfo(out);
	}
}
