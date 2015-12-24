/*******************************************************************************
 * Copyright (c) 2000, 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.ui.editors.automake;


public class OverrideDefine extends DefineVariable {

	public OverrideDefine(Directive parent, String name, StringBuffer value) {
		super(parent, name, value);
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer(GNUMakefileConstants.VARIABLE_OVERRIDE + " " + GNUMakefileConstants.VARIABLE_DEFINE); //$NON-NLS-1$
		sb.append(getName()).append('\n');
		sb.append(getValue());
		sb.append(GNUMakefileConstants.TERMINAL_ENDEF);
		return sb.toString();
	}

	@Override
	public boolean isOverride() {
		return true;
	}

}
