/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.internal.core.parser.ast;

/**
 * @author jcamelon
 *
 */
public class Offsets {

	protected int startingOffset = 0;
	protected int endingOffset = 0;
	private int startingLine;
	private int endingLine;

	public void setStartingOffsetAndLineNumber(int offset, int lineNumber) {
		startingOffset = offset;
		startingLine = lineNumber;
	}

	public void setEndingOffsetAndLineNumber(int offset, int lineNumber) {
		endingOffset = offset;
		endingLine = lineNumber;
	}

	public int getStartingOffset() {
		return startingOffset;
	}

	public int getEndingOffset() {
		return endingOffset;
	}

	/**
	 * @return
	 */
	public int getStartingLine() {
		return startingLine;
	}

	/**
	 * @return
	 */
	public int getEndingLine() {
		return endingLine;
	}

}
