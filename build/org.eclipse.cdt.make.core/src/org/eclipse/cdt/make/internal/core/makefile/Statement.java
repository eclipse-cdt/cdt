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

import org.eclipse.cdt.make.core.makefile.IStatement;

public abstract class Statement implements IStatement {

	int endLine;
	int startLine;

	public Statement() {
	}

	public abstract String toString();

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.make.core.makefile.IStatement#getEndLine()
	 */
	public int getEndLine() {
		return endLine;
	}

	public void setEndLine(int lineno) {
		endLine = lineno;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.make.core.makefile.IStatement#getStartLine()
	 */
	public int getStartLine() {
		return startLine;
	}

	public void setStartLine(int lineno) {
		startLine = lineno;
	}

	public void setLines(int start, int end) {
		setStartLine(start);
		setEndLine(end);
	}

}
