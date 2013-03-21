package org.eclipse.cdt.qt.internal.core;

import java.util.Collection;
import java.util.Collections;

import org.eclipse.cdt.core.dom.ast.IASTChildProvider;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.qt.core.QtKeywords;

/**
 * Provides names as children of SIGNAL and SLOT macro expansions.
 *
 * @see QtSignalSlotReferenceName
 */
public class QtSignalSlotNameProvider implements IASTChildProvider {

	@Override
	public Collection<IASTNode> getChildren(IASTExpression expr) {
		if (!(expr instanceof IASTFunctionCallExpression))
			return Collections.<IASTNode> emptyList();

		// The name provider runs for all calls to functions named "connect".  This includes the
		// intended "QObject::connect", but will also include calls to OtherClass::connect".  We
		// need to resolve the function name binding in order to check that we have the right
		// function.
		//
		// Timing results on my machine show that resolving the function binding takes
		// about 600us while examining all the parameters takes about 10,000us.  So we check
		// the function binding first.
		//
		// Results are only returned when the function call is found to be bound to QObject::connect
		// AND the parameters are as expected for the connect function call.

		IASTFunctionCallExpression fnCall = (IASTFunctionCallExpression)expr;
		IASTExpression fnNameExpr = fnCall.getFunctionNameExpression();
		if (!(fnNameExpr instanceof IASTIdExpression))
			return Collections.<IASTNode> emptyList();

		IASTName fnName = ((IASTIdExpression) fnNameExpr).getName();
		if (fnName == null)
			return Collections.<IASTNode> emptyList();

		IBinding fnBinding = fnName.resolveBinding();
		if (fnBinding == null)
			return Collections.<IASTNode> emptyList();

		if (!QtKeywords.is_QObject_connect(fnBinding))
			return Collections.<IASTNode> emptyList();

		QtConnectFunctionCall qtCall = new QtConnectFunctionCall(fnCall);
		Collection<IASTNode> children = qtCall.getChildren();
		return children == null ? Collections.<IASTNode> emptyList() : children;
	}
}
