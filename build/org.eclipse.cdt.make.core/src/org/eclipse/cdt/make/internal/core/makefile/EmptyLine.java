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
package org.eclipse.cdt.make.internal.core.makefile;

public class EmptyLine extends Statement {

	final public static String newline = "\n";
	final public static char nl = '\n';

	public EmptyLine() {
	}

	public String toString() {
		return newline;
	}

	public boolean equals(EmptyLine stmt) {
		return true;
	}
}
