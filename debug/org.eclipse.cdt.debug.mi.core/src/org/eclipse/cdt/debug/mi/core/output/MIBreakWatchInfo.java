/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

package org.eclipse.cdt.debug.mi.core.output;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.debug.mi.core.command.MIBreakInsert;


/**
 * -break-watch buf
 * ^done,wpt={number="2",exp="buf"}
 */
public class MIBreakWatchInfo extends MIInfo {

	MIBreakPoint[] watchpoints = null;

	public MIBreakWatchInfo(MIOutput rr) {
		super(rr);
	}

	public MIBreakPoint[] getBreakpoints () {
		if (watchpoints == null) {
			parse();
		}
		return watchpoints;
	}


	void parse() {
		List aList = new ArrayList(1);
		if (isDone()) {
			MIOutput out = getMIOutput();
			MIResultRecord rr = out.getMIResultRecord();
			if (rr != null) {
				MIResult[] results =  rr.getMIResults();
				for (int i = 0; i < results.length; i++) {
					String var = results[i].getVariable();
					if (var.equals("wpt")) {
						MIValue val = results[i].getMIValue();
						if (val instanceof MITuple) {
							aList.add(new MIBreakPoint((MITuple)val));
						}
					}
				}
			}
		}
		watchpoints = (MIBreakPoint[])aList.toArray(new MIBreakPoint[aList.size()]);
	}
}
