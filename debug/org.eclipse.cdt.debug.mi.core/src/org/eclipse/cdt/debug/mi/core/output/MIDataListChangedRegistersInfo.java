/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */
package org.eclipse.cdt.debug.mi.core.output;

import java.util.ArrayList;
import java.util.List;

/**
 * GDB/MI data list changed registers response parsing.
 */
public class MIDataListChangedRegistersInfo extends MIInfo {

	int[] registers;

	public MIDataListChangedRegistersInfo(MIOutput rr) {
		super(rr);
	}

	public int[] getRegisterNumbers() {
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
				registers[i] = Integer.parseInt(str.trim());
			} catch (NumberFormatException e) {
			}
		}
	}

	void parseRegisters(MIList list, List aList) {
		MIValue[] values = list.getMIValues();
		for (int i = 0; i < values.length; i++) {
			if (values[i] instanceof MIConst) {
				String str = ((MIConst)values[i]).getCString();
				if (str != null && str.length() > 0) {
					aList.add(str);
				}
			}
		}
	}
}
