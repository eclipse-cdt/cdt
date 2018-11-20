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
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.ui.editors.automake;

import org.eclipse.cdt.make.core.makefile.IMacroDefinition;

/**
 */
public class MacroDefinition extends Directive implements IMacroDefinition {
	String name;
	StringBuffer value;
	boolean fromCommand;
	boolean fromDefault;
	boolean fromMakefile;
	boolean fromEnvironment;
	boolean fromEnvironmentOverride;

	public MacroDefinition(Directive parent, String n, StringBuffer v) {
		super(parent);
		name = n;
		value = v;
	}

	@Override
	public String getName() {
		return name;
	}

	public void setName(String n) {
		name = (n == null) ? "" : n.trim(); //$NON-NLS-1$
	}

	@Override
	public StringBuffer getValue() {
		return value;
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(getName()).append(" = ").append(getValue()).append('\n'); //$NON-NLS-1$
		return buffer.toString();
	}

	@Override
	public boolean equals(Object v) {
		if (v instanceof MacroDefinition)
			return ((MacroDefinition) v).getName().equals(getName());
		return false;
	}

	@Override
	public int hashCode() {
		return getName().hashCode();
	}

	public void setFromCommand(boolean from) {
		fromCommand = from;
	}

	public void setFromDefault(boolean from) {
		fromDefault = from;
	}

	public void setFromEnviroment(boolean from) {
		fromEnvironment = from;
	}

	public void setFromEnviromentOverride(boolean from) {
		fromEnvironmentOverride = from;
	}

	public void setFromMakefile(boolean from) {
		fromMakefile = from;
	}

	@Override
	public boolean isFromCommand() {
		return fromCommand;
	}

	@Override
	public boolean isFromDefault() {
		return fromDefault;
	}

	@Override
	public boolean isFromEnviroment() {
		return fromEnvironment;
	}

	@Override
	public boolean isFromEnvironmentOverride() {
		return fromEnvironmentOverride;
	}

	@Override
	public boolean isFromMakefile() {
		return fromMakefile;
	}

}
