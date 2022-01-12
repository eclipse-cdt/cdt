/*******************************************************************************
 * Copyright (c) 2000, 2011 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Ericsson AB			- Modified for DSF Reference Implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.output;

import java.util.Arrays;
import java.util.Comparator;

/**
 * GDB/MI thread list parsing.
 */
public class MIThreadListIdsInfo extends MIInfo {

	private String[] strThreadIds;

	public MIThreadListIdsInfo(MIOutput out) {
		super(out);
	}

	/**
	 * @since 1.1
	 */
	public String[] getStrThreadIds() {
		if (strThreadIds == null) {
			parse();
			// Make sure the threads are in order for the debug view
			// We need our own comparator to treat these strings as integers.
			Arrays.sort(strThreadIds, new Comparator<String>() {
				@Override
				public int compare(String o1, String o2) {
					int threadInt1;
					int threadInt2;

					try {
						threadInt1 = Integer.parseInt(o1);
					} catch (NumberFormatException e) {
						return 1;
					}

					try {
						threadInt2 = Integer.parseInt(o2);
					} catch (NumberFormatException e) {
						return -1;
					}

					return threadInt1 - threadInt2;
				}
			});
		}
		return strThreadIds;
	}

	void parse() {
		if (isDone()) {
			MIOutput out = getMIOutput();
			MIResultRecord rr = out.getMIResultRecord();
			if (rr != null) {
				MIResult[] results = rr.getMIResults();
				for (int i = 0; i < results.length; i++) {
					String var = results[i].getVariable();
					if (var.equals("thread-ids")) { //$NON-NLS-1$
						MIValue val = results[i].getMIValue();
						if (val instanceof MITuple) {
							parseThreadIds((MITuple) val);
						}
					}
				}
			}
		}
		if (strThreadIds == null) {
			strThreadIds = new String[0];
		}
	}

	void parseThreadIds(MITuple tuple) {
		MIResult[] results = tuple.getMIResults();
		strThreadIds = new String[results.length];
		for (int i = 0; i < results.length; i++) {
			String var = results[i].getVariable();
			if (var.equals("thread-id")) { //$NON-NLS-1$
				MIValue value = results[i].getMIValue();
				if (value instanceof MIConst) {
					strThreadIds[i] = ((MIConst) value).getCString().trim();
				}
			}
		}
	}
}
