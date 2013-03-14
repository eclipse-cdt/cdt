package org.eclipse.cdt.qt.internal.core;

import java.util.Collection;

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

	private Collection<IASTNode> getChildren(IASTNode node, IASTFunctionCallExpression fnCall) {

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

		IASTExpression fnNameExpr = fnCall.getFunctionNameExpression();
		if (!(fnNameExpr instanceof IASTIdExpression))
			return null;

		IASTName fnName = ((IASTIdExpression) fnNameExpr).getName();
		if (fnName == null)
			return null;

		IBinding fnBinding = fnName.resolveBinding();
		if (fnBinding == null)
			return null;

		if (!QtKeywords.is_QObject_connect(fnBinding))
			return null;

		return new QtConnectFunctionCall(fnCall).getChildren(node);
	}

	@Override
	public Collection<IASTNode> getChildren(IASTNode node) {
		if (!(node instanceof IASTName))
			return null;

		IASTName name = (IASTName) node;
		IASTNode parent = name.getParent();
		while (parent != null) {
			if (parent instanceof IASTFunctionCallExpression)
				return getChildren(name, (IASTFunctionCallExpression) parent);
			parent = parent.getParent();
		}
		return null;
	}
}
