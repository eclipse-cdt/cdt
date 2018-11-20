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
 * GDB/MI result sematic (Variable=Value)
 */
public class MIResult {
	String variable = ""; //$NON-NLS-1$
	MIValue value = null;

	public String getVariable() {
		return variable;
	}

	public void setVariable(String var) {
		variable = var;
	}

	public MIValue getMIValue() {
		return value;
	}

	public void setMIValue(MIValue val) {
		value = val;
	}

	@Override
	public String toString() {
		StringBuilder buffer = new StringBuilder();
		buffer.append(variable);
		if (value != null) {
			String v = value.toString();
			buffer.append('=');
			if (!v.isEmpty() && (v.charAt(0) == '[' || v.charAt(0) == '{')) {
				buffer.append(v);
			} else {
				buffer.append('"').append(v).append('"');
			}
		}
		return buffer.toString();
	}
}
