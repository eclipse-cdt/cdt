package org.eclipse.cdt.core.parser.tests.rewrite.changegenerator;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTProblem;

public class ASTValidator extends ASTVisitor {

	private boolean problemFound;

	public ASTValidator() {
		super(false);
		problemFound = false;
		shouldVisitProblems = true;
	}

	@Override
	public int visit(IASTProblem problem) {
		problemFound = true;
		return PROCESS_ABORT;
	}

	public boolean isASTValid() {
		return !problemFound;
	}
}
