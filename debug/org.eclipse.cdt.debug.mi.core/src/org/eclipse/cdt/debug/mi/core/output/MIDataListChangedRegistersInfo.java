package org.eclipse.cdt.debug.mi.core.output;

import java.util.ArrayList;
import java.util.List;

/**
 */
public class MIDataListChangedRegistersInfo extends MIInfo {

	int[] registers;

	public MIDataListChangedRegistersInfo(MIOutput rr) {
		super(rr);
	}

	int[] getRegisters() {
		if (registers == null) {
			parse();
		}
		return registers;
	}

	void parse() {
		List aList = new ArrayList();
		if (isDone()) {
			MIOutput out = getMIOutput();
			MIResultRecord rr = out.getMIResultRecord();
			if (rr != null) {
				MIResult[] results =  rr.getMIResults();
				for (int i = 0; i < results.length; i++) {
					String var = results[i].getVariable();
					if (var.equals("changed-registers")) {
						MIValue value = results[i].getMIValue();
						if (value instanceof MIList) {
							parseRegisters((MIList)value, aList);
						}
					}
				}
			}
		}
		registers = new int[aList.size()];
		for (int i = 0; i < aList.size(); i++) {
			String str = (String)aList.get(i);
			try {
				registers[i] = Integer.parseInt(str);
			} catch (NumberFormatException e) {
			}
		}
	}

	void parseRegisters(MIList list, List aList) {
		MIValue[] values = list.getMIValues();
		for (int i = 0; i < values.length; i++) {
			if (values[i] instanceof MIConst) {
				String str = ((MIConst)values[i]).getString();
				if (str != null && str.length() > 0) {
					aList.add(str);
				}
			}
		}
	}
}
