/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.corext.refactoring.code.flow;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTReturnStatement;

import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;

class ReturnFlowInfo extends FlowInfo {

	public ReturnFlowInfo(IASTReturnStatement node) {
		super(getReturnFlag(node));
	}

	public void merge(FlowInfo info, FlowContext context) {
		if (info == null)
			return;

		assignAccessMode(info);
	}

	private static int getReturnFlag(IASTReturnStatement node) {
		IASTExpression expression= node.getReturnValue();
		if (expression == null || SemanticUtil.isVoidType(expression.getExpressionType()))
			return VOID_RETURN;
		return VALUE_RETURN;
	}
}


