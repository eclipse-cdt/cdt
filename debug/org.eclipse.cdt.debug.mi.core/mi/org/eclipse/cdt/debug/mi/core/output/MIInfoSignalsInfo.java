/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.mi.core.output;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * GDB/MI signal table parsing.
 info signals
 &"info signals\n"
 ~"Signal        Stop\tPrint\tPass to program\tDescription\n"
 ~"\n"
 ~"SIGHUP        Yes\tYes\tYes\t\tHangup\n"
 ~"SIGINT        Yes\tYes\tNo\t\tInterrupt\n"
 ~"SIGQUIT       Yes\tYes\tYes\t\tQuit\n"
 ~"SIGILL        Yes\tYes\tYes\t\tIllegal instruction\n"
 ~"SIGTRAP       Yes\tYes\tNo\t\tTrace/breakpoint trap\n"
 ~"SIGABRT       Yes\tYes\tYes\t\tAborted\n"
 ~"SIGEMT        Yes\tYes\tYes\t\tEmulation trap\n"
 */
public class MIInfoSignalsInfo extends MIInfo {

	MISigHandle[] signals;

	public MIInfoSignalsInfo(MIOutput out) {
		super(out);
		parse();
	}

	public MISigHandle[] getMISignals() {
		return signals;
	}

	void parse() {
		List aList = new ArrayList();
		if (isDone()) {
			MIOutput out = getMIOutput();
			MIOOBRecord[] oobs = out.getMIOOBRecords();
			for (int i = 0; i < oobs.length; i++) {
				if (oobs[i] instanceof MIConsoleStreamOutput) {
					MIStreamRecord cons = (MIStreamRecord) oobs[i];
					String str = cons.getString();
					// We are interested in the signal info
					parseSignal(str.trim(), aList);
				}
			}
		}
		signals = new MISigHandle[aList.size()];
		for (int i = 0; i < aList.size(); i++) {
			signals[i] = (MISigHandle)aList.get(i);
		}
	}

	void parseSignal(String str, List aList) {
		if (str.length() > 0) {
			// Pass the header and th tailer.
			// ~"Signal        Stop\tPrint\tPass to program\tDescription\n"
			// ~"Use the \"handle\" command to change these tables.\n"
			if (!str.startsWith("Signal ") && !str.startsWith("Use ")) { //$NON-NLS-1$ //$NON-NLS-2$
				String signal = ""; //$NON-NLS-1$
				boolean stop = false;
				boolean print = false;
				boolean pass = false;
				String desc = ""; //$NON-NLS-1$

				StringTokenizer tokenizer = new StringTokenizer(str);
				for (int i = 0; tokenizer.hasMoreTokens(); i++) {
					String sub = null;
					if (i == 4) {
						sub = tokenizer.nextToken("\n"); //$NON-NLS-1$
					} else {
						sub = tokenizer.nextToken();
					}
					switch (i) {
						case 0: // first column is "Signal"
							signal = sub;
						break;
						case 1: // second column is "Stop"
							stop = getBoolean(sub);
						break;
						case 2: // third column is "Print"
							print = getBoolean(sub);
						break;
						case 3: // third column is "Pass to Program"
							pass = getBoolean(sub);
						break;
						case 4: // last column is "Description"
							desc = sub;
						break;
					}
				}
				MISigHandle s = new MISigHandle(signal, stop, print, pass, desc.trim());
				aList.add(s);
			}
		}
	}
	
	static boolean getBoolean(String value) {
		if (value != null && value.equalsIgnoreCase("Yes")) { //$NON-NLS-1$
			return true;
		}
		return false;
	}

}
