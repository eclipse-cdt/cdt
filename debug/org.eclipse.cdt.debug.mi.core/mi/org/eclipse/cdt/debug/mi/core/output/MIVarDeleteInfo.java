/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */
package org.eclipse.cdt.debug.mi.core.output;


/**
 * GDB/MI var-delete.
 */
public class MIVarDeleteInfo extends MIInfo {

	int ndeleted;

	public MIVarDeleteInfo(MIOutput record) {
		super(record);
		parse();
	}

	public int getNumberDeleted () {
		return ndeleted;
	}

	void parse() {
		if (isDone()) {
			MIOutput out = getMIOutput();
			MIResultRecord rr = out.getMIResultRecord();
			if (rr != null) {
				MIResult[] results =  rr.getMIResults();
				for (int i = 0; i < results.length; i++) {
					String var = results[i].getVariable();
					if (var.equals("ndeleted")) { //$NON-NLS-1$
						MIValue value = results[i].getMIValue();
						if (value instanceof MIConst) {
							String str = ((MIConst)value).getString();
							try {
								ndeleted = Integer.parseInt(str.trim());
							} catch (NumberFormatException e) {
							}
						}
					}
				}
			}
		}
	}
}
