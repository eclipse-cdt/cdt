/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */
package org.eclipse.cdt.debug.mi.core.output;

/**
 * GDB/MI var-show-attributes
 */
public class MIVarShowAttributesInfo extends MIInfo {

	String attr = "";

	public MIVarShowAttributesInfo(MIOutput record) {
		super(record);
		parse();
	}

	public String getAttributes () {
		return attr;
	}

	public boolean isEditable() {
		return attr.equals("editable");
	}

	void parse() {
		if (isDone()) {
			MIOutput out = getMIOutput();
			MIResultRecord rr = out.getMIResultRecord();
			if (rr != null) {
				MIResult[] results =  rr.getMIResults();
				for (int i = 0; i < results.length; i++) {
					String var = results[i].getVariable();
					if (var.equals("attr")) {
						MIValue value = results[i].getMIValue();
						if (value instanceof MIConst) {
							attr = ((MIConst)value).getString();
						}
					}
				}
			}
		}
	}
}
