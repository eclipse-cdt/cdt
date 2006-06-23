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
package org.eclipse.cdt.make.internal.core.makefile.gnu;

import org.eclipse.cdt.make.internal.core.makefile.Directive;


public class DefineVariable extends VariableDefinition {

	public DefineVariable(Directive parent, String name, StringBuffer value) {
		super(parent, name, value);
	}

	public boolean isMultiLine() {
		return true;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer(GNUMakefileConstants.VARIABLE_DEFINE);
		sb.append(getName()).append('\n');
		sb.append(getValue());
		sb.append(GNUMakefileConstants.TERMINAL_ENDEF);
		return sb.toString();
	}
}
