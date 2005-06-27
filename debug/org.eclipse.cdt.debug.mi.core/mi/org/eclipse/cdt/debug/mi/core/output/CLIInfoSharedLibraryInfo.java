/*******************************************************************************
 * Copyright (c) 2000, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.mi.core.output;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 */
public class CLIInfoSharedLibraryInfo extends MIInfo {

	MIShared[] shared;
	boolean isUnixFormat = true;
	boolean hasProcessHeader = false;
	
	// HP-UX shared library view
	private boolean isHPUXFormat;
	static String mergestr=null;

	public CLIInfoSharedLibraryInfo(MIOutput out) {
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
			shared[i] = (MIShared) aList.get(i);
		}
	}

	void parseShared(String str, List aList) {
		if (!hasProcessHeader) {
			// Process the header and choose a type.
			if (str.startsWith("DLL")) { //$NON-NLS-1$
				// Cygwin info shared
				isUnixFormat = false;
			} else if(str.startsWith("Shared Object Libraries")) { //$NON-NLS-1$
				// HP-UX shared library view
				isHPUXFormat = true;				
			}
			hasProcessHeader = true;
		} else if (isHPUXFormat) {
			// FIX : HP-UX shared library view
			if(str.startsWith("0x")) { //$NON-NLS-1$
               //merging...
				mergestr +=" "+str; //$NON-NLS-1$
				parseHPUXShared(mergestr, aList);
				return;
			} else if(str.startsWith("tstart")) { //$NON-NLS-1$
				return;
			} else {
               // new shareed library entry...;
				mergestr = new String(str);
				return;
			}
		} else if (isUnixFormat) {
			parseUnixShared(str, aList);
		} else {
			parseWinShared(str, aList);
		}
	}

	/**
	 * We do the parsing backward because on some Un*x system, the To or the From
	 * and even the "Sym Read" can be empty....
	 * @param str
	 * @param aList
	 */
	void parseUnixShared(String str, List aList) {
		if (str.length() > 0) {
			// Pass the header
			int index = -1;
			String from = ""; //$NON-NLS-1$
			String to = ""; //$NON-NLS-1$
			boolean syms = false;
			String name = ""; //$NON-NLS-1$

			for (int i = 0;(index = str.lastIndexOf(' ')) != -1 || i <= 3; i++) {
				if (index == -1) {
					index = 0;
				}
				String sub = str.substring(index).trim();
				// move to previous column
				str = str.substring(0, index).trim();
				switch (i) {
					case 0 :
						name = sub;
						break;
					case 1 :
						if (sub.equalsIgnoreCase("Yes")) { //$NON-NLS-1$
							syms = true;
						}
						break;
					case 2 : // second column is "To"
							to = sub;
						break;
					case 3 : // first column is "From"
							from = sub;
						break;
				}
			}
			if (name.length() > 0) {
				MIShared s = new MIShared(from, to, syms, name);
				aList.add(s);
			}
		}
	}

	// FIX : HP-UX shared library view
	void parseHPUXShared(String str, List aList) {
		if (str.length() > 0) {
			// Pass the header
			int index = -1;
			String from = ""; //$NON-NLS-1$
			String to = ""; //$NON-NLS-1$
			boolean syms = false;
			String name = ""; //$NON-NLS-1$

			for (int i = 0;(index = str.indexOf(' ')) != -1 || i < 3; i++) 
			{
				if (index == -1) {
					index = 0;
				}

				String sub = str.substring(0,index).trim();
				str= str.substring(index).trim();
				switch (i) {
					case 0 :
						name = sub;
						break;
					case 2 : // second column is "To"
							to = sub;
						break;
					case 1 : // first column is "From"
							from = sub;
						break;
				}
			}
			syms=true;
			if (name.length() > 0) {
				MIShared s = new MIShared(from, to, syms, name);
				aList.add(s);
			}
		}
	} // end fo fix

	void parseWinShared(String str, List aList) {
		String from = ""; //$NON-NLS-1$
		String to = ""; //$NON-NLS-1$
		boolean syms = true;

		int index = str.lastIndexOf(' ');
		if (index > 0) {
			String sub = str.substring(index).trim();
			// Go figure they do not print the "0x" to indicate hexadecimal!!
			if (!sub.startsWith("0x")) { //$NON-NLS-1$
				sub = "0x" + sub; //$NON-NLS-1$
			}
			from = sub;
			str = str.substring(0, index).trim();
		}
		MIShared s = new MIShared(from, to, syms, str.trim());
		aList.add(s);
	}
}
