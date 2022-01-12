/*******************************************************************************
 * Copyright (c) 2004, 2014 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    IBM - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *    Anders Dahlberg (Ericsson) - bug 84144, indexer optimization
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser;

import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.parser.IProblem;

/**
 * @author jcamelon
 */
public class BacktrackException extends Exception {
	private static final StackTraceElement[] EMPTY_STACK = new StackTraceElement[0];

	private IASTProblem problem;
	private IASTNode nodeBeforeProblem; // a node has been created in spite of the problem.
	private int offset, length;

	public BacktrackException() {
	}

	public BacktrackException(BacktrackException e) {
		problem = e.problem;
		nodeBeforeProblem = e.nodeBeforeProblem;
		offset = e.offset;
		length = e.length;
	}

	public void initialize(IASTProblem p) {
		reset();
		problem = p;
	}

	public void initialize(IASTProblem p, IASTNode node) {
		reset();
		problem = p;
		nodeBeforeProblem = node;
	}

	/**
	 *
	 */
	private void reset() {
		nodeBeforeProblem = null;
		problem = null;
		offset = 0;
		length = 0;
	}

	/**
	 * @return Returns the problem.
	 */
	public final IASTProblem getProblem() {
		return problem;
	}

	public final IASTNode getNodeBeforeProblem() {
		return nodeBeforeProblem;
	}

	public void initialize(int start, int l) {
		reset();
		offset = start;
		length = l;
	}

	/**
	 * @return Returns the length.
	 */
	public int getLength() {
		return length;
	}

	/**
	 * @return Returns the offset.
	 */
	public int getOffset() {
		return offset;
	}

	@Override
	public Throwable fillInStackTrace() {
		// Do nothing, performance optimization
		return this;
	}

	@Override
	public StackTraceElement[] getStackTrace() {
		return EMPTY_STACK;
	}

	// Don't try alternative parses if this returns true.
	public boolean isFatal() {
		return problem != null && problem.getID() == IProblem.TEMPLATE_ARGUMENT_NESTING_DEPTH_LIMIT_EXCEEDED;
	}
}
