package org.eclipse.cdt.debug.mi.core.output;

import java.util.ArrayList;
import java.util.List;



/**
 * -break-insert main
 * ^done,bkpt={number="1",type="breakpoint",disp="keep",enabled="y",addr="0x08048468",func="main",file="hello.c",line="4",times="0"}
 */
public class MIBreakInsertInfo extends MIInfo {

	MIBreakPoint[] breakpoints;

	void parse() {
		List aList = new ArrayList(1);
		if (isDone()) {
			MIOutput out = getMIOutput();
			MIResultRecord rr = out.getMIResultRecord();
			if (rr != null) {
				MIResult[] results =  rr.getMIResults();
				for (int i = 0; i < results.length; i++) {
					String var = results[i].getVariable();
					if (var.equals("bkpt")) {
						MIValue val = results[i].getMIValue();
						if (val instanceof MITuple) {
							aList.add(new MIBreakPoint((MITuple)val));
						}
					}
				}
			}
		}
		breakpoints = (MIBreakPoint[])aList.toArray(new MIBreakPoint[aList.size()]);
	}

	public MIBreakInsertInfo(MIOutput record) {
		super(record);
	}

	public MIBreakPoint[] getBreakPoints() {
		if (breakpoints == null) {
			parse();
		}
		return breakpoints;
	}
}
