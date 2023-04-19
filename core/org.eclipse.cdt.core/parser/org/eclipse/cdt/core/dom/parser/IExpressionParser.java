package org.eclipse.cdt.core.dom.parser;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.internal.core.dom.parser.IBinaryOperator;

public interface IExpressionParser {
	IASTExpression buildExpression(IBinaryOperator leftChain, IASTInitializerClause expr);
}
