/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */
package org.eclipse.cdt.debug.mi.core.output;


/**
 * GDB/MI var-info-type
 */
public class MIVarInfoTypeInfo extends MIInfo {

	String type = ""; //$NON-NLS-1$

	public MIVarInfoTypeInfo(MIOutput record) {
		super(record);
		parse();
	}

	public String getType() {
		return type;
	}

	void parse() {
		if (isDone()) {
			MIOutput out = getMIOutput();
			MIResultRecord rr = out.getMIResultRecord();
			if (rr != null) {
				MIResult[] results =  rr.getMIResults();
				for (int i = 0; i < results.length; i++) {
					String var = results[i].getVariable();
					if (var.equals("type")) { //$NON-NLS-1$
						MIValue value = results[i].getMIValue();
						if (value instanceof MIConst) {
							type = ((MIConst)value).getString();
						}
					}
				}
			}
		}
	}
}
