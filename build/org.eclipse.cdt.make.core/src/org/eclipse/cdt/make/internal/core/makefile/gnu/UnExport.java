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
package org.eclipse.cdt.make.internal.core.makefile.gnu;

import org.eclipse.cdt.make.core.makefile.gnu.IUnExport;
import org.eclipse.cdt.make.internal.core.makefile.Directive;

public class UnExport extends Directive implements IUnExport {

	String variable;

	public UnExport(Directive parent, String var) {
		super(parent);
		variable = var;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer(GNUMakefileConstants.DIRECTIVE_UNEXPORT);
		sb.append(' ').append(variable);
		return sb.toString();
	}

	public String getVariable() {
		return variable;
	}

}
