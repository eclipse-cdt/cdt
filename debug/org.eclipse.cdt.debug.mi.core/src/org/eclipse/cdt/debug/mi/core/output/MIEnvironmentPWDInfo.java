/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

package org.eclipse.cdt.debug.mi.core.output;

import java.util.ArrayList;
import java.util.List;


/**
 * GDB/MI environment PWD info extraction.
 */
public class MIEnvironmentPWDInfo extends MIInfo {

	String pwd = "";

	public MIEnvironmentPWDInfo(MIOutput o) {
		super(o);
		parse();
	}

	public String getWorkingDirectory() {
		return pwd;
	}

	void parse() {
		if (isDone()) {
			MIOutput out = getMIOutput();
			MIOOBRecord[] oobs = out.getMIOOBRecords();
			for (int i = 0; i < oobs.length; i++) {
				if (oobs[i] instanceof MIConsoleStreamOutput) {
					MIStreamRecord cons = (MIStreamRecord)oobs[i];
					String str = cons.getString();
					if (str.startsWith("Working directory")) {
						int len = "Working directory".length();
						str = str.substring(len).trim();
						len = str.indexOf('.');
						if (len != -1) {
							str = str.substring(0, len);
						}
						pwd = str;
					}
				}
			}
		}
	}

}
