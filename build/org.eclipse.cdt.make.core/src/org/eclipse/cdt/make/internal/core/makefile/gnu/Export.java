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

import java.util.StringTokenizer;

import org.eclipse.cdt.make.internal.core.makefile.Statement;

public class Export extends Statement {

	String variable;

	public Export(String line) {
		parse(line);
	}

	public String toString() {
		StringBuffer sb = new StringBuffer("export");
		sb.append(' ').append(variable);
		return sb.toString();
	}

	public String getVariable() {
		return variable;
	}

	/**
	 *  Format of the include directive:
	 * 	export Variabe ...
	 */
	protected void parse(String line) {
		StringTokenizer st = new StringTokenizer(line);
		int count = st.countTokens();
		if (count > 0) {
			for (int i = 0; i < count; i++) {
				if (i == 0) {
					// ignore the "export" keyword.
					continue;
				}
				variable = st.nextToken();
			}
		}
		if (variable == null) {
			variable = new String();
		}
	}
}
