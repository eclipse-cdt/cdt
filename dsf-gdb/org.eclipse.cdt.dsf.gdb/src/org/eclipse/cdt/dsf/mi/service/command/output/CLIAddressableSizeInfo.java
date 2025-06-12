/*******************************************************************************
 * Copyright (c) 2014, 2025 Ericsson AB and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alvaro Sanchez-Leon (Ericsson AB) - [Memory] Support 16 bit addressable size (Bug 426730)
 *     John Dallaway - Accommodate LLDB response format (#1191)
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.output;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.ILog;

/**
 * This class takes care of parsing and providing the result of the CLI command
 * <br>"p/x (char)-1"
 *
 * <p>E.g. if the response to 'p/x (char)-1' is</p>
 * $n = 0xffff
 *
 * <p>Then we can easily resolve it to 2 octets (e.g. 2 hex characters per octet)</p>
 * @since 4.4
 */
public class CLIAddressableSizeInfo extends MIInfo {

	private static final Pattern HEX_DIGITS_PATTERN = Pattern.compile("0x([0-9a-fA-F]+)"); //$NON-NLS-1$

	private int fAddressableSize = 1;

	public CLIAddressableSizeInfo(MIOutput record) {
		super(record);
		parse();
	}

	protected void parse() {
		if (isDone()) {
			MIOutput out = getMIOutput();
			for (MIOOBRecord oob : out.getMIOOBRecords()) {
				if (oob instanceof MIConsoleStreamOutput) {
					String line = ((MIConsoleStreamOutput) oob).getString().trim();
					fAddressableSize = hexToOctetCount(line);
				}
			}
		}
	}

	public int getAddressableSize() {
		return fAddressableSize;
	}

	private int hexToOctetCount(String hexString) {
		// Receiving format is expected in hex form e.g. "$n = 0xffff" or "$n = 0xff"
		// which shall result in 2 and 1 octets respectively.
		// Also accommodate "(char) 0xff -1 '\\xff'\n" returned by LLDB-MI.
		Matcher matcher = HEX_DIGITS_PATTERN.matcher(hexString);
		if (matcher.find()) {
			return matcher.group(1).length() / 2;
		}
		ILog.get().error("CLIAddressableSizeInfo response not handled: " + hexString); //$NON-NLS-1$
		return 1;
	}
}
