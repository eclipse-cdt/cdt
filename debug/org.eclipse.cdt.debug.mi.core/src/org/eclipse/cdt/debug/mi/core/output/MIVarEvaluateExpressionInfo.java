/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */
package org.eclipse.cdt.debug.mi.core.output;

/**
 * GDB/MI var-evalute-expression
 */
public class MIVarEvaluateExpressionInfo extends MIInfo {

	String value = "";

	public MIVarEvaluateExpressionInfo(MIOutput record) {
		super(record);
		parse();
	}

	public String getValue () {
		return value;
	}

	void parse() {
		if (isDone()) {
			MIOutput out = getMIOutput();
			MIResultRecord rr = out.getMIResultRecord();
			if (rr != null) {
				MIResult[] results =  rr.getMIResults();
				for (int i = 0; i < results.length; i++) {
					String var = results[i].getVariable();
					if (var.equals("value")) {
						MIValue val = results[i].getMIValue();
						if (val instanceof MIConst) {
							value = ((MIConst)val).getCString();
						}
					}
				}
			}
		}
	}
}
