package org.eclipse.cdt.ui.tests.reducer;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;

class ProblemFinder extends ASTVisitor {
	boolean foundProblem;

	public ProblemFinder() {
		shouldVisitNames = true;
		shouldVisitImplicitNames = true;
	}

	public boolean containsProblemBinding(IASTNode node) {
		foundProblem = false;
		node.accept(this);
		return foundProblem;
	}

	@Override
	public int visit(IASTName name) {
		if (!CharArrayUtils.startsWith(name.getSimpleID(), "__builtin_")
				&& name.resolveBinding() instanceof IProblemBinding) {
			foundProblem = true;
			return PROCESS_ABORT;
		}
		return PROCESS_CONTINUE;
	}
}