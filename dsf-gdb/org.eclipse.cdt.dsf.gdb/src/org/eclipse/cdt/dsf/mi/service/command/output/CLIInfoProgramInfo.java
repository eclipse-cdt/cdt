/*******************************************************************************
 * Copyright (c) 2010 QNX Software Systems, Freescale Semiconductor and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Freescale Semiconductor, QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.output;

import java.util.StringTokenizer;

/**
 * @since 3.0
 */
public class CLIInfoProgramInfo extends MIInfo {

	Long fPid;

	public CLIInfoProgramInfo(MIOutput out) {
		super(out);
		parse();
	}

	/**
	 * Return the inferior's PID reported in the output, or null if we didn't
	 * find it there.
	 */
	public Long getPID() {
		return fPid; 
	}

	void parse() {
		if (isDone()) {
			MIOutput out = getMIOutput();
			MIOOBRecord[] oobs = out.getMIOOBRecords();
			for (MIOOBRecord oob : oobs) {
				if (oob instanceof MIConsoleStreamOutput) {
					parseLine(((MIConsoleStreamOutput)oob).getString());
					
					// quit parsing output once we have everything we want out
					// of it
					if (fPid != null) {
						break;
					}
				}
			}
		}
	}

	void parseLine(String str) {
		// Sample output on Windows:
		// 		[New thread 4960.0x5d8]
		// 			Using the running image of child thread 4960.0x5d8.
		// 		Program stopped at 0x4012f5.
		// 		It stopped at a breakpoint that has since been deleted.

		if (str != null && str.length() > 0) {
			str = str.replace('.', ' ').trim();
			if (str.startsWith("Using")) { //$NON-NLS-1$
				StringTokenizer st = new StringTokenizer(str);
				while (st.hasMoreTokens()) {
					String s = st.nextToken();
					/* Not a process id if LWP is reported */
					if (s.equals("LWP")) break; //$NON-NLS-1$

					if (Character.isDigit(s.charAt(0))) {
						try {
							fPid = Long.decode(s).longValue();
							break;
						} catch (NumberFormatException e) {
						}
					}
				}
			}
		}
	}
}

