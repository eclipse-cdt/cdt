package org.eclipse.cdt.debug.mi.core.output;

import java.util.ArrayList;
import java.util.List;

/**
 */
public class MIDataDisassembleInfo extends MIInfo {

	MIAsm[] asms;

	public MIDataDisassembleInfo(MIOutput rr) {
		super(rr);
	}

	public MIAsm[] getAsm() {
		if (asms == null) {
			parse();
		}
		return asms;
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
					if (var.equals("asm_insns")) {
						MIValue value = results[i].getMIValue();
						if (value instanceof MIList) {
							parse((MIList)value, aList);
						}
					}
				}
			}
		}
		asms = (MIAsm[])aList.toArray(new MIAsm[aList.size()]);
	}

	void parse(MIList list, List aList) {
		// src and assenbly is different
		MIResult[] results = list.getMIResults();
		if (results != null && results.length > 0) {
			for (int i = 0; i < results.length; i++) {
				String var = results[i].getVariable();
				if (var.equals("src_and_asm_line")) {
					MIValue value = results[i].getMIValue();
					if (value instanceof MITuple) {
						aList.add(new MIAsm((MITuple)value));
					}
				}
			}
		}

		MIValue[] values = list.getMIValues();
		if (values != null && values.length > 0) {
			for (int i = 0; i < values.length; i++) {
				if (values[i] instanceof MITuple) {
					aList.add(new MIAsm((MITuple)values[i]));
				}
			}
		}
	}
}
