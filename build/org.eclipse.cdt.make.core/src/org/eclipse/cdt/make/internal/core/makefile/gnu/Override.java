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

import org.eclipse.cdt.make.internal.core.makefile.MakefileUtil;
import org.eclipse.cdt.make.internal.core.makefile.Statement;


public class Override extends Statement {

	String variable;

	public Override(String line) {
		parse(line);
	}

	public String toString() {
		StringBuffer sb = new StringBuffer("override");
		sb.append(' ').append(variable);
		return sb.toString();
	}

	public String getVariable() {
		return variable;
	}

	/**
	 *  Format:
	 * 	override VARIABLE := VALUE
	 */
	protected void parse(String line) {
		int i = 0;
		line = line.trim();
		for (; i < line.length(); i++) {
			if (MakefileUtil.isSpace(line.charAt(i))) {
				break;
			}
		}

		if (i < line.length()) {
			variable = line.substring(i).trim();
		} else {
			variable = new String();
		}
	}
}
