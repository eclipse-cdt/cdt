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
import java.util.Arrays;
import java.util.List;

/**
 * GDB/MI thread list parsing.
~"\n"
~"     2 Thread 2049 (LWP 29354)  "
~"* 1 Thread 1024 (LWP 29353)  "

 */
public class MIInfoThreadsInfo extends MIInfo {

	protected int[] threadIds;
	protected int currentThreadId;

	public MIInfoThreadsInfo(MIOutput out) {
		super(out);
		parse();
	}

	public int[] getThreadIds() {
		return threadIds;
	}

	public String[] getThreadNames() {
		return null;
	}

	public int getCurrentThread() {
		return currentThreadId;
	}

	protected void parse() {
		List aList = new ArrayList();
		if (isDone()) {
			MIOutput out = getMIOutput();
			MIOOBRecord[] oobs = out.getMIOOBRecords();
			for (int i = 0; i < oobs.length; i++) {
				if (oobs[i] instanceof MIConsoleStreamOutput) {
					MIStreamRecord cons = (MIStreamRecord) oobs[i];
					String str = cons.getString();
					// We are interested in finding the current thread
					parseThreadInfo(str.trim(), aList);
				}
			}
		}
		threadIds = new int[aList.size()];
		for (int i = 0; i < aList.size(); i++) {
			threadIds[i] = ((Integer) aList.get(i)).intValue();
		}
		Arrays.sort(threadIds);
	}

	protected void parseThreadInfo(String str, List aList) {
		if (str.length() > 0) {
			boolean isCurrentThread = false;
			// Discover the current thread
			if (str.charAt(0) == '*') {
				isCurrentThread = true;
				str = str.substring(1).trim();
			}
			// Fetch the threadId
			if (str.length() > 0 && Character.isDigit(str.charAt(0))) {
				int i = 1;
				while (i < str.length() && Character.isDigit(str.charAt(i))) {
					i++;
				}
				String number = str.substring(0, i);
				try {
					Integer num = Integer.valueOf(number);
					aList.add(num);
					if (isCurrentThread) {
						currentThreadId = num.intValue();
					}
				} catch (NumberFormatException e) {
				}
			}
		}
	}
}
