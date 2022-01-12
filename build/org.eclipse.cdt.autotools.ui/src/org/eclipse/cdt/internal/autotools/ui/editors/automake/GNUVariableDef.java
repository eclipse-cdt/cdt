/*******************************************************************************
 * Copyright (c) 2000, 2015 QNX Software Systems and others.
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
 *     Red Hat Inc. - Refactor name
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.ui.editors.automake;

import org.eclipse.cdt.make.core.makefile.gnu.IVariableDefinition;

/**
 */
public class GNUVariableDef extends MacroDefinition implements IVariableDefinition {

	/**
	 * ? is Conditional
	 * : is Simply-expanded
	 * + is append
	 * 0 is recursively-expanded.
	 */
	final static int TYPE_RECURSIVE_EXPAND = 0;
	final static int TYPE_SIMPLE_EXPAND = ':';
	final static int TYPE_CONDITIONAL = '?';
	final static int TYPE_APPEND = '+';
	int type;
	String varTarget;

	public GNUVariableDef(Directive parent, String name, StringBuffer value) {
		this(parent, name, value, TYPE_RECURSIVE_EXPAND);
	}

	public GNUVariableDef(Directive parent, String name, StringBuffer value, int type) {
		this(parent, "", name, value, type); //$NON-NLS-1$
	}

	public GNUVariableDef(Directive parent, String target, String name, StringBuffer value, int type) {
		super(parent, name, value);
		varTarget = target;
		this.type = type;
	}

	@Override
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

	@Override
	public boolean isRecursivelyExpanded() {
		return type == TYPE_RECURSIVE_EXPAND;
	}

	@Override
	public boolean isSimplyExpanded() {
		return type == TYPE_SIMPLE_EXPAND;
	}

	@Override
	public boolean isConditional() {
		return type == TYPE_CONDITIONAL;
	}

	@Override
	public boolean isAppend() {
		return type == TYPE_APPEND;
	}

	@Override
	public boolean isTargetSpecific() {
		String t = getTarget();
		return t != null && t.length() > 0;
	}

	@Override
	public boolean isExport() {
		return false;
	}

	@Override
	public boolean isMultiLine() {
		return false;
	}

	/**
	 * Variable from an `override' directive.
	 */
	@Override
	public boolean isOverride() {
		return false;
	}

	/**
	 * Automatic variable -- cannot be set.
	 */
	@Override
	public boolean isAutomatic() {
		return false;
	}

	@Override
	public String getTarget() {
		return varTarget;
	}

}
