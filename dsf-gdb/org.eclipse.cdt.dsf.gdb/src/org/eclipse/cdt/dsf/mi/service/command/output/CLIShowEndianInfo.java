/*******************************************************************************
 * Copyright (c) 2013 Mentor Graphics and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Mentor Graphics - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.output;

/**
 * 'show endian' returns the endianness of the current target.
 *
 * sample output:
 *
 * (gdb) show endian
 * The target endianness is set automatically (currently little endian)
 *
 * @since 4.2
 */
public class CLIShowEndianInfo extends MIInfo {

	final private static String BIG_ENDIAN = "big endian"; //$NON-NLS-1$

	private boolean fIsBigEndian = false;

	public CLIShowEndianInfo(MIOutput record) {
		super(record);
		parse();
	}

	protected void parse() {
		if (isDone()) {
			MIOutput out = getMIOutput();
			for (MIOOBRecord oob : out.getMIOOBRecords()) {
				if (oob instanceof MIConsoleStreamOutput) {
					String line = ((MIConsoleStreamOutput) oob).getString().trim();
					if (line.indexOf(BIG_ENDIAN) >= 0) {
						fIsBigEndian = true;
						break;
					}
				}
			}
		}
	}

	public boolean isBigEndian() {
		return fIsBigEndian;
	}
}
