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
package org.eclipse.cdt.core.parser;

/**
 * @author jcamelon
 *
 */
public class BacktrackException extends Exception
{

	private IProblem problem;
	private int startOffset;
	private int endOffset;

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
	 */
	public void initialize(int startingOffset, int endingOffset) {
		reset();
		startOffset = startingOffset;
		endOffset = endingOffset;
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
}
