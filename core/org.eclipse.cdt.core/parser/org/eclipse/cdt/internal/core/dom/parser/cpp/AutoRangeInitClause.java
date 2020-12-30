package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.internal.core.dom.parser.IAutoRangeIntitClause;

public class AutoRangeInitClause extends CPPASTUnaryExpression implements IAutoRangeIntitClause {

	private IType fallbackType;

	public AutoRangeInitClause(int opStar, IASTExpression beginExpr) {
		super(opStar, beginExpr);
	}

	@Override
	public void setFallbackType(IType type) {
		this.fallbackType = type;
	}

	@Override
	public IType getFallbackType() {
		return fallbackType;
	}

}
