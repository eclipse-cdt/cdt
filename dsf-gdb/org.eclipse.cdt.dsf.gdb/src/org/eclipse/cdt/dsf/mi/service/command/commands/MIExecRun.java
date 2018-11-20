/*******************************************************************************
 * Copyright (c) 2000, 2010 QNX Software Systems and others.
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
 *     Wind River Systems   - Modified for new DSF Reference Implementation
 *     Ericsson				- Modified for handling of execution contexts
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;

/**
 *
 *      -exec-run [ARGS]
 *
 *   Asynchronous command.  Starts execution of the inferior from the
 * beginning.  The inferior executes until either a breakpoint is
 * encountered or the program exits.
 *
 * ARGS will be passed to the inferior.  This option is not documented.
 *
 */
public class MIExecRun extends MICommand<MIInfo> {
	public MIExecRun(IExecutionDMContext dmc) {
		super(dmc, "-exec-run"); //$NON-NLS-1$
	}

	public MIExecRun(IExecutionDMContext dmc, String[] args) {
		super(dmc, "-exec-run", args); //$NON-NLS-1$
	}
}
