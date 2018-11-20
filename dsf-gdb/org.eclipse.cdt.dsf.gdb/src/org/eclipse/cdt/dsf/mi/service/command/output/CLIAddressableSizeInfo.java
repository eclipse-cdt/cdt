/*******************************************************************************
 * Copyright (c) 2014 Ericsson AB and others.
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
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.output;

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
		//Receiving format is expected in hex form e.g. "$n = 0xffff" or "$n = 0xff"
		//which shall result in 2 and 1 octets respectively
		int starts = hexString.indexOf("x"); //$NON-NLS-1$
		assert (starts > 0);
		String hexDigits = hexString.substring(starts + 1);
		assert hexDigits.length() > 1;
		int octets = hexDigits.length() / 2;

		return octets;
	}
}
