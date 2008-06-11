/*******************************************************************************
 * Copyright (c) 2008 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.mi.core.output;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class CLICatchInfo extends MIInfo {
	MIBreakpoint[] breakpoints;

	public CLICatchInfo(MIOutput record) {
		super(record);
		parse();
	}

	/**
	 * sample output: Catchpoint 3 (catch)
	 */
	protected void parse() {
		List aList = new ArrayList();
		try {
			if (isDone()) {
				MIOutput out = getMIOutput();
				MIOOBRecord[] oobs = out.getMIOOBRecords();
				for (int i = 0; i < oobs.length; i++) {
					if (oobs[i] instanceof MIConsoleStreamOutput) {
						MIStreamRecord cons = (MIStreamRecord) oobs[i];
						String str = cons.getString();
						// We are interested in the signal info
						if (parseCatchpoint(str.trim(), aList))
							break;
					}
				}
			}
		} finally {
			breakpoints = (MIBreakpoint[]) aList.toArray(new MIBreakpoint[aList.size()]);
		}
	}

	private boolean parseCatchpoint(String str, List aList) {
		if (str.length() == 0)
			return false;
		if (str.startsWith("Catchpoint ")) { //$NON-NLS-1$ 
			int bn = 0;

			StringTokenizer tokenizer = new StringTokenizer(str);
			for (int i = 0; tokenizer.hasMoreTokens(); i++) {
				String sub = tokenizer.nextToken();
				switch (i) {
				case 0: // first column is "Signal"

					break;
				case 1: // second column is number
					bn = Integer.parseInt(sub);
					break;
				}
			}
			MITuple tuple = new MITuple();
			MIBreakpoint m = new MIBreakpoint(tuple);
			m.setNumber(bn);
			aList.add(m);
			return true;
		}
		return false;
	}

	public MIBreakpoint[] getMIBreakpoints() {
		if (breakpoints == null) {
			parse();
		}
		return breakpoints;
	}
}
