/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */
package org.eclipse.cdt.debug.mi.core.output;

import java.util.ArrayList;
import java.util.List;


/**
 * GDB/MI show parsing.
 * (gdb) 
 * -gdb-show convenience $_exitcode
 * ~"$_exitcode = 0"
 * ~"\n"
 * ^done
 */
public class MIGDBShowExitCodeInfo extends MIInfo {

	int code;

	public MIGDBShowExitCodeInfo(MIOutput o) {
		super(o);
		parse();
	}

	public int getCode() {
		return code;
	}

	void parse() {
		if (isDone()) {
			MIOutput out = getMIOutput();
			MIOOBRecord[] oobs = out.getMIOOBRecords();
			for (int i = 0; i < oobs.length; i++) {
				if (oobs[i] instanceof MIConsoleStreamOutput) {
					MIStreamRecord cons = (MIStreamRecord)oobs[i];
					String str = cons.getString();
					if (str.startsWith("$_exitcode")) {
						int j = str.indexOf('=');
						if (j != -1) {
							String sub = str.substring(j + 1).trim();
							try {
								code = Integer.parseInt(sub);
							} catch (NumberFormatException e) {
							}
						}
					}
				}
			}
		}
	}
}
