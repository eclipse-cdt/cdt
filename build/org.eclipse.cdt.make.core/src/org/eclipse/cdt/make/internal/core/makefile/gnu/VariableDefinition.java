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
package org.eclipse.cdt.make.internal.core.makefile.gnu;

import org.eclipse.cdt.make.internal.core.makefile.MacroDefinition;

/**
 */
public class VariableDefinition extends MacroDefinition  {

	/**
	 * ? is Conditional
	 * : is Simply-expanded
	 * + is append
	 * 0 is recusively-expanded.
	 */
	final static int TYPE_RECURSIVE_EXPAND = 0;
	final static int TYPE_SIMPLE_EXPAND = ':';
	final static int TYPE_CONDITIONAL = '?';
	final static int TYPE_APPEND = '+';
	int type;
	String varTarget;

	public VariableDefinition(String name, StringBuffer value) {
		this(name, value, TYPE_RECURSIVE_EXPAND);
	}

	public VariableDefinition(String name, StringBuffer value, int type) {
		this("", name, value, type);
	}

	public VariableDefinition(String target, String name, StringBuffer value, int type) {
		super(name, value);
		varTarget = target;
		this.type = type;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		if (isTargetSpecific()) {
			sb.append(getTarget()).append(": ");
		}
		if (isOverride()) {
			sb.append("override ");
		}
		if (isMultiLine()) {
			sb.append("define ");
			sb.append(getName()).append('\n');
			sb.append(getValue()).append('\n');
			sb.append("endef\n");
		} else {
			if (isExport()) {
				sb.append("export ");
			}
			sb.append(getName());
			if (isRecursivelyExpanded()) {
				sb.append(" = ");
			} else if (isSimplyExpanded()) {
				sb.append(" := ");
			} else if (isConditional()) {
				sb.append(" ?= ");
			} else if (isAppend()) {
				sb.append(" += ");
			}
			sb.append(getValue()).append('\n');
		}
		return sb.toString();
	}

	public boolean equals(VariableDefinition v) {
		return v.getName().equals(getName());
	}

	public boolean isRecursivelyExpanded() {
		return type == TYPE_RECURSIVE_EXPAND;
	}

	public boolean isSimplyExpanded() {
		return type == TYPE_SIMPLE_EXPAND;
	}
                                                                                                                             
	public boolean isConditional() {
		return type == TYPE_CONDITIONAL;
	}
                                                                                                                             
	public boolean isAppend() {
		return type == TYPE_APPEND;
	}

	public boolean isTargetSpecific() {
		String t = getTarget();
		return t == null || t.length() == 0;
	}

	public boolean isExport() {
		return false;
	}

	public boolean isMultiLine() {
		return false;
	}

	/**
	 * Variable from an `override' directive.
	 */
	public boolean isOverride() {
		return false;
	}

	/**
	 * Automatic variable -- cannot be set.
	 */
	public boolean isAutomatic() {
		return false;
	}

	public String getTarget() {
		return varTarget;
	}

}
