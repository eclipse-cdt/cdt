/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */
package org.eclipse.cdt.debug.mi.core.output;


/**
 * GDB/MI var-create.
 * -var-create "-" * buf3
 * ^done,name="var1",numchild="6",type="char [6]"
 */
public class MIVarCreateInfo extends MIInfo {

	String name = "";
	int children;
	String type = "";

	public MIVarCreateInfo(MIOutput record) {
		super(record);
		parse();
	}

	public String getName () {
		return name;
	}

	public int getChildNumber() {
		return children;
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
					MIValue value = results[i].getMIValue();
					String str = "";
					if (value instanceof MIConst) {
						str = ((MIConst)value).getString();
					}

					if (var.equals("name")) {
						name = str;
					} else if (var.equals("numchild")) {
						try {
							children = Integer.parseInt(str.trim());
						} catch (NumberFormatException e) {
						}
					} else if (var.equals("type")) {
						type = str;
					}
				}
			}
		}
	}
}
