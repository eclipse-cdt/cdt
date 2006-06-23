/*******************************************************************************
 * Copyright (c) 2000, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.mi.core.output;




/**
 * GDB/MI environment PWD info extraction.
 */
public class MIEnvironmentPWDInfo extends MIInfo {

	String pwd = ""; //$NON-NLS-1$

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
					if (str.startsWith("Working directory")) { //$NON-NLS-1$
						int len = "Working directory".length(); //$NON-NLS-1$
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
