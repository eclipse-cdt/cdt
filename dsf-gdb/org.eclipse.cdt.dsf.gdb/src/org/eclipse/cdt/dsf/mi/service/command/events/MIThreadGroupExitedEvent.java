/*******************************************************************************
 * Copyright (c) 2008, 2015 Ericsson and others.
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
 *     Marc Khouzam (Ericsson) - Display exit code in process console (Bug 402054)
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.events;

import org.eclipse.cdt.dsf.concurrent.Immutable;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IProcessDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.MIConst;
import org.eclipse.cdt.dsf.mi.service.command.output.MIResult;
import org.eclipse.cdt.dsf.mi.service.command.output.MIValue;

/**
 * This can only be detected by gdb/mi starting with GDB 7.0.
 * @since 1.1
 */
@Immutable
public class MIThreadGroupExitedEvent extends MIEvent<IProcessDMContext> {

	private String fGroupId;
	private String fExitCode;

	/** @since 4.2 */
	public MIThreadGroupExitedEvent(IProcessDMContext ctx, int token, MIResult[] results) {
		super(ctx, token, results);
		parse();
	}

	public String getGroupId() {
		return fGroupId;
	}

	/**
	 * Returns the exit code of the process or null if there is no exit code.
	 * Note that this information is only available with GDB 7.3;
	 * null will be returned for older GDB versions.
	 *
	 * @since 4.2
	 */
	public String getExitCode() {
		return fExitCode;
	}

	private void parse() {
		MIResult[] results = getResults();
		if (results == null)
			return;

		for (int i = 0; i < results.length; i++) {
			String var = results[i].getVariable();
			MIValue val = results[i].getMIValue();
			if (var.equals("id")) { //$NON-NLS-1$
				if (val instanceof MIConst) {
					fGroupId = ((MIConst) val).getString().trim();
				}
			} else if (var.equals("exit-code")) { //$NON-NLS-1$
				// Available starting with GDB 7.3.
				// Only present when the process properly exited
				if (val instanceof MIConst) {
					fExitCode = ((MIConst) val).getString().trim();
				}
			}
		}
	}
}
