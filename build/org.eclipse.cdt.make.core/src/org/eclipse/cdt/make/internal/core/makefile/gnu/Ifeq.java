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


public class Ifeq extends Statement {

	String conditional;

	public Ifeq(String line) {
		parse(line);
	}

	public String toString() {
		StringBuffer sb = new StringBuffer("ifeq");
		sb.append(' ').append(conditional);
		return sb.toString();
	}

	public String getConditional() {
		return conditional;
	}

	/**
	 *  Format of the include directive:
	 * 	ifeq condional-directive
	 */
	protected void parse(String line) {
		line = line.trim();
		for (int i = 0; i < line.length(); i++) {
			if (MakefileUtil.isSpace(line.charAt(i))) {
				line = line.substring(i).trim();
				break;
			}
		}

		conditional = line;
	}
}
