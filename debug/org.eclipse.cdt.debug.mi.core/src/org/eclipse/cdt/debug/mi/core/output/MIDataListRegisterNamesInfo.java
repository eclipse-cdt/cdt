/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

package org.eclipse.cdt.debug.mi.core.output;

import java.util.ArrayList;
import java.util.List;

/**
 * GDB/MI data list regiter names response extraction.
 */
public class MIDataListRegisterNamesInfo extends MIInfo {

	String[] names;
	protected int realNameCount = 0;

	public MIDataListRegisterNamesInfo(MIOutput rr) {
		super(rr);
	}

	/**
	 * @return the list of register names. This list can include 0 length
	 * strings in the case where the underlying GDB has a sparse set of 
	 * registers. They are returned as 0 length strings 
	 */
	public String[] getRegisterNames() {
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
				MIResult[] results = rr.getMIResults();
				for (int i = 0; i < results.length; i++) {
					String var = results[i].getVariable();
					if (var.equals("register-names")) {
						MIValue value = results[i].getMIValue();
						if (value instanceof MIList) {
							parseRegisters((MIList) value, aList);
						}
					}
				}
			}
		}
		names = (String[]) aList.toArray(new String[aList.size()]);
	}

	void parseRegisters(MIList list, List aList) {
		MIValue[] values = list.getMIValues();
		for (int i = 0; i < values.length; i++) {
			if (values[i] instanceof MIConst) {
				String str = ((MIConst) values[i]).getCString();

				/* this cannot filter nulls because index is critical in retreival 
				 * and index is assigned in the layers above. The MI spec allows 
				 * empty returns, for some register names. */
				if (str != null && str.length() > 0) {
					realNameCount++;
					aList.add(str);
				} else {
					aList.add("");
				}
			}
		}
	}

	/**
	 * @return the number of non-null and non-empty names in the 
	 * register list
	 */
	public int getNumRealNames() {
		if (names == null)
			parse();
		return realNameCount;
	}
}
