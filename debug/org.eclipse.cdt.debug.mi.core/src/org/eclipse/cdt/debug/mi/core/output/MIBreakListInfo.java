package org.eclipse.cdt.debug.mi.core.output;

import java.util.ArrayList;
import java.util.List;



/**
 * A -break-list result-record is the form:
 * <pre>
 * ^done,BreakpointTable={nr_rows="1",nr_cols="6",hdr=[..],body=[brkpt={},brkpt={}]}
 * </pre>
 */
public class MIBreakListInfo extends MIInfo {

	MIBreakPoint[] breakpoints;

	public MIBreakListInfo(MIOutput rr) {
		super(rr);
	}

	public MIBreakPoint[] getBreakPoints() {
		if (breakpoints == null) {
			parse();
		}
		return breakpoints;
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
					if (var.equals("BreakpointTable")) {
						parseTable(results[i].getMIValue(), aList);
					}
				}
			}
		}
		breakpoints = (MIBreakPoint[])aList.toArray(new MIBreakPoint[aList.size()]);
	}

	void parseTable(MIValue val, List aList) {
		if (val instanceof MITuple) {
			MIResult[] table = ((MITuple)val).getMIResults();
			for (int j = 0; j < table.length; j++) {
				String variable = table[j].getVariable();
				if (variable.equals("body")) {
					parseBody(table[j].getMIValue(), aList);
				}
			}
		}
	}

	void parseBody(MIValue body, List aList) {
		if (body instanceof MIList) {
			MIResult[] bkpts = ((MIList)body).getMIResults();
			for (int i = 0; i < bkpts.length; i++) {
				String b = bkpts[i].getVariable();
				if (b.equals("bkpt")) {
					MIValue value = bkpts[i].getMIValue();
					if (value instanceof MITuple) {
						aList.add(new MIBreakPoint((MITuple)value));
					}
				}
			}
		}
	}
}
