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

/**
 * GDB/MI thread list parsing.
 */
public class MIThreadListIdsInfo extends MIInfo {

	int[] threadIds;

	public MIThreadListIdsInfo(MIOutput out) {
		super(out);
	}

	public int[] getThreadIds() {
		if (threadIds == null) {
			parse();
		}
		return threadIds;
	}

	void parse() {
		if (isDone()) {
			MIOutput out = getMIOutput();
			MIResultRecord rr = out.getMIResultRecord();
			if (rr != null) {
				MIResult[] results =  rr.getMIResults();
				for (int i = 0; i < results.length; i++) {
					String var = results[i].getVariable();
					if (var.equals("thread-ids")) { //$NON-NLS-1$
						MIValue val = results[i].getMIValue();
						if (val instanceof MITuple) {
							parseThreadIds((MITuple)val);
						}
					}
				}
			}
		}
		if (threadIds == null) {
			threadIds = new int[0];
		}
	}

	void parseThreadIds(MITuple tuple) {
		MIResult[] results = tuple.getMIResults();
		threadIds = new int[results.length];
		for (int i = 0; i < results.length; i++) {
			String var = results[i].getVariable();
			if (var.equals("thread-id")) { //$NON-NLS-1$
				MIValue value = results[i].getMIValue();
				if (value instanceof MIConst) {
					String str = ((MIConst)value).getCString();
					try {
						threadIds[i] = Integer.parseInt(str.trim());
					} catch (NumberFormatException e) {
					}
				}
			}
		}
	}
}
