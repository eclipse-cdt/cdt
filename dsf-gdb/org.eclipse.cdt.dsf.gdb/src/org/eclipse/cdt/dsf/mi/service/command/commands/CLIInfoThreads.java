/*******************************************************************************
 * Copyright (c) 2000, 2009 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Ericsson			    - Modified for new DSF Reference Implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.CLIInfoThreadsInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOutput;

/**
 *
 *    info threads
 *
 */
public class CLIInfoThreads extends CLICommand<CLIInfoThreadsInfo> {

	public CLIInfoThreads(IContainerDMContext ctx) {
		super(ctx, "info threads"); //$NON-NLS-1$
	}

	@Override
	public CLIInfoThreadsInfo getResult(MIOutput output) {
		return (CLIInfoThreadsInfo) getMIInfo(output);
	}

	public MIInfo getMIInfo(MIOutput out) {
		MIInfo info = null;
		if (out != null) {
			info = new CLIInfoThreadsInfo(out);
		}
		return info;
	}
}
