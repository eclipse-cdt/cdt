package org.eclipse.cdt.internal.core.dom.parser;

import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;

public interface IBinaryOperator {
	IASTInitializerClause exchange(IASTInitializerClause expr);

	IASTInitializerClause getExpression();

	IBinaryOperator getNext();

	void setNext(IBinaryOperator next);

	int getOperatorToken();
}
