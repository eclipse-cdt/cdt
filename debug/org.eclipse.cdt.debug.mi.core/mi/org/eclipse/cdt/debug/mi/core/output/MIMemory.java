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
 * GDB/MI memory parsing.
 */
public class MIMemory {
	long addr;
	long [] data = new long[0];
	String ascii = ""; //$NON-NLS-1$

	public MIMemory(MITuple tuple) {
		parse(tuple);
	}

	public long getAddress() {
		return addr;
	}

	public long [] getData() {
		return data;
	}

	public String getAscii() {
		return ascii;
	}

	public String toSting() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("addr=\"" + Long.toHexString(addr) + "\""); //$NON-NLS-1$ //$NON-NLS-2$
		buffer.append("data=[");  //$NON-NLS-1$
		for (int i = 0 ; i < data.length; i++) {
			if (i != 0) {
				buffer.append(',');
			}
			buffer.append('"').append(Long.toHexString(data[i])).append('"');
		}
		buffer.append(']');
		if (ascii.length() > 0) {
			buffer.append(",ascii=\"" + ascii + "\""); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return buffer.toString();
	}

	void parse(MITuple tuple) {
		MIResult[] results =  tuple.getMIResults();
		for (int i = 0; i < results.length; i++) {
			String var = results[i].getVariable();
			MIValue value = results[i].getMIValue();
			String str = ""; //$NON-NLS-1$
			if (value != null && value instanceof MIConst) {
				str = ((MIConst)value).getCString();
			}

			if (var.equals("addr")) { //$NON-NLS-1$
				try {
					addr = Long.decode(str.trim()).longValue();
				} catch (NumberFormatException e) {
				}
			} else if (var.equals("data")) { //$NON-NLS-1$
				if (value != null && value instanceof MIList) {
					parseData((MIList)value);
				}
			} else if (var.equals("ascii")) { //$NON-NLS-1$
				ascii = str;
			}
		}
	}

	void parseData(MIList list) {
		MIValue[] values = list.getMIValues();
		data = new long[values.length];
		for (int i = 0; i < values.length; i++) {
			if (values[i] instanceof MIConst) {
				String str = ((MIConst)values[i]).getCString();
				try {
					data[i] = Long.decode(str.trim()).longValue();
				} catch (NumberFormatException e) {
					data[i] = 0;
				}
			}
		}
	}
}
