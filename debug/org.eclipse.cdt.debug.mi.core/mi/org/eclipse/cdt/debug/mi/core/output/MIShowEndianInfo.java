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

/**
 * show endian
&"show endian\n"
~"The target endianness is set automatically (currently little endian)\n"
^done
 *
 */
public class MIShowEndianInfo extends MIInfo {

	boolean littleEndian;

	public MIShowEndianInfo(MIOutput out) {
		super(out);
		parse();
	}

	public boolean isLittleEndian() {
		return littleEndian;
	}

	void parse() {
		if (isDone()) {
			MIOutput out = getMIOutput();
			MIOOBRecord[] oobs = out.getMIOOBRecords();
			for (int i = 0; i < oobs.length; i++) {
				if (oobs[i] instanceof MIConsoleStreamOutput) {
					MIStreamRecord cons = (MIStreamRecord) oobs[i];
					String str = cons.getString();
					// We are interested in the signal info
					parseLine(str);
				}
			}
		}
	}

	void parseLine(String str) {
		if (str != null && str.length() > 0) {
			littleEndian = (str.indexOf("little") != -1);
		}
	}

}
