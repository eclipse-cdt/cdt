/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTImplicitDestructorName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTExpression;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.ASTAmbiguousNode;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguousExpression;

public class CPPASTAmbiguousExpression extends ASTAmbiguousNode implements IASTAmbiguousExpression, ICPPASTExpression {
	private IASTExpression[] exp = new IASTExpression[2];
	private int expPos;

	public CPPASTAmbiguousExpression(IASTExpression... expressions) {
		for (IASTExpression e : expressions) {
			addExpression(e);
		}
	}

	@Override
	public IASTExpression copy() {
		throw new UnsupportedOperationException();
	}

	@Override
	public IASTExpression copy(CopyStyle style) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addExpression(IASTExpression e) {
		assertNotFrozen();
		if (e != null) {
			exp = ArrayUtil.appendAt(exp, expPos++, e);
			e.setParent(this);
			e.setPropertyInParent(SUBEXPRESSION);
		}
	}

	@Override
	public IASTExpression[] getExpressions() {
		exp = ArrayUtil.trim(exp, expPos);
		return exp;
	}

	@Override
	public IASTNode[] getNodes() {
		return getExpressions();
	}

	@Override
	public IASTImplicitDestructorName[] getImplicitDestructorNames() {
		throw new UnsupportedOperationException();
	}
}
