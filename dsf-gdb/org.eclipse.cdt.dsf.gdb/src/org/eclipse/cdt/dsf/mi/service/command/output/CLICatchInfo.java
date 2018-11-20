/*******************************************************************************
 * Copyright (c) 2010 Freescale Semiconductor and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Freescale Semiconductor - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.output;

/**
 * Processes the result of a gdb 'catch' command. Even though the command has
 * been around since gdb 6.6, it's still not supported in gdb 7.0 MI.
 *
 * @since 3.0
 */
public class CLICatchInfo extends MIInfo {
	private MIBreakpoint fMiBreakpoint;

	/**
	 * Constructor
	 * @param record the result object for the command
	 */
	public CLICatchInfo(MIOutput record) {
		super(record);
		assert record != null;
		parse();
	}

	/**
	 * sample output: Catchpoint 3 (catch)
	 */
	protected void parse() {
		if (isDone()) {
			MIOutput out = getMIOutput();
			for (MIOOBRecord oob : out.getMIOOBRecords()) {
				if (oob instanceof MIConsoleStreamOutput) {
					// We are interested in the catchpoint info
					fMiBreakpoint = parseCatchpoint(((MIConsoleStreamOutput) oob).getString().trim());
					if (fMiBreakpoint != null) {
						return;
					}
				}
			}
		}
	}

	private MIBreakpoint parseCatchpoint(String str) {
		return str.startsWith("Catchpoint ") ? createMIBreakpoint(str) : null; //$NON-NLS-1$
	}

	/**
	 * Create a target specific MIBreakpoint
	 *
	 * @param value
	 *            tuple suitable for passing to MIBreakpoint constructor
	 * @return new breakpoint
	 * @since 5.3
	 */
	protected MIBreakpoint createMIBreakpoint(String str) {
		return new MIBreakpoint(str);
	}

	/**
	 * Return an MIBreakpoint object for the catchpoint that was created.
	 *
	 * @return an MIBreakpoint object or null if the command result had
	 *         unexpected data
	 */
	public MIBreakpoint getMIBreakpoint() {
		return fMiBreakpoint;
	}
}
