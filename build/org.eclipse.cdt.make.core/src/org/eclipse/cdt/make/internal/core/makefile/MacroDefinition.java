/**********************************************************************
 * Copyright (c) 2002,2003 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.make.internal.core.makefile;

import org.eclipse.cdt.make.core.makefile.IMacroDefinition;

/**
 */
public class MacroDefinition extends Statement implements IMacroDefinition {
	String name;
	StringBuffer value;

	public MacroDefinition(String line) {
		value = new StringBuffer();
		int index = line.indexOf('=');
		if (index != -1) {
			int separator = index;
			// Check for "+=",  ":="
			if (index > 0) {
				char c = line.charAt(index - 1);
				if (c == ':' || c =='+') {
					separator = index -1;
				}
			}
			name = line.substring(0, separator).trim();
			value.append(line.substring(index + 1));
		} else {
			name = line;
		}
	}

	public MacroDefinition(String n, String v) {
		name = n;
		value = new StringBuffer(v);
	}

	public String getName() {
		return name;
	}

	public void setName(String n) {
		name = (n == null) ? "" : n.trim() ;
	}

	public String getValue() {
		return value.toString().trim();
	}

	public void setValue(String val) {
		value = new StringBuffer(val);
	}

	public void append(String val) {
		value.append(' ').append(val);
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(getName()).append(" = ").append(getValue()).append('\n');
		return buffer.toString();
	}

	public boolean equals(MacroDefinition v) {
		return v.getName().equals(getName());
	}
}
