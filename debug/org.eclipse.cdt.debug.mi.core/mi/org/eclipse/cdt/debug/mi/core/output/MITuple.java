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
 * GDB/MI tuple value.
 */
public class MITuple extends MIValue {

	final static MIResult[] nullResults = new MIResult[0];
	MIResult[] results = nullResults;

	public MIResult[] getMIResults() {
		return results;
	}

	public void setMIResults(MIResult[] res) {
		results = res;
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append('{');
		for (int i = 0; i < results.length; i++) {
			if (i != 0) {
				buffer.append(',');
			}
			buffer.append(results[i].toString());
		}
		buffer.append('}');
		return buffer.toString();
	}
}
