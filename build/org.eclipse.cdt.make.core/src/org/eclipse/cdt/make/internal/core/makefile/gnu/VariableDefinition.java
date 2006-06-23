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
package org.eclipse.cdt.make.internal.core.makefile.gnu;

import org.eclipse.cdt.make.core.makefile.gnu.IVariableDefinition;
import org.eclipse.cdt.make.internal.core.makefile.Directive;
import org.eclipse.cdt.make.internal.core.makefile.MacroDefinition;

/**
 */
public class VariableDefinition extends MacroDefinition implements IVariableDefinition  {

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

	public VariableDefinition(Directive parent, String name, StringBuffer value) {
		this(parent, name, value, TYPE_RECURSIVE_EXPAND);
	}

	public VariableDefinition(Directive parent, String name, StringBuffer value, int type) {
		this(parent,  "", name, value, type); //$NON-NLS-1$
	}

	public VariableDefinition(Directive parent, String target, String name, StringBuffer value, int type) {
		super(parent, name, value);
		varTarget = target;
		this.type = type;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		if (isTargetSpecific()) {
			sb.append(getTarget()).append(": "); //$NON-NLS-1$
		}
		if (isOverride()) {
			sb.append(GNUMakefileConstants.VARIABLE_OVERRIDE);
		}
		if (isMultiLine()) {
			sb.append(GNUMakefileConstants.VARIABLE_DEFINE);
			sb.append(' ');
			sb.append(getName()).append('\n');
			sb.append(getValue()).append('\n');
			sb.append(GNUMakefileConstants.TERMINAL_ENDEF);
			sb.append('\n');
		} else {
			if (isExport()) {
				sb.append(GNUMakefileConstants.VARIABLE_EXPORT);
				sb.append(' ');
			}
			sb.append(getName());
			if (isRecursivelyExpanded()) {
				sb.append(" = "); //$NON-NLS-1$
			} else if (isSimplyExpanded()) {
				sb.append(" := "); //$NON-NLS-1$
			} else if (isConditional()) {
				sb.append(" ?= "); //$NON-NLS-1$
			} else if (isAppend()) {
				sb.append(" += "); //$NON-NLS-1$
			}
			sb.append(getValue()).append('\n');
		}
		return sb.toString();
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
		return t != null && t.length() > 0;
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
