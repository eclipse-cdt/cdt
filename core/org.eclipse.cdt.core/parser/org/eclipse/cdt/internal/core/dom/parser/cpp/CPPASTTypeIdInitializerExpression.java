/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     John Camelon (IBM Rational Software) - Initial API and implementation
 *     Yuan Zhang / Beth Tibbitts (IBM Research)
 *     Markus Schorn (Wind River Systems)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTImplicitDestructorName;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IASTTypeIdInitializerExpression;
import org.eclipse.cdt.core.dom.ast.IProblemType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTInitializerClause;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.DestructorCallCollector;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalFixed;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalTypeId;

/**
 * Type id initializer expression for C++, type-id { initializer }
 */
public class CPPASTTypeIdInitializerExpression extends ASTNode
		implements IASTTypeIdInitializerExpression, ICPPASTExpression {
	private IASTTypeId fTypeId;
	private IASTInitializer fInitializer;
	private ICPPEvaluation fEvaluation;
	private IASTImplicitDestructorName[] fImplicitDestructorNames;

	public CPPASTTypeIdInitializerExpression() {
	}

	public CPPASTTypeIdInitializerExpression(IASTTypeId t, IASTInitializer i) {
		setTypeId(t);
		setInitializer(i);
	}

	@Override
	public IASTTypeId getTypeId() {
		return fTypeId;
	}

	@Override
	public void setTypeId(IASTTypeId typeId) {
		assertNotFrozen();
		this.fTypeId = typeId;
		if (typeId != null) {
			typeId.setParent(this);
			typeId.setPropertyInParent(TYPE_ID);
		}
	}

	@Override
	public IASTInitializer getInitializer() {
		return fInitializer;
	}

	@Override
	public void setInitializer(IASTInitializer initializer) {
		assertNotFrozen();
		this.fInitializer = initializer;
		if (initializer != null) {
			initializer.setParent(this);
			initializer.setPropertyInParent(INITIALIZER);
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

		if (fTypeId != null && !fTypeId.accept(action))
			return false;
		if (fInitializer != null && !fInitializer.accept(action))
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
	public final boolean isLValue() {
		return false;
	}

	@Override
	public IASTTypeIdInitializerExpression copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public IASTTypeIdInitializerExpression copy(CopyStyle style) {
		CPPASTTypeIdInitializerExpression copy = new CPPASTTypeIdInitializerExpression(
				fTypeId == null ? null : fTypeId.copy(style), fInitializer == null ? null : fInitializer.copy(style));
		return copy(copy, style);
	}

	@Override
	public ICPPEvaluation getEvaluation() {
		if (fEvaluation == null)
			fEvaluation = computeEvaluation();

		return fEvaluation;
	}

	private ICPPEvaluation computeEvaluation() {
		final IASTInitializer initializer = getInitializer();
		if (!(initializer instanceof ICPPASTInitializerClause))
			return EvalFixed.INCOMPLETE;

		IType type = CPPVisitor.createType(getTypeId());
		if (type == null || type instanceof IProblemType)
			return EvalFixed.INCOMPLETE;

		return new EvalTypeId(type, this, false, ((ICPPASTInitializerClause) initializer).getEvaluation());
	}

	@Override
	public IType getExpressionType() {
		return CPPEvaluation.getType(this);
	}

	@Override
	public ValueCategory getValueCategory() {
		return CPPEvaluation.getValueCategory(this);
	}
}
