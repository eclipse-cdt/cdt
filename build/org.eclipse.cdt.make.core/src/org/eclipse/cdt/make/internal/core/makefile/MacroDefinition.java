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
package org.eclipse.cdt.make.internal.core.makefile;

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

	public String getName() {
		return name;
	}

	public void setName(String n) {
		name = (n == null) ? "" : n.trim() ; //$NON-NLS-1$
	}

	public StringBuffer getValue() {
		return value;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(getName()).append(" = ").append(getValue()).append('\n'); //$NON-NLS-1$
		return buffer.toString();
	}

	public boolean equals(MacroDefinition v) {
		return v.getName().equals(getName());
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

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.make.core.makefile.IMacroDefinition#isFromCommand()
	 */
	public boolean isFromCommand() {
		return fromCommand;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.make.core.makefile.IMacroDefinition#isFromDefault()
	 */
	public boolean isFromDefault() {
		return fromDefault;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.make.core.makefile.IMacroDefinition#isFromEnviroment()
	 */
	public boolean isFromEnviroment() {
		return fromEnvironment;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.make.core.makefile.IMacroDefinition#isFromEnviroment()
	 */
	public boolean isFromEnvironmentOverride() {
		return fromEnvironmentOverride;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.make.core.makefile.IMacroDefinition#isFromMakefile()
	 */
	public boolean isFromMakefile() {
		return fromMakefile;
	}

}
