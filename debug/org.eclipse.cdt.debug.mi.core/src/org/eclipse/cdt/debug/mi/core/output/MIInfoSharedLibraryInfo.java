/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */
package org.eclipse.cdt.debug.mi.core.output;

import java.util.ArrayList;
import java.util.List;

/**
 * GDB/MI thread list parsing.
&"info shared\n"
~"From        To          Syms Read   Shared Object Library\n"
~"0x40042fa0  0x4013ba9b  Yes         /lib/i686/libc.so.6\n"
~"0x40001db0  0x4001321c  Yes         /lib/ld-linux.so.2\n"

 */
public class MIInfoSharedLibraryInfo extends MIInfo {

	MIShared[] shared;

	public MIInfoSharedLibraryInfo(MIOutput out) {
		super(out);
		parse();
	}

	public MIShared[] getMIShared() {
		return shared;
	}

	void parse() {
		List aList = new ArrayList();
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
		shared = new MIShared[aList.size()];
		for (int i = 0; i < aList.size(); i++) {
			shared[i] = (MIShared)aList.get(i);
		}
	}

	void parseShared(String str, List aList) {
		if (str.length() > 0) {
			// Pass the header
			if (Character.isDigit(str.charAt(0))) {
				int index = -1;
				long from = 0;
				long to = 0;
				boolean syms = false;

				for (int i = 0; i < 3 && (index = str.indexOf(' ')) != -1; i++) {
					String sub = str.substring(0, index).trim();
					// advance to next column
					str = str.substring(index).trim();
					switch (i) {
						case 0: // first column is "From"
							try {
								from = Long.decode(sub).longValue();
							} catch (NumberFormatException e) {
							}
						break;
						case 1: // second column is "To"
							try {
								to = Long.decode(sub).longValue();
							} catch (NumberFormatException e) {
							}
						break;
						case 2: // third column is "Syms Read"
							if (sub.equalsIgnoreCase("Yes")) {
								syms = true;
							}
						break;
						default: // last column is "Shared object library"
							i = 3; // bail out. use the entire string
					}
				}
				MIShared s = new MIShared(from, to, syms, str.trim());
				aList.add(s);
			}
		}
	}
}
