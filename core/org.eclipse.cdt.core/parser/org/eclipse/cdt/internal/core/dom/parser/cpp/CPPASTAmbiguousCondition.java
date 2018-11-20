/*******************************************************************************
 * Copyright (c) 2009, 2012 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.internal.core.dom.parser.ASTAmbiguousNode;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;

/**
 * Handles ambiguity between expression and declaration in a condition.
 */
public class CPPASTAmbiguousCondition extends ASTAmbiguousNode implements IASTAmbiguousCondition {
	private IASTExpression fExpression;
	private IASTDeclaration fDeclaration;

	public CPPASTAmbiguousCondition(IASTExpression expression, IASTSimpleDeclaration declaration) {
		fExpression = expression;
		fDeclaration = declaration;

		expression.setParent(this);
		expression.setPropertyInParent(SUBCONDITION);
		declaration.setParent(this);
		declaration.setPropertyInParent(SUBCONDITION);
	}

	@Override
	public IASTNode[] getNodes() {
		return new IASTNode[] { fExpression, fDeclaration };
	}

	@Override
	protected void beforeResolution() {
		// populate containing scope, so that it will not be affected by the alternative branches.
		IScope scope = CPPVisitor.getContainingNonTemplateScope(this);
		if (scope instanceof ICPPASTInternalScope) {
			((ICPPASTInternalScope) scope).populateCache();
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
}
