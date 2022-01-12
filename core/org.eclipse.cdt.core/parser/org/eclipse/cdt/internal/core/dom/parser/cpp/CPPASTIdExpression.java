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
 *     John Camelon (IBM) - Initial API and implementation
 *     Bryan Wilkinson (QNX)
 *     Markus Schorn (Wind River Systems)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import static org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory.LVALUE;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTImplicitDestructorName;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICPPASTCompletionContext;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTExpression;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.ProblemType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.DestructorCallCollector;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalID;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.FunctionSetType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;

public class CPPASTIdExpression extends ASTNode
		implements IASTIdExpression, ICPPASTExpression, ICPPASTCompletionContext {
	private IASTName fName;
	private ICPPEvaluation fEvaluation;
	private IASTImplicitDestructorName[] fImplicitDestructorNames;

	public CPPASTIdExpression() {
	}

	public CPPASTIdExpression(IASTName name) {
		setName(name);
	}

	@Override
	public CPPASTIdExpression copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public CPPASTIdExpression copy(CopyStyle style) {
		CPPASTIdExpression copy = new CPPASTIdExpression(fName == null ? null : fName.copy(style));
		return copy(copy, style);
	}

	@Override
	public IASTName getName() {
		return fName;
	}

	@Override
	public void setName(IASTName name) {
		assertNotFrozen();
		this.fName = name;
		if (name != null) {
			name.setParent(this);
			name.setPropertyInParent(ID_NAME);
		}
	}

	@Override
	public IASTImplicitDestructorName[] getImplicitDestructorNames() {
		if (fImplicitDestructorNames == null) {
			fImplicitDestructorNames = DestructorCallCollector.getTemporariesDestructorCalls(this);
		}

		return fImplicitDestructorNames;
	}

	@Override
	public boolean accept(ASTVisitor action) {
		if (action.shouldVisitExpressions) {
			switch (action.visit(this)) {
			case ASTVisitor.PROCESS_ABORT:
				return false;
			case ASTVisitor.PROCESS_SKIP:
				return true;
			default:
				break;
			}
		}

		if (fName != null && !fName.accept(action))
			return false;

		if (action.shouldVisitImplicitDestructorNames && !acceptByNodes(getImplicitDestructorNames(), action))
			return false;

		if (action.shouldVisitExpressions) {
			switch (action.leave(this)) {
			case ASTVisitor.PROCESS_ABORT:
				return false;
			case ASTVisitor.PROCESS_SKIP:
				return true;
			default:
				break;
			}
		}
		return true;
	}

	@Override
	public int getRoleForName(IASTName n) {
		if (fName == n)
			return r_reference;
		return r_unclear;
	}

	@Override
	public IBinding[] findBindings(IASTName n, boolean isPrefix, String[] namespaces) {
		return CPPSemantics.findBindingsForContentAssist(n, isPrefix, namespaces);
	}

	@Override
	public String toString() {
		return fName != null ? fName.toString() : "<unnamed>"; //$NON-NLS-1$
	}

	@Override
	public IBinding[] findBindings(IASTName n, boolean isPrefix) {
		return findBindings(n, isPrefix, null);
	}

	@Override
	public ICPPEvaluation getEvaluation() {
		if (fEvaluation == null) {
			fEvaluation = EvalID.create(this);
		}
		return fEvaluation;
	}

	@Override
	public IType getExpressionType() {
		CPPSemantics.pushLookupPoint(this);
		try {
			IType type = getEvaluation().getType();
			if (type instanceof FunctionSetType) {
				IBinding binding = fName.resolveBinding();
				if (binding instanceof IFunction) {
					return SemanticUtil.mapToAST(((IFunction) binding).getType());
				}
				return ProblemType.UNKNOWN_FOR_EXPRESSION;
			}
			return type;
		} finally {
			CPPSemantics.popLookupPoint();
		}
	}

	@Override
	public boolean isLValue() {
		return getValueCategory() == LVALUE;
	}

	@Override
	public ValueCategory getValueCategory() {
		return CPPEvaluation.getValueCategory(this);
	}
}
