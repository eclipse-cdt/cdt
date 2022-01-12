/*******************************************************************************
 * Copyright (c) 2010 QNX Software Systems, Freescale Semiconductor and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Freescale Semiconductor, QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.CLIInfoProgramInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOutput;

/**
 * @since 3.0
 */
public class CLIInfoProgram extends CLICommand<CLIInfoProgramInfo> {

	public CLIInfoProgram(IContainerDMContext ctx) {
		super(ctx, "info program"); //$NON-NLS-1$
	}

	@Override
	public CLIInfoProgramInfo getResult(MIOutput output) {
		return (CLIInfoProgramInfo) getMIInfo(output);
	}

	public MIInfo getMIInfo(MIOutput out) {
		MIInfo info = null;
		if (out != null) {
			info = new CLIInfoProgramInfo(out);
		}
		return info;
	}
}
