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
 * GDB/MI memory parsing.
 */
public class MIMemory {
	String addr;
	long [] data = new long[0];
	List badOffsets = new ArrayList();
	String ascii = ""; //$NON-NLS-1$

	public MIMemory(MITuple tuple) {
		parse(tuple);
	}

	public String getAddress() {
		return addr;
	}

	public long [] getData() {
		return data;
	}

	public int[] getBadOffsets() {
		int[] data = new int[badOffsets.size()];
		for (int i = 0; i < data.length; ++i) {
			Integer o = (Integer)badOffsets.get(i);
			data[i] = o.intValue();
		}
		return data;
	}

	public String getAscii() {
		return ascii;
	}

	public String toSting() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("addr=\"" + addr + "\""); //$NON-NLS-1$ //$NON-NLS-2$
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
					addr = str.trim();
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
					badOffsets.add(new Integer(i));
					data[i] = 0;
				}
			}
		}
	}
}
