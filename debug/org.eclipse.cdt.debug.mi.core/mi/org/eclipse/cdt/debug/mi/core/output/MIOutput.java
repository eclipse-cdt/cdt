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

/**
 * GDB/MI response.
 */
public class MIOutput {

	public static final MIOOBRecord[] nullOOBRecord = new MIOOBRecord[0];
	MIResultRecord rr = null;
	MIOOBRecord[] oobs = nullOOBRecord;
 

	public MIResultRecord getMIResultRecord() {
		return rr;
	}

	public void setMIResultRecord(MIResultRecord res) {
		rr = res ;
	}

	public MIOOBRecord[] getMIOOBRecords() {
		return oobs;
	}

	public void setMIOOBRecords(MIOOBRecord [] bands) {
		oobs = bands;
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < oobs.length; i++) {
			buffer.append(oobs[i].toString());
		}
		if (rr != null) {
			buffer.append(rr.toString());
		}
		return buffer.toString();
	}
}
