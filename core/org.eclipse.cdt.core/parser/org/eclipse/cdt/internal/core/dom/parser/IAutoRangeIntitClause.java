package org.eclipse.cdt.internal.core.dom.parser;

import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUnaryExpression;

public interface IAutoRangeIntitClause extends ICPPASTUnaryExpression, IASTAmbiguityParent {

	void setFallbackType(IType type);

	IType getFallbackType();
}
