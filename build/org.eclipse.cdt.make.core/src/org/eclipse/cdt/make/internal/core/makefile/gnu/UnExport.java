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

import org.eclipse.cdt.make.internal.core.makefile.Statement;

public class UnExport extends Statement {

	String variable;

	public UnExport(String var) {
		variable = var;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer("unexport");
		sb.append(' ').append(variable);
		return sb.toString();
	}

	public String getVariable() {
		return variable;
	}

}
