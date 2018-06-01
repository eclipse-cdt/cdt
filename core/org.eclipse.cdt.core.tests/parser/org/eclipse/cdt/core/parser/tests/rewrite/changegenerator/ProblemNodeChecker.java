/*******************************************************************************
 * Copyright (c) 2018 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Institute for Software - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.core.parser.tests.rewrite.changegenerator;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTProblem;

public class ProblemNodeChecker extends ASTVisitor {

	private boolean problemFound = false;

	{
		shouldVisitProblems = true;
	}

	@Override
	public int visit(IASTProblem problem) {
		problemFound = true;
		return PROCESS_ABORT;
	}

	public boolean problemsFound() {
		return problemFound;
	}
}
