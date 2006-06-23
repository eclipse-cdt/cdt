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
 * GDB/MI list semantic.
 */
public class MIList extends MIValue {

	final static MIResult[] nullResults = new MIResult[0];
	final static MIValue[] nullValues = new MIValue[0];

	MIResult[] results = nullResults;
	MIValue[] values = nullValues;

	public MIResult[] getMIResults() {
		return results;
	}

	public void setMIResults(MIResult[] res) {
		results = res;
	}

	public MIValue[] getMIValues() {
		return values;
	}

	public void setMIValues(MIValue[] vals) {
		values = vals;
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append('[');
		for (int i = 0; i < results.length; i++) {
			if (i != 0) {
				buffer.append(',');
			}
			buffer.append(results[i].toString());
		}
		for (int i = 0; i < values.length; i++) {
			if (i != 0) {
				buffer.append(',');
			}
			buffer.append(values[i].toString());
		}
		buffer.append(']');
		return buffer.toString();
	}
}
