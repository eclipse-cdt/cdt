/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */
package org.eclipse.cdt.debug.mi.core.output;

/**
 * GDB/MI var-info-expression.
 */
public class MIVarInfoExpressionInfo extends MIInfo {

	String lang = "";
	String exp = "";

	public MIVarInfoExpressionInfo(MIOutput record) {
		super(record);
		parse();
	}

	public String getLanguage () {
		return lang;
	}

	public String getExpression() {
		return exp;
	}

	void parse() {
		if (isDone()) {
			MIOutput out = getMIOutput();
			MIResultRecord rr = out.getMIResultRecord();
			if (rr != null) {
				MIResult[] results =  rr.getMIResults();
				for (int i = 0; i < results.length; i++) {
					String var = results[i].getVariable();
					MIValue value = results[i].getMIValue();
					String str = "";
					if (value instanceof MIConst) {
						str = ((MIConst)value).getString();
					}

					if (var.equals("lang")) {
						lang = str;
					} else if (var.equals("exp")) {
						exp = str;
					}
				}
			}
		}
	}
}
