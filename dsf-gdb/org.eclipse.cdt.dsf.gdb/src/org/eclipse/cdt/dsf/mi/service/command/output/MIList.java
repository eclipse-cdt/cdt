/*******************************************************************************
 * Copyright (c) 2000, 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Wind River Systems   - Modified for new DSF Reference Implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.output;

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

	@Override
	public String toString() {
		StringBuilder buffer = new StringBuilder();
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
