/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */
package org.eclipse.cdt.debug.mi.core.output;

/**
 * GDB/MI whatis parsing.
 */
public class MIPTypeInfo extends MIInfo {

	String type;

	public MIPTypeInfo(MIOutput out) {
		super(out);
		parse();
	}

	public String getType() {
		return type;
	}

	void parse() {
		StringBuffer buffer = new StringBuffer();
		if (isDone()) {
			MIOutput out = getMIOutput();
			MIOOBRecord[] oobs = out.getMIOOBRecords();
			for (int i = 0; i < oobs.length; i++) {
				if (oobs[i] instanceof MIConsoleStreamOutput) {
					MIStreamRecord cons = (MIStreamRecord) oobs[i];
					String str = cons.getString();
					// We are interested in the shared info
					if (str != null) {
						str = str.trim();
						if (str.startsWith ("type")) { //$NON-NLS-1$
							int equal = str.indexOf('=');
							if (equal > 0) {
								str = str.substring(equal + 1);
							}
						}
						buffer.append(str);
					}
				}
			}
		}
		type = buffer.toString().trim();
	}
}
