package org.eclipse.cdt.core.parser.tests.rewrite.changegenerator;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTProblem;

public class ProblemNodeChecker extends ASTVisitor {

	private boolean problemFound;

	public ProblemNodeChecker() {
		problemFound = false;
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
