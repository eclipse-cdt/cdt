/*******************************************************************************
 * Copyright (c) 2025 John Dallaway and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     John Dallaway - Initial implementation (#1191)
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.output;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.ILog;

/**
 * Parses the result of the CLI command "p/x sizeof(void*)"
 *
 * @since 7.2
 */
public class CLIAddressSizeInfo extends MIInfo {

	private static final Pattern HEX_LITERAL_PATTERN = Pattern.compile("0x[0-9a-fA-F]+"); //$NON-NLS-1$

	private int fAddressSize = 0;

	public CLIAddressSizeInfo(MIOutput record) {
		super(record);
		parse();
	}

	protected void parse() {
		if (isDone()) {
			MIOutput out = getMIOutput();
			for (MIOOBRecord oob : out.getMIOOBRecords()) {
				if (oob instanceof MIConsoleStreamOutput) {
					String line = ((MIConsoleStreamOutput) oob).getString().trim();
					fAddressSize = hexToValue(line);
				}
			}
		}
	}

	public int getAddressSize() {
		return fAddressSize;
	}

	private int hexToValue(String hexString) {
		// Extract value from responses such as "(unsigned long) 0x0000000000000008\n"
		Matcher matcher = HEX_LITERAL_PATTERN.matcher(hexString);
		if (matcher.find()) {
			return Integer.decode(matcher.group());
		}
		ILog.get().error("CLIAddressSizeInfo response not handled: " + hexString); //$NON-NLS-1$
		return 0;
	}

}
