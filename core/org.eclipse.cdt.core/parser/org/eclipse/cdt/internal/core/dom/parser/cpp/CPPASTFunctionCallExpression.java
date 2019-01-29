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
 *     Mike Kucera (IBM) - implicit names
 *     Markus Schorn (Wind River Systems)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import static org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory.LVALUE;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.CVTYPE;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.REF;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.TDEF;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.getNestedType;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.ExpansionOverlapsBoundaryException;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTImplicitDestructorName;
import org.eclipse.cdt.core.dom.ast.IASTImplicitName;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFieldReference;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.DestructorCallCollector;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalFixed;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalFunctionCall;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalTypeId;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.LookupData;

public class CPPASTFunctionCallExpression extends ASTNode
		implements ICPPASTFunctionCallExpression, IASTAmbiguityParent {
	private ICPPASTExpression fFunctionName;
	private IASTInitializerClause[] fArguments;

	private IASTImplicitName[] fImplicitNames;
	private ICPPEvaluation fEvaluation;
	private IASTImplicitDestructorName[] fImplicitDestructorNames;

	public CPPASTFunctionCallExpression() {
		setArguments(null);
	}

	public CPPASTFunctionCallExpression(IASTExpression functionName, IASTInitializerClause[] args) {
		setFunctionNameExpression(functionName);
		setArguments(args);
	}

	@Override
	public CPPASTFunctionCallExpression copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public CPPASTFunctionCallExpression copy(CopyStyle style) {
		IASTInitializerClause[] args = null;
		if (fArguments.length > 0) {
			args = new IASTInitializerClause[fArguments.length];
			for (int i = 0; i < fArguments.length; i++) {
				args[i] = fArguments[i].copy(style);
			}
		}

		CPPASTFunctionCallExpression copy = new CPPASTFunctionCallExpression(null, args);
		copy.setFunctionNameExpression(fFunctionName == null ? null : fFunctionName.copy(style));
		return copy(copy, style);
	}

	@Override
	public IASTExpression getFunctionNameExpression() {
		return fFunctionName;
	}

	@Override
	public void setFunctionNameExpression(IASTExpression expression) {
		assertNotFrozen();
		this.fFunctionName = (ICPPASTExpression) expression;
		if (expression != null) {
			expression.setParent(this);
			expression.setPropertyInParent(FUNCTION_NAME);
		}
	}

	@Override
	public IASTInitializerClause[] getArguments() {
		return fArguments;
	}

	@Override
	public void setArguments(IASTInitializerClause[] arguments) {
		assertNotFrozen();
		if (arguments == null) {
			fArguments = IASTExpression.EMPTY_EXPRESSION_ARRAY;
		} else {
			fArguments = arguments;
			for (IASTInitializerClause arg : arguments) {
				arg.setParent(this);
				arg.setPropertyInParent(ARGUMENT);
			}
		}
	}

	@Override
	public IASTImplicitName[] getImplicitNames() {
		if (fImplicitNames == null) {
			ICPPFunction overload = getOverload();
			if (overload == null)
				return fImplicitNames = IASTImplicitName.EMPTY_NAME_ARRAY;

			if (overload instanceof IProblemBinding) {
				CPPASTImplicitName overloadName = null;
				overloadName = new CPPASTImplicitName(overload.getNameCharArray(), this);
				overloadName.setBinding(overload);
				overloadName.setOffsetAndLength((ASTNode) getFunctionNameExpression());
				return fImplicitNames = new IASTImplicitName[] { overloadName };
			}

			if (getEvaluation() instanceof EvalTypeId) {
				CPPASTImplicitName n1 = new CPPASTImplicitName(overload.getNameCharArray(), this);
				n1.setOffsetAndLength((ASTNode) fFunctionName);
				n1.setBinding(overload);
				return fImplicitNames = new IASTImplicitName[] { n1 };
			}

			if (overload instanceof CPPImplicitFunction) {
				if (!(overload instanceof ICPPMethod) || ((ICPPMethod) overload).isImplicit()) {
					return fImplicitNames = IASTImplicitName.EMPTY_NAME_ARRAY;
				}
			}

			// Create separate implicit names for the two brackets
			CPPASTImplicitName n1 = new CPPASTImplicitName(OverloadableOperator.PAREN, this);
			n1.setBinding(overload);

			CPPASTImplicitName n2 = new CPPASTImplicitName(OverloadableOperator.PAREN, this);
			n2.setBinding(overload);
			n2.setAlternate(true);

			if (fArguments.length == 0) {
				int idEndOffset = ((ASTNode) fFunctionName).getOffset() + ((ASTNode) fFunctionName).getLength();
				try {
					IToken lparen = fFunctionName.getTrailingSyntax();
					IToken rparen = lparen.getNext();

					if (lparen.getType() == IToken.tLPAREN) {
						n1.setOffsetAndLength(idEndOffset + lparen.getOffset(), 1);
					} else {
						n1.setOffsetAndLength(idEndOffset + lparen.getEndOffset(), 0);
					}

					if (rparen.getType() == IToken.tRPAREN) {
						n2.setOffsetAndLength(idEndOffset + rparen.getOffset(), 1);
					} else {
						n2.setOffsetAndLength(idEndOffset + rparen.getEndOffset(), 0);
					}
				} catch (ExpansionOverlapsBoundaryException e) {
					n1.setOffsetAndLength(idEndOffset, 0);
					n2.setOffsetAndLength(idEndOffset, 0);
				}
			} else {
				n1.computeOperatorOffsets(fFunctionName, true);
				n2.computeOperatorOffsets(fArguments[fArguments.length - 1], true);
			}

			fImplicitNames = new IASTImplicitName[] { n1, n2 };
		}
		return fImplicitNames;
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

		if (fFunctionName != null && !fFunctionName.accept(action))
			return false;

		IASTImplicitName[] implicits = action.shouldVisitImplicitNames ? getImplicitNames() : null;

		if (implicits != null && implicits.length > 0 && !implicits[0].accept(action))
			return false;

		for (IASTInitializerClause arg : fArguments) {
			if (!arg.accept(action))
				return false;
		}

		if (implicits != null && implicits.length > 1 && !implicits[1].accept(action))
			return false;

		if (action.shouldVisitImplicitDestructorNames && !acceptByNodes(getImplicitDestructorNames(), action))
			return false;

		if (action.shouldVisitExpressions && action.leave(this) == ASTVisitor.PROCESS_ABORT)
			return false;

		return true;
	}

	@Override
	public void replace(IASTNode child, IASTNode other) {
		if (child == fFunctionName) {
			other.setPropertyInParent(child.getPropertyInParent());
			other.setParent(child.getParent());
			fFunctionName = (ICPPASTExpression) other;
		}
		for (int i = 0; i < fArguments.length; ++i) {
			if (child == fArguments[i]) {
				other.setPropertyInParent(child.getPropertyInParent());
				other.setParent(child.getParent());
				fArguments[i] = (IASTExpression) other;
			}
		}
	}

	@Override
	public ICPPFunction getOverload() {
		CPPSemantics.pushLookupPoint(this);
		try {
			ICPPEvaluation eval = getEvaluation();
			if (eval instanceof EvalFunctionCall)
				return ((EvalFunctionCall) eval).getOverload();

			if (eval instanceof EvalTypeId) {
				if (!eval.isTypeDependent()) {
					IType t = getNestedType(((EvalTypeId) eval).getInputType(), TDEF | CVTYPE | REF);
					if (t instanceof ICPPClassType && !(t instanceof ICPPUnknownBinding)) {
						ICPPClassType cls = (ICPPClassType) t;
						LookupData data = CPPSemantics.createLookupData(((IASTIdExpression) fFunctionName).getName());
						try {
							ICPPConstructor[] constructors = cls.getConstructors();
							IBinding b = CPPSemantics.resolveFunction(data, constructors, true, false);
							if (b instanceof IProblemBinding && !(b instanceof ICPPFunction))
								b = new CPPFunction.CPPFunctionProblem(data.getLookupName(),
										((IProblemBinding) b).getID());
							if (b instanceof ICPPFunction)
								return (ICPPFunction) b;
						} catch (DOMException e) {
						}
					}
				}
			}
			return null;
		} finally {
			CPPSemantics.popLookupPoint();
		}
	}

	@Override
	public ICPPEvaluation getEvaluation() {
		if (fEvaluation == null)
			fEvaluation = computeEvaluation();

		return fEvaluation;
	}

	private ICPPEvaluation computeEvaluation() {
		if (fFunctionName == null || fArguments == null)
			return EvalFixed.INCOMPLETE;

		ICPPEvaluation conversion = checkForExplicitTypeConversion();
		if (conversion != null)
			return conversion;

		ICPPEvaluation[] args = new ICPPEvaluation[fArguments.length + 1];
		args[0] = fFunctionName.getEvaluation();
		for (int i = 1; i < args.length; i++) {
			args[i] = ((ICPPASTInitializerClause) fArguments[i - 1]).getEvaluation();
		}
		ICPPEvaluation fieldOwnerEval = null;
		if (fFunctionName instanceof ICPPASTFieldReference) {
			ICPPASTFieldReference fieldRef = (ICPPASTFieldReference) fFunctionName;
			ICPPASTExpression fieldOwner = fieldRef.getFieldOwner();
			fieldOwnerEval = fieldOwner.getEvaluation();
		}
		return new EvalFunctionCall(args, fieldOwnerEval, this);
	}

	private ICPPEvaluation checkForExplicitTypeConversion() {
		if (fFunctionName instanceof IASTIdExpression) {
			final IASTName name = ((IASTIdExpression) fFunctionName).getName();
			IBinding b = name.resolvePreBinding();
			if (b instanceof IType) {
				ICPPEvaluation[] args = new ICPPEvaluation[fArguments.length];
				for (int i = 0; i < args.length; i++) {
					args[i] = ((ICPPASTInitializerClause) fArguments[i]).getEvaluation();
				}

				return new EvalTypeId((IType) b, this, false, args);
			}
		}
		return null;
	}

	@Override
	public IType getExpressionType() {
		return CPPEvaluation.getType(this);
	}

	@Override
	public ValueCategory getValueCategory() {
		return CPPEvaluation.getValueCategory(this);
	}

	@Override
	public boolean isLValue() {
		return getValueCategory() == LVALUE;
	}
}
