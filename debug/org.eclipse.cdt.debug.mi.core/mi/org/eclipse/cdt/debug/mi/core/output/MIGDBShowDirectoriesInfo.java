/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.mi.core.output;

import java.util.StringTokenizer;


/**
 * GDB/MI show parsing.
 * (gdb) 
 * -gdb-show directories
 * ~"Source directories searched: /tmp:$cdir:$cwd\n"
 * ^done
 */
public class MIGDBShowDirectoriesInfo extends MIInfo {

	String[] dirs = new String[0];

	public MIGDBShowDirectoriesInfo(MIOutput o) {
		super(o);
		parse();
	}

	public String[] getDirectories() {
		return dirs;
	}

	void parse() {
		if (isDone()) {
			MIOutput out = getMIOutput();
			MIOOBRecord[] oobs = out.getMIOOBRecords();
			for (int i = 0; i < oobs.length; i++) {
				if (oobs[i] instanceof MIConsoleStreamOutput) {
					MIStreamRecord cons = (MIStreamRecord)oobs[i];
					String str = cons.getString();
					if (str.startsWith("Source directories searched:")) { //$NON-NLS-1$
						int j = str.indexOf(':');
						if (j != -1) {
							String sub = str.substring(j + 1).trim();
							parseDirectories(sub);
						}
					}
				}
			}
		}
	}

	void parseDirectories(String d) {
		String sep = System.getProperty("path.separator", ":"); //$NON-NLS-1$ //$NON-NLS-2$
		StringTokenizer st = new StringTokenizer(d, sep);
		int count = st.countTokens();
		dirs = new String[count];
		for (int i = 0; st.hasMoreTokens() && i < count; i++) {
			dirs[i] = st.nextToken();
		}
	}
}
