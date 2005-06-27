/*******************************************************************************
 * Copyright (c) 2002, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.parser;

import org.eclipse.cdt.core.parser.IProblem;

/**
 * @author jcamelon
 *
 */
public class BacktrackException extends Exception
{
	private static final char [] EMPTY_CHARARRAY = "".toCharArray(); //$NON-NLS-1$
	private IProblem problem;
	private int startOffset;
	private int endOffset;
	private int lineNumber;
	private char[] filename;

	/**
	 * @param p
	 */
	public void initialize(IProblem p) {
		reset();
		problem = p;
	}

	/**
	 * 
	 */
	private void reset() {
		problem = null;
		startOffset = 0;
		endOffset = 0;
		filename = EMPTY_CHARARRAY;
	}
	/**
	 * @return Returns the problem.
	 */
	public final IProblem getProblem() {
		return problem;
	}

	/**
	 * @param startingOffset
	 * @param endingOffset
	 * @param f TODO
	 */
	public void initialize(int startingOffset, int endingOffset, int line, char[] f) {
		reset();
		startOffset = startingOffset;
		endOffset = endingOffset;
		lineNumber = line;
		this.filename = f;
	}
	/**
	 * @return Returns the offset.
	 */
	public final int getStartingOffset() {
		return startOffset;
	}
	/**
	 * @return Returns the endOffset.
	 */
	public final int getEndOffset() {
		return endOffset;
	}

	/**
	 * @return
	 */
	public int getLineNumber() {
		return lineNumber;
	}

	/**
	 * @return
	 */
	public char[] getFilename() {
		return filename;
	}
}
