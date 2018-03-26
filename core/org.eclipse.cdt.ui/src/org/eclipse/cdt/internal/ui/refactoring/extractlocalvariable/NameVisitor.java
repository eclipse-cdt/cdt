package org.eclipse.cdt.internal.ui.refactoring.extractlocalvariable;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;

public class NameVisitor extends ASTVisitor {
	List<IASTName> names;
	{
		shouldVisitNames = true;
	}

	NameVisitor() {
		names = new ArrayList<IASTName>();
	}

	@Override
	public int visit(IASTName name) {
		names.add(name);
		return PROCESS_CONTINUE;
	}

	public List<IASTName> findNames(IASTNode node) {
		node.accept(this);
		return names;
	}
}