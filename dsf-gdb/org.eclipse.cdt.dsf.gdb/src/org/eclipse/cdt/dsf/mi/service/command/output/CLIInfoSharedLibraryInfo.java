/*******************************************************************************
 * Copyright (c) 2000, 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Ericsson AB			- Modified for DSF GDB reference implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.output;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 */
public class CLIInfoSharedLibraryInfo extends MIInfo {

	DsfMISharedInfo[] shared;

	public class DsfMISharedInfo {

		String from;
		String to;
		boolean isread;
		String name;

		public DsfMISharedInfo (String start, String end, boolean read, String location) {
			from = start;
			to = end;
			isread = read;
			name = location;
		}

		public String getFrom() {
			return from;
		}

		public String getTo() {
			return to;
		}

		public boolean isRead() {
			return isread;
		}

		public String getName() {
			return name;
		}

		public void setSymbolsRead(boolean read) {
			isread = read;
		}
	}	

	public CLIInfoSharedLibraryInfo(MIOutput out) {
		super(out);
		parse();
	}

	public DsfMISharedInfo[] getMIShared() {
		return shared;
	}

	void parse() {
		List<DsfMISharedInfo> aList = new ArrayList<DsfMISharedInfo>();
		if (isDone()) {
			MIOutput out = getMIOutput();
			MIOOBRecord[] oobs = out.getMIOOBRecords();
			for (int i = 0; i < oobs.length; i++) {
				if (oobs[i] instanceof MIConsoleStreamOutput) {
					MIStreamRecord cons = (MIStreamRecord) oobs[i];
					String str = cons.getString();
					// We are interested in the shared info
					parseShared(str.trim(), aList);
				}
			}
		}
		shared = new DsfMISharedInfo[aList.size()];
		for (int i = 0; i < aList.size(); i++) {
			shared[i] = aList.get(i);
		}
	}

	void parseShared(String str, List<DsfMISharedInfo> aList) {
		if (str.length() > 0) {
			// Parsing pattern of type ~"0x40000970  0x4001331f  Yes         /lib/ld-linux.so.2\n"
            Pattern pattern = Pattern.compile("(0x.*)(0x.*)(Yes|No)(\\s*)(.*)",  Pattern.MULTILINE); //$NON-NLS-1$
            Matcher matcher = pattern.matcher(str);
            if (matcher.find()) {
				DsfMISharedInfo s = new DsfMISharedInfo(matcher.group(1), matcher.group(2),
													   (matcher.group(3).equals("Yes"))?true:false,  //$NON-NLS-1$
														matcher.group(5));
				aList.add(s);

            }
		}
	}

}
