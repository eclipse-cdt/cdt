package org.eclipse.cdt.debug.mi.core.output;

import java.util.ArrayList;
import java.util.List;

/**
 */
public class MIDataListRegisterNamesInfo extends MIInfo {

	String[] names;

	public MIDataListRegisterNamesInfo(MIOutput rr) {
		super(rr);
	}

	String[] getRegistersNames () {
		if (names == null) {
			parse();
		}
		return names;
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
					if (var.equals("register-names")) {
						MIValue value = results[i].getMIValue();
						if (value instanceof MIList) {
							parseRegisters((MIList)value, aList);
						}
					}
				}
			}
		}
		names = (String[])aList.toArray(new String[aList.size()]);
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
