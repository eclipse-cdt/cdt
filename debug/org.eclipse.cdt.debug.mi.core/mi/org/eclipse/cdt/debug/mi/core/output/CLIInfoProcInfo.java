/*******************************************************************************
 * Copyright (c) 2007 ENEA Software AB and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     ENEA Software AB - CLI command extension - fix for bug 190277
 *******************************************************************************/

package org.eclipse.cdt.debug.mi.core.output;

import java.util.StringTokenizer;


/**
 * GDB/CLI info proc parsing.
(gdb) info proc
process 19127 flags:
PR_STOPPED Process (LWP) is stopped
PR_ISTOP Stopped on an event of interest
PR_RLC Run-on-last-close is in effect
PR_FAULTED : Incurred a traced hardware fault FLTBPT: Breakpoint trap
 */
public class CLIInfoProcInfo extends MIInfo {

	int pid;

	public CLIInfoProcInfo(MIOutput out) {
		super(out);
		parse();
	}

	public int getPID() {
		return pid;
	}

	void parse() {
		if (isDone()) {
			MIOutput out = getMIOutput();
			MIOOBRecord[] oobs = out.getMIOOBRecords();
			for (int i = 0; i < oobs.length; i++) {
				if (oobs[i] instanceof MIConsoleStreamOutput) {
					MIStreamRecord cons = (MIStreamRecord) oobs[i];
					String str = cons.getString();
					// We are interested in the process info and PID
					parseLine(str);
				}
			}
		}
	}

	void parseLine(String str) {
		if (str != null && str.length() > 0) {
			str = str.trim();
			if (!str.startsWith("process")) {  //$NON-NLS-1$
				return;
			}
			StringTokenizer st = new StringTokenizer(str);
			while (st.hasMoreTokens()) {
				String s = st.nextToken();
				if (Character.isDigit(s.charAt(0))) {
					try {
						pid = Integer.decode(s).intValue();
						break;
					} catch (NumberFormatException e) {
					}
				}
			}
		}
	}

}
