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
 * base Abstract class for the OOB stream MI responses.
 */
public abstract class MIAsyncRecord extends MIOOBRecord {

	final static MIResult[] nullResults = new MIResult[0];

	MIResult[] results = null;
	String asynClass = ""; //$NON-NLS-1$
	int token = -1;

	public int getToken() {
		return token;
	}

	public void setToken(int t) {
		token = t;
	}

	public String getAsyncClass() {
		return asynClass;
	}

	public void setAsyncClass(String a) {
		asynClass = a;
	}

	public MIResult[] getMIResults() {
		if (results == null) {
			return nullResults;
		}
		return results;
	}

	public void setMIResults(MIResult[] res) {
		results = res;
	}

	@Override
	public String toString() {
		StringBuilder buffer = new StringBuilder();
		if (token > 0) {
			buffer.append(token);
		}
		if (this instanceof MIExecAsyncOutput) {
			buffer.append('*');
		} else if (this instanceof MIStatusAsyncOutput) {
			buffer.append('+');
		} else if (this instanceof MINotifyAsyncOutput) {
			buffer.append('=');
		}
		buffer.append(asynClass);
		if (results != null) {
			for (int i = 0; i < results.length; i++) {
				buffer.append(',');
				buffer.append(results[i].toString());
			}
		}
		buffer.append('\n');
		return buffer.toString();
	}
}
