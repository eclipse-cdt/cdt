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

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(variable);
		if (value != null) {
			String v = value.toString();
			buffer.append('=');
			if (v.charAt(0) == '[' || v.charAt(0) =='{') {
				buffer.append(v); 
			} else {
				buffer.append("\"" + value.toString() + "\"");  //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		return buffer.toString();
	}
}
