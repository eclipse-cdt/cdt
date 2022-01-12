/*******************************************************************************
 * Copyright (c) 2004, 2013 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *     Yuan Zhang / Beth Tibbitts (IBM Research)
 *     Anton Leherbauer (Wind River Systems)
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.internal.core.dom.parser.ASTProblem;

/**
 * C-specific implementation of ASTProblem, allows an action to visit a problem.
 */
public class CASTProblem extends ASTProblem {

	public CASTProblem(int id, char[] arg, boolean isError) {
		super(id, arg, isError);
	}

	@Override
	public CASTProblem copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public CASTProblem copy(CopyStyle style) {
		char[] arg = getArgument();
		CASTProblem copy = new CASTProblem(getID(), arg == null ? null : arg.clone(), isError());
		return copy(copy, style);
	}

	@Override
	public boolean accept(ASTVisitor action) {
		if (action.shouldVisitProblems) {
			switch (action.visit(this)) {
			case ASTVisitor.PROCESS_ABORT:
				return false;
			case ASTVisitor.PROCESS_SKIP:
				return true;
			default:
				break;
			}
		}
		if (action.shouldVisitProblems) {
			switch (action.leave(this)) {
			case ASTVisitor.PROCESS_ABORT:
				return false;
			case ASTVisitor.PROCESS_SKIP:
				return true;
			default:
				break;
			}
		}
		return true;
	}
}
