/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

package org.eclipse.cdt.debug.mi.core.output;


/**
 * GDB/MI var-info-num-children.
 */
public class MIVarInfoNumChildrenInfo extends MIInfo {

	int children;

	public MIVarInfoNumChildrenInfo(MIOutput record) {
		super(record);
		parse();
	}

	public int getChildNumber() {
		return children;
	}

	void parse() {
		if (isDone()) {
			MIOutput out = getMIOutput();
			MIResultRecord rr = out.getMIResultRecord();
			if (rr != null) {
				MIResult[] results =  rr.getMIResults();
				for (int i = 0; i < results.length; i++) {
					String var = results[i].getVariable();

					if (var.equals("numchild")) {
						MIValue value = results[i].getMIValue();
						if (value instanceof MIConst) {
							String str = ((MIConst)value).getString();
							try {
								children = Integer.parseInt(str.trim());
							} catch (NumberFormatException e) {
							}
						}
					}
				}
			}
		}
	}
}
