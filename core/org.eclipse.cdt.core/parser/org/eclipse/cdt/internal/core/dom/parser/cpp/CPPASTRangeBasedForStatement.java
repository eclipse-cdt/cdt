/*******************************************************************************
 * Copyright (c) 2010, 2015 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Markus Schorn - Initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.CVTYPE;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.TDEF;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTImplicitDestructorName;
import org.eclipse.cdt.core.dom.ast.IASTImplicitName;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IArrayType;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTRangeBasedForStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.DestructorCallCollector;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalUtil;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.ExecRangeBasedFor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.ExecSimpleDeclaration;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;

/**
 * Range based 'for' loop in C++.
 */
public class CPPASTRangeBasedForStatement extends CPPASTAttributeOwner
		implements ICPPASTRangeBasedForStatement, ICPPExecutionOwner {
	private IScope fScope;
	private IASTDeclaration fDeclaration;
	private IASTInitializerClause fInitClause;
	private IASTStatement fBody;
	private IASTImplicitName[] fImplicitNames;
	private IASTImplicitDestructorName[] fImplicitDestructorNames;

	private static final char[] RANGE_EXPR = "__range".toCharArray(); //$NON-NLS-1$

	public CPPASTRangeBasedForStatement() {
	}

	@Override
	public CPPASTRangeBasedForStatement copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public CPPASTRangeBasedForStatement copy(CopyStyle style) {
		CPPASTRangeBasedForStatement copy = new CPPASTRangeBasedForStatement();
		copy.setDeclaration(fDeclaration == null ? null : fDeclaration.copy(style));
		copy.setInitializerClause(fInitClause == null ? null : fInitClause.copy(style));
		copy.setBody(fBody == null ? null : fBody.copy(style));
		return copy(copy, style);
	}

	@Override
	public IASTDeclaration getDeclaration() {
		return fDeclaration;
	}

	@Override
	public void setDeclaration(IASTDeclaration declaration) {
		assertNotFrozen();
		this.fDeclaration = declaration;
		if (declaration != null) {
			declaration.setParent(this);
			declaration.setPropertyInParent(DECLARATION);
		}
	}

	@Override
	public IASTInitializerClause getInitializerClause() {
		return fInitClause;
	}

	@Override
	public void setInitializerClause(IASTInitializerClause initClause) {
		assertNotFrozen();
		fInitClause = initClause;
		if (initClause != null) {
			initClause.setParent(this);
			initClause.setPropertyInParent(INITIALIZER);
		}
	}

	@Override
	public IASTStatement getBody() {
		return fBody;
	}

	@Override
	public void setBody(IASTStatement statement) {
		assertNotFrozen();
		fBody = statement;
		if (statement != null) {
			statement.setParent(this);
			statement.setPropertyInParent(BODY);
		}
	}

	@Override
	public IScope getScope() {
		if (fScope == null)
			fScope = new CPPBlockScope(this);
		return fScope;
	}

	@Override
	public IASTImplicitName[] getImplicitNames() {
		if (fImplicitNames == null) {
			IASTInitializerClause forInit = getInitializerClause();
			final ASTNode position = (ASTNode) forInit;
			if (forInit instanceof IASTExpression) {
				final IASTExpression forInitExpr = (IASTExpression) forInit;
				IType type = SemanticUtil.getNestedType(forInitExpr.getExpressionType(), TDEF | CVTYPE);
				if (type instanceof IArrayType) {
					fImplicitNames = IASTImplicitName.EMPTY_NAME_ARRAY;
				} else if (type instanceof ICPPClassType) {
					ICPPClassType ct = (ICPPClassType) type;
					CPPSemantics.pushLookupPoint(this);
					try {
						if (CPPSemantics.findBindings(ct.getCompositeScope(), CPPVisitor.BEGIN, true,
								this).length > 0) {
							CPPASTName name = new CPPASTName(CPPVisitor.BEGIN);
							name.setOffset(position.getOffset());
							CPPASTFieldReference fieldRef = new CPPASTFieldReference(name, forInitExpr.copy());
							IASTExpression expr = new CPPASTFunctionCallExpression(fieldRef, CPPVisitor.NO_ARGS);
							expr.setParent(this);
							expr.setPropertyInParent(ICPPASTRangeBasedForStatement.INITIALIZER);
							CPPASTImplicitName begin = new CPPASTImplicitName(name.toCharArray(), this);
							begin.setBinding(name.resolveBinding());
							begin.setOffsetAndLength(position);

							name = new CPPASTName(CPPVisitor.END);
							name.setOffset(position.getOffset());
							fieldRef.setFieldName(name);
							CPPASTImplicitName end = new CPPASTImplicitName(name.toCharArray(), this);
							end.setBinding(name.resolveBinding());
							end.setOffsetAndLength(position);

							fImplicitNames = new IASTImplicitName[] { begin, end };
						}
					} finally {
						CPPSemantics.popLookupPoint();
					}
				}
			}
			if (fImplicitNames == null) {
				// Synthesize a notional '__range' variable to refer to the range expression.
				// We can't use the range expression itself as the argument to begin() and
				// end() because the range expression's value category might be a prvalue or
				// xvalue, but the value category of the '__range' variable appearing in the
				// notional rewrite specified in the standard is an lvalue.
				CPPASTName rangeVarDeclName = new CPPASTName(RANGE_EXPR);
				CPPVariable rangeVar = new CPPVariable(rangeVarDeclName);
				CPPASTSimpleDeclSpecifier rangeVarDeclSpec = new CPPASTSimpleDeclSpecifier();
				rangeVarDeclSpec.setType(ICPPASTSimpleDeclSpecifier.t_auto);
				CPPASTSimpleDeclaration rangeVarDecl = new CPPASTSimpleDeclaration();
				rangeVarDecl.setDeclSpecifier(rangeVarDeclSpec);
				// Make the notional declaration of '__range_ a child of the range-for
				// statement's body, so that name resolution in its initializer has
				// a scope to work with.
				rangeVarDecl.setParent(fBody);
				CPPASTDeclarator rangeVarDeclarator = new CPPASTDeclarator(rangeVarDeclName);
				rangeVarDeclarator.setInitializer(new CPPASTEqualsInitializer(forInit.copy()));
				rangeVarDecl.addDeclarator(rangeVarDeclarator);
				CPPASTName rangeVarRefName = new CPPASTName(RANGE_EXPR);
				rangeVarRefName.setBinding(rangeVar);
				CPPASTIdExpression rangeExpr = new CPPASTIdExpression(rangeVarRefName);

				CPPASTName name = new CPPASTName(CPPVisitor.BEGIN);
				name.setOffset(position.getOffset());
				CPPASTIdExpression fname = new CPPASTIdExpression(name);
				IASTExpression expr = new CPPASTFunctionCallExpression(fname,
						new IASTInitializerClause[] { rangeExpr });
				expr.setParent(this);
				expr.setPropertyInParent(ICPPASTRangeBasedForStatement.INITIALIZER);

				CPPASTImplicitName begin = new CPPASTImplicitName(name.toCharArray(), this);
				begin.setBinding(name.resolveBinding());
				begin.setOffsetAndLength(position);

				name = new CPPASTName(CPPVisitor.END);
				name.setOffset(position.getOffset());
				fname.setName(name);
				CPPASTImplicitName end = new CPPASTImplicitName(name.toCharArray(), this);
				end.setBinding(name.resolveBinding());
				end.setOffsetAndLength(position);

				fImplicitNames = new IASTImplicitName[] { begin, end };
			}
		}
		return fImplicitNames;
	}

	@Override
	public IASTImplicitDestructorName[] getImplicitDestructorNames() {
		if (fImplicitDestructorNames == null) {
			fImplicitDestructorNames = DestructorCallCollector.getLocalVariablesDestructorCalls(this);
		}

		return fImplicitDestructorNames;
	}

	@Override
	public boolean accept(ASTVisitor action) {
		if (action.shouldVisitStatements) {
			switch (action.visit(this)) {
			case ASTVisitor.PROCESS_ABORT:
				return false;
			case ASTVisitor.PROCESS_SKIP:
				return true;
			default:
				break;
			}
		}

		if (!acceptByAttributeSpecifiers(action))
			return false;
		if (fDeclaration != null && !fDeclaration.accept(action))
			return false;
		if (fInitClause != null && !fInitClause.accept(action))
			return false;
		IASTImplicitName[] implicits = action.shouldVisitImplicitNames ? getImplicitNames() : null;
		if (implicits != null) {
			for (IASTImplicitName implicit : implicits) {
				if (!implicit.accept(action))
					return false;
			}
		}

		if (fBody != null && !fBody.accept(action))
			return false;

		if (action.shouldVisitImplicitDestructorNames && !acceptByNodes(getImplicitDestructorNames(), action))
			return false;

		if (action.shouldVisitStatements && action.leave(this) == ASTVisitor.PROCESS_ABORT)
			return false;
		return true;
	}

	@Override
	public void replace(IASTNode child, IASTNode other) {
		if (child == fDeclaration) {
			setDeclaration((IASTDeclaration) other);
			return;
		} else if (child == fInitClause) {
			setInitializerClause((IASTInitializerClause) other);
			return;
		} else if (child == fBody) {
			setBody((IASTStatement) other);
			return;
		}
		super.replace(child, other);
	}

	@Override
	public ICPPExecution getExecution() {
		ExecSimpleDeclaration declarationExec = (ExecSimpleDeclaration) ((ICPPExecutionOwner) fDeclaration)
				.getExecution();
		ICPPEvaluation initClauseEval = ((ICPPASTInitializerClause) fInitClause).getEvaluation();
		ICPPExecution bodyExec = EvalUtil.getExecutionFromStatement(fBody);
		IASTImplicitName[] implicitNames = getImplicitNames();
		ICPPFunction begin = null;
		ICPPFunction end = null;
		if (implicitNames.length == 2) {
			IBinding beginBinding = implicitNames[0].resolveBinding();
			IBinding endBinding = implicitNames[1].resolveBinding();
			if (beginBinding instanceof ICPPFunction && endBinding instanceof ICPPFunction) {
				begin = (ICPPFunction) beginBinding;
				end = (ICPPFunction) endBinding;
			}
		}
		return new ExecRangeBasedFor(declarationExec, initClauseEval, begin, end, bodyExec);
	}
}
